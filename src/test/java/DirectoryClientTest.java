import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.SSLException;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.aserto.ChannelBuilder;
import com.aserto.directory.common.v3.Object;
import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.common.v3.Relation;
import com.aserto.directory.exporter.v3.ExportResponse;
import com.aserto.directory.exporter.v3.Option;
import com.aserto.directory.importer.v3.ImportRequest;
import com.aserto.directory.importer.v3.Opcode;
import com.aserto.directory.model.v3.GetManifestResponse;
import com.aserto.directory.reader.v3.CheckRelationResponse;
import com.aserto.directory.reader.v3.CheckResponse;
import com.aserto.directory.reader.v3.GetGraphRequest;
import com.aserto.directory.reader.v3.GetGraphResponse;
import com.aserto.directory.reader.v3.GetObjectManyResponse;
import com.aserto.directory.reader.v3.GetObjectResponse;
import com.aserto.directory.reader.v3.GetObjectsResponse;
import com.aserto.directory.reader.v3.GetRelationResponse;
import com.aserto.directory.reader.v3.GetRelationsRequest;
import com.aserto.directory.reader.v3.GetRelationsResponse;
import com.aserto.directory.v3.Directory;
import com.aserto.directory.v3.DirectoryClient;
import com.aserto.directory.v3.ImportEvent;
import com.aserto.directory.v3.ImportHandler;
import com.aserto.directory.v3.UninitilizedClientException;
import com.aserto.directory.writer.v3.SetObjectResponse;
import com.aserto.directory.writer.v3.SetRelationResponse;
import com.aserto.model.ImportElement;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import utils.IntegrationTestsExtension;

@Tag("IntegrationTest")
@ExtendWith({IntegrationTestsExtension.class})
class DirectoryClientTest {
    private static DirectoryClient directoryClient;
    private static ManagedChannel channel;

    private final static String ORIGINAL_MANIFEST =
            """
            # yaml-language-server: $schema=https://www.topaz.sh/schema/manifest.json
            ---

            ### filename: manifest.yaml ###
            ### datetime: 2023-10-17T00:00:00-00:00 ###
            ### description: citadel manifest ###

            ### model ###
            model:
              version: 3

            ### object type definitions ###
            types:
              ### display_name: User ###
              user:
                relations:
                  ### display_name: user#manager ###
                  manager: user
                  friend: user

              ### display_name: Identity ###
              identity:
                relations:
                  ### display_name: identity#identifier ###
                  identifier: user

              test_type:

              ### display_name: Group ###
              group:
                relations:
                  ### display_name: group#member ###
                  member: user

            """;
    private final static String MODIFIED_MANIFEST = ORIGINAL_MANIFEST +
            """
              ### display_name: Department ###
              department:
                relations:
                  ### display_name: group#member ###
                  member: user
            """;

    @BeforeAll
    @SuppressWarnings("unused")
    static void setDirectoryClient() throws SSLException {
       channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(9292)
                .withInsecure(true)
                .build();

        directoryClient = new DirectoryClient(channel);
    }

    @BeforeEach
    @SuppressWarnings("unused")
    void beforeEach() throws InterruptedException, UninitilizedClientException {
        directoryClient.setManifest(ORIGINAL_MANIFEST);
        List<ImportElement> list = importCitadelDataList();
        directoryClient.importData(list.stream());
    }

    @AfterEach
    @SuppressWarnings("unused")
    void afterEach() throws UninitilizedClientException {
        directoryClient.deleteManifest();
    }

    @AfterAll
    @SuppressWarnings("unused")
    static void closeChannel() {
        channel.shutdown();
    }


    @Test
    void testDirectoryClientWithReaderCanRead() {
        // Arrange
        DirectoryClient client = new DirectoryClient(channel, null, null, null, null);

        // Act & Assert
        assertDoesNotThrow(() -> {
            client.getObject("user", "rick@the-citadel.com");
        });
    }

    @Test
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testDirectoryClientWithReaderCannotWrite() {
        // Arrange
        DirectoryClient client = new DirectoryClient(channel, null, null, null, null);


        // Act & Assert
        assertThrows(UninitilizedClientException.class, () -> {
            client.setObject("test_type", "test_id");
        });
    }

    @Test
    void testGetUserWithNoRelations() throws UninitilizedClientException {
        // Arrange
        Object managerObject = Directory.buildObject("user", "rick@the-citadel.com");

        // Act
        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "rick@the-citadel.com");

        // Assert
        assertThat(getObjectResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("type_", "id_")
                .isEqualTo(managerObject);
        assertEquals(0, getObjectResponse.getRelationsList().size());
    }

    @Test
    void testGetUserWithRelations() throws UninitilizedClientException {
        // Arrange
        Object managerObject = Directory.buildObject("user", "rick@the-citadel.com");
        Relation managerRelation = Directory.buildRelation("user", "rick@the-citadel.com", "manager", "user", "morty@the-citadel.com");
        Relation adminRelation = Directory.buildRelation("group", "admin", "member", "user", "rick@the-citadel.com");

        // Act
        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "rick@the-citadel.com", true);

        // Assert
        assertThat(getObjectResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("type_", "id_")
                .isEqualTo(managerObject);
        assertThat(getObjectResponse.getRelationsList())
                .usingRecursiveFieldByFieldElementComparatorOnFields("objectId_", "objectType_", "relation_", "subjectId_", "subjectType_")
                .containsExactlyInAnyOrderElementsOf(List.of(managerRelation, adminRelation));
    }

    @Test
    void testGetUsers() throws UninitilizedClientException {
        // Arrange & Act
        GetObjectsResponse getObjectsResponse = directoryClient.getObjects("user");

        // Assert
        assertEquals(2, getObjectsResponse.getResultsList().size());
    }

    @Test
    void testGetUsersWithLimit() throws UninitilizedClientException {
        // Arrange & Act
        GetObjectsResponse getObjectsResponse = directoryClient.getObjects("user", 1, "");

        // Assert
        while (!getObjectsResponse.getPage().getNextToken().isEmpty()) {
            assertEquals(1,  getObjectsResponse.getResultsList().size());
            getObjectsResponse = directoryClient.getObjects("user", 1, getObjectsResponse.getPage().getNextToken());
        }
    }

    @Test
    void testGetUserManyRequest() throws UninitilizedClientException {
        // Arrange
        List<ObjectIdentifier> objects = List.of(
            Directory.buildObjectIdentifier("user", "rick@the-citadel.com"),
            Directory.buildObjectIdentifier("user", "morty@the-citadel.com"));
        Set<String> expectedUsers = objects.stream().map(ObjectIdentifier::getObjectId).collect(Collectors.toSet());

        // Act
        GetObjectManyResponse getObjectManyResponse = directoryClient.getObjectManyRequest(objects);

        // Assert
        Set<String> actualUsers = getObjectManyResponse.getResultsList().stream().map(Object::getId).collect(Collectors.toSet());
        assertEquals(expectedUsers, actualUsers);
    }

    @Test
    void testGetRelation() throws UninitilizedClientException {
        // Arrange
        Relation expectedRelation = Directory.buildRelation("user", "rick@the-citadel.com", "manager", "user", "morty@the-citadel.com");

        // Act
        GetRelationResponse getRelationResponse = directoryClient.getRelation(
                "user",
                "rick@the-citadel.com",
                "manager",
                "user",
                "morty@the-citadel.com");

        // Assert
        Relation relation = getRelationResponse.getResult();
        assertThat(relation)
                .usingRecursiveComparison()
                .comparingOnlyFields("objectType_", "objectId_", "relation_", "subjectId_", "subjectType_")
                .isEqualTo(expectedRelation);
    }

    @Test
    void testGetRelations() throws UninitilizedClientException {
        // Arrange
        Relation expectedManagerRelation = Directory.buildRelation("user", "rick@the-citadel.com", "manager", "user", "morty@the-citadel.com");
        Relation expectedFriendRelation = Directory.buildRelation("user", "rick@the-citadel.com", "friend", "user", "morty@the-citadel.com");

        directoryClient.setRelation(
                "user",
                "rick@the-citadel.com",
                "friend",
                "user",
                "morty@the-citadel.com");

        GetRelationsRequest getRelationsRequest = GetRelationsRequest.newBuilder().setObjectType("user").build();

        // Act
        GetRelationsResponse getRelationsResponse = directoryClient.getRelations(getRelationsRequest);

        // Assert
        assertEquals(2, getRelationsResponse.getResultsList().size());
        assertThat(getRelationsResponse.getResultsList())
                .usingRecursiveFieldByFieldElementComparatorOnFields("objectId_", "objectType_", "relation_", "subjectId_", "subjectType_")
                .containsExactlyInAnyOrderElementsOf(List.of(expectedManagerRelation, expectedFriendRelation));
    }

    @Test
    @SuppressWarnings("deprecation")
    void testCheckRelationManager() throws UninitilizedClientException {
        // Arrange & Act
        CheckRelationResponse checkRelationResponse = directoryClient.checkRelation(
                "user",
                "rick@the-citadel.com",
                "manager",
                "user",
                "morty@the-citadel.com");

        // Assert
        assertTrue(checkRelationResponse.getCheck());
    }

    @Test
    @SuppressWarnings("deprecation")
    void testCheckRelationFriend() throws UninitilizedClientException {
        // Arrange & Act
        CheckRelationResponse checkRelationResponse = directoryClient.checkRelation(
                "user",
                "rick@the-citadel.com",
                "friend",
                "user",
                "morty@the-citadel.com");

        // Assert
        assertFalse(checkRelationResponse.getCheck());
    }

    @Test
    void testCheckManager() throws UninitilizedClientException {
        // Arrange & Act
        CheckResponse checkResponse = directoryClient.check(
                "user",
                "rick@the-citadel.com",
                "manager",
                "user",
                "morty@the-citadel.com");

        // Assert
        assertTrue(checkResponse.getCheck());
    }

    @Test
    void testGetGraph() {
        // Arrange
        GetGraphRequest getGraphRequest = GetGraphRequest.newBuilder()
                .setObjectType("user")
                .setObjectId("rick@the-citadel.com")
                .setRelation("manager")
                .setSubjectType("user")
                .setSubjectId("morty@the-citadel.com")
                .build();

                List<ObjectIdentifier> objectDependencyList = Arrays.asList(
                    ObjectIdentifier.newBuilder()
                            .setObjectType("user")
                            .setObjectId("morty@the-citadel.com")
                            .build()
                );

        // Act
        GetGraphResponse getGraphResponse = directoryClient.getGraph(getGraphRequest);

        // Assert
        assertThat(getGraphResponse.getResultsList())
                .usingRecursiveFieldByFieldElementComparatorOnFields("objectType_", "objectId_")
                .containsExactlyInAnyOrderElementsOf(objectDependencyList);
    }

    @Test
    void setObjectTest() throws UninitilizedClientException {
        // Arrange
        Object object = Directory.buildObject("test_type", "test_id");

        // Act
        SetObjectResponse setObjectResponse = directoryClient.setObject("test_type", "test_id");

        // Assert
        assertThat(setObjectResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("type_", "id_")
                .isEqualTo(object);
    }

    @Test
    void deleteObjectTest() throws UninitilizedClientException {
        // Arrange
        directoryClient.setObject("test_type", "test_id");
        assertEquals(1, directoryClient.getObjects("test_type").getResultsList().size());

        // Act
        directoryClient.deleteObject("test_type", "test_id");

        // Assert
        assertEquals(0, directoryClient.getObjects("test_type").getResultsList().size());
    }

    @Test
    void setRelationTest() throws UninitilizedClientException {
        // Arrange
        Relation relation = Directory.buildRelation("user", "morty@the-citadel.com", "friend", "user", "rick@the-citadel.com");

        // Act
        SetRelationResponse setRelationResponse = directoryClient.setRelation(
                "user",
                "morty@the-citadel.com",
                "friend",
                "user",
                "rick@the-citadel.com");

        // Assert
        assertThat(setRelationResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("objectType_", "objectId_", "relation_", "subjectType_", "subjectId_")
                .isEqualTo(relation);
    }

    @Test
    void deleteRelationTest() throws UninitilizedClientException {
        // Arrange & Act
        directoryClient.deleteRelation(
                "user",
                "morty@the-citadel.com",
                "manager",
                "user",
                "rick@the-citadel.com");

        // Assert
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            directoryClient.getRelation(
                    "user",
                    "morty@the-citadel.com",
                    "manager",
                    "user",
                    "rick@the-citadel.com");
        });

        assertEquals("NOT_FOUND: E20051 key not found", exception.getMessage());
    }

    @Test
    void testGetManifest() throws UninitilizedClientException {
        // Arrange & Act
        GetManifestResponse getManifestResponse = directoryClient.getManifest();

        // Assert
        assertEquals(ORIGINAL_MANIFEST, getManifestResponse.getBody().getData().toStringUtf8());
    }

    @Test
    void testSetManifest() throws InterruptedException, UninitilizedClientException {
        // Arrange & Act
        directoryClient.setManifest(MODIFIED_MANIFEST);
        GetManifestResponse getManifestResponse = directoryClient.getManifest();

        // Assert
        assertEquals(MODIFIED_MANIFEST, getManifestResponse.getBody().getData().toStringUtf8());
    }

    @Test
    void testDeleteManifest() throws UninitilizedClientException {
        // Arrange & Act
        directoryClient.deleteManifest();
        GetManifestResponse getManifestResponse = directoryClient.getManifest();

        // Assert
        assertEquals("", getManifestResponse.getBody().getData().toStringUtf8());
    }

    @Test
    void importDataTest() throws InterruptedException, UninitilizedClientException {
        // Arrange
        class Handler implements ImportHandler {
            public List<ImportEvent.Error> errors = new ArrayList<>();
            public int recvObjects = 0;
            public int recvRelations = 0;
            public int setObjects = 0;
            public int setRelations = 0;

            @Override
            public void onProgress(ImportEvent.Counter counter) {
                switch(counter.type) {
                    case OBJECT -> {
                        recvObjects += counter.recv;
                        setObjects += counter.set;
                    }
                    case RELATION -> {
                        recvRelations += counter.recv;
                        setRelations += counter.set;
                    }
                    default -> {
                    }
                }
            }

            @Override
            public void onError(ImportEvent.Error error) {
                errors.add(error);
            }
        }

        List<ImportElement> list = Arrays.asList(
            new ImportElement(Directory.buildObject("user", "manager@acmecorp.com"), Opcode.OPCODE_SET),
            new ImportElement(Directory.buildObject("user", "employee@acmecorp.com"), Opcode.OPCODE_SET),
            new ImportElement(Directory.buildObject("badType", "object_id"), Opcode.OPCODE_SET),
            new ImportElement(Directory.buildRelation("user", "employee@acmecorp.com", "manager", "user", "manager@acmecorp.com"), Opcode.OPCODE_SET)
        );
        Handler handler = new Handler();

        // Act
        Status status = directoryClient.importData(list.stream(), handler);

        // Assert
        assertThat(status).isEqualTo(Status.OK);
        assertThat(handler.recvObjects).isEqualTo(3);
        assertThat(handler.setObjects).isEqualTo(2);
        assertThat(handler.recvRelations).isEqualTo(1);
        assertThat(handler.setRelations).isEqualTo(1);
        assertThat(handler.errors).hasSize(1);

        ImportRequest failedRequest = handler.errors.get(0).request;
        assertThat(failedRequest.hasObject()).isTrue();
        assertThat(failedRequest.getObject().getType()).isEqualTo("badType");

        GetRelationResponse relationResponse = directoryClient.getRelation(
            "user", "employee@acmecorp.com", "manager", "user", "manager@acmecorp.com", true
        );
        assertThat(relationResponse.getResult().getObjectType()).isEqualTo("user");
        assertThat(relationResponse.getResult().getObjectId()).isEqualTo("employee@acmecorp.com");
        assertThat(relationResponse.getResult().getRelation()).isEqualTo("manager");
        assertThat(relationResponse.getResult().getSubjectType()).isEqualTo("user");
        assertThat(relationResponse.getResult().getSubjectId()).isEqualTo("manager@acmecorp.com");

        assertThat(relationResponse.getObjectsCount()).isEqualTo(2);
        relationResponse.getObjectsOrThrow("user:employee@acmecorp.com");
        relationResponse.getObjectsOrThrow("user:manager@acmecorp.com");
    }

    @Test
    void exportDataTest() throws UninitilizedClientException {
        // Arrange & Act
        Iterator<ExportResponse> exportedData = directoryClient.exportData(Option.OPTION_DATA);

        // Assert
        int elementCount = 0;
        while(exportedData.hasNext()) {
            exportedData.next();
            elementCount++;
        }

        assertEquals(7, elementCount);
    }

    private List<ImportElement> importCitadelDataList() {
        List<ImportElement> importElements = new ArrayList<>();
        Object rick = Directory.buildObject("user", "rick@the-citadel.com");
        Object morty = Directory.buildObject("user", "morty@the-citadel.com");
        Object adminGroup = Directory.buildObject("group", "admin");
        Object editorGroup = Directory.buildObject("group", "editor");
        Relation rickAdminRelation = Directory.buildRelation("group", "admin", "member", "user", "rick@the-citadel.com");
        Relation mortyEditorRelation = Directory.buildRelation("group", "editor", "member", "user", "morty@the-citadel.com");
        Relation managerRelation = Directory.buildRelation("user", "rick@the-citadel.com", "manager", "user", "morty@the-citadel.com");


        Opcode opcode = Opcode.OPCODE_SET;
        importElements.add(new ImportElement(rick, opcode));
        importElements.add(new ImportElement(morty, opcode));
        importElements.add(new ImportElement(adminGroup, opcode));
        importElements.add(new ImportElement(editorGroup, opcode));
        importElements.add(new ImportElement(rickAdminRelation, opcode));
        importElements.add(new ImportElement(mortyEditorRelation, opcode));
        importElements.add(new ImportElement(managerRelation, opcode));

        return importElements;
    }
}
