package com.example.base.archetype.service;

import com.example.base.archetype.util.TrafficDataPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CryptoService {

    private final TrafficDataPaths trafficDataPaths;

    public String decrypt(byte[] encryptedContent) throws IOException {
        var keyFile = trafficDataPaths.keyFile();
        byte[] key = Files.readString(keyFile, StandardCharsets.UTF_8)
                .trim()
                .getBytes(StandardCharsets.UTF_8);
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IOException("AES key must contain 16, 24 or 32 bytes: " + keyFile);
        }

        try {
            byte[] ciphertext = encryptedContent;
            String base64Content = new String(encryptedContent, StandardCharsets.US_ASCII).trim();
            try {
                byte[] decodedContent = Base64.getDecoder().decode(base64Content);
                if (decodedContent.length > 0 && decodedContent.length % 16 == 0) {
                    ciphertext = decodedContent;
                }
            } catch (IllegalArgumentException ignored) {
                // The encrypted file may contain raw AES ciphertext instead of Base64.
            }
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new IOException("Unable to decrypt traffic data with AES/ECB", exception);
        }
    }
}
