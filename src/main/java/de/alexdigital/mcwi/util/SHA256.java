package de.alexdigital.mcwi.util;

import org.apache.commons.codec.binary.Hex;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Alex on 08.02.2016.
 */
public class SHA256 {

    private static SHA256 instance;

    public static SHA256 getInstance() {
        if (instance == null) instance = new SHA256();
        return instance;
    }

    public String hash(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(string.getBytes());
            byte[] digest = messageDigest.digest();
            return new String(Hex.encodeHex(digest));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "ERROR";
    }

}
