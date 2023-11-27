package com.aserto.directory.v3;

import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.reader.v3.*;

import java.util.List;

public interface DirectoryClientReader {
    GetObjectResponse getObject(String type, String id);
    GetObjectResponse getObject(String type, String id, boolean withRelations);
    GetObjectsResponse getObjects(String type);
    GetObjectsResponse getObjects(String type, int pageSize, String pageToken);
    GetObjectManyResponse getObjectManyRequest(List<ObjectIdentifier> objectIdentifiers);
    GetRelationResponse getRelation(String objectType, String objectId, String relationName,
                                           String subjectType, String subjectId);
    GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType,
                                           String subjectId, String subjectRelation);
    GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType,
                                           String subjectId, String subjectRelation, boolean withObjects);
    GetRelationsResponse getRelations(GetRelationsRequest relationsRequest);

    CheckPermissionResponse checkPermission(String objectType, String objectId, String subjectType,
                                                   String subjectId, String permissionName);
    CheckPermissionResponse checkPermission(String objectType, String objectId,
                                                   String subjectType, String subjectId, String permissionName, boolean trace);
    CheckRelationResponse checkRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId);
    CheckRelationResponse checkRelation(String objectType, String objectId, String relationName,
                                               String subjectType, String subjectId, boolean trace);
    CheckResponse check(String objectType, String objectId, String relationName, String subjectType, String subjectId);
    CheckResponse check(String objectType, String objectId, String relationName,
                               String subjectType, String subjectId, boolean trace);
    GetGraphResponse getGraph(GetGraphRequest getGraphRequest);
}
