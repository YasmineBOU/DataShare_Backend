package com.openclassrooms.datashare.utils;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.MessageDigest;

public class FileUtils {

    private static final List<String> SUPPORTED_ALGORITHMS = Arrays.asList(
            "SHA-256",
            "SHA-384",
            "SHA-512",
            "SHA-224",
            "SHA3-224",
            "SHA3-256",
            "SHA3-384",
            "SHA3-512");

    public static String calculateFileHash(MultipartFile file, String algorithm)
            throws IOException, NoSuchAlgorithmException {

        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (!SUPPORTED_ALGORITHMS.contains(algorithm)) {
            throw new IllegalArgumentException(
                    "Algorithm not supported. Use one of the following: " + SUPPORTED_ALGORITHMS);
        }

        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
