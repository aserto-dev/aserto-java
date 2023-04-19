package org.example;

import com.aserto.AuthorizerClient;
import com.aserto.AuthzClient;
import com.aserto.ChannelBuilder;
import com.aserto.authorizer.v2.Decision;
import com.aserto.authorizer.v2.api.IdentityType;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import io.grpc.ManagedChannel;

import javax.net.ssl.SSLException;
import java.util.List;

public class AuthzExample {
    public static void main(String[] args) throws Exception {
        // create a channel that has the connection details
        ManagedChannel channel = new ChannelBuilder()
                .withAddr("localhost:8282")
                .withInsecure(true)
                .build();

        // create authz client
        AuthorizerClient authzClient = new AuthzClient(channel);

        // identity context contains information abou the user that requests access to some resource
        IdentityCtx identityCtx = new IdentityCtx("rick@the-citadel.com", IdentityType.IDENTITY_TYPE_SUB);

        // contains information about the policy we want to check for the provided identity
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.DELETE.todos.__id", new String[]{"allowed"});

        // check if the identity is allowed to perform the action
        List<Decision> decisions = authzClient.is(identityCtx, policyCtx);
        authzClient.close();

        decisions.forEach(decision -> {
            String dec = decision.getDecision();
            boolean isAllowed =  decision.getIs();
            System.out.println("For decision [" + dec + "] the answer was [" + isAllowed + "]");
        });
    }
}
