package com.aserto;

import com.aserto.model.AuthorizerConfig;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;


public class ChannelBuilder {
    private AuthorizerConfig authzConfig = new AuthorizerConfig();


    public ChannelBuilder() {
    }

    public ChannelBuilder(AuthorizerConfig authzConfig) {
        this.authzConfig = authzConfig;
    }

    public ChannelBuilder withTenantId(String tenantId) {
        authzConfig.setTenantId(tenantId);

        return this;
    }

    public ChannelBuilder withAddr(String address) {
        authzConfig.setAddress(address);

        return this;
    }

    public ChannelBuilder withAPIKeyAuth(String apiKey) {
        authzConfig.setApiKey(apiKey);

        return this;
    }

    public ChannelBuilder withTokenAuth(String token) {
        authzConfig.setToken(token);

        return this;
    }

    public ChannelBuilder withInsecure(Boolean insecure) {
        authzConfig.setInsecure(insecure);

        return this;
    }

    public ChannelBuilder withCACertPath(String caCertPath) {
        authzConfig.setCaCertPath(caCertPath);

        return this;
    }

    public ManagedChannel build() throws SSLException {
        Metadata metadata = new Metadata();
        Metadata.Key<String> asertoTenantId = Metadata.Key.of("aserto-tenant-id", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> authorization = Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(asertoTenantId, authzConfig.getTenantId());
        metadata.put(authorization, "basic " + authzConfig.getApiKey());

        String address = authzConfig.getAddress();
        String[] splittedAddress = address.split(":");
        String addr = splittedAddress[0];
        Integer port = Integer.parseInt(splittedAddress[1]);

        NettyChannelBuilder channelBuilder = NettyChannelBuilder
                .forAddress(addr, port)
                .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata));

        boolean insecure = authzConfig.getInsecure();
        boolean caSpecified  = !authzConfig.getCaCertPath().isEmpty();

        if (insecure) {
            SslContext context = GrpcSslContexts.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            channelBuilder.sslContext(context);
        } else if (caSpecified) {
            SslContext context = GrpcSslContexts.forClient()
                    .trustManager(new File(authzConfig.getCaCertPath()))
                    .build();
            channelBuilder.sslContext(context);
        }


        return channelBuilder.build();
    }
}
