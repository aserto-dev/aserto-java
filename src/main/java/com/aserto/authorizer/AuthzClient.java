package com.aserto.authorizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.aserto.AuthorizerClient;
import com.aserto.authorizer.v2.AuthorizerGrpc;
import com.aserto.authorizer.v2.Decision;
import com.aserto.authorizer.v2.DecisionTreeRequest;
import com.aserto.authorizer.v2.DecisionTreeResponse;
import com.aserto.authorizer.v2.GetPolicyRequest;
import com.aserto.authorizer.v2.GetPolicyResponse;
import com.aserto.authorizer.v2.IsRequest;
import com.aserto.authorizer.v2.IsResponse;
import com.aserto.authorizer.v2.ListPoliciesRequest;
import com.aserto.authorizer.v2.ListPoliciesResponse;
import com.aserto.authorizer.v2.QueryRequest;
import com.aserto.authorizer.v2.QueryResponse;
import com.aserto.authorizer.v2.api.IdentityContext;
import com.aserto.authorizer.v2.api.IdentityType;
import com.aserto.authorizer.v2.api.Module;
import com.aserto.authorizer.v2.api.PolicyContext;
import com.aserto.authorizer.v2.api.PolicyInstance;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import io.grpc.ManagedChannel;

public class AuthzClient implements AuthorizerClient {
    private final AuthorizerGrpc.AuthorizerBlockingStub client;
    private final ManagedChannel channel;

    public AuthzClient(ManagedChannel channel) {
        client = AuthorizerGrpc.newBlockingStub(channel);
        this.channel = channel;
    }

    @Override
    public List<Module> listPolicies(String policyName, String policyLabel) {
        ListPoliciesRequest.Builder policyBuilder = ListPoliciesRequest.newBuilder();

        PolicyInstance policy = buildPolicy(policyName);
        policyBuilder.setPolicyInstance(policy);

        ListPoliciesResponse response = client.listPolicies(policyBuilder.build());

        return response.getResultList();
    }

    @Override
    public Module getPolicy(String policyId) {
        GetPolicyRequest.Builder getPolicyBuilder = GetPolicyRequest.newBuilder();
        getPolicyBuilder.setId(policyId);

        GetPolicyResponse policyResponse = client.getPolicy(getPolicyBuilder.build());

        return policyResponse.getResult();
    }

    @Override
    public List<Decision> is(IdentityCtx identityCtx, PolicyCtx policyCtx) {
        return this.is(identityCtx, policyCtx, Collections.emptyMap());
    }

    @Override
    public List<Decision> is(IdentityCtx identityCtx, PolicyCtx policyCtx, Map<String, Value> values) {
        IsRequest.Builder isBuilder = IsRequest.newBuilder();

        IdentityContext identityContext = buildIdentityContext(identityCtx);
        PolicyContext policyContext = buildPolicyContext(policyCtx);
        PolicyInstance policy = buildPolicy(policyCtx.getName());
        Struct.Builder resourceContext = buildResourceContext(values);

        isBuilder.setIdentityContext(identityContext);
        isBuilder.setPolicyContext(policyContext);
        isBuilder.setPolicyInstance(policy);
        isBuilder.setResourceContext(resourceContext);

        IsResponse isResponse = client.is(isBuilder.build());

        return isResponse.getDecisionsList();
    }

    @Override
    public Struct query(String query, IdentityCtx identityCtx, PolicyCtx policyContext,
            Map<String, Value> resourceCtx) {
        QueryRequest.Builder queryRequestBuilder = QueryRequest.newBuilder();
        queryRequestBuilder.setQuery(query);

        IdentityContext identityContext = buildIdentityContext(identityCtx);
        PolicyInstance policy = buildPolicy(policyContext.getName());
        Struct.Builder resourceContext = buildResourceContext(resourceCtx);

        queryRequestBuilder.setIdentityContext(identityContext);
        queryRequestBuilder.setPolicyInstance(policy);
        queryRequestBuilder.setResourceContext(resourceContext);

        QueryResponse queryResponse = client.query(queryRequestBuilder.build());

        return queryResponse.getResponse();
    }

    @Override
    public Struct query(String query, PolicyCtx policyContext, Map<String, Value> resourceCtx) {
        return query(query, new IdentityCtx("", IdentityType.IDENTITY_TYPE_NONE), policyContext, resourceCtx);
    }

    @Override
    public Map<String, Value> decisionTree(IdentityCtx identityCtx, PolicyCtx policyCtx) {
        DecisionTreeRequest.Builder decisionTreeBuilder = DecisionTreeRequest.newBuilder();

        IdentityContext identityContext = buildIdentityContext(identityCtx);
        PolicyContext policyContext = buildPolicyContext(policyCtx);
        PolicyInstance policy = buildPolicy(policyCtx.getName());

        decisionTreeBuilder.setIdentityContext(identityContext);
        decisionTreeBuilder.setPolicyContext(policyContext);
        decisionTreeBuilder.setPolicyInstance(policy);

        DecisionTreeResponse decisionTree = client.decisionTree(decisionTreeBuilder.build());

        return decisionTree.getPath().getFieldsMap();
    }

    @Override
    public void close() {
        channel.shutdown();
    }

    private PolicyInstance buildPolicy(String name) {
        PolicyInstance.Builder policyInstance = PolicyInstance.newBuilder();
        policyInstance.setName(name);

        return policyInstance.build();
    }

    private IdentityContext buildIdentityContext(IdentityCtx identityContext) {
        IdentityContext.Builder identityContextBuilder = IdentityContext.newBuilder();
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
