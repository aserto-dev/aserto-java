package org.example;

import com.aserto.ChannelBuilder;
import com.aserto.directory.v3.DirectoryClient;
import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.reader.v3.GetObjectManyResponse;
import com.aserto.directory.reader.v3.GetObjectResponse;
import com.aserto.directory.reader.v3.GetObjectsResponse;
import com.aserto.directory.v3.Directory;
import io.grpc.ManagedChannel;

import javax.net.ssl.SSLException;
import java.util.List;

public class DirectoryExample {
    public static void main(String[] args) throws SSLException {
        // create a channel that has the connection details
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(9292)
                .withInsecure(true)
                .build();

        // create a directory client that wil be used to interact with the directory
        DirectoryClient directoryClient = new DirectoryClient(channel);

        getUserExample(directoryClient);
        getUsersExample(directoryClient);
        getObjectManyRequest(directoryClient);
    }

    public static void getUserExample(DirectoryClient directoryClient) {
        System.out.println("------ Get user example ------");
        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "morty@the-citadel.com", false);
        System.out.println(getObjectResponse);
    }

    public static void getUsersExample(DirectoryClient directoryClient) {
        System.out.println("------ Get users example ------");
        GetObjectsResponse getObjectsResponse = directoryClient.getObjects("user", 100, "");
        System.out.println(getObjectsResponse);
    }

    public static void getObjectManyRequest(DirectoryClient directoryClient) {
        System.out.println("------ Get object many example ------");
        List<ObjectIdentifier> objects = List.of(
                Directory.buildObjectIdentifier("user", "rick@the-citadel.com"),
                Directory.buildObjectIdentifier("user", "morty@the-citadel.com"));

        GetObjectManyResponse getObjectManyRequest = directoryClient.getObjectManyRequest(objects);
        System.out.println(getObjectManyRequest);
    }
}