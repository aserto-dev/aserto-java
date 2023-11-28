package com.aserto.directory.v3;

import com.aserto.directory.common.v3.Object;
import com.aserto.directory.common.v3.ObjectIdentifier;
import com.aserto.directory.common.v3.Relation;

public class Directory {

    public static Object buildObject(String type, String id) {
        return Object.newBuilder().setType(type).setId(id).build();
    }

    public static ObjectIdentifier buildObjectIdentifier(String type, String id) {
        return ObjectIdentifier.newBuilder().setObjectType(type).setObjectId(id).build();
    }

    public static Relation buildRelation(String objectType, String objectId, String relation, String subjectType, String subjectId) {
        return Relation.newBuilder()
                .setObjectType(objectType)
                .setObjectId(objectId)
                .setRelation(relation)
                .setSubjectType(subjectType)
                .setSubjectId(subjectId)
                .build();
    }
}
