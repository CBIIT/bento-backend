opensearch_loader
------------

This role loads data from a Bento Neo4j database to Opensearch.

Requirements
------------



Role Variables
--------------

Required variables:

ansible_python_interpreter
project_name
tier
workspace
region
neo4j_user
neo4j_password
es_host
model_file1
model_file2
property_file
about_file
indices_file

Dependencies
------------

Depends on bento.common.neo4j_ip to provide neo4j_ip variable.

Example Playbook
----------------



License
-------

BSD

Author Information
------------------

Maintained by Bento Devops Team.