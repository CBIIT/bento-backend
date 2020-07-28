# Bento Backend Framework Configuration Guide
[Github Repository](https://github.com/CBIIT/bento-backend)
## Description
The Bento Backend Framework is a server-side backend written in Java to be used with Bento based applications. The Bento Backend is meant to be used in conjunction with the Bento Frontend Framework and a Neo4j database running the GraphQL plugin.
## Configuration
The following file will need to be edited to configure the Bento Backend Code to work within a Bento based application:
#### ````src/main/resources/application.properties````
1.  create this file by creating a copy of ````src/main/resources/application_example.properties```` and renaming it to use as a starting point.
1.  ````Line 2```` - change the value of ````neo4j.graphql.endpoint```` to the GraphQL endpoint running on the applications Neo4j database.
1.  ````Line 4```` - change the value of  ````graphql.schema```` to the path of the GraphQL schema file that will be uploaded to the database. The file path should be relative to the ````main```` folder in the project.
1.  ````Line 5```` - change the value of ````neo4j.authorization```` to the basic access authentication for your Neo4j database. (See the **Basic Access Authentication** section for instructions on how to generate this value).
1.  ````Line 9```` - this value is an all lowercase Boolean value to determine if GraphQL queries will be enabled for the application.
1.  ````Line 10```` - this value is an all lowercase Boolean value to determine if GraphQL mutations will be enabled for the application.
## Basic Access Authentication
The basic access authentication value used in the ````application.properties```` file is of the form ````Basic <base64 encoding of username:password>````. To generate this value generate the base64 encoding of the username and password as shown below and append that to the String “Basic “.
#### Example:
*   Username:   ````neo4j````
*   Password:   ````my_password````
*   Authorization Value:    ````Basic bmVvNGo6bXlfcGFzc3dvcmQ=````
#### Generate base64 encoding in terminal/bash:
````echo -n "neo4j:my_password" | base64````
#### Generate base64 encoding in powershell:
````[Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("neo4j:my_password"))````
codacy test
