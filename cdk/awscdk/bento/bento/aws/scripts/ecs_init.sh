set -ex
cd /tmp
rm -rf icdc-devops || true
yum -y install epel-release
yum -y install wget git python-setuptools python-pip
pip install --upgrade "pip < 21.0"
pip install ansible==2.8.0 boto boto3 botocore
git clone https://github.com/CBIIT/icdc-devops
cd icdc-devops/ansible && git checkout master
ansible-playbook ecs-agent.yml --skip-tags master -e stack_name="PROJECT" -e ecs_cluster_name="CLUSTER_NAME" -e env="ENV_NAME"
systemctl restart docker