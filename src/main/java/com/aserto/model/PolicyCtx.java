package com.aserto.model;

public class PolicyCtx {
    private String name;
    private String label;
    private String path;
    private String[] decisions;

    public PolicyCtx() {
    }

    public PolicyCtx(String name, String label, String path, String[] decisions) {
        this.name = name;
        this.label = label;
        this.path = path;
        this.decisions = decisions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String[] getDecisions() {
        return decisions;
    }

    public void setDecisions(String[] decisions) {
        this.decisions = decisions;
    }
}
