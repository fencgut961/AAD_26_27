package com.fencgut.base.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
public class CryptoService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    /**
     * Desencripta en memoria RAM un conjunto de bytes cifrados mediante AES-128.
     *
     * @param encryptedBytes Bytes cifrados provenientes del fichero .enc
     * @param secretKey      Clave secreta ("AadTexSecret2026")
     * @return Bytes descifrados en texto plano (CSV)
     * @throws Exception Si ocurre un error durante el descifrado
     */
    public byte[] decryptAes128(byte[] encryptedBytes, String secretKey) throws Exception {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return cipher.doFinal(encryptedBytes);
    }
}