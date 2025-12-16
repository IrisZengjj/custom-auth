package com.keycloak.authserver.controller;

import com.keycloak.authserver.service.CryptoService;
import com.keycloak.authserver.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class DataReceiveController {

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private CredentialService credentialService;

    private static final Logger logger = LoggerFactory.getLogger(DataReceiveController.class);

    @PostMapping("/api/upload")
    public ResponseEntity<String> receiveEncryptedData(@RequestBody String payload) {
        try {
            logger.info("收到加密数据请求");

            JSONObject jsonPayload;
            try {
                jsonPayload = new JSONObject(payload);
            } catch (JSONException e) {
                return ResponseEntity.badRequest().body("Invalid JSON payload");
            }

            if (!jsonPayload.has("encrypted_data") || !jsonPayload.has("encrypted_key")) {
                return ResponseEntity.badRequest().body("Missing 'encrypted_data' or 'encrypted_key'");
            }

            String encryptedDataB64 = jsonPayload.getString("encrypted_data");
            String encryptedKeyB64 = jsonPayload.getString("encrypted_key");

            if (encryptedDataB64.isEmpty() || encryptedKeyB64.isEmpty()) {
                return ResponseEntity.badRequest().body("Encrypted data or key is empty");
            }

            logger.info("encrypted_data长度: {}, encrypted_key长度: {}",
                    encryptedDataB64.length(), encryptedKeyB64.length());

            // Step 1: 使用服务器私钥解密AES密钥
            byte[] decryptedAesKey = cryptoService.decryptAesKey(encryptedKeyB64);

            // Step 2: 使用解密后的AES密钥解密数据
            String decryptedJsonString = cryptoService.decryptData(encryptedDataB64, decryptedAesKey);
            JSONObject decryptedJson = new JSONObject(decryptedJsonString);

            logger.info("数据解密成功");
            
            // 在控制台输出解密后的原始数据，便于开发过程中查看
            logger.info("=== 解密后的原始数据 ===");
            logger.info("硬件信息: {}", decryptedJson.optJSONObject("硬件信息"));
            logger.info("软件信息: {}", decryptedJson.optJSONObject("软件信息"));
            logger.info("SIM卡信息: {}", decryptedJson.optJSONObject("SIM卡信息"));
            logger.info("========================");

            // Step 3: 调用业务服务处理凭证
            credentialService.processAndStoreCredentials(decryptedJson);

            return ResponseEntity.ok("Data received, processed, and stored successfully.");

        } catch (Exception e) {
            logger.error("处理数据失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to process data: " + e.getMessage());
        }
    }
}