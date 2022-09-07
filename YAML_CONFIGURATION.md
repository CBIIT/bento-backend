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
| Single    | Request a single query                 |
| Facet     | Request multiple queries at once       |
| Global    | Request a pre-determined global search |

   
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


## Filter Type
| Filter Type     | Description                                                                                                                                                                                                                                                                                                                                       |
|-----------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| default         | Search in the selectedField through Open-search index <br/> <table>  <tbody>  <tr>  <td>Name</td>  <td>Required</td>  <td>Description</td></tr> <tr> <td>selectedField</td>  <td>O</td>  <td>select field desc</td></tr> </tbody>  </table>                                                                                                       |
| aggregation     | Search to group the summary of documents into buckets <br/> <table>  <tbody>  <tr>  <td>Name</td>  <td>Required</td>  <td>Description</td></tr> <tr> <td>selectedField</td>  <td>O</td>  <td>select field desc</td></tr> <tr> <td>ignoreSelectedField</td>  <td>X</td>  <td>select field desc</td></tr></tbody>  </table>                         |
| pagination      | Search with pagination params including size, offset, and order-by <br/> <table>  <tbody>  <tr>  <td>Name</td>  <td>Required</td>  <td>Description</td></tr> <tr> <td>defaultSortField</td>  <td>X</td>  <td>select field desc</td></tr> <tr> <td>alternativeSortField</td>  <td>X</td>  <td>select field desc</td></tr> </tbody>  </table>       |
| range           | Search within numerical boundary <br/> <table>  <tbody>  <tr>  <td>Name</td>  <td>Required</td>  <td>Description</td></tr> <tr> <td>selectedField</td>  <td>O</td>  <td>select field desc</td></tr></tbody>  </table>                                                                                                                             |
| sub_aggregation | In addition to aggregation, grouping the summary of each document per bucket <br/>  <table>  <tbody>  <tr>  <td>Name</td>  <td>Required</td>  <td>Description</td></tr> <tr> <td>selectedField</td>  <td>O</td>  <td>select field desc</td></tr><tr> <td>subAggSelectedField</td>  <td>O</td>  <td>select field desc</td></tr> </tbody>  </table> |
| global          | Search based on a precise value<br/> <table>  <tbody>  <tr>  <td>Name</td>  <td>Required</td>  <td>Description</td></tr> <tr> <td>defaultSortField</td>  <td>O</td>  <td>ffffff</td></tr> <tr> <td>query</td>  <td>O</td>  <td>query search desc</td></tr> <tr> <td>typedSearch</td>  <td>X</td>  <td>type search des</td></tr></tbody>  </table> |
| nested          | Searches it in a nested field objects <br/> <table>  <tbody>  <tr>  <td>Name</td>  <td>Required</td>  <td>Description</td></tr> <tr> <td>defaultSortField</td>  <td>O</td>  <td>select field desc</td></tr> <tr> <td>nestedParameters</td>  <td>X</td>  <td>select field desc</td></tr></tbody>  </table> <br/>                                   |
| sum             | Sum up numerical values from the aggregated search. <br/> <table>  <tbody>  <tr>  <td>Name</td>  <td>Required</td>  <td>Description</td></tr> <tr> <td>selectedField</td>  <td>O</td>  <td>select field desc</td></tr> </tbody>  </table>                                                                                                         |

## Global Highlighter
pre-requisite: In order to use highlight, a filter type must be stored as global

| Name         | Required | Description                                                    |
|--------------|----------|----------------------------------------------------------------|
| fields       | O        | Array of fields to highlight                                   |
| fragmentSize | X        | The size of the highlighted fragment. Default by 1             |
| preTag       | X        | Use html tag to wrap before the highlighted text. Default by $ |
| postTag      | X        | Use html tag to wrap after the highlighted text. Default by $  |

## Query Configuration Result Type
| Result Type         | Optional Method                     | Example                                                                  |
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

## Query Configuration Filter & Result Pair Rule
| Query Type | Description                                                                                 |
|------------|---------------------------------------------------------------------------------------------|
| term       | Searches based on a precise value like an userid or a price<br/> Optional: integer, boolean |
| match      | Searched text, number or boolean value after text scoring analysis                          |
| wildcard   | Searches items without knowing the exact words                                              |

## Query pairing
A filter type must pair with a result type

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

# Detailed Explanation
# alternativeSortField: sort desired field alternatively, especially sorting texts containing number
