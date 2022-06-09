package org.schemagen.model;

public class SimpleTfVar extends TfVar {

    private final TfVarType type;

    private final String defaultVal;

    public SimpleTfVar(String name, String description, TfVarType type, String defaultVal) {
        super(name, description);
        this.type = type;
        this.defaultVal = defaultVal;
    }

    public TfVarType getType() {
        return type;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    @Override
    public String toString() {
        return "SimpleTfVar{" +
                "type=" + type +
                ", defaultVal='" + defaultVal + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
