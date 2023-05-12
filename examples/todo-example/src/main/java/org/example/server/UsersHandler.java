package org.example.server;

import com.aserto.AuthorizerClient;
import com.aserto.authorizer.v2.api.IdentityType;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.google.protobuf.Value;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

public class UsersHandler implements HttpHandler {
    private static final String ALLOWED = "allowed";
    private AuthzHelper authHelper;

    public UsersHandler(AuthorizerClient authzClient) {
        authHelper = new AuthzHelper(authzClient);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod().toUpperCase()) {
            case "GET":
                getUsers(exchange);
                break;
            default:
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
        }
    }

    private void getUsers(HttpExchange exchange) throws IOException {
        String jwtToken = Utils.extractJwt(exchange);
        IdentityCtx identityCtx = new IdentityCtx(jwtToken, IdentityType.IDENTITY_TYPE_JWT);
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.GET.users.__userID\n", new String[]{ALLOWED});
        String personalId = extractPersonalId(exchange.getRequestURI().toString());
        Map<String, Value> resourceCtx = java.util.Map.of("personalId", Value.newBuilder().setStringValue(personalId).build());


        boolean allowed = authHelper.isAllowed(identityCtx, policyCtx, resourceCtx);
        if (allowed)  {
            exchange.sendResponseHeaders(200, 0);
        } else {
            exchange.sendResponseHeaders(403, 0);
        }
        exchange.close();
    }

    private String extractPersonalId(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }
}
