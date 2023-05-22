package com.aserto;

import com.aserto.directory.exporter.v2.ExporterGrpc;
import com.aserto.directory.importer.v2.ImporterGrpc;
import com.aserto.directory.reader.v2.*;
import com.aserto.directory.writer.v2.WriterGrpc;
import io.grpc.ManagedChannel;

public class DirectoryClient {
    private ReaderGrpc.ReaderBlockingStub readerClient;
    private WriterGrpc.WriterBlockingStub writerClient;
    private ImporterGrpc.ImporterBlockingStub importerClient;
    private ExporterGrpc.ExporterBlockingStub exporterClient;

    public DirectoryClient(ManagedChannel channel) {
        this.readerClient = ReaderGrpc.newBlockingStub(channel);
        this.writerClient = WriterGrpc.newBlockingStub(channel);
        this.importerClient = ImporterGrpc.newBlockingStub(channel);
        this.exporterClient = ExporterGrpc.newBlockingStub(channel);
    }

    public ReaderGrpc.ReaderBlockingStub getReaderClient() {
        return readerClient;
    }

    public WriterGrpc.WriterBlockingStub getWriterClient() {
        return writerClient;
    }

    public ImporterGrpc.ImporterBlockingStub getImporterClient() {
        return importerClient;
    }

    public ExporterGrpc.ExporterBlockingStub getExporterClient() {
        return exporterClient;
    }

    public com.aserto.directory.common.v2.Object[] getObjects() {
        GetObjectsRequest.Builder builder = GetObjectsRequest.newBuilder();
        GetObjectsRequest request = builder.build();
        GetObjectsResponse response = readerClient.getObjects(request);

        return response.getResultsList().toArray(new com.aserto.directory.common.v2.Object[0]);
    }
}
