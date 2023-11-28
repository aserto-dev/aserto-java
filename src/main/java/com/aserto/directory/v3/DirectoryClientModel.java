package com.aserto.directory.v3;

import com.aserto.directory.model.v3.DeleteManifestResponse;
import com.aserto.directory.model.v3.GetManifestResponse;

public interface DirectoryClientModel {
    GetManifestResponse getManifest();
    void setManifest(String manifest) throws InterruptedException;
    DeleteManifestResponse deleteManifest();
}
