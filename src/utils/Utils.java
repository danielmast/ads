package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class. Contains static methods for hashing purposes.
 */
public class Utils {
    public final static String HASH_DEFAULT = "SHA";

    // Used for computing hashes of sentinel nodes
    public final static String ZERO = "0";
    public final static String MININF = "-Inf";


    public static MessageDigest getMD() {
        return getMD(Utils.HASH_DEFAULT);
    }

    public static MessageDigest getMD(String algorithm) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md;
    }

    public static byte[] hash(byte[] message) {
        MessageDigest md = getMD();
        md.update(message);
        return md.digest();
    }

    public static byte[] hash(String message) {
        byte[] input = message.getBytes();
        return hash(input);
    }

    public static byte[] hash(int value) {
        return hash(Integer.toString(value));
    }

    public static byte[] hash(byte[] left, byte[] right) {
        MessageDigest md = getMD();
        md.update(left);
        md.update(right);

        return md.digest(); // Note that digest() not only return a value. Calling it twice changes the output.
    }
}
