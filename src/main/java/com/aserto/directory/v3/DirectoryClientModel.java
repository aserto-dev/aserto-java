package com.aserto.directory.v3;

import com.aserto.directory.model.v3.DeleteManifestResponse;
import com.aserto.directory.model.v3.GetManifestResponse;

public interface DirectoryClientModel {
    GetManifestResponse getManifest() throws UninitilizedClientException;
    void setManifest(String manifest) throws InterruptedException, UninitilizedClientException;
    DeleteManifestResponse deleteManifest() throws UninitilizedClientException;
}
