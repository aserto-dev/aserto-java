package com.aserto.model;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

import java.util.logging.Logger;

public class AuthorizerConfig {
    private String tenantId = "";
    private String address = "localhost:8282";
    private String apiKey = "";
    private String token = "";
    private Boolean insecure = false;
    private String caCertPath = "";

    public AuthorizerConfig() {
        Logger logger = Logger.getLogger(AuthorizerConfig.class.getName());
        Dotenv dotenv;
        try {
            dotenv = Dotenv.load();
            tenantId = dotenv.get("ASERTO_TENANT_ID", "");
            address = dotenv.get("ASERTO_AUTHORIZER_SERVICE_URL", "localhost:8282");
            apiKey = dotenv.get("ASERTO_AUTHORIZER_API_KEY", "");
            token = dotenv.get("ASERTO_AUTHORIZER_TOKEN", "");
            insecure = dotenv.get("ASERTO_AUTHORIZER_INSECURE", "false").equals("true");
            caCertPath = dotenv.get("ASERTO_AUTHORIZER_CA_CERT_PATH", "");
        }
        catch (DotenvException ex) {
            logger.log(java.util.logging.Level.INFO, ex.getMessage());
        }

    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getInsecure() {
        return insecure;
    }

    public void setInsecure(Boolean insecure) {
        this.insecure = insecure;
    }

    public String getCaCertPath() {
        return caCertPath;
    }

    public void setCaCertPath(String caCertPath) {
        this.caCertPath = caCertPath;
    }
}
