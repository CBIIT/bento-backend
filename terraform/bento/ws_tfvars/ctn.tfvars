stack_name                  = "dev"
jenkins_private_ip          = "172.18.1.120"
appserver_private_ip1       = "172.18.1.121"
appserver_private_ip2       = "172.18.1.122"
neo4j_private_ip            = "172.18.1.123"
ecs_private_ip              = "172.18.1.125"
domain                      = "bento-tools.org"
svc_app                     = "ctn"
svc_jenkins                 = "jenkins"
svc_neo4j                   = "neo4j"
svc_frontend                     =  {
    host = "dev"
    path = "/*"
    name = "frontend"
}
svc_backend                     =  {
    host = "api"
    path = "/*"
    name = "backend"
}
tags = {
    Project = "dev"
    Environment = "dev"
    ManagedBy = "terraform"
    POC  = "devops"
  }