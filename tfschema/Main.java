package org.schemagen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.*;
import org.jsonschema2pojo.rules.RuleFactory;
import org.schemagen.model.RawTfVar;
import org.schemagen.model.TfVar;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Main {

    private static final String VAR_PATH = "terraform/variables.tf.json";
    private static final String LOCALS_PATH = "terraform/locals.tf.json";
    private static final String SCHEMA_PATH = "terraform/pattern.schema.json";
    private static final String MODEL_PATH = "terraform/generated";
    private static final String DOCS_PATH = "terraform/docs.md";

    public static void main(String[] args) throws Exception {

        HclReader reader = new HclReader();
        List<RawTfVar> rawVars = reader.readRawTfVars(Path.of(VAR_PATH));
        Map<String, Map<String, String>> defaults = reader.readTfLocalsToDefaults(Path.of(LOCALS_PATH));
        List<TfVar> tfVars = reader.convertTfVars(rawVars, defaults);

        SchemaBuilder schemaBuilder = new SchemaBuilder();
        Map<String, Object> schema = schemaBuilder.createSchemaFromVars(tfVars);

        ObjectMapper mapper = new ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);
        String schemaJson = mapper.writeValueAsString(schema);
        Files.writeString(Path.of(SCHEMA_PATH), schemaJson);

        JCodeModel codeModel = new JCodeModel();
        GenerationConfig config = new DefaultGenerationConfig() {
            @Override
            public boolean isIncludeAdditionalProperties() {
                return false;
            }
        };
        SchemaMapper schemaMapper = new SchemaMapper(new RuleFactory(config, new Jackson2Annotator(config), new SchemaStore()), new SchemaGenerator());
        schemaMapper.generate(codeModel, "TerraformParams", "com.example", schemaJson);
        codeModel.build(new File(MODEL_PATH));

        DocsBuilder docsBuilder = new DocsBuilder();
        String docs = docsBuilder.generateDocsPage(schema);
        Files.writeString(Path.of(DOCS_PATH), docs);

    }

}