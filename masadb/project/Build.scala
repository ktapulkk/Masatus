import sbt._
import Keys._
import play.Project._
import templemore.xsbt.cucumber.CucumberPlugin
import de.johoop.jacoco4sbt.JacocoPlugin._

object ApplicationBuild extends Build {

  val appName         = "MasaDB"
  val appVersion      = "0.1-SNAPSHOT"

  val buildSettings = Defaults.defaultSettings ++
                      CucumberPlugin.cucumberSettings ++
                      Seq ( CucumberPlugin.cucumberFeaturesDir := file("./test/features/"),
                            CucumberPlugin.cucumberJunitReport := true,
                            CucumberPlugin.cucumberHtmlReport := true) ++
                      Seq(jacoco.settings:_*)

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore,
    javaJdbc,
    javaEbean
  )

  val main = play.Project(appName, appVersion, appDependencies, settings = buildSettings).settings(
    // Add your own project settings here      

    // JaCoCo
    parallelExecution in jacoco.Config := false,
    jacoco.excludes in jacoco.Config :=
    Seq(
        "Routes*",
        "controllers.Reverse*",
        "controllers.ref*",
        "controllers.javascript*",
        "controllers.routes*",
        // Sivutemplaatit (views/) saisivat periaatteessa olla mukana raportissa,
        // mutta mukaan tulee liikaa ylimääräistä.
        "views.html.*"
     )
  )

}
