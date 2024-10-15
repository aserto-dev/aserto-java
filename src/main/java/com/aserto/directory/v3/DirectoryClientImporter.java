package com.aserto.directory.v3;

import java.util.stream.Stream;

import com.aserto.model.ImportElement;

import io.grpc.Status;

public interface DirectoryClientImporter {
    /**
     * Sets and/or deletes a stream of objects and relations without any feedback.
     *
     * @param importStream a stream of {@link ImportElement} objects representing the set of objects and/or relations
     *   to be added or deleted.
     * @throws InterruptedException if the operations is interrupted before completion.
     * @throws UninitilizedClientException if called on an uninitialized client.
     *
     * @deprecated use {@link #importData(Stream<ImportElement>, ImportHandler)} instead
     */
    @Deprecated
    void importData(Stream<ImportElement> importStream) throws InterruptedException, UninitilizedClientException;

    /**
     * Sets and/or deletes a stream of objects and relations.
     *
     * @param importStream a stream of {@link ImportElement} objects representing the set of objects and/or relations
     *   to be added or deleted.
     * @param handler a {@link ImportHandler} object to receive progress and error events.
     * @throws InterruptedException if the operations is interrupted before completion.
     * @throws UninitilizedClientException if called on an uninitialized client.
     */
    Status importData(Stream<ImportElement> importStream, ImportHandler handler) throws InterruptedException, UninitilizedClientException;
}
