package org.example.server;

import com.aserto.DirectoryClient;
import com.aserto.directory.common.v2.Object;
import com.aserto.directory.common.v2.ObjectIdentifier;
import com.aserto.directory.common.v2.Relation;
import com.aserto.directory.common.v2.RelationIdentifier;
import com.aserto.directory.common.v2.RelationTypeIdentifier;
import com.aserto.directory.reader.v2.GetObjectRequest;
import com.aserto.directory.reader.v2.GetObjectResponse;
import com.aserto.directory.reader.v2.GetRelationRequest;
import com.aserto.directory.reader.v2.GetRelationResponse;

public class DirectoryHelper {
    private DirectoryClient directoryClient;
    public DirectoryHelper(DirectoryClient directoryClient) {
        this.directoryClient = directoryClient;
    }

    public Object getObject(String key) {
        Relation[] relations = getRelation(key);
        Object object = null;

        switch (relations.length) {
            case 0:
                throw new RuntimeException("No relations found");
            case 1:
                object = getObjectFromRelation(relations[0]);
                break;
            default:
                throw new RuntimeException("Too many relations found");
        }

        if (relations.length > 1) {
            throw new RuntimeException("Too many relations found");
        }

        return object;
    }

    private Relation[] getRelation(String key) {
        ObjectIdentifier subjectIdentifier = ObjectIdentifier.newBuilder().setType("user").build();
        ObjectIdentifier objectIdentifier = ObjectIdentifier.newBuilder().setKey(key).setType("identity").build();
        RelationTypeIdentifier relationTypeIdentifier = RelationTypeIdentifier.newBuilder().setName("identifier").setObjectType("identity").build();

        RelationIdentifier relationIdentifier = RelationIdentifier.newBuilder()
                .setSubject(subjectIdentifier)
                .setObject(objectIdentifier)
                .setRelation(relationTypeIdentifier).build();

        GetRelationRequest.Builder builder = GetRelationRequest.newBuilder();
        builder.setParam(relationIdentifier);
        GetRelationRequest request = builder.build();
        GetRelationResponse response = directoryClient.getReaderClient().getRelation(request);

        return response.getResultsList().toArray(new Relation[0]);
    }

    private Object getObjectFromRelation(Relation relation) {
        ObjectIdentifier objectIdentifier = relation.getSubject();
        GetObjectRequest.Builder objectBuilder = GetObjectRequest.newBuilder();
        objectBuilder.setParam(objectIdentifier);
        GetObjectRequest objectRequest = objectBuilder.build();
        GetObjectResponse objectResponse = directoryClient.getReaderClient().getObject(objectRequest);

        return objectResponse.getResult();
    }
}
