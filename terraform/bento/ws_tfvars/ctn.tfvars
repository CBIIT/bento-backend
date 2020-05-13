stack_name                  = "dev"
jenkins_private_ip          = "172.18.1.120"
appserver_private_ip1       = "172.18.1.121"
appserver_private_ip2       = "172.18.1.122"
neo4j_private_ip            = "172.18.1.123"
ecs_private_ip              = "172.18.1.125"
domain                      = "essential-dev.com"
svc_app                     = "ctn"
svc_jenkins                 = "jenkins"
svc_neo4j                   = "neo4j"
svc_frontend                     =  {
    host = "bento"
    path = "/*"
    name = "frontend"
}
svc_backend                     =  {
    host = "bento"
    path = "/api/*"
    name = "backend"
}
tags = {
    Project = "bento"
    Environment = "dev"
    ManagedBy = "terraform"
    POC  = "devops"
  }