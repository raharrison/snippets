## Create New Lambda API

The following guide shows how to use the platform automation framework to create and deploy a new Lambda based API to AWS. The project consists of the following components:

-   GitHub repository containing all necessary sample code and configuration for a sample hello world REST API
-   Jenkins pipelines per environment to deploy all required project infrastructure using Terraform modules and variables
-   Jenkins pipelines per environment to build the application, push to Nexus and deploy to the Lambda function
-   AWS Lambda function for the deployed application code serving the API
-   API gateway configured using the user supplied Swagger API specification, connected to the Lambda created above
-   Cloudfront distribution (if feature enabled) serving the API and any static resources
-   S3 bucket (if feature enabled) tied to the service and served by the Cloudfront distribution

1. Run the App Generator pipeline to create the new project and pipelines. This is located at URL or if you already have a project created, run `obss appgen` to open the pipeline in the browser:
    1. Provide a new project name (this will become the repository name) e.g `automation-demo`. If the repo already exists the pipeline will skip this step.
    1. Select all environments and brands you wish the API to be deployed to. Pipelines will be created and configured for each.
1. After the app generator pipeline has completed successfully, clone the newly created GitHub repo to your local machine e.g URL
1. The repo will contain just a barebones `pom.xml` that allows the automation plugin to execute, wrapper scripts and a `config` directory containing the manifests. Open `config/manifest.json` to populate the elements necessary for the project. The file is tied to a JSON schema so editors like VSCode or IntelliJ should warn you if there are any errors or missing fields.
    1. meta -> description, title and packageName
    1. profile -> features -> enable cloudfront (if needed)
    1. profile -> apiParams (optional fields)
1. Bootstrap the project by invoking the automation plugin `obss bootstrap` command. This will create a number of files required to deploy the project:
    1. Updated `pom.xml` containing OBSS dependencies and build plugins
    1. Java application for a Lambda based API with Dagger injection
    1. Sample Swagger API specification in `spec/api.yaml` (tied to API gateway)
    1. Terraform variables in `terraform/terraform.tfvars.json` (tied to which profile features were enabled)
1. Update the Terraform variables if required. Just like the manifest, the file is tied to a schema so your editor should notify you of any validation issues. For this basic example the defaults values will suffice
1. Commit and push all new project files back to the repository
1. To deploy the app first the infra needs to be created. Run `obss jenkins dev infra` to open the pipeline for dev and execute.
1. Build and deploy the application itself by opening the app pipeline `obss jenkins dev app` and executing into the dev environment.
1. After both pipelines have finished the API should be deployed and available at `https://<cloudfront>/api/hello`

## Create New UI Project

**Ensure that you have the Angular CLI installed locally before running the bootstrap command `npm install -g @angular/cli`**

The following guide shows how to create a brand new UI (Angular project) tied and served by an existing API based service (see above guide). Both the API endpoints and the static UI will be served through the same Cloudfront distribution. The project consists of the following components:

-   GitHub repository containing the UI project. The name is tied to the underlying service repo e.g `automation-demo` for the API will create `automation-demo-ui`
-   Jenkins pipelines per environment to build the Angular application, push to S3 and invalidate the underlying Cloudfront distribution
-   Bootstrap Angular project (based on the Angular CLI) with overrides for OBSS environments
-   Reuse of existing S3 bucket containing the built static UI files

1. Run the App Generator pipeline to create the new UI project (tick the UI checkbox) and build pipelines. This is located at URL or if you already have a created project run `obss appgen` to open the pipeline in the browser
    1. Provide a project name. This should be the same as the underlying API service name (if exists or otherwise two repo's will be created). e.g providing `automation-demo` to the generator pipeline will create both an `automation-demo` API repo and an `automation-demo-ui` repo for the UI
1. After the app generator pipeline has completed successfully, clone the newly created GitHub repo to your local machine e.g URL
1. The repo will contain a barebones `pom.xml` that allows the automation plugin to execute, wrapper scripts and a `config` directory containing the manifests. Open `config/manifest.json` to populate the elements necessary for this UI project. The file is tied to a JSON schema so editors like VSCode or IntelliJ should warn you if there are any errors or missing fields.
    1. meta -> description, title and packageName
    1. profile -> uiParams -> s3DeployBucket,
1. Bootstrap the project by invoking the automation plugin `obss bootstrap`. This will invoke the Angular CLI to create the project files and apply custom overrides to the configuration
    1. Demo Angular application
    1. Adds OBSS environment definitions to `package.json` and `angular.json` with appropriate build configuration
1. Commit and push all new project files back to the repository
1. Build and deploy the application by opening the app pipeline `obss jenkins dev app` and executing. This will push the built artifacts to the S3 bucket for the project.
1. After the application is deployed the UI should be available through the default behaviour of the underlying API Cloudfront distribution.
