import com.xebialabs.deployit.plugin.api.reflect.Type
def branches = ["master", "v6.0", "v7.0", "v8.0"]
//define an XL deploy instance
def xlDeployServer = Type.valueOf("xldeploy.XLDeployServer").descriptor.newInstance('/Configuration/Custom/Configurationlocal')
def xlDeployPassword = globalVariables["global.xlDeployPassword"] //password must be defined in global variables
xlr {
  release("Petstore QA release pipeline") {
    description "Petstore QA release pipeline for various app versiogitns"
    variables {
      stringVariable("newVersionFromMaster")
    }
    phases {
      branches.each { branch -> // iterate through app branches
        phase("$branch Release Pipeline") {
          tasks {
            gate('Code review and tests') {
              description "Wait for code review and automated tests in $branch branch"
              conditions {
                condition("Code review has been done")
                condition("Automated tests are green")
              }
              team "QA"
            }
            manual("Prepare QA environment") {
              description "Prepare QA environment for $branch branch"
              team "Dev"
            }
            if(branch == "master") { // this step is required only for master branch
              manual("Create new version tag") {
                description "Create a new version tag for master branch on Git"
                team "Dev"
              }
              userInput('update the new version number') {
                variables {
                  variable "newVersionFromMaster"
                }
              }
            }
            custom('Deploy package to QA') {
              description "Custom script task for XL Deploy to deploy package to QA"
              script {
                type 'xldeploy.Deploy'
                server xlDeployServer
                deploymentPackage branch == "master" ? '${newVersionFromMaster}' : branch
                deploymentEnvironment 'QA'
                username 'xlDeployUser'
                password xlDeployPassword
              }
              team "XL Deploy:Deploy"
            }
            parallelGroup('Testing') {
              tasks {
                manual("Update test scenarios") {
                  description "Update test scenarios for $branch"
                  team "QA"
                }
                manual("Regression tests") {
                  description "Run Regression tests for $branch"
                  team "QA"
                }
              }
            }
            if(branch == "master") { // this step is required only for master branch
              manual("verify new tag and deployment") {
                team "Dev"
              }
            }
          }
        }
      }
      phase("Signoff release") {
        tasks {
          gate('Sign off by QA') {
            description "Wait for Sign off by QA"
            conditions {
              condition("Notify developers")
              condition("Notify Ops")
            }
            team "QA"
          }
          notification('Notify stakeholders of successful QA') {
            description "Notify stakeholders of successful QA for all branches"
            addresses "vagrant@localhost"
            subject "Petstore app passed QA!"
            body "The application is available on the QA environment."
          }
        }
      }
    }
  }
}
