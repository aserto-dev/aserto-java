package org.example.server;

import com.aserto.AuthorizerClient;
import com.aserto.authorizer.v2.api.IdentityType;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class TodosHandler implements HttpHandler {
    private static final String ALLOWED = "allowed";

    private AuthzHelper authHelper;

    public TodosHandler(AuthorizerClient authzClient) {
        authHelper = new AuthzHelper(authzClient);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            getTodos(exchange);
        }
        else {
            exchange.sendResponseHeaders(405, 0);
        }
        exchange.close();
    }

    private void getTodos(HttpExchange exchange) throws IOException {
        String jwtToken = Utils.extractJwt(exchange);
        IdentityCtx identityCtx = new IdentityCtx(jwtToken, IdentityType.IDENTITY_TYPE_JWT);
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.GET.todos", new String[]{ALLOWED});

        boolean allowed = authHelper.isAllowed(identityCtx, policyCtx);
        if (allowed)  {
            exchange.sendResponseHeaders(200, 0);
        } else {
            exchange.sendResponseHeaders(403, 0);
        }
        exchange.close();
    }
}
