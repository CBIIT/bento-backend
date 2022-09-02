# YAML Configuration Guide
This is the user documentation to write a codeless search library running in Open-search.

## Introduction
The codeless configuration is a tool to implement search API without writing Java code in Bento Backend.

## Configuration
The following file will need to be edited to configure the Bento Backend Code to work within a Bento based application:

## Required Configuration Files

1. Open-search yaml configuration
2. Search API YAML
   - global_search
   - facet_search
   - single_search
   
## Open-search yaml configuration
This is to explain for Open-search data schema ingesting data from Neo4j to Open-search

if the field type is declared as nested, it requires properties
index_name: declare a name of Open-search index
type: data source to ingest into Open-search
mapping: declared all open-search schemes 

Indices:
- index_name: subjects 
- type: neo4j
- mapping:
  - field_name:
    - type: keyword, integer, long, double, object, text, nested
  - properties:
    - field_name:
      - type: keyword, integer, long, double, object, text, nested

