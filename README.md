[![Codacy Badge](https://app.codacy.com/project/badge/Grade/197ca1f70b6a47618332548b6da480c1)](https://www.codacy.com/gh/CBIIT/bento-backend?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=CBIIT/bento-backend&amp;utm_campaign=Badge_Grade)

# Bento Backend Framework Configuration Guide
This is the user documentation for the Bento Backend.

## Introduction
The Bento Backend Framework is a server-side backend written in Java to be used with Bento based applications. The Bento Backend is meant to be used in conjunction with the Bento Frontend Framework and a Neo4j database running the GraphQL plugin.

The Bento Backend can be found in this Github Repository: [Bento Backend](https://github.com/CBIIT/bento-backend)
## Pre-requisites
*   Java 11 or newer installed on the server hosting the Bento Backend
*   The Neo4j database containing the Bento data has been initialized and is running

## Configuration
The following file will need to be edited to configure the Bento Backend Code to work within a Bento based application:

**````src/main/resources/application.properties````**

1.  create this file by creating a copy of ````src/main/resources/application_example.properties```` and renaming it to use as a starting point.
2.  ````Line 2```` - change the value of ````neo4j.graphql.endpoint```` to the GraphQL endpoint running on the applications Neo4j database.
3.  ````Line 4```` - change the value of  ````graphql.schema```` to the path of the GraphQL schema file that will be uploaded to the database. The file path should be relative to the ````main```` folder in the project.
4.  ````Line 5```` - change the value of ````neo4j.authorization```` to the basic access authentication for your Neo4j database. (See the **Basic Access Authentication** section for instructions on how to generate this value).
5.  ````Line 9```` - this value is an all lowercase Boolean value to determine if GraphQL queries will be enabled for the application.
6.  ````Line 10```` - this value is an all lowercase Boolean value to determine if GraphQL mutations will be enabled for the application.

## Basic Access Authentication
The basic access authentication value used in the ````application.properties```` file is of the form ````Basic <base64 encoding of username:password>````. To generate this value generate the base64 encoding of the username and password as shown below and append that to the String “Basic “.
    a
### Example

*   Username:````neo4j````
*   Password:````my_password````
*   Authorization Value:````Basic bmVvNGo6bXlfcGFzc3dvcmQ=````

### Generate base64 encoding in terminal/bash

````echo -n "neo4j:my_password" | base64````

### Generate base64 encoding in powershell

````[Convert]::ToBase64String([System.Text.Encoding]::ASCII.GetBytes("neo4j:my_password"))````
