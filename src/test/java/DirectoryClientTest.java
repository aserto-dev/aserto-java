import com.aserto.ChannelBuilder;
import com.aserto.directory.common.v3.ObjectDependency;
import com.aserto.directory.exporter.v3.ExportResponse;
import com.aserto.directory.exporter.v3.Option;
import com.aserto.directory.model.v3.GetManifestResponse;
import com.aserto.directory.v3.Directory;
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
        List<ImportElement> list = importCitadelDataList();
        directoryClient.importData(list.stream());
    }

    @AfterEach
    void afterEach() {
        directoryClient.deleteManifest();
    }

    @Test
    void testGetUserWithNoRelations() {
        // Arrange
        Object managerObject = Directory.buildObject("user", "rick@the-citadel.com");

        // Act
        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "rick@the-citadel.com");

        // Assert
        assertThat(getObjectResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("objectType_", "objectId_")
                .isEqualTo(managerObject);
        assertEquals(0, getObjectResponse.getRelationsList().size());
    }

    @Test
    void testGetUserWithRelations() {
        // Arrange
        Object managerObject = Directory.buildObject("user", "rick@the-citadel.com");
        Relation managerRelation = Directory.buildRelation("user", "morty@the-citadel.com", "manager", "user", "rick@the-citadel.com");
        Relation adminRelation = Directory.buildRelation("group", "admin", "member", "user", "rick@the-citadel.com");

        // Act
        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "rick@the-citadel.com", true);

        // Assert
        assertThat(getObjectResponse.getResult())
                .usingRecursiveComparison()
                .comparingOnlyFields("objectType_", "objectId_")
                .isEqualTo(managerObject);
        assertThat(getObjectResponse.getRelationsList())
                .usingRecursiveFieldByFieldElementComparatorOnFields("objectId_", "objectType_", "relation_", "subjectId_", "subjectType_")
                .containsExactlyInAnyOrderElementsOf(List.of(managerRelation, adminRelation));
    }

    @Test
    void testGetUsers() {
        // Arrange & Act
        GetObjectsResponse getObjectsResponse = directoryClient.getObjects("user");

        // Assert
        assertEquals(2, getObjectsResponse.getResultsList().size());
    }

    @Test
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
    void testGetUserManyRequest() {
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
    void testGetRelation() {
        // Arrange
        Relation expectedRelation = Directory.buildRelation("user", "morty@the-citadel.com", "manager", "user", "rick@the-citadel.com");

        // Act
        GetRelationResponse getRelationResponse = directoryClient.getRelation(
                "user",
                "morty@the-citadel.com",
                "manager",
                "user",
                "rick@the-citadel.com");

        // Assert
        Relation relation = getRelationResponse.getResult();
        assertThat(relation)
                .usingRecursiveComparison()
                .comparingOnlyFields("objectType_", "objectId_", "relation_", "subjectId_", "subjectType_")
                .isEqualTo(expectedRelation);
    }

    @Test
    void testGetRelations() {
        // Arrange
        Relation expectedManagerRelation = Directory.buildRelation("user", "morty@the-citadel.com", "manager", "user", "rick@the-citadel.com");
        Relation expectedFriendRelation = Directory.buildRelation("user", "morty@the-citadel.com", "friend", "user", "rick@the-citadel.com");

        directoryClient.setRelation(
                "user",
                "morty@the-citadel.com",
                "friend",
                "user",
                "rick@the-citadel.com");

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
    void testCheckRelationManager() {
        // Arrange & Act
        CheckRelationResponse checkRelationResponse = directoryClient.checkRelation(
                "user",
                "morty@the-citadel.com",
                "manager",
                "user",
                "rick@the-citadel.com");

        // Assert
        assertTrue(checkRelationResponse.getCheck());
    }

    @Test
    void testCheckRelationFriend() {
        // Arrange & Act
        CheckRelationResponse checkRelationResponse = directoryClient.checkRelation(
                "user",
                "morty@the-citadel.com",
                "friend",
                "user",
                "rick@the-citadel.com");

        // Assert
        assertFalse(checkRelationResponse.getCheck());
    }

    @Test
    void testCheckManager() {
        // Arrange & Act
        CheckResponse checkResponse = directoryClient.check(
                "user",
                "morty@the-citadel.com",
                "manager",
                "user",
                "rick@the-citadel.com");

        // Assert
        assertTrue(checkResponse.getCheck());
    }

    @Test
    void testGetGraph() {
        // Arrange
        GetGraphRequest getGraphRequest = GetGraphRequest.newBuilder()
                .setAnchorType("user")
                .setAnchorId("morty@the-citadel.com")
                .setObjectType("user")
                .setObjectId("morty@the-citadel.com")
                .build();

                List<ObjectDependency> objectDependencyList = Arrays.asList(
                    ObjectDependency.newBuilder()
                            .setObjectType("user")
                            .setObjectId("morty@the-citadel.com")
                            .setRelation("manager")
                            .setSubjectType("user")
                            .setSubjectId("rick@the-citadel.com")
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
    void setObjectTest() {
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
    void setRelationTest() {
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
    void deleteRelationTest() {
        // Arrange & Act
        DeleteRelationResponse deleteRelationResponse = directoryClient.deleteRelation(
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
    void testGetManifest() {
        // Arrange & Act
        GetManifestResponse getManifestResponse = directoryClient.getManifest();

        // Assert
        assertEquals(originalManifest, getManifestResponse.getBody().getData().toStringUtf8());
    }

    @Test
    void testSetManifest() throws InterruptedException {
        // Arrange & Act
        directoryClient.setManifest(modifiedManifest);
        GetManifestResponse getManifestResponse = directoryClient.getManifest();

        // Assert
        assertEquals(modifiedManifest, getManifestResponse.getBody().getData().toStringUtf8());
    }

    @Test
    void testDeleteManifest() {
        // Arrange & Act
        directoryClient.deleteManifest();
        GetManifestResponse getManifestResponse = directoryClient.getManifest();

        // Assert
        assertEquals("", getManifestResponse.getBody().getData().toStringUtf8());
    }

    @Test
    void importDataTest() throws InterruptedException {
        // Arrange
        List<ImportElement> list = importCitadelDataList();
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
    void exportDataTest() {
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
        Relation managerRelation = Directory.buildRelation("user", "morty@the-citadel.com", "manager", "user", "rick@the-citadel.com");

        importElements.add(new ImportElement(rick));
        importElements.add(new ImportElement(morty));
        importElements.add(new ImportElement(adminGroup));
        importElements.add(new ImportElement(editorGroup));
        importElements.add(new ImportElement(rickAdminRelation));
        importElements.add(new ImportElement(mortyEditorRelation));
        importElements.add(new ImportElement(managerRelation));

        return importElements;
    }
}
