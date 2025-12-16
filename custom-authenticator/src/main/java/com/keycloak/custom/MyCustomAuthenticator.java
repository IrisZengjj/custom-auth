// 位于 com.keycloak.custom 包
package com.keycloak.custom;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.util.Map;

public class MyCustomAuthenticator implements Authenticator {

    private static final ServicesLogger logger = ServicesLogger.LOGGER;
    private static final String CREDENTIAL_SERVER_VERIFY_URL = "http://localhost:8081/api/verify";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        System.out.println("[DEBUG] [authenticate] MyCustomAuthenticator 启动");
        context.challenge(context.form().createForm("device.ftl"));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String action = formData.getFirst("form_action");
        System.out.println("[DEBUG] [action] form_action = " + action);

        // 用户点击 "用账号密码登录" 按钮
        if ("use_password".equals(action)) {
            System.out.println("[DEBUG] [action] 用户选择用密码登录");
            context.attempted(); // 跳到下一个 Alternative 执行器（UsernamePasswordForm）
            return;
        }

        String multimodalData = formData.getFirst("multimodalData");
        System.out.println("[DEBUG] [action] multimodalData = " + multimodalData);

        if (multimodalData == null || multimodalData.trim().isEmpty()) {
            System.out.println("[DEBUG] [action] 数据为空 → device-error.ftl");
            context.challenge(context.form()
                    .setAttribute("errorMsg", "设备数据为空，请选择是否进入账号密码登录")
                    .createForm("device-error.ftl"));
            return;
        }

        Map<String, Object> verifyResult = verifyWithCredentialServer(multimodalData);
        System.out.println("[DEBUG] [action] 验证结果 = " + verifyResult);

        if (verifyResult != null && Boolean.TRUE.equals(verifyResult.get("success"))) {
            String userId = (String) verifyResult.get("userId");
            String username = (String) verifyResult.get("username");
            UserModel user = findUser(context, userId, username);
            if (user != null) {
                context.setUser(user);
                context.success();
                return;
            }
        }

        System.out.println("[DEBUG] [action] 设备验证失败 → device-error.ftl");
        context.challenge(context.form()
                .setAttribute("errorMsg", "设备验证失败，请选择是否进入账号密码登录")
                .createForm("device-error.ftl"));
    }

    private Map<String, Object> verifyWithCredentialServer(String encryptedDataPackage) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(CREDENTIAL_SERVER_VERIFY_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(encryptedDataPackage));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == 200) {
                    return objectMapper.readValue(responseBody, Map.class);
                } else {
                    logger.errorf("Credential Server error: %s", responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("Credential Server 调用异常: " + e.getMessage(), e);
        }
        return null;
    }

    private UserModel findUser(AuthenticationFlowContext context, String userId, String username) {
        UserModel user = null;
        if (userId != null) {
            user = context.getSession().users().getUserById(context.getRealm(), userId);
        }
        if (user == null && username != null) {
            user = context.getSession().users().getUserByUsername(context.getRealm(), username);
        }
        return user;
    }

    @Override
    public boolean requiresUser() { return false; }
    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) { return true; }
    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {}
    @Override
    public void close() {}
}