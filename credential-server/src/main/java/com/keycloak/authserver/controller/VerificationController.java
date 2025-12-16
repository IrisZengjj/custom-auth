package com.keycloak.authserver.controller;

import com.keycloak.authserver.service.CryptoService;
import com.keycloak.authserver.service.CredentialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@RestController
public class VerificationController {

    @Autowired
    private CryptoService cryptoService;
    @Autowired
    private CredentialService credentialService;

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

    @PostMapping("/api/verify") // MyCustomAuthenticator 调用的接口
    public ResponseEntity<Map<String, Object>> verifyRealTimeData(@RequestBody Map<String, String> request) {
        try {
            logger.info("收到实时凭证验证请求");

            // 1. 获取加密数据 (格式应与 /api/upload 一致)
            String encryptedDataB64 = request.get("encryptedData");
            String encryptedKeyB64 = request.get("encryptedKey");

            if (encryptedDataB64 == null || encryptedKeyB64 == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Missing encrypted data or key"));
            }

            // 2. 解密数据 (复用CryptoService逻辑)
            byte[] decryptedAesKey = cryptoService.decryptAesKey(encryptedKeyB64);
            String decryptedJsonString = cryptoService.decryptData(encryptedDataB64, decryptedAesKey);
            JSONObject realTimeData = new JSONObject(decryptedJsonString);

            logger.info("实时数据解密成功，开始凭证验证");

            // 3. 调用业务层进行验证
            Map<String, Object> verificationResult = credentialService.verifyCredentials(realTimeData);

            return ResponseEntity.ok(verificationResult);

        } catch (Exception e) {
            logger.error("验证过程失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "error", "Verification failed: " + e.getMessage()));
        }
    }
}