import com.xebialabs.deployit.plugin.api.reflect.Type
def xlDeployServer = Type.valueOf('xldeploy.XLDeployServer').descriptor.newInstance('Configuration/Custom/Configuration526302744')

def latestPetClinicVersion = globalVariables['global.LATEST_PETCLINIC_VERSION']
def parentReleaseTitle = release.title

def subRelease = xlr {
  release("Deploy PetClinic v$latestPetClinicVersion to Prod") {
    description "Created from DSL, started from $parentReleaseTitle"
    phases {
      phase("Prod") {
        tasks {
          custom('Deploy to Prod') {
            script {
              type 'xldeploy.Deploy'
              server xlDeployServer
              deploymentPackage "Java EE/PetClinic-ear/$latestPetClinicVersion"
              deploymentEnvironment 'Demo/Java - WLS, Server-4 - Prod'
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
