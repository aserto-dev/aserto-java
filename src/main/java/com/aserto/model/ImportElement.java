package com.aserto.model;

import com.aserto.directory.common.v3.Object;
import com.aserto.directory.common.v3.Relation;
import com.aserto.directory.importer.v3.Opcode;

public class ImportElement {
    private Object object;
    private Relation relation;
    private Opcode opcode;

    public ImportElement(Object object, Opcode opcode) {
        this.object = object;
        this.opcode = opcode;
    }

    public  ImportElement(Relation relation, Opcode opcode) {
        this.relation = relation;
        this.opcode = opcode;
    }

    public Object getObject() {
        return object;
    }

    public Relation getRelation() {
        return relation;
    }

    public Opcode getOpcode() {
        return opcode;
    }
}
