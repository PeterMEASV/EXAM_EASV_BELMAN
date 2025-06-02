package exam_easv_belman.BLL.util;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class PBKDF2PasswordUtil {
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final int ITERATIONS = 50000;

    public static String hashPassword(String password) throws Exception {
        byte[] salt = generateSalt();
        byte[] hash = pbkdf2Hash(password, salt, ITERATIONS, HASH_LENGTH);

        // Use Base64 encoding to make the binary data (byte[]) into text.
        // Stored as one String to ease db overhead.
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Verifies if the provided plaintext password matches the hashed password.
     *
     * @param password the plaintext password provided for verification
     * @param hashedPassword the hashed password string, which includes iteration count, salt, and hash
     * @return true if the password matches the hashed password; false otherwise
     * @throws Exception if an error occurs during the password hashing process
     */
    public static boolean verifyPassword(String password, String hashedPassword) throws Exception {
        //split String on :
        String[] parts = hashedPassword.split(":");
        //get iterations from String array
        int iterations = Integer.parseInt(parts[0]);
        //get the stored, randomly generated, salt from String array
        byte[] salt = Base64.getDecoder().decode(parts[1]);
        //get hash from String array -- the actual password.
        byte[] hash = Base64.getDecoder().decode(parts[2]);

        //run all the parts through the pbkdf2 algo to compare.
        byte[] combinedHash = pbkdf2Hash(password, salt, iterations, hash.length);
        //use a time-adjustable equals method to counter bruteforce attacks (wont happen for this app, but probably good practice)
        return controlledEquals(hash, combinedHash);
    }



    /**
     * Compares two byte arrays in a way that mitigates timing attacks. This method ensures
     * that the time required for comparison is consistent regardless of how many
     * input values match, making it safer against attacks that rely on timing variations.
     *
     * @param hash the first byte array, typically representing the expected hash value
     * @param combinedHash the second byte array, typically representing the computed hash value
     * @return true if the two byte arrays are equal in content and length; false otherwise
     */
    private static boolean controlledEquals(byte[] hash, byte[] combinedHash) {
        //cool new operator ^ (bitwise XOR) if equal in length = 0, if not, != 0
        int diff = hash.length ^ combinedHash.length;
        for (int i = 0; i < hash.length && i < combinedHash.length; i++) {
            diff |= combinedHash[i] ^ hash[i];
        }
        return diff == 0;
    }


    /**
     * Generates a hash using the PBKDF2 (Password-Based Key Derivation Function 2) algorithm with HMAC SHA-256.
     * This method derives a secure key from the input password and salt, using the defined number of iterations
     * and hash length.
     *
     * @param password the password to be hashed
     * @param salt the cryptographic salt, used to ensure uniqueness of the derived hash
     * @param iterations the number of iterations to apply during the hashing process
     * @param hashLength the desired length of the resulting hash in bytes
     * @return a byte array containing the derived hash
     * @throws Exception if an error occurs during hash generation, such as an unsupported algorithm
     */
    private static byte[] pbkdf2Hash(String password, byte[] salt, int iterations, int hashLength) throws Exception {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, hashLength * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            //shouldn't happen
            throw new Exception("failed to generate PBKDF2WithHmacSHA256 hash", e);
        }
    }

}
