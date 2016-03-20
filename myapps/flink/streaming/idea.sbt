lazy val mainRunner = project.in(file("mainRunner")).dependsOn(RootProject(file("."))).settings(
  // we set all provided dependencies to none, so that they are included in the classpath of mainRunner
  libraryDependencies := (libraryDependencies in RootProject(file("."))).value.map{
    module =>
      if (module.configurations.equals(Some("provided"))) {
        module.copy(configurations = None)
      } else {
        module
      }
  }
)
