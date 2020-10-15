Welcome to SOAS 3 !
-This project aims at converting any OpenAPI service (version 3) to an ontology.

Technologies we are based on :
-Apache Jena was leveraged to map from OpenAPI to ontology and
Swagger Parser in order to read the OpenAPI files.

-Pellet as Reasoner

Basic Structure of Code :
- SoasMain contains the main method, within which SoasMapper method is called.

-SoasMapper takes as input parameters the following:

String directoryofOpenApiFiles= Path where OpenAPI files are located.
String ontologyPath= Path where ontology file is located.
OntModelSpec spec, Model createModelInfo, String modelType= Ontology parameters

SoasMapper also instantiates OntologyHandler and OpenAPIHandler and Convert2Ontology Class.

-OntologyHandler manipulates Ontology files (read, write, create queries,..).

-OpenAPIHandler manipulates OpenAPI files (read, write,..)

-Convert2Ontology is the worker that converts OpenAPI objects to Ontology.

OpenAPI files that were tested:
-Petstore.yaml
-UpTo.yaml
(Files exist in /TestServices directory)

Ontology File
openapi.ttl (Located in main directory)


Quick Start:
- Download Eclipse for JAVA IDE
- Import the SOAS3 project
- Go to SoasMain class
- Write your SPARQL query (or uncomment the already existed)
- Execute your code


Be careful!
pom file contains all the depedencies. Do not erase it.
