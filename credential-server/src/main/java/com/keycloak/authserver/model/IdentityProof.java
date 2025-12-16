package com.keycloak.authserver.model;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "identity_proof")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityProof {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number_hash", nullable = false, unique = true)
    private String phoneNumberHash;

    @Column(name = "device_fingerprint_hash", nullable = false)
    private String deviceFingerprintHash;

    @Column(name = "final_credential_hash", nullable = false)
    private String finalCredentialHash;

    @Column(name = "creation_timestamp", nullable = false)
    private LocalDateTime creationTimestamp;

    @Column(name = "last_update_timestamp", nullable = false)
    private LocalDateTime lastUpdateTimestamp;

    // JPA 自动处理创建和更新时间戳
    @PrePersist
    protected void onCreate() {
        creationTimestamp = LocalDateTime.now();
        lastUpdateTimestamp = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdateTimestamp = LocalDateTime.now();
    }
}
