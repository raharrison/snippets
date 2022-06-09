package org.schemagen.model;

public class TfVarType {

    private final boolean optional;
    private final String complexType;
    private final String type;

    public TfVarType(boolean optional, String type) {
        this.optional = optional;
        this.complexType = "";
        this.type = type;
    }

    public TfVarType(boolean optional, String complexType, String type) {
        this.optional = optional;
        this.complexType = complexType;
        this.type = type;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getComplexType() {
        return complexType;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "TfVarType{" +
                "optional=" + optional +
                ", complexType='" + complexType + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
