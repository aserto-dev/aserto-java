package com.aserto.model;

import com.aserto.authorizer.v2.api.IdentityType;

public class IdentityCtx {
    private String identity;
    private IdentityType identityType;

    public IdentityCtx() {
    }

    public IdentityCtx(String identity, IdentityType identityType) {
        this.identity = identity;
        this.identityType = identityType;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public IdentityType getIdentityType() {
        return identityType;
    }

    public void setIdentityType(IdentityType identityType) {
        this.identityType = identityType;
    }
}
