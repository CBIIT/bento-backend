# YAML Configuration Guide
This is the user documentation to build a codeless search API written in YAML running in Open-search.

## Introduction
The codeless configuration is a tool to implement search API without writing Java code in Bento Backend.

## Required Configuration Files
The following file will need to be edited to configure the Bento Backend Code to work within a Bento based application:
1. Open-search import yaml configuration
2. Search API YAML files

| File Type | Description                            |
|-----------|----------------------------------------|
| Single    | request a single query                 |
| Facet     | request multiple queries at once       |
| Global    | request a pre-determined global search |

   
## TODO Open-search yaml configuration
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

## TODO Search API YAML Description
  - index: Declare Elasticsearch Index Name
  - name: Declare GraphQl Query Name
  - filter: Declare Type of GraphQl Query
  - result: Declare Desired Return Type


# Filter Type
| Filter Type     | Description                                                                                                                        |
|-----------------|------------------------------------------------------------------------------------------------------------------------------------|
| default         | Search it in the selectedField through Open-search index - Required: selectedField                                                 |
| aggregation     | Search it to group the summary of documents into buckets <br/>- Required: selectedField <br/> - Optional: filter                   |
| pagination      | Search it with pagination params including size, offset, and order-by <br/>- Optional: defaultSortField, alternativeSortField      |
| range           | Search it within numerical boundary <br/>- Required: selectedField<br/> - Optional: filter                                         |
| sub_aggregation | In addition to aggregation, supporting the summary of each document per bucket <br/>- Required: selectedField, subAggSelectedField |
| global          | Searches it based on a precise value <br/> - Required: defaultSortField, query<br/> - Optional: typedSearch                        |
| nested          | Searches it in a nested field objects <br/> - Required: defaultSortField, query<br/> - Optional: nestedParameters                  |
| sum             | Sum up numerical values from the aggregated search. <br/> - Required: selectedField                                                |

# Query Configuration Result Type
| Result Type         | Option                              | Example                                                                  |
|---------------------|-------------------------------------|--------------------------------------------------------------------------|
| object_array        | -                                   | [object, object, object...]                                              |
| group_count         | -                                   | {boy: 10, girl:20...}                                                    |
| str_array           | -                                   | [ boy, girl...]                                                          |
| int                 | count_bucket_keys<br/> nested_count | 1, 2, 3 ...                                                              |
| float               | sum                                 | 1.0, 2.0, 3.0...                                                         |
| global_about        | -                                   | {type: '', page: '', title: '', text: ''}                                |
| global              | -                                   | {boy: object, girl: object}                                              |
| global_multi_models | -                                   | {boy: object, girl:object}                                               |
| range               | -                                   | {lowerBound: 0.00, upperBound: 0.00, subjects: XXX}                      |
| arm_program         | -                                   | {program: 0, caseSize: 0, children: [{arm: 0, caseSize: 0, size: 0}...]} |
| empty               | -                                   | 0                                                                        |

# Query Configuration Filter & Result Pair Rule
| Query Type | Description                                                                                 |
|------------|---------------------------------------------------------------------------------------------|
| term       | Searches based on a precise value like an userid or a price<br/> Optional: integer, boolean |
| match      | Searched text, number or boolean value after text scoring analysis                          |
| wildcard   | Searches items without knowing the exact words                                              |

## Query pairing
A filter type must match with a result type
| Query Type      | Result Type                                              |
|-----------------|----------------------------------------------------------|
| default         | object_array, str_array                                  |
| pagination      | object_array                                             |
| sub_aggregation | arm_program                                              |
| aggregation     | aggregation, int                                         |
| range           | range                                                    |
| global          | global, global_multi_models                              |
| nested          | nested_list, nested_total                                |

# Filter Type
# - default(filter query in Elasticsearch triggered to filter by graphQL input arguments)
    # Required: selectedField
# - pagination(including pagination)
    # Required: none
    # Optional: defaultSortField, alternativeSortField
# - aggregation
    # Required: selectedField
    # Optional: filter
# - range
    # Required: selectedField
    # Optional: filter
# - sub_aggregation
    # Required: selectedField, subAggSelectedField
# - global
    # Required: defaultSortField, query
      # query
        # Required: field, type
          # type includes elements with term, match, and wildcard
    # Optional: typedSearch
      # query: same rules applied as required above
# - nested
    # Required: defaultSortField, query
      # query
        # Required: type, selectedField, nestedPath
          # selectedField: declare a target field in aggregation search
          # nestedPath: declare a root path in nested document
    # Optional: nestedParameters
      # nestedParameters: declare number of fields to search. Multiple fields
      # on the purpose of total number of documents filtering multiple nested fields

# Return Type
# - object_array @return List<Map<String, Object>>
# - aggregation @return List<Map<String, Object>>
# - int @return Integer
# - range @return Map<String, Object>
    # ex) {lowerBound: 0.00, upperBound: 0.00, subjects: XXX}
# - arm_program @return List<Map<String, Object>>
# - int_total_count @return Long
# - str_array @return List<String>
# - global_about @return Map<String, Object>
    # ex) {type: about, page: XXXX, title: XXXX, text, XXXX}
# - global @return Map<String, Object>
    # ex) {result: {A: XX, B: X...}, count: 9999}
# - global_multi_models @return Map<String, Object>
    #  ex) {result: {A: XX, B: X...}, count: 9999}
# - global_multi_models @return Map<String, Object>
# - nested_list @return List<Map<String, Object>>
# - nested_total @return Integer

# Highlight
# Pre-requisite: Global Filter Type
# highlight:
    # Required: fields
      # declare list of Strings to highlight
    # Optional: fragmentSize, preTag, postTag, fragmentSize

# Query Pairing Must be Filter Type <-> Return Type
# default <-> object_array, str_array
# pagination <-> object_array
# sub_aggregation <-> arm_program
# aggregation <-> aggregation, int(int_total_aggregation, int_total_count)
# range <-> range
# global <-> global, global_multi_models
# nested <-> nested_list, nested_total


# Detailed Explanation
# alternativeSortField: sort desired field alternatively, especially sorting texts containing number
