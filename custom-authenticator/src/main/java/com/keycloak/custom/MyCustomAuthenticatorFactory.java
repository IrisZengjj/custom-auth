package com.keycloak.custom;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class MyCustomAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "my-custom-authenticator";
    private static final String DISPLAY_TYPE = "My Custom Authenticator";
    private static final String HELP_TEXT = "自定义认证器，支持用户名密码和设备凭证验证";

    @Override
    public String getDisplayType() {
        return DISPLAY_TYPE;
    }

    @Override
    public String getReferenceCategory() {
        return "custom-authenticator";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();

        // 验证URL配置
        ProviderConfigProperty verifyUrl = new ProviderConfigProperty();
        verifyUrl.setName("credentialServerVerifyUrl");
        verifyUrl.setLabel("验证服务器URL");
        verifyUrl.setType(ProviderConfigProperty.STRING_TYPE);
        verifyUrl.setHelpText("设备验证服务器URL");
        verifyUrl.setDefaultValue("http://localhost:8081/api/verify");
        configProperties.add(verifyUrl);

        // 绑定URL配置
        ProviderConfigProperty bindUrl = new ProviderConfigProperty();
        bindUrl.setName("credentialServerBindUrl");
        bindUrl.setLabel("绑定服务器URL");
        bindUrl.setType(ProviderConfigProperty.STRING_TYPE);
        bindUrl.setHelpText("设备绑定服务器URL");
        bindUrl.setDefaultValue("http://localhost:8081/api/bind");
        configProperties.add(bindUrl);

        return configProperties;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new MyCustomAuthenticator();
    }

    @Override
    public void init(Config.Scope config) {
        // 初始化配置
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // 初始化后的操作
    }

    @Override
    public void close() {
        // 清理资源
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}

/**
package com.keycloak.custom;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class MyCustomAuthenticatorFactory implements AuthenticatorFactory {

    private static final String PROVIDER_ID = "my-custom-authenticator";
    private static final String DISPLAY_TYPE = "My Custom Authenticator";
    private static final String HELP_TEXT = "自定义认证器，支持用户名密码和设备凭证验证";
    private static final String REFERENCE_CATEGORY = "custom-authenticator";

    @Override
    public String getDisplayType() {
        return DISPLAY_TYPE;
    }

    @Override
    public String getReferenceCategory() {
        return REFERENCE_CATEGORY;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> configProperties = new ArrayList<>();

        ProviderConfigProperty verifyUrl = new ProviderConfigProperty();
        verifyUrl.setName("credentialServerVerifyUrl");
        verifyUrl.setLabel("Credential Server验证URL");
        verifyUrl.setType(ProviderConfigProperty.STRING_TYPE);
        verifyUrl.setHelpText("用于设备验证的credential-server接口URL");
        verifyUrl.setDefaultValue("http://localhost:8081/api/verify");
        configProperties.add(verifyUrl);

        ProviderConfigProperty bindUrl = new ProviderConfigProperty();
        bindUrl.setName("credentialServerBindUrl");
        bindUrl.setLabel("Credential Server绑定URL");
        bindUrl.setType(ProviderConfigProperty.STRING_TYPE);
        bindUrl.setHelpText("用于设备绑定的credential-server接口URL");
        bindUrl.setDefaultValue("http://localhost:8081/api/bind");
        configProperties.add(bindUrl);

        return configProperties;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new MyCustomAuthenticator();
    }

    @Override
    public void init(Config.Scope config) {
        // 初始化配置
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // 初始化后的操作
    }

    @Override
    public void close() {
        // 清理资源
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}

**/

/**

package com.keycloak.custom;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.ArrayList;
import java.util.List;

public class MyCustomAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

    public static final String PROVIDER_ID = "my-custom-authenticator";
    private static final MyCustomAuthenticator SINGLETON = new MyCustomAuthenticator();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    private static AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public String getHelpText() {
        return "My Custom Authenticator";
    }

    @Override
    public String getDisplayType() {
        return "My Custom Authenticator";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return new ArrayList<>();
    }

    @Override
    public String getReferenceCategory() {
        return "password";
    }

    @Override
    public void init(org.keycloak.Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}

 **/