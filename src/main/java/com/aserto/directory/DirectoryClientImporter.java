package com.aserto.directory;

import com.aserto.model.ImportElement;
import java.util.stream.Stream;

public interface DirectoryClientImporter {
    void importData(Stream<ImportElement> importStream) throws InterruptedException;
}