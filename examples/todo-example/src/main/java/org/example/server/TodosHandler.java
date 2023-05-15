package org.example.server;

import com.aserto.AuthorizerClient;
import com.aserto.authorizer.v2.api.IdentityType;
import com.aserto.model.IdentityCtx;
import com.aserto.model.PolicyCtx;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Value;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.example.model.Todo;
import org.example.model.User;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TodosHandler implements HttpHandler {
    private static final String ALLOWED = "allowed";

    private AuthzHelper authHelper;

    private ObjectMapper objectMapper;
    private Map<String, Todo> todos;
    private User[] users;
    public TodosHandler(AuthorizerClient authzClient) {
        authHelper = new AuthzHelper(authzClient);
        objectMapper = new ObjectMapper();
        todos = new HashMap<>();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // For cors
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        switch (exchange.getRequestMethod().toUpperCase()) {
            case "GET":
                getTodos(exchange);
                break;
            case "OPTIONS":
                setOptions(exchange);
                break;
            case "POST":
                postTodos(exchange);
                break;
            case "PUT":
                putTodos(exchange);
                break;
            case "DELETE":
                deleteTodos(exchange);
                break;
            default:
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
            String response = objectMapper.writeValueAsString(todos.values());

            exchange.sendResponseHeaders(200, response.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.flush();
            outputStream.close();
        } else {
            exchange.sendResponseHeaders(403, 0);
        }
    }

    private String objectToJson(Todo[] todos) throws JsonProcessingException {
        return objectMapper.writeValueAsString(todos);
    }

    private void setOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, GET, PATCH, OPTIONS, DELETE, PUT");
        exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
        exchange.sendResponseHeaders(204, -1);
    }

    private void postTodos(HttpExchange exchange) throws IOException {
        String jwtToken = Utils.extractJwt(exchange);
        IdentityCtx identityCtx = new IdentityCtx(jwtToken, IdentityType.IDENTITY_TYPE_JWT);
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.POST.todos", new String[]{ALLOWED});
        String value = getResponseBody(exchange);

        Todo todo = objectMapper.readValue(value, Todo.class);
        todo.setId(UUID.randomUUID().toString());
        //!TODO: set the ownerID to the user's ID
        todo.setOwnerID("CiRmZDA2MTRkMy1jMzlhLTQ3ODEtYjdiZC04Yjk2ZjVhNTEwMGQSBWxvY2Fs");

        todos.put(todo.getId(), todo);

        boolean allowed = authHelper.isAllowed(identityCtx, policyCtx);
        if (allowed)  {
            exchange.sendResponseHeaders(200, 0);
        } else {
            exchange.sendResponseHeaders(403, 0);
        }
    }

    private String getResponseBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr =  new InputStreamReader(exchange.getRequestBody(),"utf-8");
        BufferedReader br = new BufferedReader(isr);

        int b;
        StringBuilder buf = new StringBuilder(512);
        while ((b = br.read()) != -1) {
            buf.append((char) b);
        }

        br.close();
        isr.close();

        return buf.toString();
    }

    private void putTodos(HttpExchange exchange) throws IOException {
        String jwtToken = Utils.extractJwt(exchange);
        IdentityCtx identityCtx = new IdentityCtx(jwtToken, IdentityType.IDENTITY_TYPE_JWT);
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.PUT.todos.__id", new String[]{ALLOWED});
        String personalId = extractPersonalId(exchange.getRequestURI().toString());
        Map<String, Value> resourceCtx = java.util.Map.of("personalId", Value.newBuilder().setStringValue(personalId).build());

        boolean allowed = authHelper.isAllowed(identityCtx, policyCtx, resourceCtx);
        if (allowed)  {
            exchange.sendResponseHeaders(200, 0);
        } else {
            exchange.sendResponseHeaders(403, 0);
        }
    }

    private void deleteTodos(HttpExchange exchange) throws IOException {
        String jwtToken = Utils.extractJwt(exchange);
        IdentityCtx identityCtx = new IdentityCtx(jwtToken, IdentityType.IDENTITY_TYPE_JWT);
        PolicyCtx policyCtx = new PolicyCtx("todo", "todo", "todoApp.PUT.todos.__id", new String[]{ALLOWED});
        String personalId = extractPersonalId(exchange.getRequestURI().toString());
        Map<String, Value> resourceCtx = java.util.Map.of("personalId", Value.newBuilder().setStringValue(personalId).build());

        boolean allowed = authHelper.isAllowed(identityCtx, policyCtx, resourceCtx);
        if (allowed)  {
            exchange.sendResponseHeaders(200, 0);
        } else {
            exchange.sendResponseHeaders(403, 0);
        }
    }

    private String extractPersonalId(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }
 }
