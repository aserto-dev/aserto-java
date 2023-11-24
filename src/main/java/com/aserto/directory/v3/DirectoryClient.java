package com.aserto.directory.v3;

import com.aserto.utils.MessageChunker;
import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.common.v3.PaginationRequest;
import com.aserto.directory.common.v3.Relation;
import com.aserto.directory.exporter.v3.ExportRequest;
import com.aserto.directory.exporter.v3.ExportResponse;
import com.aserto.directory.exporter.v3.ExporterGrpc;
import com.aserto.directory.exporter.v3.Option;
import com.aserto.directory.importer.v3.ImportRequest;
import com.aserto.directory.importer.v3.ImportResponse;
import com.aserto.directory.importer.v3.ImporterGrpc;
import com.aserto.directory.model.v3.*;
import com.aserto.directory.reader.v3.*;
import com.aserto.directory.writer.v3.*;
import com.aserto.model.ImportElement;
import com.google.protobuf.ByteString;
import com.google.protobuf.Struct;
import com.aserto.directory.common.v3.Object;


import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class DirectoryClient implements DirectoryClientReader,
        DirectoryClientWriter,
        DirectoryClientModel,
        DirectoryClientImporter,
        DirectoryClientExporter {
    static final int MAX_CHUNK_SIZE = 65536;
    Logger logger = LogManager.getLogger(DirectoryClient.class);
    private ReaderGrpc.ReaderBlockingStub readerClient;
    private WriterGrpc.WriterBlockingStub writerClient;
    private ImporterGrpc.ImporterStub importerClient;
    private ExporterGrpc.ExporterBlockingStub exporterClient;
    private ModelGrpc.ModelBlockingStub modelClient;
    private ModelGrpc.ModelStub modelClientAsync;

    public DirectoryClient(ManagedChannel channelBuilder) {
        DirectoryClientBuilder dirClientBuilder = new DirectoryClientBuilder(channelBuilder);
        readerClient = dirClientBuilder.getReaderClient();
        writerClient = dirClientBuilder.getWriterClient();
        importerClient = dirClientBuilder.getImporterClient();
        exporterClient = dirClientBuilder.getExporterClient();
        modelClient = dirClientBuilder.getModelClient();
        modelClientAsync = dirClientBuilder.getModelClientAsync();
    }

    @Override
    public GetObjectResponse getObject(String type, String id) {
        return getObject(type, id, false);
    }
    @Override
    public GetObjectResponse getObject(String type, String id, boolean withRelations) {
        return readerClient.getObject(GetObjectRequest.newBuilder()
                .setObjectType(type)
                .setObjectId(id)
                .setWithRelations(withRelations)
                .build());
    }

    @Override
    public GetObjectsResponse getObjects(String type) {
        return getObjects(type, 100, "");
    }

    @Override
    public GetObjectsResponse getObjects(String type, int pageSize, String pageToken) {
        return readerClient.getObjects(GetObjectsRequest.newBuilder()
                .setObjectType(type)
                .setPage(buildPaginationRequest(pageSize, pageToken))
                .build());
    }

    @Override
    public GetObjectManyResponse getObjectManyRequest(List<ObjectIdentifier> objectIdentifiers) {
        return readerClient.getObjectMany(GetObjectManyRequest.newBuilder()
                .addAllParam(new ObjectIdentifierList(objectIdentifiers))
                .build());
    }

    private PaginationRequest buildPaginationRequest(int pageSize, String pageToken) {
        return PaginationRequest.newBuilder()
                .setSize(pageSize)
                .setToken(pageToken)
                .build();
    }

    @Override
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId) {
        return getRelation(objectType, objectId, relationName, subjectType, subjectId, "", false);
    }

    @Override
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation) {
        return getRelation(objectType, objectId, relationName, subjectType, subjectId, subjectRelation, false);
    }
    @Override
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation, boolean withObjects) {
        return readerClient.getRelation(GetRelationRequest.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .setSubjectRelation(subjectRelation)
                .setWithObjects(withObjects)
                .build());
    }

    @Override
    public GetRelationsResponse getRelations(GetRelationsRequest relationsRequest) {
        return readerClient.getRelations(relationsRequest);
    }

    @Override
    public CheckPermissionResponse checkPermission(String objectType, String objectId, String subjectType, String subjectId, String permissionName) {
        return checkPermission(objectType, objectId, subjectType, subjectId, permissionName, false);
    }

    @Override
    public CheckPermissionResponse checkPermission(String objectType, String objectId, String subjectType, String subjectId, String permissionName, boolean trace) {
        return readerClient.checkPermission(CheckPermissionRequest.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .setPermission(permissionName)
                .setTrace(trace)
                .build());
    }

    @Override
    public CheckRelationResponse checkRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId) {
        return checkRelation(objectType, objectId, relationName, subjectType, subjectId, false);
    }

    @Override
    public CheckRelationResponse checkRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, boolean trace) {
        return readerClient.checkRelation(CheckRelationRequest.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .setTrace(trace)
                .build());
    }

    @Override
    public CheckResponse check(String objectType, String objectId, String relationName, String subjectType, String subjectId) {
        return check(objectType, objectId, relationName, subjectType, subjectId, false);
    }

    @Override
    public CheckResponse check(String objectType, String objectId, String relationName, String subjectType, String subjectId, boolean trace) {
        return readerClient.check(CheckRequest.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .setTrace(trace)
                .build());
    }


    @Override
    public GetGraphResponse getGraph(GetGraphRequest getGraphRequest) {
        return readerClient.getGraph(getGraphRequest);
    }

    @Override
    public SetObjectResponse setObject(String type, String id) {
        return setObject(type, id, "", Struct.newBuilder().build(), "");
    }

    @Override
    public SetObjectResponse setObject(String type, String id, String displayName, Struct properties, String hash) {
        Instant time = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(time.getEpochSecond())
                .setNanos(time.getNano()).build();

        SetObjectRequest objRequest = SetObjectRequest.newBuilder().setObject(
                Object.newBuilder()
                        .setType(type)
                        .setId(id)
                        .setDisplayName(displayName)
                        .setProperties(properties)
                        .setCreatedAt(timestamp)
                        .build()
        ).build();

        return writerClient.setObject(objRequest);
    }

    @Override
    public DeleteObjectResponse deleteObject(String type, String id) {
        return deleteObject(type, id, false);
    }

    @Override
    public DeleteObjectResponse deleteObject(String type, String id, boolean withRelations) {
        return writerClient.deleteObject(DeleteObjectRequest.newBuilder()
                .setObjectType(type)
                .setObjectId(id)
                .setWithRelations(withRelations)
                .build());
    }

    @Override
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId) {
        Relation relation = Relation.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .build();

        return writerClient.setRelation(SetRelationRequest.newBuilder().setRelation(relation).build());
    }

    @Override
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation) {
        Relation relation = Relation.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .setSubjectRelation(subjectRelation)
                .build();

        return writerClient.setRelation(SetRelationRequest.newBuilder().setRelation(relation).build());
    }

    @Override
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation, String hash) {
        Relation relation = Relation.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .setSubjectRelation(subjectRelation)
                .setEtag(hash)
                .build();

        return writerClient.setRelation(SetRelationRequest.newBuilder().setRelation(relation).build());
    }

    @Override
    public DeleteRelationResponse deleteRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId) {
        return writerClient.deleteRelation(DeleteRelationRequest.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .build());
    }

    @Override
    public DeleteRelationResponse deleteRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation) {
        return writerClient.deleteRelation(DeleteRelationRequest.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .setSubjectRelation(subjectRelation)
                .build());
    }

    @Override
    public GetManifestResponse getManifest() {
        GetManifestRequest manifestRequest = GetManifestRequest.newBuilder().build();
        Iterator<GetManifestResponse> manifestResponses =  modelClient.getManifest(manifestRequest);

        Metadata.Builder metadataBuilder =  Metadata.newBuilder();


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        manifestResponses.forEachRemaining(manifestResponse -> {
            if (!manifestResponse.getMetadata().getAllFields().isEmpty()) {
                manifestResponse.getMetadata().getAllFields().forEach(metadataBuilder::setField);
            } else if (!manifestResponse.getBody().getData().isEmpty()) {
                try {
                    outputStream.write(manifestResponse.getBody().getData().toByteArray());
                } catch (IOException e) {
                    logger.error("Could not write to stream the fallowing message: {}", manifestResponse.getBody().getData().toByteArray());
                }
            }
        });

        Body manifestBody = Body.newBuilder().setData(ByteString.copyFrom(outputStream.toByteArray())).build();

        return GetManifestResponse.newBuilder()
                .setMetadata(metadataBuilder.build())
                .setBody(manifestBody)
                .build();
    }

    @Override
    public void setManifest(String manifest) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<SetManifestResponse> readStream = new StreamObserver<SetManifestResponse>() {
            @Override
            public void onNext(SetManifestResponse setManifestResponse) {
                logger.info("Received response: [{}] ", setManifestResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("Error from server: [{}]: ", throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.trace("Server completed stream.");
                latch.countDown();
            }
        };

        StreamObserver<SetManifestRequest> writeStream = modelClientAsync.setManifest(readStream);
        
        MessageChunker chunker = new MessageChunker(MAX_CHUNK_SIZE, manifest.getBytes());
        while (chunker.hasNextChunk()) {
            byte[] chunk = chunker.nextChunk();
            Body manifestBody = Body.newBuilder().setData(ByteString.copyFrom(chunk)).build();
            writeStream.onNext(SetManifestRequest.newBuilder().setBody(manifestBody).build());
        }

        writeStream.onCompleted();

        boolean timedOut = !latch.await(5, TimeUnit.SECONDS);
        if (timedOut) {
            logger.error("Timed out waiting for server response.");
        }
    }


    @Override
    public DeleteManifestResponse deleteManifest() {
        return modelClient.deleteManifest(DeleteManifestRequest.newBuilder().build());
    }

    @Override
    public void importData(Stream<ImportElement> importStream) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ImportResponse> readStream = new StreamObserver<ImportResponse>() {
            @Override
            public void onNext(ImportResponse importResponse) {
                logger.info("Received response: [{}] ", importResponse.getObject());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.error("Error from server: [{}]: ", throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.trace("Server completed importStream.");
                latch.countDown();
            }
        };

        StreamObserver<ImportRequest> writer = importerClient.import_(readStream);

        importStream.forEach(importElement -> {
            if (importElement.getObject() != null) {
                writer.onNext(ImportRequest.newBuilder().setObject(importElement.getObject()).build());
            } else if (importElement.getRelation() != null) {
                writer.onNext(ImportRequest.newBuilder().setRelation(importElement.getRelation()).build());
            }
        });
        writer.onCompleted();

        boolean timedOut = !latch.await(5, TimeUnit.SECONDS);
        if (timedOut) {
            logger.error("Timed out waiting for server response.");
        }
    }

    @Override
    public Iterator<ExportResponse> exportData(Option options) {
        return exporterClient.export(ExportRequest.newBuilder()
                .setOptions(options.getNumber())
                .build());
    }

    @Override
    public Iterator<ExportResponse> exportData(Option options, Timestamp startFrom) {
        return exporterClient.export(ExportRequest.newBuilder()
                .setOptions(options.getNumber())
                .setStartFrom(startFrom)
                .build());
    }
}
