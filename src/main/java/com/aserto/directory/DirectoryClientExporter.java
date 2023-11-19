package com.aserto.directory;

import com.aserto.directory.exporter.v3.ExportResponse;
import com.google.protobuf.Timestamp;

import java.util.Iterator;

public interface DirectoryClientExporter {
    Iterator<ExportResponse> exportData(int options, Timestamp startFrom);
}
