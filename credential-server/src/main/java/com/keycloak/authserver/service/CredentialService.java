package com.keycloak.authserver.service;

import com.keycloak.authserver.model.IdentityProof;
import com.keycloak.authserver.repository.IdentityProofRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class CredentialService {

    @Autowired
    private IdentityProofRepository identityProofRepository;

    // 静态盐值，作为项目层面的固定盐值，与设备主盐共同增强安全性
    private static final String STATIC_SALT = "YOUR_PROJECT_SPECIFIC_STATIC_SALT";

    /**
     * 处理并存储客户端上传的凭证数据。
     */
    public void processAndStoreCredentials(JSONObject clientData) {
        try {
            // 从JSON中提取原始数据
            JSONObject hardwareInfo = clientData.getJSONObject("硬件信息");
            JSONObject softwareInfo = clientData.getJSONObject("软件信息");
            JSONObject simInfo = clientData.getJSONObject("SIM卡信息");

            // 生成设备指纹
            String deviceFingerprint = generateDeviceFingerprint(hardwareInfo, softwareInfo);

            // 生成唯一凭证
            CredentialResult credentialResult = generateUniqueCredential(simInfo, deviceFingerprint);

            // 存储到数据库
            IdentityProof proof = new IdentityProof();
            proof.setPhoneNumberHash(credentialResult.phoneHash);
            proof.setDeviceFingerprintHash(credentialResult.deviceFingerprintHash);
            proof.setFinalCredentialHash(credentialResult.finalCredential);

            identityProofRepository.save(proof);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to process credentials", e);
        }
    }

    /**
     * 生成设备指纹-硬件和软件属性融合
     * @param hardwareInfo 硬件信息
     * @param softwareInfo 软件信息
     * @return 设备指纹的SHA-256哈希值
     */
    private String generateDeviceFingerprint(JSONObject hardwareInfo, JSONObject softwareInfo) throws Exception {
        StringBuilder fingerprintData = new StringBuilder();
        
        // 硬件属性
        fingerprintData.append(hardwareInfo.optString("设备名称", ""));
        fingerprintData.append(hardwareInfo.optString("设备型号", ""));
        fingerprintData.append(hardwareInfo.optString("硬件序列号", ""));
        fingerprintData.append(hardwareInfo.optString("IMEI主卡", ""));
        fingerprintData.append(hardwareInfo.optString("IMEI副卡", ""));
        fingerprintData.append(hardwareInfo.optString("CPU架构", ""));
        fingerprintData.append(hardwareInfo.optString("内存大小", ""));
        fingerprintData.append(hardwareInfo.optString("基带版本", ""));
        
        // 软件属性
        fingerprintData.append(softwareInfo.optString("操作系统名称", ""));
        fingerprintData.append(softwareInfo.optString("操作系统版本", ""));
        fingerprintData.append(softwareInfo.optString("Android_ID", ""));
        fingerprintData.append(softwareInfo.optString("内核版本", ""));
        
        return hashSHA256(fingerprintData.toString() + STATIC_SALT);
    }

    /**
     * 生成唯一凭证
     * @param simInfo SIM卡信息
     * @param deviceFingerprint 设备指纹
     * @return CredentialResult对象包含各层哈希值
     */
    private CredentialResult generateUniqueCredential(JSONObject simInfo, String deviceFingerprint) throws Exception {
        // Step 1: 手机号层（哈希根节点）
        String phoneNumber1 = simInfo.optString("手机号 (卡1)", "");
        String phoneNumber2 = simInfo.optString("手机号 (卡2)", "");
        String masterSalt = UUID.randomUUID().toString(); // 生成设备专属的主盐
        
        // 确保两个手机号共同参与哈希，避免单卡情况下的哈希碰撞
        List<String> phoneNumbers = Arrays.asList(phoneNumber1, phoneNumber2);
        Collections.sort(phoneNumbers);
        String phoneNumbersData = String.join("", phoneNumbers);
        
        // 将设备主盐加入到第一层哈希中
        String phoneHash = hashSHA256(phoneNumbersData + masterSalt);

        // Step 2: 设备指纹层（第一层融合）
        // 使用电话号码哈希作为密钥，与设备指纹哈希进行HMAC融合
        String combinedHash1 = generateHMAC(phoneHash, deviceFingerprint);

        // Step 3: SIM卡信息层（第二层融合）
        StringBuilder simInfoData = new StringBuilder();
        simInfoData.append(simInfo.optString("IMSI (卡1)", ""));
        simInfoData.append(simInfo.optString("IMSI (卡2)", ""));
        simInfoData.append(simInfo.optString("ICCID (卡1)", ""));
        simInfoData.append(simInfo.optString("ICCID (卡2)", ""));

        // 使用第一层融合结果作为密钥，与SIM卡数据进行HMAC融合
        String finalCredential = generateHMAC(combinedHash1, simInfoData.toString());

        return new CredentialResult(phoneHash, deviceFingerprint, finalCredential);
    }

    /**
     * 生成HMAC-SHA256
     */
    private String generateHMAC(String key, String data) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hmacBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 使用SHA-256算法对输入字符串进行哈希
     */
    private String hashSHA256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 内部类用于保存凭证生成结果
     */
    private static class CredentialResult {
        final String phoneHash;
        final String deviceFingerprintHash;
        final String finalCredential;

        public CredentialResult(String phoneHash, String deviceFingerprintHash, String finalCredential) {
            this.phoneHash = phoneHash;
            this.deviceFingerprintHash = deviceFingerprintHash;
            this.finalCredential = finalCredential;
        }
    }

    /**
     * 验证实时采集的设备凭证
     * @param clientData 解密后的客户端实时数据 (JSON格式)
     * @return 验证结果，包含是否成功及用户标识（如phoneHash）
     */
    public Map<String, Object> verifyCredentials(JSONObject clientData) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 使用相同的逻辑处理实时数据，生成临时凭证
            JSONObject hardwareInfo = clientData.getJSONObject("硬件信息");
            JSONObject softwareInfo = clientData.getJSONObject("软件信息");
            JSONObject simInfo = clientData.getJSONObject("SIM卡信息");

            String deviceFingerprint = generateDeviceFingerprint(hardwareInfo, softwareInfo);
            CredentialResult tempCredential = generateUniqueCredential(simInfo, deviceFingerprint);

            // 2. 使用 phoneHash 作为用户唯一标识，到数据库中查找历史凭证
            Optional<IdentityProof> storedProof = identityProofRepository.findByPhoneNumberHash(tempCredential.phoneHash);

            if (storedProof.isPresent()) {
                // 3. 比对“临时生成的最终凭证”与“数据库中存储的最终凭证”
                boolean isMatch = tempCredential.finalCredential.equals(storedProof.get().getFinalCredentialHash());
                result.put("success", isMatch);
                result.put("userId", tempCredential.phoneHash); // 使用phoneHash作为Keycloak用户标识
                result.put("message", isMatch ? "Credential verified" : "Credential mismatch");
            } else {
                // 该设备（phoneHash）首次出现，无记录可比对
                result.put("success", false);
                result.put("userId", tempCredential.phoneHash);
                result.put("message", "No previous credential found for this device");
            }
            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Verification process error: " + e.getMessage());
            return result;
        }
    }
}