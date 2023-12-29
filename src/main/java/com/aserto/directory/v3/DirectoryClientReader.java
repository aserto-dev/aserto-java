package com.aserto.directory.v3;

import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.reader.v3.*;

import java.util.List;

public interface DirectoryClientReader {
    GetObjectResponse getObject(String type, String id) throws UninitilizedClientException;
    GetObjectResponse getObject(String type, String id, boolean withRelations) throws UninitilizedClientException;
    GetObjectsResponse getObjects(String type) throws UninitilizedClientException;
    GetObjectsResponse getObjects(String type, int pageSize, String pageToken) throws UninitilizedClientException;
    GetObjectManyResponse getObjectManyRequest(List<ObjectIdentifier> objectIdentifiers) throws UninitilizedClientException;
    GetRelationResponse getRelation(String objectType, String objectId, String relationName,
                                           String subjectType, String subjectId) throws UninitilizedClientException;
    GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType,
                                           String subjectId, String subjectRelation) throws UninitilizedClientException;
    GetRelationResponse getRelation(String objectType, String objectId, String relationName, String subjectType,
                                           String subjectId, String subjectRelation, boolean withObjects) throws UninitilizedClientException;
    GetRelationsResponse getRelations(GetRelationsRequest relationsRequest) throws UninitilizedClientException;

    CheckPermissionResponse checkPermission(String objectType, String objectId, String subjectType,
                                                   String subjectId, String permissionName) throws UninitilizedClientException;
    CheckPermissionResponse checkPermission(String objectType, String objectId,
                                                   String subjectType, String subjectId, String permissionName, boolean trace) throws UninitilizedClientException;
    CheckRelationResponse checkRelation(String objectType, String objectId, String relationName, String subjectType, String subjectId) throws UninitilizedClientException;
    CheckRelationResponse checkRelation(String objectType, String objectId, String relationName,
                                               String subjectType, String subjectId, boolean trace) throws UninitilizedClientException;
    CheckResponse check(String objectType, String objectId, String relationName, String subjectType, String subjectId) throws UninitilizedClientException;
    CheckResponse check(String objectType, String objectId, String relationName,
                               String subjectType, String subjectId, boolean trace) throws UninitilizedClientException;
    GetGraphResponse getGraph(GetGraphRequest getGraphRequest);
}
