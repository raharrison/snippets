package org.schemagen.model;

public class RawTfVar {

    private final String name;
    private final String type;
    private final String description;

    private final String defaultVal;

    public RawTfVar(String name, String type, String description, String defaultVal) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.defaultVal = defaultVal;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    @Override
    public String toString() {
        return "RawTfVar{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", defaultVal='" + defaultVal + '\'' +
                '}';
    }
}
