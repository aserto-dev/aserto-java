package com.aserto;

import com.aserto.directory.reader.v3.*;

public interface DirectoryClientReader {
    public GetObjectResponse getObject(String type, String id, boolean withRelations);
    public GetObjectsResponse getObjects(String type, int pageSize, String pageToken);
    public GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType,
                                           String subjectId, String subjectRelation, boolean withObjects);
    public GetRelationsResponse getRelations(String objectType, String objectId, String relationName,
                                             String subjectType, String subjectId, String subjectRelation,
                                             boolean withObjects, int pageSize, String pageToken);
    public CheckRelationResponse checkRelation(String objectType, String objectId, String relationName,
                                               String subjectType, String subjectId, boolean trace);
    public CheckPermissionResponse checkPermission(String objectType, String objectId,
                                                   String subjectType, String subjectId, String permissionName, boolean trace);

    public CheckResponse check(String objectType, String objectId, String relationName,
                               String subjectType, String subjectId, boolean trace);
}
