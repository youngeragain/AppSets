data class ProjectWrapper(val name: String, val enable: Boolean = true, val isLib: Boolean = true) {
    companion object {

        fun get(name: String): ProjectWrapper? {
            return all().firstOrNull { it.name == name }
        }

        fun all(): List<ProjectWrapper> {
            return listOf<ProjectWrapper>(
                ProjectWrapper(":app", enable = true, isLib = false),
                ProjectWrapper(":starter", enable = true),
                ProjectWrapper(":appsets", enable = true),
                ProjectWrapper(":io", enable = true),
                ProjectWrapper(":compose_share", enable = true),
                ProjectWrapper(":launcher", enable = true),
                ProjectWrapper(":webserver", enable = true),
                ProjectWrapper(":share", enable = true),
                ProjectWrapper(":proxy", enable = true),
                ProjectWrapper(":baselineprofile", enable = true),
                ProjectWrapper(":compose_addons", enable = false),
                ProjectWrapper(":binder", enable = false),
                ProjectWrapper(":purple_native", enable = false),
                ProjectWrapper(":webrtc", enable = false),
            )
        }
    }
}

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
            url = uri("https://jcenter.bintray.com/")
        }
        maven {
            name = "MavenLocal"
            url = uri("file://D:/MavenLocal")
        }
    }
}

rootProject.name = "AppSets-Android"

ProjectWrapper.all().forEach { projectWrapper ->
    if (projectWrapper.enable) {
        include(projectWrapper.name)
    }
}