package org.schemagen;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class DocsBuilder {

    public String generateDocsPage(Map<String, Object> schema) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Terraform Var Docs\n");
        appendDocs(markdown, schema, "");
        return markdown.toString();
    }

    @SuppressWarnings("unchecked")
    private void appendDocs(StringBuilder md, Map<String, Object> schema, String path) {
        String type = schema.get("type").toString();
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        if (type.equals("object") && !properties.isEmpty()) {
            md.append("\n|Property|Type|Description|Default|\n|---|---|---|---|\n");

            properties.forEach((property, v) -> {
                Map<String, Object> prop = (Map<String, Object>) v;
                String propType = createTypeField(prop);
                String desc = prop.getOrDefault("description", "").toString();
                String defaultVal = prop.getOrDefault("default", "").toString();
                md.append(String.format("|**%s**|`%s`|%s|%s|\n", property, propType, desc, defaultVal));
            });

            String subpath = path;
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                Map<String, Object> prop = (Map<String, Object>) entry.getValue();
                String propType = createTypeField(prop);
                String title = StringUtils.strip(path + "." + entry.getKey(), ".");
                if (propType.equals("object")) {
                    subpath = path + "." + entry.getKey();
                }
                String header = StringUtils.repeat('#', Math.min(3, StringUtils.countMatches(title, '.') + 2));
                md.append(String.format("\n%s %s\n", header, title));
                appendDocs(md, prop, subpath);
            }
        } else {
            md.append(String.format("\n%s\n* **Type**: `%s`\n",
                    schema.getOrDefault("description", "").toString(), createTypeField(schema)));
            String defaultVal = schema.getOrDefault("default", "").toString();
            if (StringUtils.isNotEmpty(defaultVal)) {
                md.append(String.format("* **Default**: %s\n", defaultVal));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private String createTypeField(Map<String, Object> property) {
        String baseType = property.get("type").toString();
        if (baseType.equals("array")) {
            Map<String, Object> properties = (Map<String, Object>) property.get("items");
            return String.format("list(%s)", properties.getOrDefault("type", "string"));
        } else if (baseType.equals("object")) {
            Map<String, Object> properties = (Map<String, Object>) property.get("properties");
            if (properties.isEmpty()) {
                return "map(string)";
            }
        }
        return baseType;
    }

}
