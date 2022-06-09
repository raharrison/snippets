package org.schemagen;

import org.apache.commons.lang3.StringUtils;
import org.schemagen.model.TfVarType;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HclUtils {

    private static final Pattern TF_PAIR_PATTERN = Pattern.compile("(.+?)=(.+)");

    public static String removeHclInterp(String str) {
        return StringUtils.removeEnd(StringUtils.removeStart(str, "${"), "}");
    }

    public static String removeTypeWrap(String str, String wrap) {
        return StringUtils.removeEnd(StringUtils.removeStart(str, wrap + "("), ")");
    }

    public static TfVarType parseTfVarType(String type) {
        boolean optional = false;
        if (type.startsWith("optional")) {
            optional = true;
            type = removeTypeWrap(type, "optional");
        }
        if (type.startsWith("map")) {
            return new TfVarType(optional, "map", removeTypeWrap(type, "map"));
        } else if (type.startsWith("list")) {
            return new TfVarType(optional, "list", removeTypeWrap(type, "list"));
        } else {
            return new TfVarType(optional, type);
        }
    }

    public static Map<String, String> parseTfPairs(String element) {
        Map<String, String> vars = new HashMap<>();
        if(StringUtils.isEmpty(element)) {
            return vars;
        }
        Matcher matcher = TF_PAIR_PATTERN.matcher(element);
        while (matcher.find()) {
            String name = matcher.group(1).trim();
            String varType = StringUtils.strip(matcher.group(2).trim(), "\"");
            vars.put(name, varType);
        }
        return vars;
    }

}
