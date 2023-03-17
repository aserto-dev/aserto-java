import com.aserto.AuthzClient;
import com.aserto.authorizer.v2.*;
import com.aserto.authorizer.v2.api.IdentityType;
import com.aserto.authorizer.v2.api.Module;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;


class AuthzClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    // Will be used to mock the grpc server
    private final AuthorizerGrpc.AuthorizerImplBase serviceImpl =
            mock(AuthorizerGrpc.AuthorizerImplBase.class, delegatesTo(
                    new AuthorizerGrpc.AuthorizerImplBase() {
                        // Implement necessary behaviour for tests by overriding the grpc called methods

                        @Override
                        public void listPolicies(ListPoliciesRequest request, StreamObserver<ListPoliciesResponse> responseObserver) {
                            ListPoliciesResponse listPoliciesResponse = ListPoliciesResponse.newBuilder()
                                    .addResult(Module.newBuilder().setId("test").build())
                                    .addResult(Module.newBuilder().setId("test2").build())
                                    .build();

                            responseObserver.onNext(listPoliciesResponse);
                            responseObserver.onCompleted();
                        }

                        @Override
                        public void is(IsRequest request, StreamObserver<IsResponse> responseObserver) {
                            boolean deleteAction = request.getPolicyContext().getPath().equals("todoApp.DELETE.todos.__id");
                            boolean editorRole = request.getIdentityContext().getIdentity().equals("rick@the-citadel.com");

                            Decision decision;
                            if (deleteAction && editorRole) {
                                decision = Decision.newBuilder().setDecision("allowed").setIs(true).build();
                            } else {
                                decision = Decision.newBuilder().setDecision("allowed").setIs(false).build();
                            }

                            IsResponse isResponse = IsResponse.newBuilder()
                                    .addDecisions(decision)
                                    .build();

                            responseObserver.onNext(isResponse);
                            responseObserver.onCompleted();
                        }

                        @Override
                        public void query(QueryRequest request, StreamObserver<QueryResponse> responseObserver) {
                            boolean expectedQuery = request.getQuery().equals("x = input; y = data");
                            boolean expectedResourceContext = request.getResourceContext().getFieldsMap().get("id").getStringValue().equals("string_value");

                            Struct response = null;
                            if (expectedQuery && expectedResourceContext) {
                                Value value = Value.newBuilder().setStringValue("response_string_value").build();

                                response = Struct.newBuilder().putFields("response_id",value).build();
                            }

                            QueryResponse queryResponse = QueryResponse.newBuilder()
                                    .setResponse(response)
                                    .build();


                            responseObserver.onNext(queryResponse);
                            responseObserver.onCompleted();
                        }

                        @Override
                        public void decisionTree(DecisionTreeRequest request, StreamObserver<DecisionTreeResponse> responseObserver) {
                            Value falseValue = Value.newBuilder().setBoolValue(false).build();
                            Struct decision = Struct.newBuilder().putFields("allowed", falseValue).build();

                            Value structValue = Value.newBuilder().setStructValue(decision).build();
                            Struct response = Struct.newBuilder().putFields("todoApp.GET.todos", structValue).build();

                            DecisionTreeResponse decisionTreeResponse = DecisionTreeResponse.newBuilder().setPath(response).build();

                            responseObserver.onNext(decisionTreeResponse);
                            responseObserver.onCompleted();
                        }

                        @Override
                        public void getPolicy(GetPolicyRequest request, StreamObserver<GetPolicyResponse> responseObserver) {
                            Module module = Module.newBuilder().setId(request.getId()).build();
                            GetPolicyResponse getPolicyResponse = GetPolicyResponse.newBuilder().setResult(module).build();


                            responseObserver.onNext(getPolicyResponse);
                            responseObserver.onCompleted();
                        }
                    }));

    private AuthzClient client;

    @BeforeEach
    public void setUp() throws IOException {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create a client channel and register for automatic graceful shutdown.
        ManagedChannel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        // Create a HelloWorldClient using the in-process channel;
        client = new AuthzClient(channel);
    }

    @Test
    void testIsTrueCall() throws SSLException {
        // Arrange
        IdentityCtx identityCtx = new IdentityCtx("rick@the-citadel.com", IdentityType.IDENTITY_TYPE_SUB);
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.DELETE.todos.__id", new String[]{"allowed"});

        Decision expectedDecision = Decision.newBuilder().setDecision("allowed").setIs(true).build();
        List<Decision> expectedDecisions = Arrays.asList(expectedDecision);

        // Act
        List<Decision> decisions = client.is(identityCtx, policyCtx);
        client.close();

        // Assert
        assertTrue(compareLists(decisions, expectedDecisions));
    }

    @Test
    void testIsFalseCall() throws SSLException {
        // Arrange
        IdentityCtx identityCtx = new IdentityCtx("beth@the-smiths.com", IdentityType.IDENTITY_TYPE_SUB);
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.DELETE.todos.__id", new String[]{"allowed"});

        Decision expectedDecision = Decision.newBuilder().setDecision("allowed").setIs(false).build();
        List<Decision> expectedDecisions = Arrays.asList(expectedDecision);

        // Act
        List<Decision> decisions = client.is(identityCtx, policyCtx);
        client.close();

        // Assert
        assertTrue(compareLists(decisions, expectedDecisions));
    }

    @Test
    void testQueryCall() throws SSLException {
        // Arrange
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.DELETE.todos.__id", new String[]{"allowed"});
        String query = "x = input; y = data";

        Map<String, Value> values = new HashMap<>();
        values.put("id", Value.newBuilder().setStringValue("string_value").build());

        // Act
        Struct queryResponse = client.query(query, policyCtx, values);
        client.close();

        // Assert
        assertEquals(1, queryResponse.getFieldsCount());
    }

    @Test
    void testDecisionTree() throws SSLException {
        // Arrange
        IdentityCtx identityCtx = new IdentityCtx("beth@the-smiths.com", IdentityType.IDENTITY_TYPE_SUB);
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "", new String[]{"allowed"});

        // Act
        Map<String, Value> decisionTreeResponse = client.decisionTree(identityCtx, policyCtx);


        Value falseValue = Value.newBuilder().setBoolValue(false).build();
        Struct decision = Struct.newBuilder().putFields("allowed", falseValue).build();

        Value structValue = Value.newBuilder().setStructValue(decision).build();
        Struct expectedResponse = Struct.newBuilder().putFields("todoApp.GET.todos", structValue).build();

        // Assert
        assertTrue(compareDecisionMaps(expectedResponse.getFieldsMap(), decisionTreeResponse));
    }

    @Test
    void testGetPolicy() throws SSLException {
        // Arrange
        String policyPath = "todo/tmp/opa/oci/github/workspace/content/src/policies/todoApp.GET.users.__userID.rego";

        // Act
        Module policyResponse = client.getPolicy(policyPath);

        // Assert
        assertEquals(policyPath, policyResponse.getId());
    }

    private boolean compareLists(List list1, List list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).equals(list2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean compareDecisionMaps(Map<String, Value> s1, Map<String, Value> s2) {
        if (s1.size() != s2.size()) {
            return false;
        }
        for (Map.Entry<String, Value> entry : s1.entrySet()) {
            if (!s2.containsKey(entry.getKey())) {
                return false;
            }
            if (!s2.get(entry.getKey()).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }
}
