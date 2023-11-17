package com.aserto;

import com.aserto.directory.model.v3.DeleteManifestResponse;
import com.aserto.directory.model.v3.GetManifestResponse;

public interface DirectoryClientModel {
    public GetManifestResponse getManifest();
    public void setManifest(String manifest) throws InterruptedException;
    public DeleteManifestResponse deleteManifest();
}
