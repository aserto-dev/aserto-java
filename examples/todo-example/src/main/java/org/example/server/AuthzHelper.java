package org.example.server;

import com.aserto.AuthorizerClient;
import com.aserto.authorizer.v2.Decision;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.google.protobuf.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AuthzHelper {
    private AuthorizerClient authzClient;

    public AuthzHelper(AuthorizerClient authzClient) {
        this.authzClient = authzClient;
    }

    public boolean isAllowed(IdentityCtx identityCtx, PolicyCtx policyCtx) {
        return isAllowed(identityCtx,policyCtx, Collections.emptyMap());
    }
    public boolean isAllowed(IdentityCtx identityCtx, PolicyCtx policyCtx, Map<String, Value> resourceCtx) {
        List<Decision> decisions = authzClient.is(identityCtx, policyCtx, resourceCtx);


        return decisions.stream()
                .filter(decision -> decision.getDecision().equals("allowed"))
                .findFirst()
                .get()
                .getIs();
    }
}
