package com.aserto.directory;

import com.aserto.directory.common.v3.ObjectIdentifier;

import java.util.Iterator;
import java.util.List;

class ObjectIdentifierList implements Iterable<ObjectIdentifier> {
    private List<ObjectIdentifier> objects;

    public ObjectIdentifierList(List<ObjectIdentifier> objects) {
        this.objects = objects;
    }

    @Override
    public Iterator<ObjectIdentifier> iterator() {
        return objects.iterator();
    }
}
