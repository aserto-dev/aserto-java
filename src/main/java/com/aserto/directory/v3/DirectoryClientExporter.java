package com.aserto.directory.v3;

import com.aserto.directory.exporter.v3.ExportResponse;
import com.aserto.directory.exporter.v3.Option;
import com.google.protobuf.Timestamp;

import java.util.Iterator;

public interface DirectoryClientExporter {
    Iterator<ExportResponse> exportData(Option options) throws UninitilizedClientException;
    Iterator<ExportResponse> exportData(Option options, Timestamp startFrom) throws UninitilizedClientException;
}
