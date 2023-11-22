import com.aserto.ChannelBuilder;
import com.aserto.directory.DirectoryClient;
import com.aserto.directory.common.v3.Object;
import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.common.v3.Relation;
import com.aserto.directory.reader.v3.*;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.net.ssl.SSLException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("IntegrationTest")
@ExtendWith({IntegrationTestsExtenion.class})
public class DirectoryClientTest {
    private static DirectoryClient directoryClient;

    @BeforeAll
    static void setDirectoryClient() throws SSLException {
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(9292)
                .withInsecure(true)
                .build();

        directoryClient = new DirectoryClient(channel);
    }

    @Test
    @Tag("IntegrationTest")
    void testGetUserWithNoRelations() {
        // Arrange & Act
        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "morty@the-citadel.com");

        // Assert
        assertEquals("morty@the-citadel.com", getObjectResponse.getResult().getId());
        assertEquals(0, getObjectResponse.getRelationsList().size());

    }

    @Test
    @Tag("IntegrationTest")
    void testGetUserWithRelations() {
        // Arrange & Act
        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "morty@the-citadel.com", true);

        // Assert
        assertEquals("morty@the-citadel.com", getObjectResponse.getResult().getId());
        assertEquals(4, getObjectResponse.getRelationsList().size());
    }

    @Test
    @Tag("IntegrationTest")
    void testGetUsers() {
        // Arrange & Act
        GetObjectsResponse getObjectsResponse = directoryClient.getObjects("user");

        // Assert
        assertEquals(5, getObjectsResponse.getResultsList().size());
    }

    @Test
    @Tag("IntegrationTest")
    void testGetUsersWithLimit() {
        // Arrange & Act
        GetObjectsResponse getObjectsResponse = directoryClient.getObjects("user", 2, "");

        // Assert
        while (!getObjectsResponse.getPage().getNextToken().isEmpty()) {
            assertTrue( getObjectsResponse.getResultsList().size() <= 2 );
            getObjectsResponse = directoryClient.getObjects("user", 2, getObjectsResponse.getPage().getNextToken());
        }
    }

    @Test
    @Tag("IntegrationTest")
    void testGetUserManyRequest() {
        // Arrange
        List<ObjectIdentifier> objects = List.of(
            ObjectIdentifier.newBuilder()
                    .setObjectType("user")
                    .setObjectId("rick@the-citadel.com")
                    .build(),
            ObjectIdentifier.newBuilder()
                    .setObjectType("user")
                    .setObjectId("morty@the-citadel.com")
                    .build());

        // Act
        GetObjectManyResponse getObjectManyResponse = directoryClient.getObjectManyRequest(objects);

        // Assert
        Set<String> actualUsers = getObjectManyResponse.getResultsList().stream().map(Object::getId).collect(Collectors.toSet());
        Set<String> expectedUsers = objects.stream().map(ObjectIdentifier::getObjectId).collect(Collectors.toSet());

        assertEquals(actualUsers, expectedUsers);
    }

    @Test
    @Tag("IntegrationTest")
    void testGetRelation() {
        // Arrange
        GetRelationResponse getRelationResponse = directoryClient.getRelation(
                "group",
                "editor",
                "member",
                "user",
                "morty@the-citadel.com");

        // Act
        Relation relation = getRelationResponse.getResult();

        // Assert
        assertEquals("group", relation.getObjectType());
        assertEquals("editor", relation.getObjectId());
        assertEquals("member", relation.getRelation());
        assertEquals("user", relation.getSubjectType());
        assertEquals("morty@the-citadel.com", relation.getSubjectId());
    }

    @Test
    @Tag("IntegrationTest")
    void testGetRelations() {
        // Arrange
        GetRelationsRequest getRelationsRequest = GetRelationsRequest.newBuilder().setObjectType("identity").build();

        // Act
        GetRelationsResponse getRelationsResponse = directoryClient.getRelations(getRelationsRequest);

        // Assert
        assertEquals(10, getRelationsResponse.getResultsList().size());
    }
}
