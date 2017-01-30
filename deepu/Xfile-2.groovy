def environments = ["QA", "UAT", "Production"]
def phaseColors = ["#34967f", "#15b2d6", "#1552d6"]
def server(type, title) {
  def cis = configurationApi.searchByTypeAndTitle(type, title)
  if (cis.isEmpty() || cis.size() > 1) {
    throw new RuntimeException("No unique CI found for the Type and Title")
  }
  cis.get(0)
}
//define an XL deploy instance
def xlDeployServer = server("xldeploy.XLDeployServer", "XL Deploy Server")
def xlDeployPassword = globalVariables["global.xlDeployPassword"] //password must be defined in global variables
xlr {
  release("Petstore Release pipeline") {
    description "Petstore Release pipeline for various environments"
    variables {
      stringVariable("appVersion")
    }
    phases {
      environments.eachWithIndex { env, index -> // iterate through app environments
        phase("$env Release Pipeline") {
          color phaseColors[index]
          tasks {
            if (env == "Production") { // do tasks for specific env
              manual("Go/No go meeting") {
                description "Decide about production deployment"
                team "Release Mgmnt"
              }
              manual("Decide upgrade slot") {
                description "Decide an upgrade slot"
                team "Release Mgmnt"
              }
            }
            if (env == "QA") {
              gate('Code review and tests') {
                description "Wait for code review and automated tests in $env env"
                conditions {
                  condition("Code review has been done")
                  condition("Automated tests are green")
                }
                team "QA"
              }
              userInput('update the new app version number') {
                variables {
                  variable "appVersion"
                }
              }
            } else {
              gate("$env environment available") {
                description "Check if $env environment is available"
                conditions {
                  condition("Verify environment availability")
                }
                team "Ops"
              }
            }
            manual("Prepare $env environment") {
              description "Prepare $env environment for deployment"
              team env == "QA" ? "Dev" : "Ops"
            }
            if (env == "Production") {
              gate('Everybody available') {
                description "Check if everyone is available"
                conditions {
                  condition("Invitations sent")
                  condition("All participants accepted")
                  condition("Set start time on XL Deploy task")
                }
                team "Release Mgmnt"
              }
              manual("Run backups") {
                description "backup production setup"
                team "Ops"
              }
            }
            custom("Deploy package to $env") {
              description "Custom script task for XL Deploy to deploy package to $env"
              script {
                type 'xldeploy.Deploy'
                server xlDeployServer
                deploymentPackage env == "Production" ? '${appVersion}' : ('${appVersion}-' + env)
                deploymentEnvironment env
                username 'xlDeployUser'
                password xlDeployPassword
              }
              team "XL Deploy:Deploy"
            }
            parallelGroup('Testing') {
              tasks {
                if (env == "Production") {
                  manual("Run smoke tests") {
                    description "Run smoke for $env"
                    team "QA"
                  }
                } else {
                  if (env == "QA") {
                    manual("Update test scenarios") {
                      description "Update test scenarios for $env"
                      team "QA"
                    }
                  } else {
                    manual("Run performance tests") {
                      description "Run performance test for $env"
                      team "QA"
                    }
                  }
                  manual("Regression tests") {
                    description "Run Regression tests for $env"
                    team "QA"
                  }
                }
              }
            }
            if (env == "QA") {
              gate('Sign off by QA') {
                description "Wait for Sign off by QA"
                conditions {
                  condition("Notify developers")
                  condition("Notify Ops")
                }
                team "QA"
              }
            } else if (env == "UAT") {
              gate('Approve UAT') {
                description "Approve UAT"
                conditions {
                  condition("Collect approvals")
                }
                team "QA"
              }
            }
            notification('Notify stakeholders of successful Release') {
              description "Notify stakeholders of successful Release for $env environment"
              addresses "vagrant@localhost"
              subject "Petstore app release successfully to $env!"
              body "The application is available on the $env environment."
            }
          }
        }
      }
    }
  }
}
