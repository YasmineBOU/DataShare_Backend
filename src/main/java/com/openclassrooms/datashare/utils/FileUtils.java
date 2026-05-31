package com.openclassrooms.datashare.utils;

import org.bouncycastle.crypto.digests.Blake3Digest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class FileUtils {

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

    public static String calculateFileHash(MultipartFile file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Process file in chunks to avoid loading entire large files into memory
            Blake3Digest digest = new Blake3Digest();
            byte[] buffer = new byte[8192]; // 8 KB chunks
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = new byte[digest.getDigestSize()];
            digest.doFinal(hashBytes, 0);

            return bytesToHex(hashBytes);
        }
    }

    public static String generateUniqueFileToken(String hash, String fileKey, int tokenLength) {
        try {
            String base = hash.substring(0, 10) +
                    fileKey.substring(0, 5) +
                    Instant.now().toEpochMilli();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(base.getBytes(StandardCharsets.UTF_8));
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
            return token.substring(0, Math.min(tokenLength, token.length()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate unique file token", e);
        }
    }

}
