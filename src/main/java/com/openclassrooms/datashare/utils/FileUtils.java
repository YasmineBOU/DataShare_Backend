package com.openclassrooms.datashare.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;

import org.bouncycastle.crypto.digests.Blake3Digest;
import org.springframework.web.multipart.MultipartFile;

/**
 * Utility class for file-related operations such as calculating file hashes,
 * generating unique tokens, and extracting file extensions.
 * This class provides static methods to handle common file processing tasks.
 *
 * <p>
 * Key functionalities include:
 * <ul>
 * <li>Calculating file hashes using the BLAKE3 algorithm for integrity
 * checks.</li>
 * <li>Generating unique tokens for files based on their hash, key, and
 * timestamp.</li>
 * <li>Extracting file extensions from filenames.</li>
 * </ul>
 */
public class FileUtils {

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes The byte array to convert.
     * @return A hexadecimal string representation of the byte array.
     */
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

    /**
     * Calculates the BLAKE3 hash of a file for integrity checks.
     * The file is processed in chunks to avoid loading large files entirely into
     * memory.
     *
     * @param file The file to calculate the hash for (as a {@link MultipartFile}).
     * @return A hexadecimal string representing the file's hash.
     * @throws IOException              If the file cannot be read.
     * @throws IllegalArgumentException If the file is null.
     */
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

    /**
     * Generates a unique token for a file based on its hash, key, and timestamp.
     * The token is derived using SHA-256 hashing and Base64 URL encoding.
     *
     * @param hash        The hash of the file (used as part of the token
     *                    generation).
     * @param fileKey     The unique key of the file (used as part of the token
     *                    generation).
     * @param tokenLength The desired length of the token.
     * @return A unique token for the file.
     * @throws IllegalStateException If the SHA-256 algorithm is not available.
     */
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

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename The filename to extract the extension from.
     * @return The file extension (including the dot), or an empty string if no
     *         extension is found.
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.')).toLowerCase();
    }

}
