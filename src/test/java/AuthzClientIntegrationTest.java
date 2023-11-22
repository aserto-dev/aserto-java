import com.aserto.authorizer.AuthzClient;
import com.aserto.ChannelBuilder;
import com.aserto.authorizer.v2.api.Module;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import utils.IntegrationTestsExtenion;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
@ExtendWith({IntegrationTestsExtenion.class})
class AuthzClientIntegrationTest {
    @Test
    @Tag("IntegrationTest")
    void testBuildAuthzClient() throws IOException {
        // Arrange
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(8282)
                .withInsecure(false)
                .withCACertPath(System.getProperty("user.home") + "/.config/topaz/certs/grpc-ca.crt")
                .build();

        AuthzClient authzClient =  new AuthzClient(channel);

        // Act
        List<Module> policies = authzClient.listPolicies("todo", "todo");
        authzClient.close();

        // Assert
        assertEquals(5, policies.size());
    }

    @Test
    @Tag("IntegrationTest")
    void testInsecureConnectionToInsecureClient() throws SSLException {
        // Arrange
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(8282)
                .withInsecure(true)
                .build();

        AuthzClient authzClient =  new AuthzClient(channel);

        // Act
        List<Module> policies = authzClient.listPolicies("todo", "todo");
        authzClient.close();

        // Assert
        assertEquals(5, policies.size());
    }

    @Test
    @Tag("IntegrationTest")
    void testFailWhenSecureConnectionToInsecureClient() throws SSLException {
        // Arrange
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(8282)
                .withInsecure(false)
                .build();

        AuthzClient authzClient =  new AuthzClient(channel);

        // Act & Assert
        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            authzClient.listPolicies("todo", "todo");
        });
        authzClient.close();
    }
}
