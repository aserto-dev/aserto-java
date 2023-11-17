package com.aserto;

import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.common.v3.PaginationRequest;
import com.aserto.directory.common.v3.Relation;
import com.aserto.directory.exporter.v3.ExporterGrpc;
import com.aserto.directory.importer.v3.ImportRequest;
import com.aserto.directory.importer.v3.ImporterGrpc;
import com.aserto.directory.model.v3.*;
import com.aserto.directory.reader.v3.*;
import com.aserto.directory.writer.v3.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Struct;
import com.aserto.directory.common.v3.Object;


import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class DirectoryClient implements DirectoryClientReader, DirectoryClientWriter, DirectoryClientModel {
    static final int MAX_CHUNK_SIZE = 65536;
    Logger logger = LogManager.getLogger(DirectoryClient.class);
    private ReaderGrpc.ReaderBlockingStub readerClient;
    private WriterGrpc.WriterBlockingStub writerClient;
    private ImporterGrpc.ImporterBlockingStub importerClient;
    private ExporterGrpc.ExporterBlockingStub exporterClient;
    private ModelGrpc.ModelBlockingStub modelClient;
    private ModelGrpc.ModelStub modelClientAsync;

    public DirectoryClient(DirClientBuilder dirClientBuilder) {
        readerClient = dirClientBuilder.getReaderClient();
        writerClient = dirClientBuilder.getWriterClient();
        modelClient = dirClientBuilder.getModelClient();
        modelClientAsync = dirClientBuilder.getModelClientAsync();
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
    public GetObjectsResponse getObjects(String type, int pageSize, String pageToken) {
        return readerClient.getObjects(GetObjectsRequest.newBuilder()
                .setObjectType(type)
                .setPage(buildPaginationRequest(pageSize, pageToken))
                .build());
    }

    @Override
    public GetObjectManyRequest getObjectManyRequest(List<ObjectIdentifier> objectIdentifiers) {
        return GetObjectManyRequest.newBuilder()
                .addAllParam(new ObjectIdentifierList(objectIdentifiers))
                .build();
    }

    private PaginationRequest buildPaginationRequest(int pageSize, String pageToken) {
        return PaginationRequest.newBuilder()
                .setSize(pageSize)
                .setToken(pageToken)
                .build();
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
    public GetRelationsResponse getRelations(String objectType, String objectId, String relationName, String subjectType, String subjectId, String subjectRelation, boolean withObjects, int pageSize, String pageToken) {
        return readerClient.getRelations(GetRelationsRequest.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relationName)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .setSubjectRelation(subjectRelation)
                .setWithObjects(withObjects)
                .setPage(buildPaginationRequest(pageSize, pageToken))
                .build());
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
    public GetGraphResponse getGraph(String anchorType, String anchorId, String objectType, String objectId,
                                     String relation, String subjectType, String subjectId, String subjectRelation) {
        return readerClient.getGraph(GetGraphRequest.newBuilder()
                .setAnchorType(anchorType)
                .setAnchorId(anchorId)
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relation)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .setSubjectRelation(subjectRelation)
                .build());
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
    public DeleteObjectResponse deleteObject(String type, String id, boolean withRelations) {
        return writerClient.deleteObject(DeleteObjectRequest.newBuilder()
                .setObjectType(type)
                .setObjectId(id)
                .setWithRelations(withRelations)
                .build());
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
                    throw new RuntimeException(e);
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
    public SetManifestResponse setManifest(String manifest) throws InterruptedException {
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

        return null;
    }


    @Override
    public DeleteManifestResponse deleteManifest() {
        return modelClient.deleteManifest(DeleteManifestRequest.newBuilder().build());
    }

//    public ImportRequest importData() {
//        return importerClient.
//    }



    private class ObjectIdentifierList implements Iterable<ObjectIdentifier> {
        private List<ObjectIdentifier> objects;

        public ObjectIdentifierList(List<ObjectIdentifier> objects) {
            this.objects = objects;
        }

        @Override
        public Iterator<ObjectIdentifier> iterator() {
            return objects.iterator();
        }

    }


    public static void main(String[] args) throws SSLException, InterruptedException {
        // create a channel that has the connection details
        ManagedChannel channel = new ChannelBuilder()
                .withHost("localhost")
                .withPort(9292)
                .withInsecure(true)
                .build();

        DirClientBuilder dirClientBuilder = new DirClientBuilder(channel);
        DirectoryClient directoryClient = new DirectoryClient(dirClientBuilder);

//        GetObjectResponse getObjectResponse = directoryClient.getObject("user", "morty@the-citadel.com", false);
//        GetManifestResponse getManifestResponse = directoryClient.getManifest();
//
//        System.out.println(getObjectResponse.toString());
//        System.out.println(getManifestResponse.getBody().getData().toStringUtf8());

//        ---------------------------
//        List<ObjectIdentifier> objects = List.of(
//                ObjectIdentifier.newBuilder()
//                        .setObjectType("user")
//                        .setObjectId("rick@the-citadel.com")
//                        .build(),
//                ObjectIdentifier.newBuilder()
//                        .setObjectType("user")
//                        .setObjectId("morty@the-citadel.com")
//                        .build());
//
//        GetObjectManyRequest getObjectManyRequest = directoryClient.getObjectManyRequest(objects);
//        System.out.println(getObjectManyRequest);
//    --------------------------------------

//        GetGraphResponse getGraphResponse = directoryClient.getGraph("user", "rick@the-citadel.com", "user", "rick@the-citadel.com","", "", "", "");
//        System.out.println(getGraphResponse);
//    ---------------------------------


        String manifest =  "# yaml-language-server: $schema=https://www.topaz.sh/schema/manifest.json\n" +
                "---\n" +
                "### model ###\n" +
                "model:\n" +
                "  version: 3\n" +
                "\n" +
                "### object type definitions ###\n" +
                "types:\n" +
                "  ### display_name: User ###\n" +
                "  user:\n" +
                "    relations:\n" +
                "      ### display_name: user#manager ###\n" +
                "      manager: user\n" +
                "\n" +
                "  ### display_name: Identity ###\n" +
                "  identity:\n" +
                "    relations:\n" +
                "      ### display_name: identity#identifier ###\n" +
                "      identifier: user\n" +
                "\n" +
                "  ### display_name: Group ###\n" +
                "  group:\n" +
                "    relations:\n" +
                "      ### display_name: group#member ###\n" +
                "      member: user";

        directoryClient.setManifest(manifest);


        System.out.println(directoryClient.getManifest().getBody().getData().toStringUtf8());

//    --------------------------------------




    }



}
