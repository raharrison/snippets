package org.schemagen;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.schemagen.model.ObjectTfVar;
import org.schemagen.model.RawTfVar;
import org.schemagen.model.SimpleTfVar;
import org.schemagen.model.TfVar;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class HclReader {

    public List<RawTfVar> readRawTfVars(Path varFile) throws IOException {
        JsonNode tree = readHclFile(varFile);
        JsonNode variables = tree.get("variable");
        List<RawTfVar> rawTfVars = new ArrayList<>();
        for (Iterator<Map.Entry<String, JsonNode>> it = variables.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> field = it.next();
            JsonNode var = field.getValue().get(0);
            String name = field.getKey();
            String type = var.get("type").asText();
            String desc = var.has("description") ? var.get("description").asText() : null;
            String defaultVal = var.has("default") ? var.get("default").asText() : null;
            rawTfVars.add(new RawTfVar(name, type, desc, defaultVal));
        }
        return rawTfVars;
    }

    private JsonNode readHclFile(Path path) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(path.toFile());
    }

    public List<TfVar> convertTfVars(List<RawTfVar> rawTfVars, Map<String, Map<String, String>> defaults) {
        List<TfVar> tfVars = new ArrayList<>();
        for (RawTfVar rawVar : rawTfVars) {
            String type = HclUtils.removeHclInterp(rawVar.getType().toLowerCase()).trim();
            if (type.startsWith("object")) {
                tfVars.add(createObjectTfVar(rawVar, type, defaults.getOrDefault(rawVar.getName(), new HashMap<>())));
            } else {
                tfVars.add(new SimpleTfVar(rawVar.getName().trim(), rawVar.getDescription().trim(),
                        HclUtils.parseTfVarType(type), StringUtils.trim(rawVar.getDefaultVal())));
            }
        }
        return tfVars;
    }

    private ObjectTfVar createObjectTfVar(RawTfVar base, String type, Map<String, String> defaults) {
        String rawType = HclUtils.removeTypeWrap(type, "object");
        Map<String, String> vars = HclUtils.parseTfPairs(rawType);
        Map<String, String> descriptions = HclUtils.parseTfPairs(base.getDescription());

        List<TfVar> children = vars.entrySet().stream()
                .map(entry -> new SimpleTfVar(entry.getKey(), descriptions.get(entry.getKey()),
                        HclUtils.parseTfVarType(entry.getValue()), defaults.get(entry.getKey())))
                .collect(Collectors.toList());

        String objectDesc = base.getDescription() != null ? base.getDescription().split("\n")[0].trim() : null;
        return new ObjectTfVar(base.getName(), objectDesc, children);
    }

    public Map<String, Map<String, String>> readTfLocalsToDefaults(Path localsFile) throws IOException {
        Map<String, Map<String, String>> defaults = new HashMap<>();
        JsonNode jsonNode = readHclFile(localsFile);
        JsonNode locals = jsonNode.get("locals");
        for (Iterator<JsonNode> it = locals.elements(); it.hasNext(); ) {
            JsonNode item = it.next();
            for (Iterator<Map.Entry<String, JsonNode>> itt = item.fields(); itt.hasNext(); ) {
                Map.Entry<String, JsonNode> field = itt.next();
                Map<String, String> itemDefaults = HclUtils.parseTfPairs(HclUtils.removeHclInterp(field.getValue().textValue()));
                defaults.put(field.getKey(), itemDefaults);
            }
        }
        return defaults;
    }

}
