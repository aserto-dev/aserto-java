import com.aserto.ChannelBuilder;
import com.aserto.directory.common.v3.ObjectDependency;
import com.aserto.directory.exporter.v3.ExportResponse;
import com.aserto.directory.exporter.v3.Option;
import com.aserto.directory.model.v3.GetManifestResponse;
import com.aserto.directory.v3.DirectoryClient;
import com.aserto.directory.common.v3.Object;
import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.common.v3.Relation;
import com.aserto.directory.reader.v3.*;
import com.aserto.directory.writer.v3.DeleteRelationResponse;
import com.aserto.directory.writer.v3.SetObjectResponse;
import com.aserto.directory.writer.v3.SetRelationResponse;
import com.aserto.model.ImportElement;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import utils.IntegrationTestsExtenion;

import javax.net.ssl.SSLException;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("IntegrationTest")
@ExtendWith({IntegrationTestsExtenion.class})
class DirectoryClientTest {
    private static DirectoryClient directoryClient;

    private final static String originalManifest =
            "# yaml-language-server: $schema=https://www.topaz.sh/schema/manifest.json\n" +
            "---\n" +
            "\n" +
            "### filename: manifest.yaml ###\n" +
            "### datetime: 2023-10-17T00:00:00-00:00 ###\n" +
            "### description: citadel manifest ###\n" +
            "\n" +
            "### model ###\n" +
            "model:\n" +
            "  version: 3\n" +
            "\n" +
            "### object type definitions ###\n" +
            "types:\n" +
            "  ### display_name: User ###\n" +
            "  user:\n" +
            "    relations:\n" +
            "      ### display_name: user#manager ###\n" +
            "      manager: user\n" +
            "\n" +
            "  ### display_name: Identity ###\n" +
            "  identity:\n" +
            "    relations:\n" +
            "      ### display_name: identity#identifier ###\n" +
            "      identifier: user\n" +
            "\n" +
            "  ### display_name: Group ###\n" +
            "  group:\n" +
            "    relations:\n" +
            "      ### display_name: group#member ###\n" +
            "      member: user\n\n";
    private final static String modifiedManifest = originalManifest +
            "  ### display_name: Department ###\n" +
            "  department:\n" +
            "    relations:\n" +
            "      ### display_name: group#member ###\n" +
            "      member: user\n";

    @BeforeAll
    static void setDirectoryClient() throws SSLException, InterruptedException {
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(9292)
                .withInsecure(true)
                .build();

        directoryClient = new DirectoryClient(channel);
    }

    @BeforeEach
    void beforeEach() throws InterruptedException {
        directoryClient.setManifest(originalManifest);
        List<ImportElement> list = importDataList();
        directoryClient.importData(list.stream());
    }

    @AfterEach
    void afterEach() {
        directoryClient.deleteManifest();
    }

    @Test
    @Tag("IntegrationTest")
    void testGetUserWithNoRelations() {
        // Arrange
        Object managerObject = Object.newBuilder()
                .setType("user")
                .setId("manager@aserto.com")
                .build();

        // Act
        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "manager@aserto.com");

        // Assert
        assertThat(getObjectResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("objectType_", "objectId_")
                .isEqualTo(managerObject);
        assertEquals(0, getObjectResponse.getRelationsList().size());
    }

    @Test
    @Tag("IntegrationTest")
    void testGetUserWithRelations() {
        // Arrange
        Object managerObject = Object.newBuilder()
                .setType("user")
                .setId("manager@aserto.com")
                .build();
        Relation relation = Relation.newBuilder()
                .setObjectType("user")
                .setObjectId("user@aserto.com")
                .setRelation("manager")
                .setSubjectType("user")
                .setSubjectId("manager@aserto.com")
                .build();

        // Act
        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "user@aserto.com", true);

        // Assert
        assertThat(getObjectResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("objectType_", "objectId_")
                .isEqualTo(managerObject);
        assertThat(getObjectResponse.getRelationsList())
                .usingRecursiveFieldByFieldElementComparatorOnFields("objectId_", "objectType_", "relation_", "subjectId_", "subjectType_")
                .containsExactlyInAnyOrderElementsOf(List.of(relation));
    }

    @Test
    @Tag("IntegrationTest")
    void testGetUsers() {
        // Arrange & Act
        GetObjectsResponse getObjectsResponse = directoryClient.getObjects("user");

        // Assert
        assertEquals(2, getObjectsResponse.getResultsList().size());
    }

    @Test
    @Tag("IntegrationTest")
    void testGetUsersWithLimit() {
        // Arrange & Act
        GetObjectsResponse getObjectsResponse = directoryClient.getObjects("user", 1, "");

        // Assert
        while (!getObjectsResponse.getPage().getNextToken().isEmpty()) {
            assertEquals(1,  getObjectsResponse.getResultsList().size());
            getObjectsResponse = directoryClient.getObjects("user", 1, getObjectsResponse.getPage().getNextToken());
        }
    }

    @Test
    @Tag("IntegrationTest")
    void testGetUserManyRequest() {
        // Arrange
        List<ObjectIdentifier> objects = List.of(
            ObjectIdentifier.newBuilder()
                    .setObjectType("user")
                    .setObjectId("manager@aserto.com")
                    .build(),
            ObjectIdentifier.newBuilder()
                    .setObjectType("user")
                    .setObjectId("user@aserto.com")
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
        Relation expectedRelation = Relation.newBuilder()
                .setObjectType("user")
                .setObjectId("user@aserto.com")
                .setRelation("manager")
                .setSubjectType("user")
                .setSubjectId("manager@aserto.com")
                .build();
        GetRelationResponse getRelationResponse = directoryClient.getRelation(
                "user",
                "user@aserto.com",
                "manager",
                "user",
                "manager@aserto.com");

        // Act
        Relation relation = getRelationResponse.getResult();

        // Assert
        assertThat(relation)
                .usingRecursiveComparison()
                .comparingOnlyFields("objectType_", "objectId_", "relation_", "subjectId_", "subjectType_")
                .isEqualTo(expectedRelation);
    }

    @Test
    @Tag("IntegrationTest")
    void testGetRelations() {
        // Arrange
        Relation expectedManagerRelation = Relation.newBuilder()
                .setObjectType("user")
                .setObjectId("user@aserto.com")
                .setRelation("manager")
                .setSubjectType("user")
                .setSubjectId("manager@aserto.com")
                .build();
        Relation expectedFriendRelation = Relation.newBuilder()
                .setObjectType("user")
                .setObjectId("user@aserto.com")
                .setRelation("friend")
                .setSubjectType("user")
                .setSubjectId("manager@aserto.com")
                .build();

        directoryClient.setRelation(
                "user",
                "user@aserto.com",
                "friend",
                "user",
                "manager@aserto.com");

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
    @Tag("IntegrationTest")
    void testCheckRelationManager() {
        // Arrange & Act
        CheckRelationResponse checkRelationResponse = directoryClient.checkRelation(
                "user",
                "user@aserto.com",
                "manager",
                "user",
                "manager@aserto.com");

        // Assert
        assertTrue(checkRelationResponse.getCheck());
    }

    @Test
    @Tag("IntegrationTest")
    void testCheckRelationFriend() {
        // Arrange & Act
        CheckRelationResponse checkRelationResponse = directoryClient.checkRelation(
                "user",
                "user@aserto.com",
                "friend",
                "user",
                "manager@aserto.com");

        // Assert
        assertFalse(checkRelationResponse.getCheck());
    }

    @Test
    @Tag("IntegrationTest")
    void testCheckManager() {
        // Arrange & Act
        CheckResponse checkResponse = directoryClient.check(
                "user",
                "user@aserto.com",
                "manager",
                "user",
                "manager@aserto.com");

        // Assert
        assertTrue(checkResponse.getCheck());
    }

    @Test
    @Tag("IntegrationTest")
    void testGetGraph() {
        // Arrange
        GetGraphRequest getGraphRequest = GetGraphRequest.newBuilder()
                .setAnchorType("user")
                .setAnchorId("user@aserto.com")
                .setObjectType("user")
                .setObjectId("user@aserto.com")
                .build();

                List<ObjectDependency> objectDependencyList = Arrays.asList(
                    ObjectDependency.newBuilder()
                            .setObjectType("user")
                            .setObjectId("user@aserto.com")
                            .setRelation("manager")
                            .setSubjectType("user")
                            .setSubjectId("manager@aserto.com")
                            .build()
                );

        // Act
        GetGraphResponse getGraphResponse = directoryClient.getGraph(getGraphRequest);

        // Assert
        assertThat(getGraphResponse.getResultsList())
                .usingRecursiveFieldByFieldElementComparatorOnFields("objectId_", "objectType_", "relation_", "subjectId_", "subjectType_")
                .containsExactlyInAnyOrderElementsOf(objectDependencyList);
    }

    @Test
    @Tag("IntegrationTest")
    void setObjectTest() {
        // Arrange
        Object object = Object.newBuilder()
                .setType("test_type")
                .setId("test_id")
                .build();

        // Act
        SetObjectResponse setObjectResponse = directoryClient.setObject("test_type", "test_id");

        // Assert
        assertThat(setObjectResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("type_", "id_")
                .isEqualTo(object);
    }

    @Test
    @Tag("IntegrationTest")
    void deleteObjectTest() {
        // Arrange
        directoryClient.setObject("test_type", "test_id");
        assertEquals(1, directoryClient.getObjects("test_type").getResultsList().size());

        // Act
        directoryClient.deleteObject("test_type", "test_id");

        // Assert
        assertEquals(0, directoryClient.getObjects("test_type").getResultsList().size());
    }

    @Test
    @Tag("IntegrationTest")
    void setRelationTest() {
        // Arrange
        Relation relation = Relation.newBuilder()
                .setObjectType("user")
                .setObjectId("user@aserto.com")
                .setRelation("friend")
                .setSubjectType("user")
                .setSubjectId("manager@aserto.com")
                .build();

        // Act
        SetRelationResponse setRelationResponse = directoryClient.setRelation(
                "user",
                "user@aserto.com",
                "friend",
                "user",
                "manager@aserto.com");

        // Assert
        assertThat(setRelationResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("objectType_", "objectId_", "relation_", "subjectType_", "subjectId_")
                .isEqualTo(relation);
    }

    @Test
    @Tag("IntegrationTest")
    void deleteRelationTest() {
        // Arrange & Act
        DeleteRelationResponse deleteRelationResponse = directoryClient.deleteRelation(
                "user",
                "user@aserto.com",
                "manager",
                "user",
                "manager@aserto.com");

        // Assert
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            directoryClient.getRelation(
                    "user",
                    "user@aserto.com",
                    "manager",
                    "user",
                    "manager@aserto.com");
        });

        assertEquals("NOT_FOUND: E20051 key not found", exception.getMessage());
    }

    @Test
    @Tag("IntegrationTest")
    void testGetManifest() {
        // Arrange & Act
        GetManifestResponse getManifestResponse = directoryClient.getManifest();

        // Assert
        assertEquals(originalManifest, getManifestResponse.getBody().getData().toStringUtf8());
    }

    @Test
    @Tag("IntegrationTest")
    void testSetManifest() throws InterruptedException {
        // Arrange & Act
        directoryClient.setManifest(modifiedManifest);
        GetManifestResponse getManifestResponse = directoryClient.getManifest();

        // Assert
        assertEquals(modifiedManifest, getManifestResponse.getBody().getData().toStringUtf8());
    }

    @Test
    @Tag("IntegrationTest")
    void testDeleteManifest() {
        // Arrange & Act
        directoryClient.deleteManifest();
        GetManifestResponse getManifestResponse = directoryClient.getManifest();

        // Assert
        assertEquals("", getManifestResponse.getBody().getData().toStringUtf8());
    }

    @Test
    @Tag("IntegrationTest")
    void importDataTest() throws InterruptedException {
        // Arrange
        List<ImportElement> list = importDataList();
        List<Object> users = list.stream()
                .map(ImportElement::getObject)
                .filter(object -> object != null && object.getType().equals("user"))
                .collect(Collectors.toList());

        // Act
        directoryClient.importData(list.stream());

        // Assert
        GetObjectsResponse getObjectsResponse = directoryClient.getObjects("user");
        assertThat(getObjectsResponse.getResultsList())
                .usingRecursiveFieldByFieldElementComparatorOnFields("objectId_", "objectType_", "relation_", "subjectId_", "subjectType_")
                .containsAll(users);

    }

    @Test
    @Tag("IntegrationTest")
    void exportDataTest() throws InterruptedException {
        // Arrange & Act
        Iterator<ExportResponse> exportedData = directoryClient.exportData(Option.OPTION_DATA);

        // Assert
        int elementCount = 0;
        while(exportedData.hasNext()) {
            exportedData.next();
            elementCount++;
        }

        assertEquals(3, elementCount);
    }

    private List<ImportElement> importDataList() {
        List<ImportElement> importElements = new ArrayList<>();
        Object managerUser = Object.newBuilder()
                .setType("user")
                .setId("manager@aserto.com").build();
        Object user = Object.newBuilder()
                .setType("user")
                .setId("user@aserto.com").build();
        Relation managerRelation = Relation.newBuilder()
                .setObjectType("user")
                .setObjectId("user@aserto.com")
                .setRelation("manager")
                .setSubjectType("user")
                .setSubjectId("manager@aserto.com")
                .build();

        importElements.add(new ImportElement(managerUser));
        importElements.add(new ImportElement(user));
        importElements.add(new ImportElement(managerRelation));

        return importElements;
    }
}
