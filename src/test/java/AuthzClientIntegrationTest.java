import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.aserto.ChannelBuilder;
import com.aserto.authorizer.AuthzClient;
import com.aserto.authorizer.v2.api.Module;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import utils.IntegrationTestsExtension;

@Tag("IntegrationTest")
@ExtendWith({IntegrationTestsExtension.class})
class AuthzClientIntegrationTest {
    @Test
    void testBuildAuthzClient() throws IOException {
        // Arrange
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(8282)
                .withInsecure(false)
                .withCACertPath(System.getProperty("user.home") + "/.local/share/topaz/certs/grpc-ca.crt")
                .build();

        AuthzClient authzClient =  new AuthzClient(channel);

        // Act
        List<Module> policies = authzClient.listPolicies("todo", "todo");
        authzClient.close();

        // Assert
        assertEquals(5, policies.size());
    }

    @Test
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
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    void testFailWhenSecureConnectionToInsecureClient() throws SSLException {
        // Arrange
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(8282)
                .withInsecure(false)
                .build();

        AuthzClient authzClient =  new AuthzClient(channel);

        // Act & Assert
        assertThrows(StatusRuntimeException.class, () -> {
            authzClient.listPolicies("todo", "todo");
        });
        authzClient.close();
    }
}
