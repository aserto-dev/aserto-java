package utils;

import com.aserto.ChannelBuilder;
import com.aserto.directory.v3.DirectoryClient;
import io.grpc.ManagedChannel;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.*;

public class Topaz {
    private String HOME_DIR = System.getProperty("user.home");
    private String DB_DIR = HOME_DIR + "/.local/share/topaz/db";
    private String TOPAZ_CFG_DIR = HOME_DIR + "/.config/topaz/cfg";
    private DirectoryClient directoryClient;

    public Topaz() throws SSLException {
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(9292)
                .withInsecure(true)
                .build();
        directoryClient = new DirectoryClient(channel);
    }

    public void run() throws IOException, InterruptedException, URISyntaxException {
        stop();
        backupDb();
        backupCfg();
        configure();
        start();
    }

    public void stop() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("topaz","stop");
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();
        restoreDb();
        restoreCfg();
    }

    private void start() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("topaz","start","--wait");
        pb.inheritIO();
        Process process = pb.start();
        process.waitFor();

        // final Duration timeout = Duration.ofSeconds(60);
        // ExecutorService executor = Executors.newSingleThreadExecutor();

        // final Future<Integer> handler = executor.submit(new Callable() {
        //     @Override
        //     public Integer call() throws Exception {
        //         while (true) {
        //             try {
        //                 directoryClient.getObjects("user");
        //             } catch (Exception e) {
        //                 Thread.sleep(2000);
        //                 continue;
        //             }

        //             return directoryClient.getObjects("user").getResultsList().size();
        //         }
        //     }
        // });

        // try {
        //     handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        // } catch (TimeoutException | InterruptedException | ExecutionException e) {
        //     handler.cancel(true);
        // }
    }

    private void configure() throws IOException, InterruptedException {
        {
            ProcessBuilder pb = new ProcessBuilder("topaz", "config", "new", "--name", "todo-test", "--resource", "ghcr.io/aserto-policies/policy-todo:2.1.0", "--force");
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        }
        {
            ProcessBuilder pb = new ProcessBuilder("topaz", "config", "use", "todo-test");
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        }
    }

    private void backupDb() {
        File directoryDb = new File(DB_DIR + "/todo-test.db" );
        if(directoryDb.exists()) {
            directoryDb.renameTo(new File(DB_DIR + "/todo-test.db.bak" ));
        }
    }

    private void restoreDb() {
        File directoryDb = new File(DB_DIR + "/todo-test.db.bak" );
        if(directoryDb.exists()) {
            directoryDb.renameTo(new File(DB_DIR + "/todo-test.db" ));
        }
    }

    private void backupCfg() {
        File directoryDb = new File(TOPAZ_CFG_DIR + "/todo-test.yaml" );
        if(directoryDb.exists()) {
            directoryDb.renameTo(new File(TOPAZ_CFG_DIR + "/todo-test.yaml.bak" ));
        }
    }

    private void restoreCfg() {
        File directoryDb = new File(TOPAZ_CFG_DIR + "/todo-test.yaml.bak" );
        if(directoryDb.exists()) {
            directoryDb.renameTo(new File(TOPAZ_CFG_DIR + "/todo-test.yaml" ));
        }
    }
}
