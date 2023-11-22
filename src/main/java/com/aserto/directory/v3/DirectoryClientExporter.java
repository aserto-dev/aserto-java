package com.aserto.directory.v3;

import com.aserto.directory.exporter.v3.ExportResponse;
import com.google.protobuf.Timestamp;

import java.util.Iterator;

public interface DirectoryClientExporter {
    Iterator<ExportResponse> exportData(int options, Timestamp startFrom);
}
