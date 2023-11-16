package com.aserto;

import com.aserto.directory.model.v3.DeleteManifestResponse;
import com.aserto.directory.model.v3.GetManifestResponse;
import com.aserto.directory.model.v3.SetManifestRequest;
import com.aserto.directory.model.v3.SetManifestResponse;

public interface DirectoryClientModel {
    public GetManifestResponse getManifest();
    public SetManifestResponse setManifest(String manifest) throws InterruptedException;
    public DeleteManifestResponse deleteManifest();
}
