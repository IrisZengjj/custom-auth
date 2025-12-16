package com.keycloak.authserver.controller;

import com.keycloak.authserver.service.ProgrammaticKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProgrammaticKeyController {

    @Autowired
    private ProgrammaticKeyService programmaticKeyService;

    @GetMapping("/programmatic-public-key")
    public ResponseEntity<String> getProgrammaticPublicKey() {
        try {
            String publicKey = programmaticKeyService.getPublicKeyBase64();
            return ResponseEntity.ok(publicKey);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Error retrieving programmatic public key: " + e.getMessage());
        }
    }

    @GetMapping("/key-info")
    public ResponseEntity<String> getKeyInfo() {
        try {
            String publicKey = programmaticKeyService.getPublicKeyBase64();
            return ResponseEntity.ok("程序生成公钥成功！长度: " + publicKey.length());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("程序生成密钥失败: " + e.getMessage());
        }
    }
}