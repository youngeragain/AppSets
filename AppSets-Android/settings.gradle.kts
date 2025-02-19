pluginManagement {

    /**
     * The pluginManagement.repositories block configures the
     * repositories Gradle uses to search or download the Gradle plugins and
     * their transitive dependencies. Gradle pre-configures support for remote
     * repositories such as JCenter, Maven Central, and Ivy. You can also use
     * local repositories or define your own remote repositories. Here we
     * define the Gradle Plugin Portal, Google's Maven repository,
     * and the Maven Central Repository as the repositories Gradle should use to look for its
     * dependencies.
     */

    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
        maven {
            name = "Google"
            url = uri("https://maven.google.com/")
        }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        maven {
            name = "MavenLocal"
            url = uri("file://D:/MavenLocal")
        }
    }
}

dependencyResolutionManagement {

    /**
     * The dependencyResolutionManagement.repositories
     * block is where you configure the repositories and dependencies used by
     * all modules in your project, such as libraries that you are using to
     * create your application. However, you should configure module-specific
     * dependencies in each module-level build.gradle file. For new projects,
     * Android Studio includes Google's Maven repository and the Maven Central
     * Repository by default, but it does not configure any dependencies (unless
     * you select a template that requires some).
     */

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://storage.googleapis.com/r8-releases/raw")
        }
        maven {
            name = "Google"
            url = uri("https://maven.google.com/")
        }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        maven {
            name = "MavenLocal"
            url = uri("file://D:/MavenLocal")
        }
    }
}

rootProject.name = "AppSets-Android"
include(":app")
include(":starter")
include(":appsets")
include(":io")
include(":compose_share")
include(":launcher")
include(":webserver")
include(":share")
include(":proxy")
include(":baselineprofile")

//include(":compose_addons")
//include(":binder")
//include(":purple_native")
//include(":webrtc")