import com.xebialabs.deployit.plugin.api.reflect.Type

def xlDeployServer = Type.valueOf('xldeploy.XLDeployServer').descriptor.newInstance('/Configuration/Configuration526302744')

def parentReleaseTitle = release.title

def subRelease = xlr {
  release("Pipeline for $parentReleaseTitle") {
    phases {
      phase("Deploy to prod") {
        tasks {
          custom('Deploy package to Prod') {
            script {
              type 'xldeploy.Deploy'
              server xlDeployServer
              deploymentPackage 'Java EE/PetClinic-ear/4.10'
              deploymentEnvironment 'Demo/Java - WLS (Server-3)'
            }
            team "Release Admins"
          }
        }
      }
    }
  }
}

releaseVariables['SUB_RELEASE_ID'] = subRelease.id
releaseVariables['SUB_RELEASE_TITLE'] = subRelease.title
