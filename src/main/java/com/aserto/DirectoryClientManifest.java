package com.aserto;

import com.aserto.directory.model.v3.DeleteManifestRequest;
import com.aserto.directory.model.v3.GetManifestResponse;
import com.aserto.directory.model.v3.SetManifestRequest;

public interface DirectoryClientManifest {
    public GetManifestResponse getManifest();
    public SetManifestRequest setManifest(String manifest);
    public DeleteManifestRequest deleteManifest();
}
