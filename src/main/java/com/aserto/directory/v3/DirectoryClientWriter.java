package com.aserto.directory.v3;

import com.aserto.directory.writer.v3.DeleteObjectResponse;
import com.aserto.directory.writer.v3.DeleteRelationResponse;
import com.aserto.directory.writer.v3.SetObjectResponse;
import com.aserto.directory.writer.v3.SetRelationResponse;
import com.google.protobuf.Struct;

public interface DirectoryClientWriter {
    public SetObjectResponse setObject(String type, String id) throws UninitilizedClientException;
    public SetObjectResponse setObject(String type, String id, String displayName, Struct properties, String hash) throws UninitilizedClientException;
    public DeleteObjectResponse deleteObject(String type, String id) throws UninitilizedClientException;
    public DeleteObjectResponse deleteObject(String type, String id, boolean withRelations) throws UninitilizedClientException;
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName,
                                           String subjectType, String subjectId) throws UninitilizedClientException;
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName,
                                           String subjectType, String subjectId, String subjectRelation) throws UninitilizedClientException;
    public SetRelationResponse setRelation(String objectType, String objectId, String relationName,
                                           String subjectType, String subjectId, String subjectRelation, String hash) throws UninitilizedClientException;
    public DeleteRelationResponse deleteRelation(String objectType, String objectId, String relationName,
                                                 String subjectType, String subjectId) throws UninitilizedClientException;
    public DeleteRelationResponse deleteRelation(String objectType, String objectId, String relationName,
                                                 String subjectType, String subjectId, String subjectRelation) throws UninitilizedClientException;
}
