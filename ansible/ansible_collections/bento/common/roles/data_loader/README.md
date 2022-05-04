data_loader
------------

This role loads data to a Bento Neo4j database.

Requirements
------------



Role Variables
--------------

Required variables:

ansible_python_interpreter
workspace
project_name
tier
s3_folder
wipe_db
cheat_mode
split_transactions
region
neo4j_user
neo4j_password
model_file1
model_file2
property_file
data_bucket

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