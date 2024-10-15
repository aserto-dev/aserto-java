package com.aserto.model;

import com.aserto.directory.common.v3.Object;
import com.aserto.directory.common.v3.Relation;
import com.aserto.directory.importer.v3.Opcode;

public class ImportElement {
    private final Object object;
    private final Relation relation;
    private final Opcode opcode;

    public ImportElement(Object object, Opcode opcode) {
        this.object = object;
        this.opcode = opcode;
      this.relation = null;
    }

    public  ImportElement(Relation relation, Opcode opcode) {
        this.relation = relation;
        this.opcode = opcode;
      this.object = null;
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
