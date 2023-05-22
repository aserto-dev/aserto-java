package org.example.server;

import com.aserto.AuthorizerClient;
import com.aserto.AuthzClient;
import com.aserto.ChannelBuilder;
import com.aserto.DirectoryClient;
import com.aserto.model.Config;
import com.sun.net.httpserver.HttpServer;
import io.grpc.ManagedChannel;
import org.example.DatabaseHelper;
import org.example.EnvConfigLoader;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.net.InetSocketAddress;

public class ToDoServer {
     private AuthorizerClient authzClient;
     private DirectoryClient directoryClient;
     public ToDoServer() throws IOException {
          EnvConfigLoader envCfgLoader = new EnvConfigLoader();
          ManagedChannel authzChannel = new ChannelBuilder(envCfgLoader.getAuthzConfig()).build();
          ManagedChannel directoryChannel = new ChannelBuilder(envCfgLoader.getDirectoryConfig()).build();

          authzClient = new AuthzClient(authzChannel);
          directoryClient = new DirectoryClient(directoryChannel);
          DatabaseHelper dbHelper = new DatabaseHelper();

          // Create HTTP server
          HttpServer server = HttpServer.create(new InetSocketAddress(3001), 0);
          server.createContext("/todos", new TodosHandler(authzClient, dbHelper));
          server.createContext("/user", new UsersHandler(authzClient, directoryClient));

          server.start();
     }

     public void close() throws Exception {
          authzClient.close();
//          directoryClient.close();
     }
}
