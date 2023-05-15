package org.example.server;

import com.aserto.AuthorizerClient;
import com.aserto.authorizer.v2.api.IdentityType;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Value;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class UsersHandler implements HttpHandler {
    private static final String ALLOWED = "allowed";
    private AuthzHelper authHelper;
    private ObjectMapper objectMapper;
    private Map<String, User> users;

    public UsersHandler(AuthorizerClient authzClient) {
        authHelper = new AuthzHelper(authzClient);
        objectMapper = new ObjectMapper();
        users = loadUsers();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // For cors
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        switch (exchange.getRequestMethod().toUpperCase()) {
            case "GET":
                getUsers(exchange);
                break;
            case "OPTIONS":
                setOptions(exchange);
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
            String response = objectMapper.writeValueAsString(users.get(personalId));

            exchange.sendResponseHeaders(200, response.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.flush();
            outputStream.close();
        } else {
            exchange.sendResponseHeaders(403, 0);
        }
        exchange.close();
    }

    private void setOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, PATCH, OPTIONS, DELETE, PUT");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
        exchange.sendResponseHeaders(204, -1);
    }

    private String extractPersonalId(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private Map<String, User> loadUsers() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("users.json");
            User[] users = objectMapper.readValue(inputStream, User[].class);
            Map<String, User> usersMap = new HashMap<>();
            for (User user : users) {
                usersMap.put(user.getKey(), user);
            }

            return usersMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
