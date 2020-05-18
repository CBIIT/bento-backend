pipelineJob('icdc/k9dc') {
  
  def repo = 'https://github.com/vdonkor/ctn.git' 
  description("ICDC pipeline Job") 

  definition  {
    cpsScm {
      scm {
        git {
          remote { url(repo) }
          branches('master', '**/feature*')
          scriptPath('jobs/icdc/Jenkinsfile')
          extensions { }  // required to avoid tagging
        }
      }
    }
  }
}