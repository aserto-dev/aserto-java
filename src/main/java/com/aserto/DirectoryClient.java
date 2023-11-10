package com.aserto;

import com.aserto.directory.reader.v2.*;
import com.aserto.directory.writer.v2.DeleteObjectResponse;
import com.aserto.directory.writer.v2.DeleteRelationResponse;
import com.aserto.directory.writer.v2.SetObjectResponse;
import com.aserto.directory.writer.v2.SetRelationResponse;
import com.google.protobuf.Struct;

public interface DirectoryClient {
    public GetObjectResponse getObject(String type, String id, boolean withRelations);
    public GetObjectsResponse getObjects(String type, int pageSize, String pageToken);
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType,
                                           String subjectId, String subjectRelation, boolean withObjects);
    public GetRelationsResponse getRelations(String objectType, String objectId, String relationName,
                                             String subjectType, String subjectId, String subjectRelation,
                                             boolean withObjects, int pageSize, String pageToken);
    public CheckRelationResponse checkRelation(String objectType, String objectId, String relationName,
                                               String subjectType, String subjectId, boolean trace);
    public CheckPermissionResponse checkPermission(String objectType, String objectId, String relationName,
                                                   String subjectType, String subjectId, boolean trace);

    public CheckResponse check(String objectType, String objectId, String relationName,
                               String subjectType, String subjectId, boolean trace);

    public SetObjectResponse setObject(String type, String id, String displayName, Struct properties, String hash);
    public DeleteObjectResponse deleteObject(String type, String id, boolean withRelations);
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName,
                                           String subjectType, String subjectId, String subjectRelation, String hash);
    public DeleteRelationResponse deleteRelation(String objectType, String objectId, String relationName,
                                                 String subjectType, String subjectId, String subjectRelation);
}
