package org.example;

import com.aserto.model.Config;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

import java.util.logging.Logger;

public class EnvConfigLoader {
    private Config authzCfg;
    private Config directoryCfg;
    private Dotenv dotenv;

    private class Address {
        private String host;
        private Integer port;

        public Address(String host, Integer port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {return this.host;}
        public Integer getPort() {return this.port;}
    }


    public EnvConfigLoader() {
        Logger logger = Logger.getLogger(EnvConfigLoader.class.getName());
        try {
            dotenv = Dotenv.load();
        }
        catch (DotenvException ex) {
            logger.log(java.util.logging.Level.INFO, ex.getMessage());
        }
    }

    public Config getAuthzConfig() {
        if (authzCfg == null) {
            authzCfg = new Config();
        }

        String addressFromConfig = dotenv.get("ASERTO_AUTHORIZER_SERVICE_URL", "localhost:8282");
        Address address = splitAddress(addressFromConfig);

        authzCfg.setTenantId(dotenv.get("ASERTO_TENANT_ID", ""));
        authzCfg.setHost(address.getHost());
        authzCfg.setPort(address.getPort());
        authzCfg.setApiKey(dotenv.get("ASERTO_AUTHORIZER_API_KEY", ""));
        authzCfg.setToken(dotenv.get("ASERTO_AUTHORIZER_TOKEN", ""));
        authzCfg.setInsecure(dotenv.get("ASERTO_AUTHORIZER_INSECURE", "false").equals("true"));
        authzCfg.setCaCertPath(dotenv.get("ASERTO_AUTHORIZER_CA_CERT_PATH", ""));

        return authzCfg;
    }

    public Config getDirectoryConfig() {
        if (directoryCfg == null) {
            directoryCfg = new Config();
        }

        String addressFromConfig = dotenv.get("ASERTO_DIRECTORY_SERVICE_URL", "localhost:8282");
        Address address = splitAddress(addressFromConfig);

        directoryCfg.setTenantId(dotenv.get("ASERTO_TENANT_ID", ""));
        directoryCfg.setHost(address.getHost());
        directoryCfg.setPort(address.getPort());
        directoryCfg.setApiKey(dotenv.get("ASERTO_DIRECTORY_API_KEY", ""));
        directoryCfg.setToken(dotenv.get("ASERTO_DIRECTORY_TOKEN", ""));
        directoryCfg.setInsecure(dotenv.get("ASERTO_DIRECTORY_INSECURE", "false").equals("true"));
        directoryCfg.setCaCertPath(dotenv.get("ASERTO_DIRECTORY_CA_CERT_PATH", ""));

        return directoryCfg;
    }

    private Address splitAddress(String address) {
        String[] splittedAddress = address.split(":");
        String host = splittedAddress[0];
        Integer port = Integer.parseInt(splittedAddress[1]);

        return new Address(host, port);
    }
}
