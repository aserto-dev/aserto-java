package com.aserto;

import com.aserto.directory.common.v2.ObjectTypeIdentifier;
import com.aserto.directory.common.v2.PaginationRequest;
import com.aserto.directory.exporter.v2.ExporterGrpc;
import com.aserto.directory.importer.v2.ImporterGrpc;
import com.aserto.directory.reader.v2.*;
import com.aserto.directory.writer.v2.WriterGrpc;
import com.aserto.directory.common.v2.Object;
import io.grpc.ManagedChannel;

public class DirectoryClient {
    private ReaderGrpc.ReaderBlockingStub readerClient;
    private WriterGrpc.WriterBlockingStub writerClient;
    private ImporterGrpc.ImporterBlockingStub importerClient;
    private ExporterGrpc.ExporterBlockingStub exporterClient;
    private ManagedChannel channel;

    public DirectoryClient(ManagedChannel channel) {
        this.readerClient = ReaderGrpc.newBlockingStub(channel);
        this.writerClient = WriterGrpc.newBlockingStub(channel);
        this.importerClient = ImporterGrpc.newBlockingStub(channel);
        this.exporterClient = ExporterGrpc.newBlockingStub(channel);
        this.channel = channel;
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

    public Result<Object> getObjects(String objectType, Integer pageSize, String nextPageToken) {
        GetObjectsRequest.Builder builder = GetObjectsRequest.newBuilder();

        PaginationRequest paginationRequest = PaginationRequest.newBuilder()
            .setSize(pageSize)
            .setToken(nextPageToken)
            .build();
        ObjectTypeIdentifier objectIdentifier = ObjectTypeIdentifier.newBuilder()
                .setName(objectType)
                .build();

        GetObjectsRequest request = builder
                .setPage(paginationRequest)
                .setParam(objectIdentifier)
                .build();
        GetObjectsResponse response = readerClient.getObjects(request);
        String nextToken = response.getPage().getNextToken();


        Object[] objects = response.getResultsList().toArray(new Object[0]);
        return new Result<>(objects, nextToken);
    }

    public void close() {
        channel.shutdown();
    }
}
