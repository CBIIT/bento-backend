pipelineJob('icdc/canine-pipeline') {

  def repo = 'https://github.com/CBIIT/icdc_devops.git' 
  description("canine testing pipeline Job") 

  definition  {
    cpsScm {
      scm {
        git {
          remote { url(repo) }
          branches('master', '**/feature*')
          scriptPath('jenkins/jobs/canine-testing/Jenkinsfile')
          extensions { }  // required to avoid tagging
        }
      }
    }
  }
}