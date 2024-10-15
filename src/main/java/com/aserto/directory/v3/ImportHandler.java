package com.aserto.directory.v3;

/**
 * Receiver of directory import status events.
 */
public interface ImportHandler {
    void onProgress(ImportEvent.Counter counter);
    void onError(ImportEvent.Error error);
}
