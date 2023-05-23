package com.aserto;

import com.aserto.authorizer.v2.Decision;
import com.aserto.authorizer.v2.api.Module;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import java.util.List;
import java.util.Map;

public interface AuthorizerClient {
    public List<Module> listPolicies(String policyName, String policyLabel);
    public Module getPolicy(String policyId);
    public List<Decision> is(IdentityCtx identityCtx, PolicyCtx policyCtx);
    public List<Decision> is(IdentityCtx identityCtx, PolicyCtx policyCtx, Map<String, Value> resourceCtx);
    public Struct query(String query, PolicyCtx policyContext, Map<String, Value> values);
    public Map<String, Value> decisionTree(IdentityCtx identityCtx, PolicyCtx policyCtx);
}
