package com.aserto;

import com.aserto.authorizer.v2.*;
import com.aserto.authorizer.v2.api.IdentityContext;
import com.aserto.authorizer.v2.api.Module;
import com.aserto.authorizer.v2.api.PolicyContext;
import com.aserto.authorizer.v2.api.PolicyInstance;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.ManagedChannel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AuthzClient implements AutoCloseable {
    private AuthorizerGrpc.AuthorizerBlockingStub client;
    private ManagedChannel channel;
    public AuthzClient(ManagedChannel channel) {
        client = AuthorizerGrpc.newBlockingStub(channel);
        this.channel = channel;
    }

    public List<Module> listPolicies(String policyName, String policyLabel) {
        ListPoliciesRequest.Builder policyBuilder = ListPoliciesRequest.newBuilder();

        PolicyInstance policy = getPolicy(policyName, policyLabel);
        policyBuilder.setPolicyInstance(policy);

        ListPoliciesRequest listPoliciesRequest = policyBuilder.build();
        ListPoliciesResponse response = client.listPolicies(listPoliciesRequest);

        return response.getResultList();
    }

    public List<Decision> is(IdentityCtx identityContext, PolicyCtx policyContext) {
        IsRequest.Builder isBuilder = IsRequest.newBuilder();

        IdentityContext.Builder identityContextBuilder =  IdentityContext.newBuilder();
        identityContextBuilder.setIdentity(identityContext.getIdentity());
        identityContextBuilder.setType(identityContext.getIdentityType());


        PolicyContext.Builder policyContextBuilder = PolicyContext.newBuilder();
        policyContextBuilder.setPath(policyContext.getPath());
        policyContextBuilder.addAllDecisions(Arrays.asList(policyContext.getDecisions()));


        isBuilder.setIdentityContext(identityContextBuilder.build());
        isBuilder.setPolicyContext(policyContextBuilder.build());

        PolicyInstance policy = getPolicy(policyContext.getName(), policyContext.getLabel());
        isBuilder.setPolicyInstance(policy);

        IsResponse isReponse = client.is(isBuilder.build());

        return isReponse.getDecisionsList();
    }

    public Struct query(String query, PolicyCtx policyContext, Map<String, Value> values) {
        QueryRequest.Builder queryRequestBuilder = QueryRequest.newBuilder();
        queryRequestBuilder.setQuery(query);

        PolicyInstance policy = getPolicy(policyContext.getName(), policyContext.getLabel());
        queryRequestBuilder.setPolicyInstance(policy);


        Struct.Builder structBuilder = Struct.newBuilder();
        values.forEach(structBuilder::putFields);
        queryRequestBuilder.setResourceContext(structBuilder);

        QueryRequest queryRequest = queryRequestBuilder.build();
        QueryResponse queryResponse = client.query(queryRequest);

        return queryResponse.getResponse();
    }


    public void close() {
        channel.shutdown();
    }

    private PolicyInstance getPolicy(String name, String label) {
        PolicyInstance.Builder policyInstance = PolicyInstance.newBuilder();
        policyInstance.setName(name);
        policyInstance.setInstanceLabel(label);

        return policyInstance.build();
    }
}