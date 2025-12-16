package com.keycloak.authserver.repository;

import com.keycloak.authserver.model.IdentityProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface IdentityProofRepository extends JpaRepository<IdentityProof, Long> {

    /**
     * 根据电话号码哈希值查找身份凭证记录。
     * @param phoneNumberHash 电话号码的哈希值
     * @return 匹配的 IdentityProof 记录
     */
    Optional<IdentityProof> findByPhoneNumberHash(String phoneNumberHash);
}
