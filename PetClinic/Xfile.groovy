def server(type, title) {
    def cis = configurationApi.searchByTypeAndTitle(type, title)
    if (cis.isEmpty()) {
        throw new RuntimeException("No CI found for the type '${type}' and title '${title}'")
    }
    if (cis.size() > 1) {
        throw new RuntimeException("More than one CI found for the type '${type}' and title '${title}'")
    }
    cis.get(0)
}
def xlDeployServer = server('xldeploy.XLDeployServer','XL Deploy (token)')

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
              deploymentPackage "Java EE/PetClinic-ear/$petClinicVersion"
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
