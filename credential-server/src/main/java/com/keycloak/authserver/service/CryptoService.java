package com.keycloak.authserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

@Service
public class CryptoService {

    @Value("${app.rsa.private-key:}")
    private String privateKeyString;

    public boolean isConfigured() {
        return privateKeyString != null && !privateKeyString.trim().isEmpty();
    }

    public String getPublicKeyInBase64() throws Exception {
        System.out.println("=== getPublicKeyInBase64 开始 ===");

        if (!isConfigured()) {
            throw new IllegalStateException("RSA私钥未配置，请检查app.rsa.private-key配置");
        }

        try {
            // 先验证私钥
            validatePrivateKey();

            PrivateKey privateKey = getPrivateKey();
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateCrtKey privateCrtKey = (RSAPrivateCrtKey) privateKey;
            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privateCrtKey.getModulus(), privateCrtKey.getPublicExponent());
            java.security.PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            String publicKeyB64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

            System.out.println("公钥生成成功");
            return publicKeyB64;

        } catch (Exception e) {
            System.out.println("生成公钥失败: " + e.getMessage());
            throw e;
        }
    }

    private void validatePrivateKey() throws Exception {
        System.out.println("=== 验证私钥 ===");
        System.out.println("原始私钥长度: " + privateKeyString.length());

        String cleanedKey = privateKeyString.trim().replaceAll("\\s", "");
        System.out.println("清理后私钥长度: " + cleanedKey.length());

        if (cleanedKey.isEmpty()) {
            throw new IllegalArgumentException("私钥字符串为空");
        }

        // 检查Base64格式
        try {
            byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
            System.out.println("Base64解码成功，字节数: " + keyBytes.length);
        } catch (IllegalArgumentException e) {
            System.out.println("Base64解码失败");
            throw e;
        }
    }

    private PrivateKey getPrivateKey() throws Exception {
        System.out.println("=== 加载私钥对象 ===");

        String cleanedKey = privateKeyString.trim().replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);

        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            System.out.println("私钥对象创建成功");
            return privateKey;
        } catch (Exception e) {
            System.out.println("创建私钥对象失败: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }

    public byte[] decryptAesKey(String encryptedKeyB64) throws Exception {
        //清理输入的空格、换行、制表符
        String cleanedKey = encryptedKeyB64.trim().replaceAll("\\s+", "");
        byte[] encryptedAesKey = Base64.getUrlDecoder().decode(cleanedKey);
        PrivateKey privateKey = getPrivateKey();
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        return rsaCipher.doFinal(encryptedAesKey);
    }

    public String decryptData(String encryptedDataB64, byte[] decryptedAesKey) throws Exception {
        //清理输入的空格、换行、制表符
        String cleanedData = encryptedDataB64.trim().replaceAll("\\s+", "");
        byte[] encryptedData = Base64.getUrlDecoder().decode(cleanedData);
        SecretKeySpec aesKey = new SecretKeySpec(decryptedAesKey, "AES");
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedData = aesCipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }
}

/** 暂时修改，检查密钥，以下为原版
@Service
public class CryptoService {

    @Value("${app.rsa.private-key}")
    private String privateKeyString;

    // 添加Logger
    private static final Logger logger = LoggerFactory.getLogger(CryptoService.class);

    public byte[] decryptAesKey(String encryptedKeyB64) throws Exception {
        logger.info("开始解密AES密钥，Base64长度: " + encryptedKeyB64.length());

        byte[] encryptedAesKey = Base64.getDecoder().decode(encryptedKeyB64);
        PrivateKey privateKey = getPrivateKey(privateKeyString);

        logger.info("成功加载私钥，算法: " + privateKey.getAlgorithm());

        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedKey = rsaCipher.doFinal(encryptedAesKey);

        logger.info("AES密钥解密成功，长度: " + decryptedKey.length);
        return decryptedKey;
    }

    public String getPublicKeyInBase64() throws Exception {
        logger.info("开始生成公钥");
        PrivateKey privateKey = getPrivateKey(privateKeyString);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateCrtKey privateCrtKey = (RSAPrivateCrtKey) privateKey;
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privateCrtKey.getModulus(), privateCrtKey.getPublicExponent());
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        String publicKeyB64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        logger.info("公钥生成成功，Base64长度: " + publicKeyB64.length());
        return publicKeyB64;
    }

    public String decryptData(String encryptedDataB64, byte[] decryptedAesKey) throws Exception {
        logger.info("开始解密数据，加密数据长度: " + encryptedDataB64.length() + ", AES密钥长度: " + decryptedAesKey.length);

        byte[] encryptedData = Base64.getDecoder().decode(encryptedDataB64);
        SecretKeySpec aesKey = new SecretKeySpec(decryptedAesKey, "AES");
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedData = aesCipher.doFinal(encryptedData);

        String result = new String(decryptedData);
        logger.info("数据解密成功，解密后内容长度: " + result.length());
        return result;
    }

    private PrivateKey getPrivateKey(String key) throws Exception {
        logger.info("开始加载私钥，Base64输入长度: " + key.length());
        byte[] keyBytes = Base64.getDecoder().decode(key);
        logger.info("私钥解码后字节长度: " + keyBytes.length);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        logger.info("私钥加载成功");
        return privateKey;
    }
}
**/

/**

package com.keycloak.authserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import com.keycloak.authserver.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;

@Service
public class CryptoService {

    @Value("${app.rsa.private-key}")
    private String privateKeyString;

    public byte[] decryptAesKey(String encryptedKeyB64) throws Exception {
        byte[] encryptedAesKey = Base64.getDecoder().decode(encryptedKeyB64);
        PrivateKey privateKey = getPrivateKey(privateKeyString);
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        return rsaCipher.doFinal(encryptedAesKey);
    }

    public String getPublicKeyInBase64() throws Exception {
        PrivateKey privateKey = getPrivateKey(privateKeyString);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPrivateCrtKey privateCrtKey = (RSAPrivateCrtKey) privateKey;
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(privateCrtKey.getModulus(), privateCrtKey.getPublicExponent());
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String decryptData(String encryptedDataB64, byte[] decryptedAesKey) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(encryptedDataB64);
        SecretKeySpec aesKey = new SecretKeySpec(decryptedAesKey, "AES");
        Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedData = aesCipher.doFinal(encryptedData);
        return new String(decryptedData);
    }

    private PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}

 */