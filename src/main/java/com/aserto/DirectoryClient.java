package com.aserto;

import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.common.v3.PaginationRequest;
import com.aserto.directory.common.v3.Relation;
import com.aserto.directory.exporter.v3.ExporterGrpc;
import com.aserto.directory.importer.v3.ImporterGrpc;
import com.aserto.directory.model.v3.*;
import com.aserto.directory.reader.v3.*;
import com.aserto.directory.writer.v3.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Struct;
import com.aserto.directory.common.v3.Object;


import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;

import javax.net.ssl.SSLException;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;

public class DirectoryClient implements DirectoryClientReader, DirectoryClientWriter, DirectoryClientManifest {
    private ReaderGrpc.ReaderBlockingStub readerClient;
    private WriterGrpc.WriterBlockingStub writerClient;
    private ImporterGrpc.ImporterBlockingStub importerClient;
    private ExporterGrpc.ExporterBlockingStub exporterClient;
    private ModelGrpc.ModelBlockingStub modelClient;

    public DirectoryClient(DirClientBuilder dirClientBuilder) {
        readerClient = dirClientBuilder.getReaderClient();
        writerClient = dirClientBuilder.getWriterClient();
        modelClient = dirClientBuilder.getModelClient();
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

        StringBuilder bodyBuilder = new StringBuilder();
        Metadata.Builder metadataBuilder =  Metadata.newBuilder();

        manifestResponses.forEachRemaining(manifestResponse -> {
            if (!manifestResponse.getMetadata().getAllFields().isEmpty()) {
                manifestResponse.getMetadata().getAllFields().forEach(metadataBuilder::setField);
            } else if (!manifestResponse.getBody().getData().isEmpty()) {
                bodyBuilder.append(manifestResponse.getBody().getData().toStringUtf8());
            }
        });


        Body manifestBody = Body.newBuilder().setData(ByteString.copyFrom(bodyBuilder.toString().getBytes())).build();

        return GetManifestResponse.newBuilder()
                .setMetadata(metadataBuilder.build())
                .setBody(manifestBody)
                .build();
    }

    @Override
    public SetManifestRequest setManifest(String manifest) {
        Body manifestBody = Body.newBuilder().setData(ByteString.copyFrom(manifest.getBytes())).build();

        return SetManifestRequest.newBuilder()
                .setBody(manifestBody)
                .build();
    }

    @Override
    public DeleteManifestRequest deleteManifest() {
        return DeleteManifestRequest.newBuilder().build();
    }
    class ObjectIdentifierList implements Iterable<ObjectIdentifier> {

        private List<ObjectIdentifier> objects;

        public ObjectIdentifierList(List<ObjectIdentifier> objects) {
            this.objects = objects;
        }

        @Override
        public Iterator<ObjectIdentifier> iterator() {
            return objects.iterator();
        }

    }


    public static void main(String[] args) throws SSLException {
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

//        System.out.println(getObjectResponse.toString());
//        System.out.println(getManifestResponse.getBody().getData().toStringUtf8());


        List objects = List.of(
                ObjectIdentifier.newBuilder()
                        .setObjectType("user")
                        .setObjectId("rick@the-citadel.com")
                        .build(),
                ObjectIdentifier.newBuilder()
                        .setObjectType("user")
                        .setObjectId("morty@the-citadel.com")
                        .build());

        GetObjectManyRequest getObjectManyRequest = directoryClient.getObjectManyRequest(objects);
        System.out.println(getObjectManyRequest);
    }



}
