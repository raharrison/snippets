package org.schemagen.model;

public abstract class TfVar {

    protected final String name;
    protected final String description;

    public TfVar(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
