package org.schemagen;

import org.apache.commons.lang3.StringUtils;
import org.schemagen.model.ObjectTfVar;
import org.schemagen.model.SimpleTfVar;
import org.schemagen.model.TfVar;
import org.schemagen.model.TfVarType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SchemaBuilder {

    public Map<String, Object> createSchemaFromVars(List<TfVar> vars) {
        Map<String, Object> schema = createMap();
        schema.put("$schema", "https://json-schema.org/draft-07/schema#");
        schema.put("type", "object");
        schema.put("additionalProperties", false);
        schema.put("properties", createProperties(vars));
        return schema;
    }

    private Map<String, Object> createProperties(List<TfVar> vars) {
        Map<String, Object> properties = createMap();
        for (TfVar var : vars) {
            if (var instanceof SimpleTfVar) {
                properties.put(var.getName(), createSimpleObject((SimpleTfVar) var));
            } else {
                properties.put(var.getName(), createNestedObject((ObjectTfVar) var));
            }
        }
        return properties;
    }

    private Map<String, Object> createSimpleObject(SimpleTfVar tfVar) {
        Map<String, Object> element = createMap();
        element.putAll(tfVarTypeToSchemaType(tfVar.getType()));
        element.put("title", tfVarToTitle(tfVar.getName()));
        if (tfVar.getDescription() != null) {
            element.put("description", tfVar.getDescription());
        }
        if (StringUtils.isNotEmpty(tfVar.getDefaultVal())) {
            Object defaultVal = tfVar.getType().getType().equals("bool") ? Boolean.parseBoolean(tfVar.getDefaultVal()) : tfVar.getDefaultVal();
            element.put("default", defaultVal);
        }
        return element;
    }

    private Map<String, Object> createNestedObject(ObjectTfVar tfVar) {
        Map<String, Object> element = createMap();
        element.put("type", "object");
        element.put("additionalProperties", false);
        if (tfVar.getDescription() != null) {
            element.put("description", tfVar.getDescription());
        }
        element.put("properties", createProperties(tfVar.getChildren()));
        element.put("required", tfVar.getChildren().stream()
                .filter(v -> v instanceof SimpleTfVar && !((SimpleTfVar) v).getType().isOptional())
                .map(TfVar::getName)
                .collect(Collectors.toList()));
        return element;
    }

    private Map<String, Object> tfVarTypeToSchemaType(TfVarType tfVarType) {
        Map<String, Object> element = createMap();
        String type = tfVarType.getType();
        if (tfVarType.getComplexType().equalsIgnoreCase("list")) {
            Map<String, String> arrayType = createMap();
            arrayType.put("type", type);
            element.put("type", "array");
            element.put("items", arrayType);
        } else if (tfVarType.getComplexType().equalsIgnoreCase("map")) {
            element.put("type", "object");
            element.put("properties", createMap());
        } else if (type.equalsIgnoreCase("bool")) {
            element.put("type", "boolean");
        } else if (type.equalsIgnoreCase("string")) {
            element.put("type", "string");
        } else if (type.equalsIgnoreCase("number")) {
            element.put("type", "number");
        } else {
            element.put("type", "string");
        }
        return element;
    }

    private String tfVarToTitle(String str) {
        return Utils.toTitleCase(str.replace("_", " "));
    }

    private <K, V> Map<K, V> createMap() {
        return new LinkedHashMap<>();
    }

}
