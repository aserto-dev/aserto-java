package org.example.server;

import com.aserto.AuthorizerClient;
import com.aserto.AuthzClient;
import com.aserto.ChannelBuilder;
import com.sun.net.httpserver.HttpServer;
import io.grpc.ManagedChannel;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ToDoServer {
     private AuthorizerClient authzClient;
     public ToDoServer() throws IOException {
          ManagedChannel channel = new ChannelBuilder().build();

          authzClient = new AuthzClient(channel);

          // Create HTTP server
          HttpServer server = HttpServer.create(new InetSocketAddress(3001), 0);
          server.createContext("/todos", new TodosHandler(authzClient));
          server.createContext("/user", new UsersHandler(authzClient));

          server.start();
     }

     public void close() throws Exception {
       authzClient.close();
     }
}
