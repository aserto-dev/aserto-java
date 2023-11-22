package com.aserto.directory.v3;

import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.reader.v3.*;

import java.util.List;

public interface DirectoryClientReader {
    public GetObjectResponse getObject(String type, String id);
    public GetObjectResponse getObject(String type, String id, boolean withRelations);
    public GetObjectsResponse getObjects(String type);
    public GetObjectsResponse getObjects(String type, int pageSize, String pageToken);
    public GetObjectManyResponse getObjectManyRequest(List<ObjectIdentifier> objectIdentifiers);
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName,
                                           String subjectType, String subjectId);
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType,
                                           String subjectId, String subjectRelation);
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType,
                                           String subjectId, String subjectRelation, boolean withObjects);
    public GetRelationsResponse getRelations(GetRelationsRequest relationsRequest);

    public CheckPermissionResponse checkPermission(String objectType, String objectId, String subjectType,
                                                   String subjectId, String permissionName);
    public CheckPermissionResponse checkPermission(String objectType, String objectId,
                                                   String subjectType, String subjectId, String permissionName, boolean trace);
    public CheckRelationResponse checkRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId);
    public CheckRelationResponse checkRelation(String objectType, String objectId, String relationName,
                                               String subjectType, String subjectId, boolean trace);
    public CheckResponse check(String objectType, String objectId, String relationName, String subjectType, String subjectId);
    public CheckResponse check(String objectType, String objectId, String relationName,
                               String subjectType, String subjectId, boolean trace);
    public GetGraphResponse getGraph(String anchorType, String anchorId, String objectType, String objectId,
                                     String relation, String subjectType, String subjectId, String subjectRelation);
}
