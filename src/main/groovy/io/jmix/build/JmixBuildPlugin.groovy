package io.jmix.build

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.JavadocMemberLevel

class JmixBuildPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        JmixBuildExtension extension = project.extensions.create('jmixBuild', JmixBuildExtension)

        setupRepositories(project)
        setupCompilation(project, extension)
        setupTestExecution(project)
        setupJavadocsBuilding(project)
        setupPublishing(project)
        setupSpotbugs(project)

    }

    private void setupRepositories(Project project) {
        project.with {
            String jmixRepoUrl = rootProject.findProperty('jmixRepoUrl')
            if (jmixRepoUrl) {
                repositories {
                    maven {
                        url jmixRepoUrl
                        String jmixRepoUser = rootProject.findProperty('jmixRepoUser')
                        String jmixRepoPassword = rootProject.findProperty('jmixRepoPassword')
                        if (jmixRepoUser && jmixRepoPassword) {
                            credentials {
                                username jmixRepoUser
                                password jmixRepoPassword
                            }
                        }
                    }
                    jcenter()
                }
            } else {
                repositories {
                    mavenLocal()
                    maven {
                        url('https://nexus.jmix.io/repository/public')
                    }
                    jcenter()
                }
            }
        }
    }

    private void setupCompilation(Project project, JmixBuildExtension extension) {
        project.with {
            apply plugin: 'java'
            apply plugin: 'java-library'

            afterEvaluate {
                java {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
            }
        }

        project.tasks.withType(JavaCompile) {
            options.encoding = 'UTF-8'
        }
    }

    private void setupPublishing(Project project) {
        if (!project.name.startsWith('sample-')) {
            project.with {
                apply plugin: 'maven-publish'

                afterEvaluate {
                    java {
                        withSourcesJar()
                    }
                    artifacts {
                        archives sourcesJar
                    }
                    publishing {
                        String jmixUploadUrl = rootProject.findProperty('jmixUploadUrl')
                        if (jmixUploadUrl) {
                            repositories {
                                maven {
                                    url = version.endsWith('SNAPSHOT') ? "$jmixUploadUrl/snapshots" : "$jmixUploadUrl/releases"
                                    credentials {
                                        username rootProject['jmixUploadUser']
                                        password rootProject['jmixUploadPassword']
                                    }
                                }
                            }
                        }
                        publications {
                            javaMaven(MavenPublication) {
                                artifactId = archivesBaseName
                                from components.java
                            }
                        }
                    }
                }

                // remove after https://youtrack.jetbrains.com/issue/IDEA-227215 is fixed
                tasks.withType(GenerateModuleMetadata) {
                    enabled = false
                }
            }
        }
    }

    private void setupTestExecution(Project project) {
        project.tasks.withType(Test) {
            systemProperty('org.slf4j.simpleLogger.defaultLogLevel', 'debug')
            systemProperty('org.slf4j.simpleLogger.log.org.springframework', 'info')
            systemProperty('org.slf4j.simpleLogger.log.eclipselink.sql', 'debug')
        }
        project.with {
            afterEvaluate {
                test {
                    useJUnitPlatform()
                }
            }
        }
    }

    private void setupJavadocsBuilding(Project project) {
        project.tasks.withType(Javadoc) {
            options.addStringOption("sourcepath", "")
            options.encoding = 'UTF-8'
            options.memberLevel = JavadocMemberLevel.PROTECTED
            destinationDir = project.file("$project.buildDir/docs/javadoc")

            title = "${project.name.toUpperCase()} ${project.version.replace('-SNAPSHOT', '')} API"
        }
    }

    private void setupSpotbugs(Project project) {
        project.with {
            if (rootProject.hasProperty('spotbugsEnabled')) {
                apply plugin: 'com.github.spotbugs'

                project.afterEvaluate {
                    spotbugs {
                        toolVersion = '4.0.1'
                        ignoreFailures = false
                        omitVisitors = ['FindDoubleCheck']
                        excludeFilter = project.file("../etc/spotbugs/spotbugs-exclude-filter.xml")
                        effort = "max"
                        reportLevel = "medium"
                    }

                    spotbugsMain {
                        jvmArgs = ['-Xmx1024m']

                        reports {
                            xml.enabled = false
                            html {
                                enabled = true
                                stylesheet = 'fancy-hist.xsl'
                                destination file("${project.buildDir}/reports/spotbugs/${project.name}.html")
                            }
                        }
                    }

                    spotbugsTest {
                        jvmArgs = ['-Xmx1024m']

                        reports {
                            xml.enabled = false
                            html {
                                enabled = true
                                stylesheet = 'fancy-hist.xsl'
                                destination file("${project.buildDir}/reports/spotbugs/test-${project.name}.html")
                            }
                        }
                    }
                }
            }
        }
    }
}
