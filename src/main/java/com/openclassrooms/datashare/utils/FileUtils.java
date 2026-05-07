package com.openclassrooms.datashare.utils;

import org.bouncycastle.crypto.digests.Blake3Digest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

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
            byte[] fileBytes = inputStream.readAllBytes();

            Blake3Digest digest = new Blake3Digest();
            digest.update(fileBytes, 0, fileBytes.length);
            byte[] hashBytes = new byte[digest.getDigestSize()];
            digest.doFinal(hashBytes, 0);

            return bytesToHex(hashBytes);
        }
    }
}
