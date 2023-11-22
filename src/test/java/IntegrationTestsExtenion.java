import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

public class IntegrationTestsExtenion implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static boolean started = false;
    private static Topaz topaz;

    @Override
    public void beforeAll(ExtensionContext context) throws IOException, InterruptedException, URISyntaxException {
        if (!started) {
            started = true;
//          Your "before all tests" startup logic goes here
//          https://stackoverflow.com/questions/43282798/in-junit-5-how-to-run-code-before-all-tests
            topaz = new Topaz();
            topaz.run();

//          The following line registers a callback hook when the root test context is shut down
            context.getRoot().getStore(GLOBAL).put("test close hook", this);
        }

    }

    @Override
    public void close() throws IOException, InterruptedException {
        topaz.stop();
    }
}