package org.example;

import org.example.server.ToDoServer;

public class TodoExample {
    public static void main(String[] args) throws Exception {
        ToDoServer server = new ToDoServer();

        Thread stopServerHook = new Thread(() -> {
            try {
                server.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Runtime.getRuntime().addShutdownHook(stopServerHook);
    }
}
