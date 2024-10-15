package com.aserto.directory.v3;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.aserto.directory.importer.v3.ImportResponse;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;


public class ImportObserver implements StreamObserver<ImportResponse> {
    private final ImportHandler handler;
    private final CountDownLatch latch = new CountDownLatch(1);
    private Status status = Status.OK;

    public ImportObserver(ImportHandler handler) {
        this.handler = handler;
    }

    public Status getStatus() {
        return status;
    }

    public Status await(long timeout, TimeUnit unit) throws InterruptedException {
        if (latch.await(timeout, unit))  {
            return status;
        }
        return Status.CANCELLED;
    }

    @Override
    public void onNext(ImportResponse importResponse) {
        if (importResponse.hasStatus()) {
            handler.onError(new ImportEvent.Error(importResponse.getStatus()));
        } else if (importResponse.hasCounter()) {
            handler.onProgress(new ImportEvent.Counter(importResponse.getCounter()));
        }
    }

    @Override
    public void onError(Throwable throwable) {
        status = io.grpc.Status.fromThrowable(throwable);
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        latch.countDown();
    }
}
