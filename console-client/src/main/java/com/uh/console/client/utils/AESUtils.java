package com.uh.console.client.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class AESUtils {

    private static final String KEY = "hj7x89H$yuBI0456";
    private static final String IV = "NIfb&95GUY86Gfgh";

    private AESUtils() {
    }

    public static String encryptAES(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = cipher.getBlockSize();
            byte[] dataBytes = plainText.getBytes(StandardCharsets.UTF_8);
            int plainLength = dataBytes.length;
            if (plainLength % blockSize != 0) {
                plainLength += blockSize - (plainLength % blockSize);
            }
            byte[] plaintext = new byte[plainLength];
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);

            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext));
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt content with AES", e);
        }
    }
}

