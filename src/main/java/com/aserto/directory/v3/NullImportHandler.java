package com.aserto.directory.v3;

class NullImportHandler implements ImportHandler {
    @Override
    public void onProgress(ImportEvent.Counter counter) {}

    @Override
    public void onError(ImportEvent.Error error) {}
}
