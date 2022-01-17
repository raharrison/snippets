# Terraform Var Docs

|Property|Type|Description|Default|
|---|---|---|---|
|**api_gateway**|`object`|todo||
|**api_gateway_swagger_variables**|`null`|todo|None|
|**cloudfront_distribution**|`object`|todo||
|**cloudfront_distribution_default_cache_behavior**|`null`|todo|None|
|**cloudfront_distribution_ordered_cache_behaviors**|`null`|todo|None|
|**cloudfront_distribution_origins**|`null`|todo|None|
|**cloudfront_distribution_s3_origins**|`null`|todo|None|
|**cloudfront_enabled**|`boolean`|todo|False|
|**cloudfront_origin_access_identity_name**|`null`|todo|None|
|**environment_type**|`null`|todo|None|
|**lambda_function**|`object`|todo||
|**lambda_function_brands**|`array`|todo|['hsbc']|
|**logging_bucket_name**|`null`|todo|None|
|**region**|`null`|todo|None|
|**s3_bucket**|`object`|todo||
|**terraform_state_bucket_name**|`null`|todo|None|
|**waf_enabled**|`boolean`|todo|True|
|**waf_web_acl_arn**|`null`|todo|None|
|**web_acl_id**|`null`|todo|None|

## api_gateway

|Property|Type|Description|Default|
|---|---|---|---|
|**cloudwatch_metrics_enabled**|`boolean`|todo|False|
|**data_trace_enabled**|`boolean`|todo|False|
|**endpoint_type**|`string`|todo|PRIVATE|
|**key_enabled**|`boolean`|todo|True|
|**name**|`null`|todo|None|
|**stage_name**|`null`|todo|None|
|**usage_plan_burst_limit**|`integer`|todo|10000|
|**usage_plan_rate_limit**|`integer`|todo|5000|
|**xray_tracing_enabled**|`boolean`|todo|False|

### api_gateway.cloudwatch_metrics_enabled

todo

* **Type**: `boolean`
* **Default**: False

### api_gateway.data_trace_enabled

todo

* **Type**: `boolean`
* **Default**: False

### api_gateway.endpoint_type

todo

* **Type**: `string`
* **Default**: PRIVATE

### api_gateway.key_enabled

todo

* **Type**: `boolean`
* **Default**: True

### api_gateway.name

todo

* **Type**: `null`
* **Default**: None

### api_gateway.stage_name

todo

* **Type**: `null`
* **Default**: None

### api_gateway.usage_plan_burst_limit

todo

* **Type**: `integer`
* **Default**: 10000

### api_gateway.usage_plan_rate_limit

todo

* **Type**: `integer`
* **Default**: 5000

### api_gateway.xray_tracing_enabled

todo

* **Type**: `boolean`
* **Default**: False

## api_gateway_swagger_variables

todo

* **Type**: `null`
* **Default**: None

## cloudfront_distribution

|Property|Type|Description|Default|
|---|---|---|---|
|**acm_certificate_arn**|`null`|todo|None|
|**alias**|`null`|todo|None|
|**default_root_object**|`string`|todo|index.html|
|**geo_restriction_locations**|`array`|todo|[]|
|**geo_restriction_type**|`string`|todo|none|
|**is_enabled**|`boolean`|todo|True|
|**is_ipv6_enabled**|`boolean`|todo|False|
|**logging_include_cookies**|`boolean`|todo|False|
|**logging_prefix**|`null`|todo|None|
|**name**|`null`|todo|None|
|**price_class**|`string`|todo|PriceClass_200|

### cloudfront_distribution.acm_certificate_arn

todo

* **Type**: `null`
* **Default**: None

### cloudfront_distribution.alias

todo

* **Type**: `null`
* **Default**: None

### cloudfront_distribution.default_root_object

todo

* **Type**: `string`
* **Default**: index.html

### cloudfront_distribution.geo_restriction_locations

todo

* **Type**: `array`
* **Default**: []

### cloudfront_distribution.geo_restriction_type

todo

* **Type**: `string`
* **Default**: none

### cloudfront_distribution.is_enabled

todo

* **Type**: `boolean`
* **Default**: True

### cloudfront_distribution.is_ipv6_enabled

todo

* **Type**: `boolean`
* **Default**: False

### cloudfront_distribution.logging_include_cookies

todo

* **Type**: `boolean`
* **Default**: False

### cloudfront_distribution.logging_prefix

todo

* **Type**: `null`
* **Default**: None

### cloudfront_distribution.name

todo

* **Type**: `null`
* **Default**: None

### cloudfront_distribution.price_class

todo

* **Type**: `string`
* **Default**: PriceClass_200

## cloudfront_distribution_default_cache_behavior

todo

* **Type**: `null`
* **Default**: None

## cloudfront_distribution_ordered_cache_behaviors

todo

* **Type**: `null`
* **Default**: None

## cloudfront_distribution_origins

todo

* **Type**: `null`
* **Default**: None

## cloudfront_distribution_s3_origins

todo

* **Type**: `null`
* **Default**: None

## cloudfront_enabled

todo

* **Type**: `boolean`
* **Default**: False

## cloudfront_origin_access_identity_name

todo

* **Type**: `null`
* **Default**: None

## environment_type

todo

* **Type**: `null`
* **Default**: None

## lambda_function

|Property|Type|Description|Default|
|---|---|---|---|
|**canary_enabled**|`boolean`|todo|False|
|**description**|`null`|todo|None|
|**environment_variables**|`object`|todo||
|**execution_role_arn**|`null`|todo|None|
|**handler**|`null`|todo|None|
|**memory_size**|`integer`|todo|1024|
|**name**|`null`|todo|None|
|**permission_paths**|`array`|todo|[]|
|**runtime**|`null`|todo|None|
|**timeout**|`integer`|todo|300|
|**tracing_config_mode**|`string`|todo|Active|

### lambda_function.canary_enabled

todo

* **Type**: `boolean`
* **Default**: False

### lambda_function.description

todo

* **Type**: `null`
* **Default**: None

### lambda_function.environment_variables

|Property|Type|Description|Default|
|---|---|---|---|
|**first**|`string`|todo|value|
|**second**|`string`|todo|value|

### lambda_function.environment_variables.first

todo

* **Type**: `string`
* **Default**: value

### lambda_function.environment_variables.second

todo

* **Type**: `string`
* **Default**: value

### lambda_function.execution_role_arn

todo

* **Type**: `null`
* **Default**: None

### lambda_function.handler

todo

* **Type**: `null`
* **Default**: None

### lambda_function.memory_size

todo

* **Type**: `integer`
* **Default**: 1024

### lambda_function.name

todo

* **Type**: `null`
* **Default**: None

### lambda_function.permission_paths

todo

* **Type**: `array`
* **Default**: []

### lambda_function.runtime

todo

* **Type**: `null`
* **Default**: None

### lambda_function.timeout

todo

* **Type**: `integer`
* **Default**: 300

### lambda_function.tracing_config_mode

todo

* **Type**: `string`
* **Default**: Active

## lambda_function_brands

todo

* **Type**: `array`
* **Default**: ['hsbc']

## logging_bucket_name

todo

* **Type**: `null`
* **Default**: None

## region

todo

* **Type**: `null`
* **Default**: None

## s3_bucket

|Property|Type|Description|Default|
|---|---|---|---|
|**acl**|`string`|todo|private|
|**force_destroy**|`boolean`|todo|False|
|**name**|`null`|todo|None|
|**tags**|`object`|todo||
|**versioning_enabled**|`boolean`|todo|True|

### s3_bucket.acl

todo

* **Type**: `string`
* **Default**: private

### s3_bucket.force_destroy

todo

* **Type**: `boolean`
* **Default**: False

### s3_bucket.name

todo

* **Type**: `null`
* **Default**: None

### s3_bucket.tags

|Property|Type|Description|Default|
|---|---|---|---|

### s3_bucket.versioning_enabled

todo

* **Type**: `boolean`
* **Default**: True

## terraform_state_bucket_name

todo

* **Type**: `null`
* **Default**: None

## waf_enabled

todo

* **Type**: `boolean`
* **Default**: True

## waf_web_acl_arn

todo

* **Type**: `null`
* **Default**: None

## web_acl_id

todo

* **Type**: `null`
* **Default**: None
