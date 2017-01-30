def parentReleaseTitle = release.title

def subRelease = xlr {
  release("Release started from " + parentReleaseTitle) {
    description "Petstore Release pipeline for various environments"
    phases {
      phase("test") {
        tasks {
          manual("Prepare environment") {
            description "Prepare environment for deployment"
            team "Release Admins"
          }
        }
      }
    }
  }
}

releaseVariables['SUB_RELEASE_ID'] = subRelease.id
releaseVariables['SUB_RELEASE_TITLE'] = subRelease.title
