package com.aserto;

import com.aserto.directory.exporter.v3.ExporterGrpc;
import com.aserto.directory.importer.v3.ImporterGrpc;
import com.aserto.directory.model.v3.ModelGrpc;
import com.aserto.directory.reader.v3.*;
import com.aserto.directory.writer.v3.WriterGrpc;
import io.grpc.ManagedChannel;

public class DirClientBuilder {
    private ReaderGrpc.ReaderBlockingStub readerClient;
    private WriterGrpc.WriterBlockingStub writerClient;
    private ImporterGrpc.ImporterStub importerClient;
    private ExporterGrpc.ExporterBlockingStub exporterClient;
    private ModelGrpc.ModelBlockingStub modelClient;
    private ModelGrpc.ModelStub modelClientAsync;
    private ManagedChannel channel;
    private ManagedChannel readerChannel;
    private ManagedChannel writerChannel;
    private ManagedChannel importerChannel;
    private ManagedChannel exporterChannel;
    private ManagedChannel modelChannel;

    public DirClientBuilder(ManagedChannel channel) {
        this.readerClient = ReaderGrpc.newBlockingStub(channel);
        this.writerClient = WriterGrpc.newBlockingStub(channel);
        this.importerClient = ImporterGrpc.newStub(channel);
        this.exporterClient = ExporterGrpc.newBlockingStub(channel);
        this.modelClient = ModelGrpc.newBlockingStub(channel);
        this.modelClientAsync = ModelGrpc.newStub(channel);
        this.channel = channel;
    }

    public DirClientBuilder(ManagedChannel readerChannel,
                            ManagedChannel writerChannel,
                            ManagedChannel importerChannel,
                            ManagedChannel exporterChannel,
                            ManagedChannel modelChannel) {

        if (readerChannel != null) {
            this.readerClient = ReaderGrpc.newBlockingStub(readerChannel);
            this.readerChannel = readerChannel;
        }

        if (writerChannel != null) {
            this.writerClient = WriterGrpc.newBlockingStub(writerChannel);
            this.writerChannel = writerChannel;
        }

        if (importerChannel != null) {
            this.importerClient = ImporterGrpc.newStub(importerChannel);
            this.importerChannel = importerChannel;
        }

        if (exporterChannel != null) {
            this.exporterClient = ExporterGrpc.newBlockingStub(exporterChannel);
            this.exporterChannel = exporterChannel;
        }

        if (modelClient != null) {
            this.modelClient = ModelGrpc.newBlockingStub(modelChannel);
            this.modelClientAsync = ModelGrpc.newStub(modelChannel);
            this.modelChannel = modelChannel;
        }
    }

    public ReaderGrpc.ReaderBlockingStub getReaderClient() {
        return readerClient;
    }

    public WriterGrpc.WriterBlockingStub getWriterClient() {
        return writerClient;
    }

    public ImporterGrpc.ImporterStub getImporterClient() {
        return importerClient;
    }

    public ExporterGrpc.ExporterBlockingStub getExporterClient() {
        return exporterClient;
    }

    public ModelGrpc.ModelBlockingStub getModelClient() {
        return modelClient;
    }

    public ModelGrpc.ModelStub getModelClientAsync() {
        return modelClientAsync;
    }

    class Result<T> {
        private T[] results;
        private String nextPageToken;

        public Result(T[] results, String nextPageToken) {
            this.results = results;
            this.nextPageToken = nextPageToken;
        }

        public T[] getResults() {
            return results;
        }

        public String getNextPageToken() {
            return nextPageToken;
        }
    }

    public void close() {
        if (channel != null) {
            channel.shutdown();
        }

        if (readerChannel != null) {
            readerChannel.shutdown();
        }

        if (writerChannel != null) {
            writerChannel.shutdown();
        }

        if (importerChannel != null) {
            importerChannel.shutdown();
        }

        if (exporterChannel != null) {
            exporterChannel.shutdown();
        }

        if (modelChannel != null) {
            modelChannel.shutdown();
        }
    }

    public void closeReader() {
        if (readerChannel != null) {
            readerChannel.shutdown();
        }
    }

    public void closeWriter() {
        if (writerChannel != null) {
            writerChannel.shutdown();
        }
    }

    public void closeImporter() {
        if (importerChannel != null) {
            importerChannel.shutdown();
        }
    }

    public void closeExporter() {
        if (exporterChannel != null) {
            exporterChannel.shutdown();
        }
    }

    public void closeModel() {
        if (modelChannel != null) {
            modelChannel.shutdown();
        }
    }
}
