package gym_app;

import com.licel.jcardsim.base.Simulator;
import javacard.framework.AID;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

/**
 * SmartCardService - FULL ENCRYPTION
 * ✅ Avatar: PC mã hóa → Card lưu encrypted → PC giải mã khi get
 * ✅ Info: Card mã hóa (vì nhỏ, không chunking)
 */
public class SmartCardService {

    // ====================== CONFIG ======================
    private static final byte[] APPLET_AID = {
        (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, 0x00
    };

    private static final int PIN_TRY_LIMIT = 5;
    private static final int PIN_SIZE = 6;
    private static final int AVATAR_MAX_SIZE = 10240;
    private static final int INFO_MAX_SIZE = 1024;
    private static final int BALANCE_UNIT = 10000;

    private static final String CARD_STATE_FILE = "gym_card.state";

    // ====================== APDU COMMANDS ======================
    private static final byte CLA = (byte) 0x80;

    private static final byte INS_VERIFY_PIN = (byte) 0x10;
    private static final byte INS_CHANGE_PIN = (byte) 0x11;
    private static final byte INS_UNBLOCK_PIN = (byte) 0x12;

    private static final byte INS_UPDATE_INFO = (byte) 0x20;
    private static final byte INS_GET_INFO = (byte) 0x21;
    private static final byte INS_UPLOAD_AVATAR = (byte) 0x22;
    private static final byte INS_GET_AVATAR = (byte) 0x23;

    private static final byte INS_TOPUP = (byte) 0x30;
    private static final byte INS_PAYMENT = (byte) 0x31;
    private static final byte INS_CHECK_BALANCE = (byte) 0x32;

    private static final byte INS_GET_HISTORY = (byte) 0x40;
    private static final byte INS_INIT_CRYPTO = (byte) 0x50;
    private static final byte INS_GET_STATUS = (byte) 0x51;
    private static final byte INS_HASH_SHA1 = (byte) 0x81;
    private static final byte INS_SET_AES_KEY = (byte) 0x72;

    // ====================== CRYPTO - PC SIDE ======================
    private SecretKeySpec desKey;
    private IvParameterSpec ivSpec;
    private Cipher desCipher;

    // ====================== JCARDSIM ======================
    private Simulator simulator;
    private AID appletAID;
    private boolean isConnected = false;

    // ====================== STATE ======================
    private String cardId = "GYM000001";
    private String recoveryPhone = null;
    private String currentPIN = "123456";
    private int pinTriesRemaining = PIN_TRY_LIMIT;
    private boolean pinVerified = false;
    private boolean isFirstLogin = true;
    private boolean isCardBlocked = false;

    private String savedInfo = null;
    private byte[] savedAvatar = null;
    private long savedBalance = 0;

    // ====================== CONSTRUCTOR ======================
    public SmartCardService() {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║  SMARTCARD SERVICE - JCARDSIM MODE                     ║");
        System.out.println("║  ✅ FULL ENCRYPTION: PC encrypts Avatar                ║");
        System.out.println("║  ✅ FULL ENCRYPTION: Card encrypts Info                ║");
        System.out.println("║  📦 Max Avatar: 10KB | Max Info: 256 bytes             ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");

        initPCCrypto("123456");
        loadCardState();
        initSimulator();
        restoreDataToApplet();
    }

    /**
     * ✅ Khởi tạo crypto PC-side (3DES)
     */
    private void initPCCrypto(String pin) {
        try {
            // Derive key từ PIN
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes("UTF-8"));
            byte[] keyBytes = Arrays.copyOf(hash, 24); // 3DES key 24 bytes
            
            desKey = new SecretKeySpec(keyBytes, "DESede");
            
            // IV cố định (hoặc derive từ PIN)
            byte[] ivBytes = new byte[8];
            System.arraycopy(hash, 0, ivBytes, 0, 8);
            ivSpec = new IvParameterSpec(ivBytes);
            
            desCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            
            System.out.println("[Crypto] ✅ PC-side 3DES initialized");
        } catch (Exception e) {
            System.out.println("[Crypto] ❌ Init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ Update crypto key khi đổi PIN
     */
    private void updatePCCrypto(String newPin) {
        initPCCrypto(newPin);
    }

    private void initSimulator() {
        try {
            simulator = new Simulator();
            appletAID = new AID(APPLET_AID, (short) 0, (byte) APPLET_AID.length);

            System.out.println("[JCSIM] 🔧 Installing applet...");
            simulator.installApplet(appletAID, thegym.thegym.class);
            System.out.println("[JCSIM] ✅ Applet installed!");

            boolean selected = simulator.selectApplet(appletAID);
            if (selected) {
                isConnected = true;
                System.out.println("[JCSIM] ✅ Applet selected!");

                // Init crypto
                CommandAPDU apdu = new CommandAPDU(CLA, INS_INIT_CRYPTO, 0x00, 0x00, 1);
                sendAPDU(apdu);

                // Set 3DES key on card
                byte[] cardKey = derive3DESKey("123456");
                apdu = new CommandAPDU(CLA, INS_SET_AES_KEY, 0x00, 0x00, cardKey);
                ResponseAPDU resp = sendAPDU(apdu);
                if (resp != null && resp.getSW() == 0x9000) {
                    System.out.println("[JCSIM] ✅ Card 3DES key initialized");
                }
            }
        } catch (Exception e) {
            System.out.println("[JCSIM] ❌ Init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] derive3DESKey(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes("UTF-8"));
            return Arrays.copyOf(hash, 24);
        } catch (Exception e) {
            byte[] key = new byte[24];
            byte[] pinBytes = pin.getBytes();
            for (int i = 0; i < 24; i++) {
                key[i] = pinBytes[i % pinBytes.length];
            }
            return key;
        }
    }

    private void restoreDataToApplet() {
        if (!isConnected) {
            return;
        }

        System.out.println("[JCSIM] 🔄 Restoring data to applet...");

        byte[] defaultPin = "123456".getBytes();
        CommandAPDU apdu = new CommandAPDU(CLA, INS_VERIFY_PIN, 0x00, 0x00, defaultPin);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp == null || resp.getSW() != 0x9000) {
            System.out.println("[JCSIM] ⚠️ Cannot verify default PIN for restore");
            return;
        }

        // Restore info
        if (savedInfo != null || recoveryPhone != null) {
            restoreInfo(recoveryPhone, savedInfo);
            System.out.println("[JCSIM] ✅ Info restored");
        }

        // Restore avatar (ENCRYPTED)
        if (savedAvatar != null && savedAvatar.length > 0) {
            restoreAvatarEncrypted(savedAvatar);
            System.out.println("[JCSIM] ✅ Avatar restored (encrypted)");
        }

        // Restore balance
        if (savedBalance > 0) {
            restoreBalance(savedBalance);
            System.out.println("[JCSIM] ✅ Balance restored");
        }

        // Change PIN
        if (currentPIN != null && !currentPIN.equals("123456")) {
            byte[] data = new byte[12];
            System.arraycopy("123456".getBytes(), 0, data, 0, 6);
            System.arraycopy(currentPIN.getBytes(), 0, data, 6, 6);

            apdu = new CommandAPDU(CLA, INS_CHANGE_PIN, 0x00, 0x00, data);
            sendAPDU(apdu);
            
            byte[] newKey = derive3DESKey(currentPIN);
            apdu = new CommandAPDU(CLA, INS_SET_AES_KEY, 0x00, 0x00, newKey);
            sendAPDU(apdu);
            
            System.out.println("[JCSIM] ✅ PIN restored");
        }

        simulator.reset();
        simulator.selectApplet(appletAID);
        
        apdu = new CommandAPDU(CLA, INS_INIT_CRYPTO, 0x00, 0x00, 1);
        sendAPDU(apdu);
        
        byte[] keyToSet = derive3DESKey(currentPIN != null ? currentPIN : "123456");
        apdu = new CommandAPDU(CLA, INS_SET_AES_KEY, 0x00, 0x00, keyToSet);
        sendAPDU(apdu);
        
        pinVerified = false;

        System.out.println("[JCSIM] ✅ Data restore complete!");
    }

    private void restoreInfo(String phone, String info) {
        try {
            byte[] phoneBytes = (phone != null) ? phone.getBytes("UTF-8") : new byte[0];
            byte[] infoBytes = (info != null) ? info.getBytes("UTF-8") : new byte[0];

            if (phoneBytes.length > 12) {
                phoneBytes = Arrays.copyOf(phoneBytes, 12);
            }
            if (infoBytes.length > INFO_MAX_SIZE) {
                infoBytes = Arrays.copyOf(infoBytes, INFO_MAX_SIZE);
            }

            byte[] data = new byte[1 + phoneBytes.length + infoBytes.length];
            data[0] = (byte) phoneBytes.length;
            System.arraycopy(phoneBytes, 0, data, 1, phoneBytes.length);
            System.arraycopy(infoBytes, 0, data, 1 + phoneBytes.length, infoBytes.length);

            CommandAPDU apdu = new CommandAPDU(CLA, INS_UPDATE_INFO, 0x00, 0x00, data);
            sendAPDU(apdu);
        } catch (Exception e) {
            System.out.println("[JCSIM] ⚠️ Restore info error: " + e.getMessage());
        }
    }

    /**
     * ✅ Restore avatar - Lưu ENCRYPTED data (plaintext đã decrypt từ state)
     */
    private void restoreAvatarEncrypted(byte[] plaintextAvatar) {
        try {
            // Encrypt toàn bộ trước
            byte[] encrypted = encryptAvatar(plaintextAvatar);
            
            System.out.println("[JCSIM] 📤 Uploading encrypted avatar (" + encrypted.length + " bytes)...");
            
            // Upload encrypted chunks
            int offset = 0;
            int chunkCount = 0;
            
            while (offset < encrypted.length) {
                int chunkSize = Math.min(128, encrypted.length - offset);
                byte[] chunk = new byte[chunkSize];
                System.arraycopy(encrypted, offset, chunk, 0, chunkSize);

                byte p1 = (byte) ((offset >> 8) & 0xFF);
                byte p2 = (byte) (offset & 0xFF);

                CommandAPDU apdu = new CommandAPDU(CLA, INS_UPLOAD_AVATAR, p1, p2, chunk);
                ResponseAPDU resp = sendAPDU(apdu);
                
                if (resp == null || resp.getSW() != 0x9000) {
                    System.out.println("[JCSIM] ❌ Chunk " + chunkCount + " failed");
                    return;
                }

                offset += chunkSize;
                chunkCount++;
            }
            
            System.out.println("[JCSIM] ✅ Encrypted avatar uploaded: " + chunkCount + " chunks");
            
        } catch (Exception e) {
            System.out.println("[JCSIM] ⚠️ Restore avatar error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void restoreBalance(long balanceVND) {
        try {
            long remaining = balanceVND;
            while (remaining > 0) {
                int units = (int) Math.min(remaining / BALANCE_UNIT, 255);
                if (units <= 0) break;

                CommandAPDU apdu = new CommandAPDU(CLA, INS_TOPUP, (byte) units, 0x00, 2);
                ResponseAPDU resp = sendAPDU(apdu);

                if (resp == null || resp.getSW() != 0x9000) break;

                remaining -= units * BALANCE_UNIT;
            }
        } catch (Exception e) {
            System.out.println("[JCSIM] ⚠️ Restore balance error: " + e.getMessage());
        }
    }

    // ====================== SEND APDU ======================
    private ResponseAPDU sendAPDU(CommandAPDU apdu) {
        if (!isConnected) return null;

        try {
            byte[] response = simulator.transmitCommand(apdu.getBytes());
            ResponseAPDU resp = new ResponseAPDU(response);

            System.out.println("[JCSIM] → " + bytesToHex(apdu.getBytes()));
            System.out.println("[JCSIM] ← " + bytesToHex(response)
                    + " (SW=" + String.format("%04X", resp.getSW()) + ")");

            return resp;
        } catch (Exception e) {
            System.out.println("[JCSIM] ❌ APDU error: " + e.getMessage());
            return null;
        }
    }

    // ====================== PIN OPERATIONS ======================
    public boolean verifyPIN(String pin6) {
        if (pin6 == null || pin6.length() != PIN_SIZE) return false;
        if (isCardBlocked) {
            System.out.println("[JCSIM] ❌ Card is BLOCKED!");
            return false;
        }

        byte[] pinBytes = pin6.getBytes();
        CommandAPDU apdu = new CommandAPDU(CLA, INS_VERIFY_PIN, 0x00, 0x00, pinBytes);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null) {
            if (resp.getSW() == 0x9000) {
                pinVerified = true;
                pinTriesRemaining = PIN_TRY_LIMIT;
                
                // Update PC crypto
                updatePCCrypto(pin6);
                
                // Update card key
                byte[] cardKey = derive3DESKey(pin6);
                CommandAPDU keyApdu = new CommandAPDU(CLA, INS_SET_AES_KEY, 0x00, 0x00, cardKey);
                sendAPDU(keyApdu);
                
                System.out.println("[JCSIM] ✅ PIN verified!");
                return true;
            } else if ((resp.getSW() & 0xFFF0) == 0x63C0) {
                pinTriesRemaining = resp.getSW() & 0x000F;
                pinVerified = false;

                if (pinTriesRemaining <= 0) {
                    isCardBlocked = true;
                    System.out.println("[JCSIM] 🔒 CARD BLOCKED!");
                }
                saveCardState();
            }
        }

        return false;
    }

    public boolean changePIN(String oldPin, String newPin) {
        if (oldPin == null || oldPin.length() != PIN_SIZE
                || newPin == null || newPin.length() != PIN_SIZE) {
            return false;
        }

        byte[] data = new byte[12];
        System.arraycopy(oldPin.getBytes(), 0, data, 0, 6);
        System.arraycopy(newPin.getBytes(), 0, data, 6, 6);

        CommandAPDU apdu = new CommandAPDU(CLA, INS_CHANGE_PIN, 0x00, 0x00, data);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            currentPIN = newPin;
            isFirstLogin = false;
            
            // Update PC crypto
            updatePCCrypto(newPin);
            
            // Update card key
            byte[] newKey = derive3DESKey(newPin);
            CommandAPDU keyApdu = new CommandAPDU(CLA, INS_SET_AES_KEY, 0x00, 0x00, newKey);
            sendAPDU(keyApdu);
            
            System.out.println("[JCSIM] ✅ PIN changed!");

            if (verifyPIN(newPin)) {
                System.out.println("[JCSIM] ✅ Auto-verified with new PIN!");
            }

            saveCardState();
            return true;
        }

        return false;
    }

    public boolean unblockCard(String phone) {
        if (phone == null || phone.isEmpty()) return false;

        if (recoveryPhone == null || !recoveryPhone.equals(phone)) {
            System.out.println("[JCSIM] ❌ Phone not match!");
            return false;
        }

        byte[] phoneBytes = phone.getBytes();
        CommandAPDU apdu = new CommandAPDU(CLA, INS_UNBLOCK_PIN, 0x00, 0x00, phoneBytes);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            pinTriesRemaining = PIN_TRY_LIMIT;
            isCardBlocked = false;
            isFirstLogin = true;
            currentPIN = "123456";
            pinVerified = false;
            
            // Reset crypto
            updatePCCrypto("123456");
            
            byte[] defaultKey = derive3DESKey("123456");
            CommandAPDU keyApdu = new CommandAPDU(CLA, INS_SET_AES_KEY, 0x00, 0x00, defaultKey);
            sendAPDU(keyApdu);
            
            System.out.println("[JCSIM] ✅ Card unblocked!");
            saveCardState();
            return true;
        }

        return false;
    }

    // ====================== INFO OPERATIONS ======================
    
    public boolean updateInfo(String phone, String info) {
        if (!pinVerified) {
            System.out.println("[JCSIM] ❌ PIN not verified");
            return false;
        }

        try {
            byte[] phoneBytes = (phone != null) ? phone.getBytes("UTF-8") : new byte[0];
            byte[] infoBytes = (info != null) ? info.getBytes("UTF-8") : new byte[0];

            if (phoneBytes.length > 12) {
                phoneBytes = Arrays.copyOf(phoneBytes, 12);
            }
            if (infoBytes.length > INFO_MAX_SIZE) {
                infoBytes = Arrays.copyOf(infoBytes, INFO_MAX_SIZE);
            }

            byte[] data = new byte[1 + phoneBytes.length + infoBytes.length];
            data[0] = (byte) phoneBytes.length;
            System.arraycopy(phoneBytes, 0, data, 1, phoneBytes.length);
            System.arraycopy(infoBytes, 0, data, 1 + phoneBytes.length, infoBytes.length);

            System.out.println("[JCSIM] 📤 Sending info (" + infoBytes.length + " bytes) to card for encryption...");
            CommandAPDU apdu = new CommandAPDU(CLA, INS_UPDATE_INFO, 0x00, 0x00, data);
            ResponseAPDU resp = sendAPDU(apdu);

            if (resp != null && resp.getSW() == 0x9000) {
                this.recoveryPhone = phone;
                this.savedInfo = info;
                saveCardState();
                System.out.println("[JCSIM] ✅ Info encrypted and saved");
                return true;
            }
        } catch (Exception e) {
            System.out.println("[JCSIM] ❌ Update info error: " + e.getMessage());
        }

        return false;
    }

    public String getInfo() {
        if (!pinVerified) {
            System.out.println("[JCSIM] ⚠️ PIN not verified, returning cached");
            return savedInfo;
        }

        System.out.println("[JCSIM] 📥 Requesting decrypted info...");
        CommandAPDU apdu = new CommandAPDU(CLA, INS_GET_INFO, 0x00, 0x00, INFO_MAX_SIZE);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            byte[] data = resp.getData();
            if (data.length > 0) {
                try {
                    savedInfo = new String(data, "UTF-8").trim();
                    System.out.println("[JCSIM] ✅ Received plaintext info");
                    return savedInfo;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return savedInfo;
    }

    // ====================== AVATAR OPERATIONS - PC ENCRYPTS ======================
    
    /**
     * ✅ MÃ HÓA AVATAR PC-SIDE
     */
    private byte[] encryptAvatar(byte[] plaintext) throws Exception {
        desCipher.init(Cipher.ENCRYPT_MODE, desKey, ivSpec);
        return desCipher.doFinal(plaintext);
    }

    /**
     * ✅ GIẢI MÃ AVATAR PC-SIDE
     */
    private byte[] decryptAvatar(byte[] encrypted) throws Exception {
        desCipher.init(Cipher.DECRYPT_MODE, desKey, ivSpec);
        return desCipher.doFinal(encrypted);
    }

    /**
     * ✅ UPLOAD AVATAR - MÃ HÓA TOÀN BỘ TRƯỚC
     */
    public boolean uploadAvatar(byte[] avatarData) {
        if (!pinVerified) {
            System.out.println("[JCSIM] ❌ PIN not verified");
            return false;
        }
        
        if (avatarData == null || avatarData.length == 0) {
            System.out.println("[JCSIM] ❌ Avatar data is null");
            return false;
        }
        
        if (avatarData.length > AVATAR_MAX_SIZE) {
            System.out.println("[JCSIM] ❌ Avatar too large");
            return false;
        }

        try {
            System.out.println("[JCSIM] 🔐 Encrypting avatar on PC (" + avatarData.length + " bytes)...");
            
            // ✅ MÃ HÓA TOÀN BỘ
            byte[] encrypted = encryptAvatar(avatarData);
            
            System.out.println("[JCSIM] ✅ Encrypted size: " + encrypted.length + " bytes");
            System.out.println("[JCSIM] 📤 Uploading encrypted chunks...");
            
            // Upload chunks
            int offset = 0;
            int chunkCount = 0;
            
            while (offset < encrypted.length) {
                int chunkSize = Math.min(128, encrypted.length - offset);
                byte[] chunk = new byte[chunkSize];
                System.arraycopy(encrypted, offset, chunk, 0, chunkSize);

                byte p1 = (byte) ((offset >> 8) & 0xFF);
                byte p2 = (byte) (offset & 0xFF);

                CommandAPDU apdu = new CommandAPDU(CLA, INS_UPLOAD_AVATAR, p1, p2, chunk);
                ResponseAPDU resp = sendAPDU(apdu);

                if (resp == null || resp.getSW() != 0x9000) {
                    System.out.println("[JCSIM] ❌ Chunk " + chunkCount + " failed");
                    return false;
                }

                offset += chunkSize;
                chunkCount++;
            }

            // Lưu plaintext vào state (để hiển thị)
            savedAvatar = avatarData.clone();
            saveCardState();
            
            System.out.println("[JCSIM] ✅ Avatar uploaded! Chunks: " + chunkCount);
            return true;

        } catch (Exception e) {
            System.out.println("[JCSIM] ❌ Upload error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ✅ GET AVATAR - TẢI VỀ VÀ GIẢI MÃ
     */
    public byte[] getAvatar() {
        if (!pinVerified) {
            System.out.println("[JCSIM] ⚠️ PIN not verified, returning cached");
            return savedAvatar;
        }

        try {
            System.out.println("[JCSIM] 📥 Downloading encrypted avatar...");
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int offset = 0;

            while (offset < AVATAR_MAX_SIZE) {
                byte p1 = (byte) ((offset >> 8) & 0xFF);
                byte p2 = (byte) (offset & 0xFF);

                CommandAPDU apdu = new CommandAPDU(CLA, INS_GET_AVATAR, p1, p2, 128);
                ResponseAPDU resp = sendAPDU(apdu);

                if (resp == null || resp.getSW() != 0x9000) {
                    break;
                }

                byte[] chunk = resp.getData();
                if (chunk.length == 0) {
                    break;
                }

                baos.write(chunk);
                offset += 128;

                if (chunk.length < 128) {
                    break;
                }
            }

            byte[] encrypted = baos.toByteArray();
            
            if (encrypted.length > 0) {
                System.out.println("[JCSIM] ✅ Downloaded " + encrypted.length + " bytes (encrypted)");
                System.out.println("[JCSIM] 🔓 Decrypting on PC...");
                
                // ✅ GIẢI MÃ
                byte[] plaintext = decryptAvatar(encrypted);
                
                savedAvatar = plaintext;
                System.out.println("[JCSIM] ✅ Decrypted avatar: " + plaintext.length + " bytes");
                return plaintext;
            }
            
        } catch (Exception e) {
            System.out.println("[JCSIM] ❌ Get avatar error: " + e.getMessage());
            e.printStackTrace();
        }

        return savedAvatar;
    }

    // ====================== BALANCE OPERATIONS ======================
    public long getBalance() {
        if (!pinVerified) return savedBalance;

        CommandAPDU apdu = new CommandAPDU(CLA, INS_CHECK_BALANCE, 0x00, 0x00, 2);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            byte[] data = resp.getData();
            if (data.length == 2) {
                int units = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
                savedBalance = (long) units * BALANCE_UNIT;
                return savedBalance;
            }
        }
        return savedBalance;
    }

    public boolean topup(long amountVND) {
        if (!pinVerified || amountVND <= 0) return false;

        int units = (int) (amountVND / BALANCE_UNIT);
        if (units <= 0 || units > 255) return false;

        CommandAPDU apdu = new CommandAPDU(CLA, INS_TOPUP, (byte) units, 0x00, 2);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            savedBalance = getBalance();
            saveCardState();
            return true;
        }

        return false;
    }

    public boolean deductBalance(long amountVND) {
        if (!pinVerified || amountVND <= 0) return false;

        int units = (int) (amountVND / BALANCE_UNIT);
        if (units <= 0 || units > 255) return false;

        CommandAPDU apdu = new CommandAPDU(CLA, INS_PAYMENT, (byte) units, 0x00, 2);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            savedBalance = getBalance();
            saveCardState();
            return true;
        }

        return false;
    }

    // ====================== OTHER ======================
    public boolean checkIn() {
        return pinVerified;
    }

    public byte[] signTransaction(byte type, long amountVND) {
        if (!pinVerified) return new byte[0];

        try {
            String txData = type + "|" + amountVND + "|" + System.currentTimeMillis();
            byte[] data = txData.getBytes();

            CommandAPDU apdu = new CommandAPDU(CLA, INS_HASH_SHA1, 0x00, 0x00, data, 20);
            ResponseAPDU resp = sendAPDU(apdu);

            if (resp != null && resp.getSW() == 0x9000) {
                return resp.getData();
            }
        } catch (Exception e) {
            // ignore
        }

        return new byte[0];
    }

    // ====================== STATE ======================
    public void saveCardState() {
        try {
            CardState state = new CardState();
            state.cardId = cardId;
            state.recoveryPhone = recoveryPhone;
            state.currentPIN = currentPIN;
            state.pinTriesRemaining = pinTriesRemaining;
            state.isFirstLogin = isFirstLogin;
            state.isCardBlocked = isCardBlocked;
            state.savedInfo = savedInfo;
            state.savedAvatar = savedAvatar;
            state.savedBalance = savedBalance;

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CARD_STATE_FILE))) {
                oos.writeObject(state);
            }
            System.out.println("[JCSIM] 💾 State saved");
        } catch (Exception e) {
            System.out.println("[JCSIM] ⚠️ Save error: " + e.getMessage());
        }
    }

    private void loadCardState() {
        try {
            File file = new File(CARD_STATE_FILE);
            if (!file.exists()) {
                System.out.println("[JCSIM] ℹ️ No saved state");
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                CardState state = (CardState) ois.readObject();
                cardId = state.cardId;
                recoveryPhone = state.recoveryPhone;
                currentPIN = state.currentPIN;
                pinTriesRemaining = state.pinTriesRemaining;
                isFirstLogin = state.isFirstLogin;
                isCardBlocked = state.isCardBlocked;
                savedInfo = state.savedInfo;
                savedAvatar = state.savedAvatar;
                savedBalance = state.savedBalance;
            }

            System.out.println("[JCSIM] ✅ State loaded");
        } catch (Exception e) {
            System.out.println("[JCSIM] ⚠️ Load error: " + e.getMessage());
        }
    }

    // ====================== GETTERS/SETTERS ======================
    public void setCardId(String id) { this.cardId = id; }
    public String getCardId() { return cardId; }
    public void setRecoveryPhone(String phone) { this.recoveryPhone = phone; saveCardState(); }
    public String getRecoveryPhone() { return recoveryPhone; }
    public boolean isFirstLogin() { return isFirstLogin; }
    public void setFirstLoginComplete() { isFirstLogin = false; saveCardState(); }
    public int getPinTriesRemaining() { return pinTriesRemaining; }
    public boolean isPinVerified() { return pinVerified; }
    public boolean isCardBlocked() { return isCardBlocked; }
    
    public void logout() {
        saveCardState();
        pinVerified = false;
    }

    public void reset() {
        simulator.reset();
        simulator.selectApplet(appletAID);
        
        CommandAPDU apdu = new CommandAPDU(CLA, INS_INIT_CRYPTO, 0x00, 0x00, 1);
        sendAPDU(apdu);
        
        byte[] keyToSet = derive3DESKey(currentPIN != null ? currentPIN : "123456");
        apdu = new CommandAPDU(CLA, INS_SET_AES_KEY, 0x00, 0x00, keyToSet);
        sendAPDU(apdu);
        
        pinVerified = false;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    public void printStatus() {
        System.out.println("\n╔═══════════════ CARD STATUS ═══════════════╗");
        System.out.println("║ Card ID:      " + cardId);
        System.out.println("║ Phone:        " + (recoveryPhone != null ? recoveryPhone : "Not set"));
        System.out.println("║ PIN verified: " + (pinVerified ? "✅" : "❌"));
        System.out.println("║ Tries left:   " + pinTriesRemaining);
        System.out.println("║ First login:  " + (isFirstLogin ? "⚠️ Yes" : "No"));
        System.out.println("║ Blocked:      " + (isCardBlocked ? "🔒 Yes" : "No"));
        System.out.println("║ Balance:      " + savedBalance + " VNĐ");
        System.out.println("║ Encryption:   ✅ PC-side 3DES");
        System.out.println("╚════════════════════════════════════════════╝\n");
    }

    private static class CardState implements Serializable {
        private static final long serialVersionUID = 6L;
        String cardId;
        String recoveryPhone;
        String currentPIN;
        int pinTriesRemaining;
        boolean isFirstLogin;
        boolean isCardBlocked;
        String savedInfo;
        byte[] savedAvatar;
        long savedBalance;
    }
}