package com.aserto;

import com.aserto.authorizer.v2.*;
import com.aserto.authorizer.v2.Decision;
import com.aserto.authorizer.v2.api.*;
import com.aserto.authorizer.v2.api.Module;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.ManagedChannel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AuthzClient implements AuthorizerClient {
    private AuthorizerGrpc.AuthorizerBlockingStub client;
    private ManagedChannel channel;
    public AuthzClient(ManagedChannel channel) {
        client = AuthorizerGrpc.newBlockingStub(channel);
        this.channel = channel;
    }

    public List<Module> listPolicies(String policyName, String policyLabel) {
        ListPoliciesRequest.Builder policyBuilder = ListPoliciesRequest.newBuilder();

        PolicyInstance policy = buildPolicy(policyName, policyLabel);
        policyBuilder.setPolicyInstance(policy);

        ListPoliciesResponse response = client.listPolicies(policyBuilder.build());

        return response.getResultList();
    }

    public Module getPolicy(String policyId) {
        GetPolicyRequest.Builder getPolicyBuilder = GetPolicyRequest.newBuilder();
        getPolicyBuilder.setId(policyId);

        GetPolicyResponse policyResponse = client.getPolicy(getPolicyBuilder.build());

        return policyResponse.getResult();
    }

    public List<Decision> is(IdentityCtx identityCtx, PolicyCtx policyCtx) {
        return this.is(identityCtx, policyCtx, Collections.emptyMap());
    }

    public List<Decision> is(IdentityCtx identityCtx, PolicyCtx policyCtx, Map<String, Value> values) {
        IsRequest.Builder isBuilder = IsRequest.newBuilder();

        IdentityContext identityContext = buildIdentityContext(identityCtx);
        PolicyContext policyContext = buildPolicyContext(policyCtx);
        PolicyInstance policy = buildPolicy(policyCtx.getName(), policyCtx.getLabel());
        Struct.Builder resourceContext = buildResourceContext(values);

        isBuilder.setIdentityContext(identityContext);
        isBuilder.setPolicyContext(policyContext);
        isBuilder.setPolicyInstance(policy);
        isBuilder.setResourceContext(resourceContext);

        IsResponse isResponse = client.is(isBuilder.build());

        return isResponse.getDecisionsList();
    }

    public Struct query(String query, PolicyCtx policyContext, Map<String, Value> values) {
        QueryRequest.Builder queryRequestBuilder = QueryRequest.newBuilder();
        queryRequestBuilder.setQuery(query);

        PolicyInstance policy = buildPolicy(policyContext.getName(), policyContext.getLabel());
        Struct.Builder structBuilder = buildResourceContext(values);

        queryRequestBuilder.setPolicyInstance(policy);
        queryRequestBuilder.setResourceContext(structBuilder);

        QueryResponse queryResponse = client.query(queryRequestBuilder.build());

        return queryResponse.getResponse();
    }

    public Map<String, Value> decisionTree(IdentityCtx identityCtx, PolicyCtx policyCtx) {
        DecisionTreeRequest.Builder decisionTreeBuilder = DecisionTreeRequest.newBuilder();

        IdentityContext identityContext = buildIdentityContext(identityCtx);
        PolicyContext policyContext = buildPolicyContext(policyCtx);
        PolicyInstance policy = buildPolicy(policyCtx.getName(), policyCtx.getLabel());

        decisionTreeBuilder.setIdentityContext(identityContext);
        decisionTreeBuilder.setPolicyContext(policyContext);
        decisionTreeBuilder.setPolicyInstance(policy);

        DecisionTreeResponse decisionTree = client.decisionTree(decisionTreeBuilder.build());

        return decisionTree.getPath().getFieldsMap();
    }

    public void close() {
        channel.shutdown();
    }

    private PolicyInstance buildPolicy(String name, String label) {
        PolicyInstance.Builder policyInstance = PolicyInstance.newBuilder();
        policyInstance.setName(name);
        policyInstance.setInstanceLabel(label);

        return policyInstance.build();
    }

    private IdentityContext buildIdentityContext(IdentityCtx identityContext) {
        IdentityContext.Builder identityContextBuilder =  IdentityContext.newBuilder();
        identityContextBuilder.setIdentity(identityContext.getIdentity());
        identityContextBuilder.setType(identityContext.getIdentityType());

        return identityContextBuilder.build();
    }

    private PolicyContext buildPolicyContext(PolicyCtx policyContext) {
        PolicyContext.Builder policyContextBuilder = PolicyContext.newBuilder();
        policyContextBuilder.setPath(policyContext.getPath());
        policyContextBuilder.addAllDecisions(Arrays.asList(policyContext.getDecisions()));

        return policyContextBuilder.build();
    }

    private Struct.Builder buildResourceContext(Map<String, Value> values) {
        Struct.Builder structBuilder = Struct.newBuilder();
        values.forEach(structBuilder::putFields);

        return structBuilder;
    }
}
