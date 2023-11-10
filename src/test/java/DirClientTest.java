import com.aserto.DirClient;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class DirClientTest {
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
        DirClient dirClient = new DirClient(channel, null, null, null);

        // Assert
        assertNotEquals(null, dirClient.getReaderClient());
        assertNull(dirClient.getWriterClient());
        assertNull(dirClient.getImporterClient());
        assertNull(dirClient.getExporterClient());
    }

    @Test
    void createDirectoryClientsWithSameChannel() {
        // Arrange & Act
        DirClient dirClient = new DirClient(channel);

        // Assert
        assertNotEquals(null, dirClient.getReaderClient());
        assertNotEquals(null, dirClient.getWriterClient());
        assertNotEquals(null, dirClient.getImporterClient());
        assertNotEquals(null, dirClient.getExporterClient());
    }
}
