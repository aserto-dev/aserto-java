import com.aserto.DirClientBuilder;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class DirectoryClientBuilderTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ManagedChannel channel;

    @BeforeEach
    public void setUp() throws IOException {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a client channel and register for automatic graceful shutdown.
        channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());
    }

    @Test
    void createDirectoryClientWithReader() {
        // Arrange & Act
        DirClientBuilder dirClientBuilder = new DirClientBuilder(channel, null, null, null, null);

        // Assert
        assertNotEquals(null, dirClientBuilder.getReaderClient());
        assertNull(dirClientBuilder.getWriterClient());
        assertNull(dirClientBuilder.getImporterClient());
        assertNull(dirClientBuilder.getExporterClient());
    }

    @Test
    void createDirectoryClientsWithSameChannel() {
        // Arrange & Act
        DirClientBuilder dirClientBuilder = new DirClientBuilder(channel);

        // Assert
        assertNotEquals(null, dirClientBuilder.getReaderClient());
        assertNotEquals(null, dirClientBuilder.getWriterClient());
        assertNotEquals(null, dirClientBuilder.getImporterClient());
        assertNotEquals(null, dirClientBuilder.getExporterClient());
    }
}
