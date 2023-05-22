package org.example.server;

import java.util.Base64;

public class JwtDecoder {
    private String[] chunks;
    private Base64.Decoder decoder = Base64.getUrlDecoder();

    public JwtDecoder(String jwt) {
        chunks = jwt.split("\\.");
    }

    public String decodePayload() {
        return new String(decoder.decode(chunks[1]));
    }
}
