package com.keycloak.authserver.controller;

import com.keycloak.authserver.service.CryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class KeyController {

    @Autowired
    private CryptoService cryptoService;

    private static final Logger logger = LoggerFactory.getLogger(KeyController.class);

    @GetMapping("/configured-public-key")
    public ResponseEntity<String> getConfiguredPublicKey() {
        try {
            logger.info("收到配置的公钥请求");
            String publicKey = cryptoService.getPublicKeyInBase64();
            return ResponseEntity.ok(publicKey);
        } catch (Exception e) {
            logger.error("获取配置公钥失败: " + e.getMessage(), e);
            return ResponseEntity.status(500).body("Error retrieving configured public key: " + e.getMessage());
        }
    }
}


/**
@RestController
@RequestMapping("/api")
public class KeyController {

    @Autowired
    private CryptoService cryptoService;

    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() {
        try {
            String publicKey = cryptoService.getPublicKeyInBase64();
            return ResponseEntity.ok(publicKey);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving public key");
        }
    }
}

 **/