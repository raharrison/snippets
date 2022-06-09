package org.schemagen.model;

import java.util.List;

public class ObjectTfVar extends TfVar {

    private final List<TfVar> children;

    public ObjectTfVar(String name, String description, List<TfVar> children) {
        super(name, description);
        this.children = children;
    }

    public List<TfVar> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return "ObjectTfVar{" +
                "children=" + children +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
