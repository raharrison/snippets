import hcl2
import json
from io import StringIO


# memory efficient in-place string concat
class StringBuilder:
    _file_str = None

    def __init__(self):
        self._file_str = StringIO()

    def append(self, str):
        self._file_str.write(str)

    def __str__(self):
        return self._file_str.getvalue()


# generate a full set of tfvars from variables defined in provided file
def gen_tfvars(vars_path):
    with open(vars_path, 'r') as fi:
        raw_tf = hcl2.load(fi)

    tfvars = {}

    # extract defaults from all variable definitions
    for var_elem in raw_tf["variable"]:
        name = next(iter(var_elem))
        if "default" in var_elem[name]:
            defaults = var_elem[name]["default"]
            if type(defaults) is dict:
                tfvars[name] = dict(sorted(var_elem[name]["default"].items()))
            else:
                tfvars[name] = var_elem[name]["default"]
        else:
            tfvars[name] = None

    # sort tfvars by key
    return dict(sorted(tfvars.items()))


# create human readable element title from a variable name
def tfvar_to_title(name):
    return name.replace("_", " ").title()


# convert terraform var type to json schema type
def obj_to_schema_type(obj):
    if type(obj) is bool:
        return "boolean"
    elif type(obj) is int:
        return "integer"
    elif type(obj) is str:
        return "string"
    elif type(obj) is list:
        return "array"
    else:
        return "null"


# create array schema element (assuming all items are of same type)
def create_array_schema_property(value):
    item_type = "string"
    if len(value) > 0:
        item_type = obj_to_schema_type(value[0])
    return {"type": "array", "items": {"type": item_type}}


# create schema element for simple non-object types
def create_schema_property(name, value):
    type = obj_to_schema_type(value)
    if type == "array":
        prop = create_array_schema_property(value)
    else:
        prop = {"type": type}
    prop["title"] = tfvar_to_title(name)
    prop["description"] = "todo"
    prop["default"] = value
    return prop


# json schema object definition with nested properties
def create_schema_object(schema, tfvar):
    schema["type"] = "object"
    schema["description"] = "todo"
    properties = {}
    schema["properties"] = properties
    for var_name in tfvar:
        if type(tfvar[var_name]) is dict:
            schema_object = {}
            schema_object["title"] = tfvar_to_title(var_name)
            create_schema_object(schema_object, tfvar[var_name])
            properties[var_name] = schema_object
        else:
            properties[var_name] = create_schema_property(
                var_name, tfvar[var_name])


# create a json schema from a set of tfvars
def create_schema(tf_vars):
    schema = {"$schema": "http://json-schema.org/draft-07/schema#"}
    create_schema_object(schema, tf_vars)
    return schema


# create documentation page based on generated json schema
def generate_md(md, schema, path):
    type = schema["type"]
    if type == "object":
        # table showing all object properties
        md.append("""
|Property|Type|Description|Default|
|---|---|---|---|
""")
        for property in schema["properties"]:
            prop = schema["properties"][property]
            type = prop["type"]
            desc = prop["description"]
            default = prop["default"] if "default" in prop else ""
            md.append(f"|**{property}**|`{type}`|{desc}|{default}|\n")

        subpath = path
        for property in schema["properties"]:
            title = f"{path}.{property}".strip(".")
            if schema["properties"][property]["type"] == "object":
                subpath = f"{path}.{property}"
            header = min(3, title.count(".") + 2) * "#"
            md.append(f"""\n{header} {title}\n""")
            generate_md(md, schema["properties"][property], subpath)
    else:
        md.append(f"""
{schema["description"]}

* **Type**: `{type}`
* **Default**: {schema.get("default")}
""")


if __name__ == "__main__":
    vars = gen_tfvars("hcl2.tf")
    vars_json = json.dumps(vars, indent=4)
    with open("terraform.tfvars.json", 'w') as fi:
        fi.write(vars_json)

    schema = create_schema(vars)
    schema_json = json.dumps(schema, indent=4)
    with open("tfvars.schema.json", 'w') as fi:
        fi.write(schema_json)

    markdown = StringBuilder()
    markdown.append("# Terraform Var Docs\n")
    generate_md(markdown, schema, "")
    with open("docs.md", 'w') as fi:
        fi.write(str(markdown))
