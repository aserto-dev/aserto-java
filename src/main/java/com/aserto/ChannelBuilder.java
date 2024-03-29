package com.aserto;

import com.aserto.model.Config;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.io.File;


public class ChannelBuilder {
    private Config cfg;

    public ChannelBuilder() {
        this.cfg = new Config();
    }

    public ChannelBuilder(Config config) {
        this.cfg = config;
    }

    public ChannelBuilder withTenantId(String tenantId) {
        cfg.setTenantId(tenantId);

        return this;
    }

    public ChannelBuilder withHost(String host) {
        cfg.setHost(host);

        return this;
    }

    public ChannelBuilder withPort(int port) {
        cfg.setPort(port);

        return this;
    }

    public ChannelBuilder withAPIKeyAuth(String apiKey) {
        cfg.setApiKey(apiKey);

        return this;
    }

    public ChannelBuilder withTokenAuth(String token) {
        cfg.setToken(token);

        return this;
    }

    public ChannelBuilder withInsecure(Boolean insecure) {
        cfg.setInsecure(insecure);

        return this;
    }

    public ChannelBuilder withCACertPath(String caCertPath) {
        cfg.setCaCertPath(caCertPath);

        return this;
    }

    public ManagedChannel build() throws SSLException {
        Metadata metadata = new Metadata();
        Metadata.Key<String> asertoTenantId = Metadata.Key.of("aserto-tenant-id", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> authorization = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

        if (cfg.getTenantId() != null) {
            metadata.put(asertoTenantId, cfg.getTenantId());
        }

        if (cfg.getApiKey() != null && cfg.getToken() != null) {
            throw new IllegalArgumentException("ApiKey and Token cannot be both specified");
        }

        if (cfg.getApiKey() != null) {
            metadata.put(authorization, "basic " + cfg.getApiKey());
        } else if (cfg.getToken() != null) {
            metadata.put(authorization, "bearer " + cfg.getToken());
        }

        NettyChannelBuilder channelBuilder = NettyChannelBuilder
                .forAddress(cfg.getHost(), cfg.getPort())
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata));

        boolean insecure = cfg.getInsecure();

        boolean caSpecified = true;
        if (cfg.getCaCertPath() == null || cfg.getCaCertPath().isEmpty()) {
            caSpecified = false;
        }

        if (insecure) {
            SslContext context = GrpcSslContexts.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            channelBuilder.sslContext(context);
        } else if (caSpecified) {
            SslContext context = GrpcSslContexts.forClient()
                    .trustManager(new File(cfg.getCaCertPath()))
                    .build();
            channelBuilder.sslContext(context);
        }


        return channelBuilder.build();
    }
}
