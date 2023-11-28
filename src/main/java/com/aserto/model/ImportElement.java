package com.aserto.model;

import com.aserto.directory.common.v3.Object;
import com.aserto.directory.common.v3.Relation;

public class ImportElement {
    private Object object;
    private Relation relation;

    public ImportElement(Object object) {
        this.object = object;
    }

    public  ImportElement(Relation relation){
        this.relation = relation;
    }

    public Object getObject() {
        return object;
    }

    public Relation getRelation() {
        return relation;
    }
}
