ecs
------------

This role deploys an ECS Fargate service.

Requirements
------------

Requires the community.aws and amazon.aws collections to be installed.

Role Variables
--------------

Required variables:

ansible_python_interpreter
project_name
platform
region
container_name
image_version
tier
neo4j_user
neo4j_password
newrelic_license_key
container_port
container_memory
container_cpu
container_image_url
es_host
ecs_cluster_name
es_schema
enable_es_filter
enable_redis
redis_port
use_cluster
redis_host
container_entrypoint
container_env

Dependencies
------------

This role depends on the role bento.common.ecr_login to provide a valid login to the required ECR Repo.

Example Playbook
----------------



License
-------

BSD

Author Information
------------------

Maintained by Bento Devops Team.