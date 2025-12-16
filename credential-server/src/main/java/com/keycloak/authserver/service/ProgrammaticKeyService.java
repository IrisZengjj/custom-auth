package com.keycloak.authserver.service;

import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

@Service
public class ProgrammaticKeyService {

    private KeyPair keyPair;

    @PostConstruct
    public void init() {
        try {
            System.out.println("=== 程序生成RSA密钥对 ===");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            this.keyPair = keyGen.generateKeyPair();

            // 验证密钥生成成功
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            System.out.println("✅ 密钥对生成成功");
            System.out.println("公钥算法: " + publicKey.getAlgorithm());
            System.out.println("公钥格式: " + publicKey.getFormat());
            System.out.println("私钥算法: " + privateKey.getAlgorithm());
            System.out.println("私钥格式: " + privateKey.getFormat());

        } catch (Exception e) {
            System.err.println("❌ 密钥对生成失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getPublicKeyBase64() {
        if (keyPair != null) {
            byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
            return Base64.getEncoder().encodeToString(publicKeyBytes);
        }
        throw new RuntimeException("密钥对未初始化");
    }

    public PrivateKey getPrivateKey() {
        if (keyPair != null) {
            return keyPair.getPrivate();
        }
        throw new RuntimeException("密钥对未初始化");
    }

    public byte[] decryptAesKey(String encryptedKeyB64) throws Exception {
        byte[] encryptedAesKey = Base64.getDecoder().decode(encryptedKeyB64);
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        return rsaCipher.doFinal(encryptedAesKey);
    }

    public String decryptData(String encryptedDataB64, byte[] decryptedAesKey) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(encryptedDataB64);
        SecretKeySpec aesKey = new SecretKeySpec(decryptedAesKey, "AES");
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedData = aesCipher.doFinal(encryptedData);
        return new String(decryptedData);
    }
}