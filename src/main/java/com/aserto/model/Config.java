package com.aserto.model;

public class Config {
    private String host;
    private int port;
    private String apiKey;
    private String tenantId;
    private Boolean insecure = false;
    private String caCertPath = "";

    public Config() {
    }

    public Config(String host, int port, String apiKey, String tenantID, Boolean insecure, String caCertPath) {
        this.host = host;
        this.port = port;
        this.apiKey = apiKey;
        this.tenantId = tenantID;
        this.insecure = insecure;
        this.caCertPath = caCertPath;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
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
