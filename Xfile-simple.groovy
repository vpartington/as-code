xlr {
  release("Petstore Release pipeline") {
    description "Petstore Release pipeline for various environments"
    phases {
        phase("test") {
          tasks {
          manual("Prepare environment") {
              description "Prepare environment for deployment"
            }
}
       }
  }
}
}
