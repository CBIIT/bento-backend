# Install neo4j db
set -ex
cd /tmp
rm -rf icdc-devops || true
yum -y install epel-release
yum -y install wget git python-setuptools python-pip
pip install --upgrade "pip < 21.0"
pip install ansible==2.8.0 boto boto3 botocore
git clone https://github.com/CBIIT/icdc-devops
cd icdc-devops/ansible && git checkout master
mkdir -p /var/lib/neo4j/conf
ansible-playbook community-neo4j.yml
systemctl restart neo4j

echo "Userdata script complete" >> /tmp/script_confirmation.txt