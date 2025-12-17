package gym_app;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.MessageDigest;
import java.util.Random;
import java.util.Arrays;

public class SecurityUtils {
    // Khóa mã hóa cho DB (16 bytes) - Hardcoded key
    private static final String KEY_STRING = "PowerGym2025Key!"; 
    
    // IV cố định (16 bytes) cho chế độ CBC
    private static final byte[] IV = { 
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 
        0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F
    };

    /**
     * Mã hóa tên, số điện thoại bằng AES/CBC/PKCS5Padding
     */
    public static String encrypt(String data) {
        try {
            SecretKeySpec key = new SecretKeySpec(KEY_STRING.getBytes("UTF-8"), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            
            // SỬA: Dùng chế độ CBC (an toàn hơn ECB)
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); 
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

            return java.util.Base64.getEncoder()
                    .encodeToString(cipher.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("[Crypto] Encrypt failed: " + e.getMessage());
            e.printStackTrace();
            return data;
        }
    }

    /**
     * Giải mã bằng AES/CBC/PKCS5Padding
     */
    public static String decrypt(String encrypted) {
        try {
            SecretKeySpec key = new SecretKeySpec(KEY_STRING.getBytes("UTF-8"), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            
            // SỬA: Dùng chế độ CBC
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

            return new String(cipher.doFinal(java.util.Base64.getDecoder().decode(encrypted)), "UTF-8");
        } catch (Exception e) {
            System.out.println("[Crypto] Decrypt failed: " + e.getMessage());
            e.printStackTrace();
            return encrypted;
        }
    }

    /**
     * Hash PIN (SHA-256) - Không được dùng trong flow chính, giữ lại theo yêu cầu
     */
    public static String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return pin;
        }
    }

    /**
     * Hash SỐ ĐIỆN THOẠI – SHA-256 (Dùng cho phone_hash trong DB)
     */
    public static String hashPhone(String phone) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(phone.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return phone;
        }
    }

    /**
     * Sinh mã thẻ ngẫu nhiên (chỉ dùng trong SmartCardService.initNewCard)
     */
    public static String generateCardId() {
        Random r = new Random();
        return String.format("GYM%04d", r.nextInt(10000));
    }
}