import com.xebialabs.deployit.plugin.api.reflect.Type
def xlDeployServer = Type.valueOf('xldeploy.XLDeployServer').descriptor.newInstance('Configuration/Custom/Configuration526302744')

def petClinicVersion = releaseVariables['PETCLINIC_VERSION']
def parentReleaseTitle = release.title

def subRelease = xlr {
  release("Deploy PetClinic v$petClinicVersion to Prod") {
    description "Created from DSL, started from $parentReleaseTitle"
    phases {
      phase("Prod") {
        tasks {
          manual("Update firewall rules") {
            description "Update the firewall rules for v$petClinicVersion of PetClinic"
            team "Network"
          }
          custom('Deploy to Prod') {
            script {
              type 'xldeploy.Deploy'
              server xlDeployServer
              deploymentPackage 'Java EE/PetClinic-ear/$petClinicVersion'
              deploymentEnvironment 'Java EE/WLS - Prod (Server-4)'
            }
            team 'Release Admins'
          }
        }
      }
    }
  }
}

releaseVariables['SUB_RELEASE_ID'] = subRelease.id
releaseVariables['SUB_RELEASE_TITLE'] = subRelease.title
