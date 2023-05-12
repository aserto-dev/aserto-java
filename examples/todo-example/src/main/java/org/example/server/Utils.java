package org.example.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static String extractJwt(HttpExchange exchange) {
        Map<String, String> headersMap = getHeadersMap(exchange);
        String auth = headersMap.get("Authorization");
        if (auth == null) {
            throw new RuntimeException("No authorization header");
        }

        String[] authTokens = auth.split(" ");

        return authTokens[authTokens.length - 1];
    }

    public static Map<String, String> getHeadersMap(HttpExchange exchange) {
        Headers headers = exchange.getRequestHeaders();
        Map<String, String> headersMap = new HashMap<>();

        headers.forEach((key, value) -> {
            headersMap.put(key, value.get(0));
        });

        return headersMap;
    }
}
