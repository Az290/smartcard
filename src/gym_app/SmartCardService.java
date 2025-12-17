package gym_app;

import com.licel.jcardsim.base.Simulator;
import javacard.framework.AID;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;

/**
 * SmartCardService - RSA KEM với Master Key do Card tự sinh
 *
 * ✅ GIAI ĐOẠN 1: Card sinh RSA KeyPair, App lấy Public Key ✅ GIAI ĐOẠN 2: PIN
 * được RSA encrypt → Card verify → Card tự hash PIN → Master Key ✅ GIAI ĐOẠN 3:
 * UPDATE - App gửi [RSA(SessionKey)] + [AES_Session(Data)] → Card lưu
 * [AES_Master(Data)] ✅ GIAI ĐOẠN 4: GET - Card đọc [AES_Master(Data)] → trả về
 * [AES_Session(Data)]
 *
 * 🔒 PC KHÔNG BAO GIỜ BIẾT MASTER KEY!
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
    private static final int AES_BLOCK_SIZE = 16;
    private static final int RSA_BLOCK_SIZE = 128;  // RSA-1024
    private static final int SESSION_KEY_SIZE = 16; // AES-128
    private static final int IV_SIZE = 16;

    // ====================== APDU COMMANDS ======================
    private static final byte CLA = (byte) 0x80;

    // PIN Operations
    private static final byte INS_VERIFY_PIN = (byte) 0x10;
    private static final byte INS_CHANGE_PIN = (byte) 0x11;
    private static final byte INS_UNBLOCK_PIN = (byte) 0x12;
    private static final byte INS_VERIFY_PIN_SECURE = (byte) 0x13;   // ✅ MỚI
    private static final byte INS_CHANGE_PIN_SECURE = (byte) 0x14;   // ✅ MỚI

    // Data Operations (RSA KEM)
    private static final byte INS_UPDATE_INFO_ENCAPS = (byte) 0x24;
    private static final byte INS_GET_INFO_ENCAPS = (byte) 0x25;
    private static final byte INS_UPLOAD_AVATAR_ENCAPS = (byte) 0x26;
    private static final byte INS_GET_AVATAR_ENCAPS = (byte) 0x27;

    // Transaction Operations (không mã hóa)
    private static final byte INS_TOPUP = (byte) 0x30;
    private static final byte INS_PAYMENT = (byte) 0x31;
    private static final byte INS_CHECK_BALANCE = (byte) 0x32;
    private static final byte INS_GET_HISTORY = (byte) 0x40;

    // Crypto Setup
    private static final byte INS_INIT_CRYPTO = (byte) 0x50;
    private static final byte INS_GET_STATUS = (byte) 0x51;
    private static final byte INS_GEN_RSA_KEYPAIR = (byte) 0x60;
    private static final byte INS_GET_RSA_PUBLIC = (byte) 0x61;

    // Hash
    private static final byte INS_HASH_DATA = (byte) 0x81;

    // ====================== CRYPTO - PC SIDE ======================
    // ✅ BỎ: masterAesKey, masterIvSpec - PC không biết Master Key
    private Cipher aesCipher;
    private Cipher rsaCipher;
    private SecureRandom secureRandom;

    // RSA Public Key của Card (để encrypt PIN và Session Key)
    private java.security.PublicKey cardRsaPublicKey;
    private boolean cardRsaKeyLoaded = false;

    // Session Key hiện tại (cho mỗi operation)
    private byte[] currentSessionKey;
    private byte[] currentSessionIv;

    // ====================== JCARDSIM ======================
    private Simulator simulator;
    private AID appletAID;
    private boolean isConnected = false;

    // ====================== STATE ======================
    private String cardId;
    private String recoveryPhone = null;
    private String currentPIN = "123456";
    private int pinTriesRemaining = PIN_TRY_LIMIT;
    private boolean pinVerified = false;
    private boolean isFirstLogin = true;
    private boolean isCardBlocked = false;
    private boolean rsaKeysGenerated = false;

    // Cache (chỉ để hiển thị, không dùng để mã hóa)
    private String savedInfo = null;
    private byte[] savedAvatar = null;
    private long savedBalance = 0;

    // ====================== CONSTRUCTOR ======================
    public SmartCardService() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  SMARTCARD SERVICE v2.0 - MASTER KEY DO CARD TỰ SINH       ║");
        System.out.println("║  ✅ PIN: RSA encrypted trên đường truyền                   ║");
        System.out.println("║  ✅ Master Key: Card hash PIN nội bộ (PC không biết!)      ║");
        System.out.println("║  ✅ Session Key: RSA encrypted cho mỗi transaction         ║");
        System.out.println("║  ✅ Data: Session Key (truyền) + Master Key (lưu trữ)      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        try {
            secureRandom = SecureRandom.getInstanceStrong();
        } catch (Exception e) {
            secureRandom = new SecureRandom();
        }

        initCrypto();
        initNewCard();
    }

    /**
     * Khởi tạo crypto objects ở PC-side ✅ KHÔNG CÒN Master Key ở đây
     */
    private void initCrypto() {
        try {
            aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            System.out.println("[Crypto] ✅ PC-side crypto initialized (NO Master Key!)");
        } catch (Exception e) {
            System.out.println("[Crypto] ❌ Init failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Khởi tạo thẻ mới hoàn toàn
     */
    private void initNewCard() {
        cardId = generateNewCardId();

        recoveryPhone = null;
        currentPIN = "123456";
        pinTriesRemaining = PIN_TRY_LIMIT;
        pinVerified = false;
        isFirstLogin = true;
        isCardBlocked = false;
        rsaKeysGenerated = false;
        cardRsaKeyLoaded = false;
        cardRsaPublicKey = null;
        currentSessionKey = null;
        currentSessionIv = null;
        savedInfo = null;
        savedAvatar = null;
        savedBalance = 0;

        initSimulator();

        System.out.println("[CARD] ✅ Thẻ mới được tạo: " + cardId);
    }

    private String generateNewCardId() {
        Random random = new Random();
        return String.format("GYM%06d", random.nextInt(1000000));
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

                // Init crypto trên Card
                CommandAPDU apdu = new CommandAPDU(CLA, INS_INIT_CRYPTO, 0x00, 0x00, 1);
                ResponseAPDU resp = sendAPDU(apdu);
                if (resp != null && resp.getSW() == 0x9000) {
                    System.out.println("[JCSIM] ✅ Card crypto initialized");
                }

                // ✅ GIAI ĐOẠN 1: Sinh RSA Key Pair ngay từ đầu
                System.out.println("[JCSIM] 🔑 Generating RSA key pair...");
                apdu = new CommandAPDU(CLA, INS_GEN_RSA_KEYPAIR, 0x00, 0x00, 1);
                resp = sendAPDU(apdu);
                if (resp != null && resp.getSW() == 0x9000) {
                    rsaKeysGenerated = true;
                    System.out.println("[JCSIM] ✅ RSA key pair generated!");

                    // Load Public Key về PC
                    loadCardRSAPublicKey();
                }
            }
        } catch (Exception e) {
            System.out.println("[JCSIM] ❌ Init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ====================== SEND APDU ======================
    private ResponseAPDU sendAPDU(CommandAPDU apdu) {
        if (!isConnected) {
            return null;
        }

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

    // ====================== RSA KEY MANAGEMENT ======================
    /**
     * Load RSA Public Key từ Card
     */
    private boolean loadCardRSAPublicKey() {
        try {
            System.out.println("[RSA] 📥 Loading RSA public key from card...");

            // Lấy Modulus
            CommandAPDU apdu = new CommandAPDU(CLA, INS_GET_RSA_PUBLIC, 0x01, 0x00, RSA_BLOCK_SIZE);
            ResponseAPDU resp = sendAPDU(apdu);

            if (resp == null || resp.getSW() != 0x9000) {
                System.out.println("[RSA] ❌ Failed to get modulus");
                return false;
            }
            byte[] modulus = resp.getData();

            // Lấy Exponent
            apdu = new CommandAPDU(CLA, INS_GET_RSA_PUBLIC, 0x02, 0x00, 8);
            resp = sendAPDU(apdu);

            if (resp == null || resp.getSW() != 0x9000) {
                System.out.println("[RSA] ❌ Failed to get exponent");
                return false;
            }
            byte[] exponent = resp.getData();

            // Tạo Public Key object
            BigInteger n = new BigInteger(1, modulus);
            BigInteger e = new BigInteger(1, exponent);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            cardRsaPublicKey = factory.generatePublic(spec);

            cardRsaKeyLoaded = true;
            System.out.println("[RSA] ✅ RSA public key loaded!");

            return true;

        } catch (Exception ex) {
            System.out.println("[RSA] ❌ Error loading public key: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Sinh Session Key + IV ngẫu nhiên
     */
    private void generateSessionKey() {
        currentSessionKey = new byte[SESSION_KEY_SIZE];
        currentSessionIv = new byte[IV_SIZE];

        secureRandom.nextBytes(currentSessionKey);
        secureRandom.nextBytes(currentSessionIv);

        System.out.println("[Session] ✅ Generated new session key & IV");
    }

    /**
     * RSA Encrypt Session Key + IV để gửi cho Card
     *
     * @return 128 bytes RSA encrypted (Session Key + IV)
     */
    private byte[] encapsulateSessionKey() throws Exception {
        if (!cardRsaKeyLoaded) {
            throw new Exception("Card RSA public key not loaded");
        }

        // Ghép Session Key (16 bytes) + IV (16 bytes) = 32 bytes
        byte[] keyMaterial = new byte[SESSION_KEY_SIZE + IV_SIZE];
        System.arraycopy(currentSessionKey, 0, keyMaterial, 0, SESSION_KEY_SIZE);
        System.arraycopy(currentSessionIv, 0, keyMaterial, SESSION_KEY_SIZE, IV_SIZE);

        // RSA Encrypt với Card's Public Key
        rsaCipher.init(Cipher.ENCRYPT_MODE, cardRsaPublicKey);
        byte[] encrypted = rsaCipher.doFinal(keyMaterial);

        System.out.println("[Session] 🔐 Encapsulated session key: " + encrypted.length + " bytes");

        return encrypted;
    }

    /**
     * RSA Encrypt PIN để gửi cho Card
     *
     * @return 128 bytes RSA encrypted PIN
     */
    private byte[] encryptPIN(String pin) throws Exception {
        if (!cardRsaKeyLoaded) {
            throw new Exception("Card RSA public key not loaded");
        }

        byte[] pinBytes = pin.getBytes("UTF-8");

        rsaCipher.init(Cipher.ENCRYPT_MODE, cardRsaPublicKey);
        byte[] encrypted = rsaCipher.doFinal(pinBytes);

        System.out.println("[PIN] 🔐 PIN encrypted with RSA: " + encrypted.length + " bytes");

        return encrypted;
    }

    /**
     * AES Encrypt với Session Key
     */
    private byte[] encryptWithSessionKey(byte[] plaintext) throws Exception {
        SecretKeySpec sessionKeySpec = new SecretKeySpec(currentSessionKey, "AES");
        IvParameterSpec sessionIvSpec = new IvParameterSpec(currentSessionIv);

        aesCipher.init(Cipher.ENCRYPT_MODE, sessionKeySpec, sessionIvSpec);
        return aesCipher.doFinal(plaintext);
    }

    /**
     * AES Decrypt với Session Key
     */
    private byte[] decryptWithSessionKey(byte[] ciphertext) throws Exception {
        SecretKeySpec sessionKeySpec = new SecretKeySpec(currentSessionKey, "AES");
        IvParameterSpec sessionIvSpec = new IvParameterSpec(currentSessionIv);

        aesCipher.init(Cipher.DECRYPT_MODE, sessionKeySpec, sessionIvSpec);
        return aesCipher.doFinal(ciphertext);
    }

    // ====================== ✅ PIN OPERATIONS - CẬP NHẬT ======================
    /**
     * ✅ GIAI ĐOẠN 2: Verify PIN với RSA encryption
     *
     * Flow: 1. PC encrypt PIN bằng RSA Public Key của Card 2. Card decrypt bằng
     * RSA Private Key → lấy PIN 3. Card verify PIN 4. Card hash PIN → sinh
     * Master Key (PC không biết!)
     */
    public boolean verifyPIN(String pin6) {
        if (pin6 == null || pin6.length() != PIN_SIZE) {
            return false;
        }
        if (isCardBlocked) {
            System.out.println("[JCSIM] ❌ Card is BLOCKED!");
            return false;
        }

        System.out.println("\n[PIN] ═══════════════════════════════════════════════");
        System.out.println("[PIN] 🔐 VERIFY PIN với RSA encryption");

        try {
            if (!cardRsaKeyLoaded) {
                System.out.println("[PIN] ⚠️ RSA not ready, using legacy method");
                return verifyPINLegacy(pin6);
            }

            // ✅ Encrypt PIN bằng RSA
            byte[] encryptedPIN = encryptPIN(pin6);

            // Gửi INS_VERIFY_PIN_SECURE
            CommandAPDU apdu = new CommandAPDU(CLA, INS_VERIFY_PIN_SECURE, 0x00, 0x00, encryptedPIN);
            ResponseAPDU resp = sendAPDU(apdu);

            if (resp != null && resp.getSW() == 0x9000) {
                pinVerified = true;
                pinTriesRemaining = PIN_TRY_LIMIT;
                currentPIN = pin6;

                System.out.println("[PIN] ✅ PIN verified! Master Key đã được Card tự sinh.");
                System.out.println("[PIN] ═══════════════════════════════════════════════\n");

                return true;

            } else if (resp != null && (resp.getSW() & 0xFFF0) == 0x63C0) {
                pinTriesRemaining = resp.getSW() & 0x000F;
                pinVerified = false;

                if (pinTriesRemaining <= 0) {
                    isCardBlocked = true;
                    System.out.println("[PIN] 🔒 CARD BLOCKED!");
                }

                System.out.println("[PIN] ❌ Wrong PIN! Remaining: " + pinTriesRemaining);
            }

        } catch (Exception e) {
            System.out.println("[PIN] ❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Legacy verify PIN (không RSA encrypt) - fallback
     */
    private boolean verifyPINLegacy(String pin6) {
        byte[] pinBytes = pin6.getBytes();
        CommandAPDU apdu = new CommandAPDU(CLA, INS_VERIFY_PIN, 0x00, 0x00, pinBytes);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            pinVerified = true;
            pinTriesRemaining = PIN_TRY_LIMIT;
            currentPIN = pin6;
            System.out.println("[PIN] ✅ PIN verified (legacy)!");
            return true;
        } else if (resp != null && (resp.getSW() & 0xFFF0) == 0x63C0) {
            pinTriesRemaining = resp.getSW() & 0x000F;
            pinVerified = false;
            if (pinTriesRemaining <= 0) {
                isCardBlocked = true;
            }
        }
        return false;
    }

    /**
     * ✅ Change PIN với RSA encryption
     */
    public boolean changePIN(String oldPin, String newPin) {
        if (oldPin == null || oldPin.length() != PIN_SIZE
                || newPin == null || newPin.length() != PIN_SIZE) {
            return false;
        }

        System.out.println("\n[PIN] ═══════════════════════════════════════════════");
        System.out.println("[PIN] 🔐 CHANGE PIN với RSA encryption");

        try {
            if (!cardRsaKeyLoaded) {
                System.out.println("[PIN] ⚠️ RSA not ready, using legacy method");
                return changePINLegacy(oldPin, newPin);
            }

            // Ghép oldPIN + newPIN
            byte[] bothPins = new byte[PIN_SIZE * 2];
            System.arraycopy(oldPin.getBytes("UTF-8"), 0, bothPins, 0, PIN_SIZE);
            System.arraycopy(newPin.getBytes("UTF-8"), 0, bothPins, PIN_SIZE, PIN_SIZE);

            // RSA Encrypt
            rsaCipher.init(Cipher.ENCRYPT_MODE, cardRsaPublicKey);
            byte[] encryptedPins = rsaCipher.doFinal(bothPins);

            // Gửi INS_CHANGE_PIN_SECURE
            CommandAPDU apdu = new CommandAPDU(CLA, INS_CHANGE_PIN_SECURE, 0x00, 0x00, encryptedPins);
            ResponseAPDU resp = sendAPDU(apdu);

            if (resp != null && resp.getSW() == 0x9000) {
                currentPIN = newPin;
                isFirstLogin = false;

                System.out.println("[PIN] ✅ PIN changed! Master Key đã được cập nhật.");
                System.out.println("[PIN] ═══════════════════════════════════════════════\n");

                // Re-verify với PIN mới
                pinVerified = false;
                return verifyPIN(newPin);

            } else if (resp != null && (resp.getSW() & 0xFFF0) == 0x63C0) {
                pinTriesRemaining = resp.getSW() & 0x000F;
                System.out.println("[PIN] ❌ Wrong old PIN! Remaining: " + pinTriesRemaining);
            }

        } catch (Exception e) {
            System.out.println("[PIN] ❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Legacy change PIN - fallback
     */
    private boolean changePINLegacy(String oldPin, String newPin) {
        byte[] data = new byte[12];
        System.arraycopy(oldPin.getBytes(), 0, data, 0, 6);
        System.arraycopy(newPin.getBytes(), 0, data, 6, 6);

        CommandAPDU apdu = new CommandAPDU(CLA, INS_CHANGE_PIN, 0x00, 0x00, data);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            currentPIN = newPin;
            isFirstLogin = false;
            System.out.println("[PIN] ✅ PIN changed (legacy)!");

            pinVerified = false;
            return verifyPINLegacy(newPin);
        }
        return false;
    }

    /**
     * Unblock Card bằng số điện thoại
     */
    public boolean unblockCard(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }

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

            System.out.println("[JCSIM] ✅ Card unblocked! PIN reset về 123456");
            return true;
        }

        return false;
    }

    // ====================== ✅ INFO OPERATIONS - RSA KEM ======================
    /**
     * ✅ GIAI ĐOẠN 3: UPDATE INFO
     *
     * Flow: 1. PC sinh Session Key + IV 2. PC RSA encrypt Session Key+IV bằng
     * Card's Public Key 3. PC AES encrypt payload bằng Session Key 4. Gửi
     * [RSA(SessionKey+IV)] + [AES_Session(Payload)] 5. Card RSA decrypt → lấy
     * Session Key 6. Card AES decrypt với Session Key → plaintext 7. Card AES
     * encrypt với Master Key → lưu EEPROM
     */
    public boolean updateInfo(String phone, String info) {
        if (!pinVerified) {
            System.out.println("[INFO] ❌ PIN not verified");
            return false;
        }

        if (!cardRsaKeyLoaded) {
            System.out.println("[INFO] ❌ RSA not ready");
            return false;
        }

        try {
            System.out.println("\n[INFO] ═══════════════════════════════════════════════");
            System.out.println("[INFO] 📤 UPDATE INFO với RSA KEM + Master Key");

            // Chuẩn bị payload plaintext
            byte[] phoneBytes = (phone != null) ? phone.getBytes("UTF-8") : new byte[0];
            byte[] infoBytes = (info != null) ? info.getBytes("UTF-8") : new byte[0];

            if (phoneBytes.length > 12) {
                phoneBytes = Arrays.copyOf(phoneBytes, 12);
            }
            if (infoBytes.length > INFO_MAX_SIZE) {
                infoBytes = Arrays.copyOf(infoBytes, INFO_MAX_SIZE);
            }

            // Format: [SDT_LEN][SDT...][INFO...]
            ByteArrayOutputStream payloadBaos = new ByteArrayOutputStream();
            payloadBaos.write((byte) phoneBytes.length);
            payloadBaos.write(phoneBytes);
            payloadBaos.write(infoBytes);
            byte[] plaintextPayload = payloadBaos.toByteArray();

            // Sinh Session Key + Encapsulate
            generateSessionKey();
            byte[] encapsulatedKey = encapsulateSessionKey();
            byte[] encryptedPayload = encryptWithSessionKey(plaintextPayload);

            // Ghép gói APDU
            byte[] apduData = new byte[encapsulatedKey.length + encryptedPayload.length];
            System.arraycopy(encapsulatedKey, 0, apduData, 0, encapsulatedKey.length);
            System.arraycopy(encryptedPayload, 0, apduData, encapsulatedKey.length, encryptedPayload.length);

            System.out.println("[INFO] Total APDU data: " + apduData.length + " bytes");
            System.out.println("[INFO] → Card sẽ lưu encrypted bằng Master Key (PC không biết!)");

            // Gửi APDU
            CommandAPDU apdu = new CommandAPDU(CLA, INS_UPDATE_INFO_ENCAPS, 0x00, 0x00, apduData);
            ResponseAPDU resp = sendAPDU(apdu);

            if (resp != null && resp.getSW() == 0x9000) {
                this.recoveryPhone = phone;
                this.savedInfo = info;
                System.out.println("[INFO] ✅ Info saved (Master Key encrypted on Card)!");
                System.out.println("[INFO] ═══════════════════════════════════════════════\n");
                return true;
            }

            System.out.println("[INFO] ❌ Failed: SW=" + String.format("%04X", resp != null ? resp.getSW() : 0));

        } catch (Exception e) {
            System.out.println("[INFO] ❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * ✅ GIAI ĐOẠN 4: GET INFO
     *
     * Flow: 1. PC sinh Session Key + IV 2. PC RSA encrypt Session Key+IV 3. Gửi
     * [RSA(SessionKey+IV)] 4. Card RSA decrypt → lấy Session Key 5. Card AES
     * decrypt từ EEPROM với Master Key → plaintext 6. Card AES encrypt với
     * Session Key → trả về 7. PC AES decrypt với Session Key → hiển thị
     */
    public String getInfo() {
        if (!pinVerified) {
            System.out.println("[INFO] ⚠️ PIN not verified, returning cached");
            return savedInfo;
        }

        if (!cardRsaKeyLoaded) {
            System.out.println("[INFO] ❌ RSA not ready");
            return savedInfo;
        }

        try {
            System.out.println("\n[INFO] ═══════════════════════════════════════════════");
            System.out.println("[INFO] 📥 GET INFO với RSA KEM + Master Key");

            // Sinh Session Key + Encapsulate
            generateSessionKey();
            byte[] encapsulatedKey = encapsulateSessionKey();

            // Gửi APDU
            CommandAPDU apdu = new CommandAPDU(CLA, INS_GET_INFO_ENCAPS, 0x00, 0x00, encapsulatedKey);
            ResponseAPDU resp = sendAPDU(apdu);

            if (resp != null && resp.getSW() == 0x9000) {
                byte[] encryptedData = resp.getData();

                if (encryptedData.length > 0 && encryptedData.length % AES_BLOCK_SIZE == 0) {
                    // Decrypt với Session Key
                    byte[] plaintext = decryptWithSessionKey(encryptedData);

                    // Xóa padding
                    int decLen = plaintext.length;
                    byte padValue = plaintext[decLen - 1];
                    if (padValue > 0 && padValue <= AES_BLOCK_SIZE) {
                        decLen -= padValue;
                    }

                    savedInfo = new String(plaintext, 0, decLen, "UTF-8").trim();

                    System.out.println("[INFO] ✅ Received: " + savedInfo.length() + " chars");
                    System.out.println("[INFO] ═══════════════════════════════════════════════\n");
                    return savedInfo;
                }
            } else if (resp != null && resp.getSW() == 0x6985) {
                System.out.println("[INFO] ℹ️ No data stored on card");
            }

        } catch (Exception e) {
            System.out.println("[INFO] ❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        return savedInfo;
    }

    // ====================== ✅ AVATAR OPERATIONS - RSA KEM ======================
    /**
     * ✅ UPLOAD AVATAR với RSA KEM + Master Key
     */
    public boolean uploadAvatar(byte[] avatarData) {
        if (!pinVerified) {
            System.out.println("[AVATAR] ❌ PIN not verified");
            return false;
        }

        if (!cardRsaKeyLoaded) {
            System.out.println("[AVATAR] ❌ RSA not ready");
            return false;
        }

        try {
            System.out.println("\n[AVATAR] ═══════════════════════════════════════════════");
            System.out.println("[AVATAR] 📤 UPLOAD AVATAR với RSA KEM + Master Key");
            System.out.println("[AVATAR] Original size: " + avatarData.length + " bytes");

            // Sinh Session Key
            generateSessionKey();
            byte[] encapsulatedKey = encapsulateSessionKey();

            // Encrypt toàn bộ avatar với Session Key (có padding)
            byte[] encryptedAvatar = encryptWithSessionKey(avatarData);

            System.out.println("[AVATAR] Encrypted size: " + encryptedAvatar.length + " bytes");

            // Upload theo chunks
            int offset = 0;
            int maxPayloadPerChunk = 96; // 96 bytes để an toàn

            while (offset < encryptedAvatar.length) {
                int remaining = encryptedAvatar.length - offset;
                int chunkSize = Math.min(maxPayloadPerChunk, remaining);

                // Đảm bảo chunk size là bội số của 16
                if (remaining > maxPayloadPerChunk && chunkSize % AES_BLOCK_SIZE != 0) {
                    chunkSize = (chunkSize / AES_BLOCK_SIZE) * AES_BLOCK_SIZE;
                }

                boolean isLastChunk = (offset + chunkSize >= encryptedAvatar.length);

                byte[] chunkData;
                byte p1, p2;

                if (offset == 0) {
                    // Chunk đầu tiên: có RSA encapsulated key
                    chunkData = new byte[encapsulatedKey.length + chunkSize];
                    System.arraycopy(encapsulatedKey, 0, chunkData, 0, encapsulatedKey.length);
                    System.arraycopy(encryptedAvatar, offset, chunkData, encapsulatedKey.length, chunkSize);
                    p1 = 0;
                    p2 = 0;
                } else {
                    // Các chunk tiếp theo
                    chunkData = new byte[chunkSize];
                    System.arraycopy(encryptedAvatar, offset, chunkData, 0, chunkSize);
                    p1 = (byte) ((offset >> 8) & 0x7F);
                    p2 = (byte) (offset & 0xFF);
                }

                // Đánh dấu chunk cuối
                if (isLastChunk) {
                    p1 = (byte) (p1 | 0x80);  // Set bit 7
                }

                CommandAPDU apdu = new CommandAPDU(CLA, INS_UPLOAD_AVATAR_ENCAPS, p1, p2, chunkData);
                ResponseAPDU resp = sendAPDU(apdu);

                if (resp == null || (resp.getSW() != 0x9000 && !isLastChunk)) {
                    System.out.println("[AVATAR] ❌ Chunk upload failed at offset " + offset);
                    return false;
                }

                // Chunk cuối trả về size
                if (isLastChunk && resp.getSW() == 0x9000 && resp.getData().length == 2) {
                    int storedSize = ((resp.getData()[0] & 0xFF) << 8) | (resp.getData()[1] & 0xFF);
                    System.out.println("[AVATAR] ✓ Card stored: " + storedSize + " bytes (plaintext)");
                }

                System.out.println("[AVATAR] ✓ Chunk: offset=" + offset + ", size=" + chunkSize
                        + (isLastChunk ? " [FINAL]" : ""));

                offset += chunkSize;
            }

            savedAvatar = avatarData.clone();
            System.out.println("[AVATAR] ✅ Avatar uploaded successfully!");
            System.out.println("[AVATAR] ═══════════════════════════════════════════════\n");
            return true;

        } catch (Exception e) {
            System.out.println("[AVATAR] ❌ Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ✅ GET AVATAR với RSA KEM + Master Key
     */
    public byte[] getAvatar() {
        if (!pinVerified) {
            System.out.println("[AVATAR] ⚠️ PIN not verified, returning cached");
            return savedAvatar;
        }

        if (!cardRsaKeyLoaded) {
            System.out.println("[AVATAR] ❌ RSA not ready");
            return savedAvatar;
        }

        try {
            System.out.println("\n[AVATAR] ═══════════════════════════════════════════════");
            System.out.println("[AVATAR] 📥 GET AVATAR với RSA KEM + Master Key");

            // Sinh Session Key
            generateSessionKey();
            byte[] encapsulatedKey = encapsulateSessionKey();

            // Bước 1: Gửi Session Key, nhận plaintext size
            CommandAPDU apdu = new CommandAPDU(CLA, INS_GET_AVATAR_ENCAPS, 0x00, 0x00, encapsulatedKey);
            ResponseAPDU resp = sendAPDU(apdu);

            if (resp == null || resp.getSW() != 0x9000) {
                System.out.println("[AVATAR] ❌ Failed to init get avatar");
                return savedAvatar;
            }

            byte[] sizeData = resp.getData();
            if (sizeData.length < 2) {
                System.out.println("[AVATAR] ℹ️ No avatar stored");
                return null;
            }

            int plaintextSize = ((sizeData[0] & 0xFF) << 8) | (sizeData[1] & 0xFF);
            if (plaintextSize == 0) {
                System.out.println("[AVATAR] ℹ️ Avatar size is 0");
                return null;
            }

            System.out.println("[AVATAR] Plaintext size: " + plaintextSize + " bytes");

            // Bước 2: Download từng chunk
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int offset = 0;

            while (offset < plaintextSize) {
                byte p1 = (byte) ((offset >> 8) & 0xFF);
                byte p2 = (byte) (offset & 0xFF);

                // Gửi request (không cần data)
                apdu = new CommandAPDU(CLA, INS_GET_AVATAR_ENCAPS, p1, p2, new byte[0], 256);
                resp = sendAPDU(apdu);

                if (resp == null || resp.getSW() != 0x9000) {
                    System.out.println("[AVATAR] ❌ Failed at offset " + offset);
                    break;
                }

                byte[] encryptedChunk = resp.getData();
                if (encryptedChunk.length == 0) {
                    break;
                }

                // Decrypt chunk với Session Key
                byte[] decryptedChunk = decryptWithSessionKey(encryptedChunk);

                // Xóa padding (chỉ chunk cuối có padding thực, các chunk khác padding = block size)
                int chunkPlainLen = decryptedChunk.length;
                int remainingToRead = plaintextSize - offset;

                if (remainingToRead < chunkPlainLen) {
                    // Chunk cuối - có padding
                    chunkPlainLen = remainingToRead;
                } else {
                    // Không phải chunk cuối - xóa padding nếu có
                    byte padValue = decryptedChunk[chunkPlainLen - 1];
                    if (padValue > 0 && padValue <= AES_BLOCK_SIZE && chunkPlainLen - padValue >= remainingToRead - 100) {
                        // Có thể là padding, kiểm tra
                        boolean validPad = true;
                        for (int i = chunkPlainLen - padValue; i < chunkPlainLen; i++) {
                            if (decryptedChunk[i] != padValue) {
                                validPad = false;
                                break;
                            }
                        }
                        if (validPad && chunkPlainLen - padValue <= remainingToRead) {
                            chunkPlainLen -= padValue;
                        }
                    }
                }

                // Chỉ lấy đúng số bytes cần thiết
                if (offset + chunkPlainLen > plaintextSize) {
                    chunkPlainLen = plaintextSize - offset;
                }

                baos.write(decryptedChunk, 0, chunkPlainLen);

                System.out.println("[AVATAR] ✓ Chunk: offset=" + offset
                        + ", encrypted=" + encryptedChunk.length
                        + ", plaintext=" + chunkPlainLen);

                offset += chunkPlainLen;

                // Kiểm tra đã đủ chưa
                if (offset >= plaintextSize) {
                    break;
                }
            }

            byte[] result = baos.toByteArray();

            // Cắt đúng size nếu cần
            if (result.length > plaintextSize) {
                result = Arrays.copyOf(result, plaintextSize);
            }

            if (result.length > 0) {
                savedAvatar = result;
                System.out.println("[AVATAR] ✅ Downloaded: " + result.length + " bytes");
                System.out.println("[AVATAR] ═══════════════════════════════════════════════\n");
                return result;
            }

        } catch (Exception e) {
            System.out.println("[AVATAR] ❌ Error: " + e.getMessage());
            e.printStackTrace();
        }

        return savedAvatar;
    }

    // ====================== BALANCE OPERATIONS (không mã hóa) ======================
    public long getBalance() {
        if (!pinVerified) {
            return savedBalance;
        }

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
        if (!pinVerified || amountVND <= 0) {
            return false;
        }

        int units = (int) (amountVND / BALANCE_UNIT);
        if (units <= 0 || units > 255) {
            return false;
        }

        CommandAPDU apdu = new CommandAPDU(CLA, INS_TOPUP, (byte) units, 0x00, 2);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            savedBalance = getBalance();
            return true;
        }

        return false;
    }

    public boolean deductBalance(long amountVND) {
        if (!pinVerified || amountVND <= 0) {
            return false;
        }

        int units = (int) (amountVND / BALANCE_UNIT);
        if (units <= 0 || units > 255) {
            return false;
        }

        CommandAPDU apdu = new CommandAPDU(CLA, INS_PAYMENT, (byte) units, 0x00, 2);
        ResponseAPDU resp = sendAPDU(apdu);

        if (resp != null && resp.getSW() == 0x9000) {
            savedBalance = getBalance();
            return true;
        }

        return false;
    }

    // ====================== OTHER OPERATIONS ======================
    public boolean checkIn() {
        return pinVerified;
    }

    public byte[] signTransaction(byte type, long amountVND) {
        if (!pinVerified) {
            return new byte[0];
        }

        try {
            String txData = type + "|" + amountVND + "|" + System.currentTimeMillis();
            byte[] data = txData.getBytes();

            CommandAPDU apdu = new CommandAPDU(CLA, INS_HASH_DATA, 0x00, 0x00, data, 32);
            ResponseAPDU resp = sendAPDU(apdu);

            if (resp != null && resp.getSW() == 0x9000) {
                return resp.getData();
            }
        } catch (Exception e) {
            // ignore
        }

        return new byte[0];
    }

    public void saveCardState() {
        System.out.println("[CARD] ℹ️ Master Key chỉ tồn tại trên Card - không lưu ở PC");
    }

    // ====================== GETTERS/SETTERS ======================
    public void setCardId(String id) {
        this.cardId = id;
    }

    public String getCardId() {
        return cardId;
    }

    public void setRecoveryPhone(String phone) {
        this.recoveryPhone = phone;
    }

    public String getRecoveryPhone() {
        return recoveryPhone;
    }

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLoginComplete() {
        isFirstLogin = false;
    }

    public int getPinTriesRemaining() {
        return pinTriesRemaining;
    }

    public boolean isPinVerified() {
        return pinVerified;
    }

    public boolean isCardBlocked() {
        return isCardBlocked;
    }

    public boolean isRsaReady() {
        return cardRsaKeyLoaded;
    }

    public void logout() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║  🔄 RÚT THẺ - MASTER KEY TỰ HỦY TRÊN CARD  ║");
        System.out.println("╚════════════════════════════════════════════╝");

        String oldCardId = cardId;

        if (simulator != null) {
            simulator.reset();
        }

        initNewCard();

        System.out.println("[CARD] 🗑️ Đã hủy thẻ: " + oldCardId);
        System.out.println("[CARD] ✅ Thẻ mới sẵn sàng: " + cardId);
    }

    public void reset() {
        simulator.reset();
        simulator.selectApplet(appletAID);

        CommandAPDU apdu = new CommandAPDU(CLA, INS_INIT_CRYPTO, 0x00, 0x00, 1);
        sendAPDU(apdu);

        // Re-generate RSA keys
        apdu = new CommandAPDU(CLA, INS_GEN_RSA_KEYPAIR, 0x00, 0x00, 1);
        ResponseAPDU resp = sendAPDU(apdu);
        if (resp != null && resp.getSW() == 0x9000) {
            rsaKeysGenerated = true;
            loadCardRSAPublicKey();
        }

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
        System.out.println("\n╔═══════════════ CARD STATUS v2.0 ═══════════════╗");
        System.out.println("║ Card ID:       " + cardId);
        System.out.println("║ Phone:         " + (recoveryPhone != null ? recoveryPhone : "Not set"));
        System.out.println("║ PIN verified:  " + (pinVerified ? "✅" : "❌"));
        System.out.println("║ Tries left:    " + pinTriesRemaining);
        System.out.println("║ First login:   " + (isFirstLogin ? "⚠️ Yes" : "No"));
        System.out.println("║ Blocked:       " + (isCardBlocked ? "🔒 Yes" : "No"));
        System.out.println("║ RSA Ready:     " + (cardRsaKeyLoaded ? "✅" : "❌"));
        System.out.println("║ Balance:       " + savedBalance + " VNĐ");
        System.out.println("║ ─────────────────────────────────────────────");
        System.out.println("║ 🔐 Master Key: CHỈ TỒN TẠI TRÊN CARD!");
        System.out.println("║ 🔐 PC không biết Master Key!");
        System.out.println("╚════════════════════════════════════════════════╝\n");
    }
}
