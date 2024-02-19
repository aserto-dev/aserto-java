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

    public DirectoryClient(ManagedChannel readerChannel, ManagedChannel writerChannel, ManagedChannel importerChannel, ManagedChannel exporterChannel, ManagedChannel modelChannel) {
        if (readerChannel != null) {
            readerClient = ReaderGrpc.newBlockingStub(readerChannel);
        }

        if (writerChannel != null) {
            writerClient = WriterGrpc.newBlockingStub(writerChannel);
        }

        if (importerChannel != null) {
            importerClient = ImporterGrpc.newStub(importerChannel);
        }

        if (exporterChannel != null) {
            exporterClient = ExporterGrpc.newBlockingStub(exporterChannel);
        }

        if (modelChannel != null) {
            modelClient = ModelGrpc.newBlockingStub(modelChannel);
            modelClientAsync = ModelGrpc.newStub(modelChannel);
        }
    }

    public DirectoryClient(ManagedChannel managedChannel) {
        readerClient = ReaderGrpc.newBlockingStub(managedChannel);
        writerClient = WriterGrpc.newBlockingStub(managedChannel);
        importerClient = ImporterGrpc.newStub(managedChannel);
        exporterClient = ExporterGrpc.newBlockingStub(managedChannel);
        modelClient = ModelGrpc.newBlockingStub(managedChannel);
        modelClientAsync = ModelGrpc.newStub(managedChannel);
    }

    @Override
    public GetObjectResponse getObject(String type, String id) throws UninitilizedClientException {
        return getObject(type, id, false);
    }
    @Override
    public GetObjectResponse getObject(String type, String id, boolean withRelations) throws UninitilizedClientException {
        if (readerClient == null) {
            throw new UninitilizedClientException("Reader client is not initialized");
        }

        return readerClient.getObject(GetObjectRequest.newBuilder()
                .setObjectType(type)
                .setObjectId(id)
                .setWithRelations(withRelations)
                .build());
    }

    @Override
    public GetObjectsResponse getObjects(String type) throws UninitilizedClientException {
        return getObjects(type, 100, "");
    }

    @Override
    public GetObjectsResponse getObjects(String type, int pageSize, String pageToken) throws UninitilizedClientException {
        if (readerClient == null) {
            throw new UninitilizedClientException("Reader client is not initialized");
        }

        return readerClient.getObjects(GetObjectsRequest.newBuilder()
                .setObjectType(type)
                .setPage(buildPaginationRequest(pageSize, pageToken))
                .build());
    }

    @Override
    public GetObjectManyResponse getObjectManyRequest(List<ObjectIdentifier> objectIdentifiers) throws UninitilizedClientException {
        if (readerClient == null) {
            throw new UninitilizedClientException("Reader client is not initialized");
        }

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
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId) throws UninitilizedClientException {
        return getRelation(objectType, objectId, relationName, subjectType, subjectId, "", false);
    }

    @Override
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation) throws UninitilizedClientException {
        return getRelation(objectType, objectId, relationName, subjectType, subjectId, subjectRelation, false);
    }

    @Override
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation, boolean withObjects) throws UninitilizedClientException {
        if (readerClient == null) {
            throw new UninitilizedClientException("Reader client is not initialized");
        }

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
    public GetRelationsResponse getRelations(GetRelationsRequest relationsRequest) throws UninitilizedClientException {
        if (readerClient == null) {
            throw new UninitilizedClientException("Reader client is not initialized");
        }

        return readerClient.getRelations(relationsRequest);
    }

    @Override
    public CheckPermissionResponse checkPermission(String objectType, String objectId, String subjectType, String subjectId, String permissionName) throws UninitilizedClientException {
        return checkPermission(objectType, objectId, subjectType, subjectId, permissionName, false);
    }

    @Override
    public CheckPermissionResponse checkPermission(String objectType, String objectId, String subjectType, String subjectId, String permissionName, boolean trace) throws UninitilizedClientException {
        if (readerClient == null) {
            throw new UninitilizedClientException("Reader client is not initialized");
        }

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
    public CheckRelationResponse checkRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId) throws UninitilizedClientException {
        return checkRelation(objectType, objectId, relationName, subjectType, subjectId, false);
    }

    @Override
    public CheckRelationResponse checkRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, boolean trace) throws UninitilizedClientException {
        if (readerClient == null) {
            throw new UninitilizedClientException("Reader client is not initialized");
        }

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
    public CheckResponse check(String objectType, String objectId, String relationName, String subjectType, String subjectId) throws UninitilizedClientException {
        return check(objectType, objectId, relationName, subjectType, subjectId, false);
    }

    @Override
    public CheckResponse check(String objectType, String objectId, String relationName, String subjectType, String subjectId, boolean trace) throws UninitilizedClientException {
        if (readerClient == null) {
            throw new UninitilizedClientException("Reader client is not initialized");
        }

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
    public SetObjectResponse setObject(String type, String id) throws UninitilizedClientException {
        return setObject(type, id, "", Struct.newBuilder().build(), "");
    }

    @Override
    public SetObjectResponse setObject(String type, String id, String displayName, Struct properties, String hash) throws UninitilizedClientException {
        if (writerClient == null) {
            throw new UninitilizedClientException("Writer client is not initialized");
        }

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
    public DeleteObjectResponse deleteObject(String type, String id) throws UninitilizedClientException {
        return deleteObject(type, id, false);
    }

    @Override
    public DeleteObjectResponse deleteObject(String type, String id, boolean withRelations) throws UninitilizedClientException {
        if (writerClient == null) {
            throw new UninitilizedClientException("Writer client is not initialized");
        }

        return writerClient.deleteObject(DeleteObjectRequest.newBuilder()
                .setObjectType(type)
                .setObjectId(id)
                .setWithRelations(withRelations)
                .build());
    }

    @Override
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId) throws UninitilizedClientException {
        if (writerClient == null) {
            throw new UninitilizedClientException("Writer client is not initialized");
        }

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
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation) throws UninitilizedClientException {
        if (writerClient == null) {
            throw new UninitilizedClientException("Writer client is not initialized");
        }

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
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation, String hash) throws UninitilizedClientException {
        if (writerClient == null) {
            throw new UninitilizedClientException("Writer client is not initialized");
        }

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
    public DeleteRelationResponse deleteRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId) throws UninitilizedClientException {
        if (writerClient == null) {
            throw new UninitilizedClientException("Writer client is not initialized");
        }

        return writerClient.deleteRelation(DeleteRelationRequest.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .build());
    }

    @Override
    public DeleteRelationResponse deleteRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation) throws UninitilizedClientException {
        if (writerClient == null) {
            throw new UninitilizedClientException("Writer client is not initialized");
        }

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
    public GetManifestResponse getManifest() throws UninitilizedClientException {
        if (modelClient == null) {
            throw new UninitilizedClientException("Model client is not initialized");
        }

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
    public void setManifest(String manifest) throws InterruptedException, UninitilizedClientException {
        if (modelClientAsync == null) {
            throw new UninitilizedClientException("Model client is not initialized");
        }

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
    public DeleteManifestResponse deleteManifest() throws UninitilizedClientException {
        if (modelClient == null) {
            throw new UninitilizedClientException("Model client is not initialized");
        }

        return modelClient.deleteManifest(DeleteManifestRequest.newBuilder().build());
    }

    @Override
    public void importData(Stream<ImportElement> importStream) throws InterruptedException, UninitilizedClientException {
        if (importerClient == null) {
            throw new UninitilizedClientException("Import client is not initialized");
        }

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
                writer.onNext(ImportRequest.newBuilder().setOpCode(importElement.getOpcode()).setObject(importElement.getObject()).build());
            } else if (importElement.getRelation() != null) {
                writer.onNext(ImportRequest.newBuilder().setOpCode(importElement.getOpcode()).setRelation(importElement.getRelation()).build());
            }
        });
        writer.onCompleted();

        boolean timedOut = !latch.await(5, TimeUnit.SECONDS);
        if (timedOut) {
            logger.error("Timed out waiting for server response.");
        }
    }

    @Override
    public Iterator<ExportResponse> exportData(Option options) throws UninitilizedClientException {
        if (exporterClient == null) {
            throw new UninitilizedClientException("Export client is not initialized");
        }

        return exporterClient.export(ExportRequest.newBuilder()
                .setOptions(options.getNumber())
                .build());
    }

    @Override
    public Iterator<ExportResponse> exportData(Option options, Timestamp startFrom) throws UninitilizedClientException {
        if (exporterClient == null) {
            throw new UninitilizedClientException("Export client is not initialized");
        }

        return exporterClient.export(ExportRequest.newBuilder()
                .setOptions(options.getNumber())
                .setStartFrom(startFrom)
                .build());
    }
}
