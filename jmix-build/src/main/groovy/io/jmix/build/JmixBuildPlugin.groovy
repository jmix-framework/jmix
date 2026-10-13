/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.build

import com.github.spotbugs.snom.SpotBugsTask
import groovy.xml.MarkupBuilder
import groovy.xml.XmlSlurper
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.provider.Provider
import org.gradle.api.publish.tasks.GenerateModuleMetadata
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.api.tasks.testing.Test
import org.gradle.external.javadoc.JavadocMemberLevel
import org.gradle.process.ExecOperations

import javax.inject.Inject
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.zip.ZipFile

class JmixBuildPlugin implements Plugin<Project> {

    private final ExecOperations execOperations

    @Inject
    JmixBuildPlugin(ExecOperations execOperations) {
        this.execOperations = execOperations
    }

    @Override
    void apply(Project project) {
        JmixBuildExtension extension = project.extensions.create('jmixBuild', JmixBuildExtension)

        setupRepositories(project)
        setupCompilation(project, extension)
        setupTestExecution(project)
        setupJavadocsBuilding(project)
        setupAggregateJavadocsBuilding(project)
        setupPublishing(project)
        setupDependencyManagement(project)
        setupSpotbugs(project)
        setupAggregateSpotbugs(project)
        setupConfigurationMetadataGeneration(project)
    }

    private void setupRepositories(Project project) {
        project.with {
            String jmixRepoUrl = rootProject.findProperty('jmixRepoUrl')
            if (jmixRepoUrl) {
                repositories {
                    if (rootProject.hasProperty('jmixUseLocalMavenRepository')) {
                        mavenLocal()
                    }
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
                    mavenCentral()
                }
            } else {
                repositories {
                    mavenLocal()
                    maven {
                        url('https://nexus.jmix.io/repository/public')
                    }
                    mavenCentral()
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
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }

        project.tasks.withType(JavaCompile).configureEach {
            options.encoding = 'UTF-8'
            options.compilerArgs.add("-parameters")
        }
    }

    private void setupPublishing(Project project) {
        if (!project.name.startsWith('sample')) {
            project.with {
                apply plugin: 'maven-publish'

                afterEvaluate {
                    def archivesName = project.base.archivesName.get()
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
                                    url = jmixUploadUrl
                                    credentials {
                                        username rootProject['jmixUploadUser']
                                        password rootProject['jmixUploadPassword']
                                    }
                                    allowInsecureProtocol = true
                                }
                            }
                        }
                        publications {
                            javaMaven(MavenPublication) {
                                artifactId = archivesName
                                from components.java
                                pom {
                                    name = 'Jmix'
                                    description = 'A high-level full-stack framework for business applications'
                                    url = 'http://jmix.io'
                                    licenses {
                                        license {
                                            name = 'Apache License, Version 2.0'
                                            url = 'https://www.apache.org/licenses/LICENSE-2.0'
                                        }
                                    }
                                }
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
            maxHeapSize = '2g'
            systemProperty('org.slf4j.simpleLogger.defaultLogLevel', 'debug')
            systemProperty('org.slf4j.simpleLogger.log.org.springframework', 'info')
            systemProperty('org.slf4j.simpleLogger.log.eclipselink.sql', 'debug')
            // Gradle 9 fails a test task that discovers no tests (unless a filter is applied).
            // Modules may legitimately have test sources without runnable tests, so don't fail on that.
            failOnNoDiscoveredTests = false
        }
        project.with {
            afterEvaluate {
                test {
                    useJUnitPlatform {
                        boolean result = project.hasProperty("includeSlowTests") ? project.getProperty("includeSlowTests") : false
                        if (!result) {
                            excludeTags 'slowTests'
                        } else {
                            environment "slowTests", "true"
                        }
                    }
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

            title = "Jmix ${project.name} ${project.version.replace('-SNAPSHOT', '')} API"
        }
    }

    private void setupAggregateJavadocsBuilding(Project project) {
        ExecOperations execOps = execOperations
        Project rootProject = project.rootProject
        if (rootProject) {
            rootProject.gradle.projectsEvaluated {
                Set<Task> existingTasks = rootProject.getTasksByName("aggregateJavadoc", false)
                if (existingTasks.isEmpty()) {
                    Set<Project> javaSubprojects = getJavaSubProjects(rootProject)
                    if (!javaSubprojects.isEmpty()) {
                        rootProject.task("aggregateJavadoc", type: Javadoc) {
                            description = 'Aggregates Javadoc API documentation of all subprojects.'
                            group = JavaBasePlugin.DOCUMENTATION_GROUP
                            options.encoding = 'UTF-8'
                            options.memberLevel = JavadocMemberLevel.PROTECTED
                            options.addStringOption("sourcepath", "")
                            include('io/jmix/**')

                            dependsOn javaSubprojects.javadoc
                            source javaSubprojects.javadoc.source

                            destinationDir = rootProject.file("$rootProject.buildDir/docs/javadoc")
                            classpath = rootProject.files(javaSubprojects.javadoc.classpath)

                            title = "${rootProject.name.capitalize()} ${getApiVersion(rootProject)} API"

                            if (rootProject.hasProperty('javadocPublishCmd')) {
                                doLast {
                                    execOps.exec { spec ->
                                        spec.workingDir "$rootProject.buildDir/docs/javadoc"
                                        spec.commandLine 'sh', '-c', rootProject.javadocPublishCmd
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private String getApiVersion(Project rootProject) {
        String[] parts = rootProject.version.split('\\.')
        if (parts.length > 1) {
            return parts[0] + '.' + parts[1]
        } else {
            return parts[0]
        }
    }

    private void setupSpotbugs(Project project) {
        project.with {
            if (rootProject.hasProperty('spotbugsEnabled')) {
                apply plugin: 'com.github.spotbugs'

                project.afterEvaluate {
                    //check that spotbugs-exclude-filter.xml is defined in the project. If not then copy the file from plugin resources
                    def excludeFilterFilePath = '../etc/spotbugs/spotbugs-exclude-filter.xml'
                    if (!file(excludeFilterFilePath).exists()) {
                        excludeFilterFilePath = "$project.buildDir/spotbugs/spotbugs-exclude-filter.xml"
                        def pluginExcludeFilterFileContent = getClass().getClassLoader().getResource('spotbugs/spotbugs-exclude-filter.xml').text
                        tasks.register('initSpotbugsExcludeFilter') {
                            doLast {
                                def excludeFilterFile = new File(excludeFilterFilePath)
                                excludeFilterFile.getParentFile().mkdirs()
                                excludeFilterFile.createNewFile()
                                excludeFilterFile.withWriter('utf-8') { writer ->
                                    writer.write pluginExcludeFilterFileContent
                                }
                            }
                        }
                        tasks.named('spotbugsMain').get().dependsOn('initSpotbugsExcludeFilter')
                    }

                    spotbugs {
                        ignoreFailures = false
                        omitVisitors = ['FindDoubleCheck']
                        excludeFilter = project.file(excludeFilterFilePath)
                        effort = "max"
                        reportLevel = "medium"
                    }

                    spotbugsMain {
                        jvmArgs = ['-Xmx1024m']

                        reports {
                            // The XML report is merged by the root 'aggregateSpotbugs' task.
                            xml {
                                required = true
                                outputLocation = project.layout.buildDirectory.file("reports/spotbugs/${project.name}.xml")
                            }
                            html {
                                required = true
                                stylesheet = 'fancy-hist.xsl'
                                outputLocation = project.layout.buildDirectory.file("reports/spotbugs/${project.name}.html")
                            }
                        }
                    }

                    spotbugsTest {
                        jvmArgs = ['-Xmx1024m']

                        reports {
                            xml {
                                required = false
                            }
                            html {
                                required = true
                                stylesheet = 'fancy-hist.xsl'
                                outputLocation = project.layout.buildDirectory.file("reports/spotbugs/test-${project.name}.html")
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Registers the root 'aggregateSpotbugs' task: it runs 'spotbugsMain' in every subproject (or only in the
     * subprojects under the directories listed in the 'spotbugsIncludeDirs' property), merges the XML results and
     * writes a single HTML report to 'build/reports/spotbugs'. The build does not stop on the first module with
     * bugs: the module tasks ignore failures while the aggregate task is in the task graph.
     */
    private void setupAggregateSpotbugs(Project project) {
        ExecOperations execOps = execOperations
        Project rootProject = project.rootProject
        if (!rootProject.hasProperty('spotbugsEnabled')) {
            return
        }
        rootProject.gradle.projectsEvaluated {
            if (rootProject.tasks.findByName('aggregateSpotbugs') != null) {
                return
            }
            List<SpotBugsTask> spotbugsTasks = getSpotbugsProjects(rootProject)
                    .collect { it.tasks.getByName('spotbugsMain') as SpotBugsTask }
            Map<String, File> xmlReports = spotbugsTasks.collectEntries {
                [it.project.name, it.reports.getByName('xml').outputLocation.get().asFile]
            }
            FileCollection spotbugsClasspath = spotbugsTasks ? spotbugsTasks.first().spotbugsClasspath : rootProject.files()
            Provider<Directory> reportDir = rootProject.layout.buildDirectory.dir('reports/spotbugs')
            String reportName = rootProject.name
            String release = rootProject.version.toString()

            rootProject.tasks.register('aggregateSpotbugs') {
                description = 'Runs SpotBugs in all subprojects and merges the results into a single report.'
                group = JavaBasePlugin.VERIFICATION_GROUP
                dependsOn spotbugsTasks
                inputs.files(xmlReports.values())
                outputs.dir(reportDir)
                doLast {
                    Map<String, File> reports = xmlReports.findAll { it.value.exists() }
                    if (reports.isEmpty()) {
                        throw new GradleException('No SpotBugs XML reports found. Check the spotbugsIncludeDirs property.')
                    }
                    File dir = reportDir.get().asFile
                    dir.mkdirs()
                    // UnionResults reads the report paths from a .txt file, which keeps the command line short.
                    File reportList = new File(dir, "${reportName}-reports.txt")
                    reportList.setText(reports.values()*.absolutePath.join('\n'), 'UTF-8')
                    File mergedXml = new File(dir, "${reportName}.xml")
                    File mergedHtml = new File(dir, "${reportName}.html")
                    execOps.javaexec { spec ->
                        spec.classpath = spotbugsClasspath
                        spec.mainClass.set('edu.umd.cs.findbugs.workflow.UnionResults')
                        spec.args('-withMessages', '-output', mergedXml.absolutePath, reportList.absolutePath)
                    }
                    rebuildSpotbugsSummary(mergedXml, reportName, release)
                    // Render with the Saxon shipped with SpotBugs straight from the XML: SpotBugs' own converter
                    // would re-read the file and drop the statistics again.
                    File stylesheet = new File(dir, 'fancy-hist.xsl')
                    extractSpotbugsStylesheet(spotbugsClasspath, stylesheet)
                    execOps.javaexec { spec ->
                        spec.classpath = spotbugsClasspath
                        spec.mainClass.set('net.sf.saxon.Transform')
                        spec.args("-s:${mergedXml.absolutePath}", "-xsl:${stylesheet.absolutePath}",
                                "-o:${mergedHtml.absolutePath}")
                    }
                    List<String> missing = (xmlReports.keySet() - reports.keySet()).sort()
                    logger.lifecycle(buildSpotbugsSummary(reports, missing, mergedXml, mergedHtml))
                }
            }

            // Let every module finish so that the aggregate report covers the whole build.
            rootProject.gradle.taskGraph.whenReady { graph ->
                if (graph.hasTask(':aggregateSpotbugs')) {
                    spotbugsTasks.each { it.ignoreFailures = true }
                }
            }
        }
    }

    private static Set<Project> getSpotbugsProjects(Project rootProject) {
        Set<Project> projects = rootProject.subprojects.findAll { it.tasks.findByName('spotbugsMain') != null }
        String includeDirs = rootProject.findProperty('spotbugsIncludeDirs')
        if (!includeDirs) {
            return projects
        }
        List<Path> dirs = includeDirs.split(',').collect { rootProject.file(it.trim()).toPath().toAbsolutePath().normalize() }
        return projects.findAll { subproject ->
            Path projectDir = subproject.projectDir.toPath().toAbsolutePath().normalize()
            dirs.any { projectDir.startsWith(it) }
        }
    }

    /**
     * Fixes the merged XML before rendering. UnionResults drops the project name and the release (the HTML title)
     * and inflates the counters of FindBugsSummary. The module XML reports carry no package and class statistics
     * at all, because SpotBugs collects them only for the first configured report, the HTML one; without them
     * 'Browse by Packages' and the package rows of 'Summary' stay empty. Rebuilds FindBugsSummary from the merged
     * bug instances: a ClassStats per class with findings, grouped into PackageStats.
     */
    private static void rebuildSpotbugsSummary(File mergedXml, String projectName, String release) {
        String xml = mergedXml.getText('UTF-8')
                .replaceFirst(/<BugCollection ([^>]*) release="[^"]*">/, "<BugCollection \$1 release=\"${release}\">")
                .replaceFirst(/<Project>/, "<Project projectName=\"${projectName}\">")
        def bugCollection = new XmlSlurper().parseText(xml)

        // Package -> class -> priority -> number of bugs.
        Map<String, Map<String, Map<String, Integer>>> packages = new TreeMap<>()
        Map<String, String> sourceFiles = [:]
        bugCollection.BugInstance.each { bug ->
            def primaryClass = bug.Class.find { it.@primary.text() == 'true' } ?: bug.Class[0]
            String className = primaryClass.@classname.text()
            String packageName = className.contains('.') ? className.substring(0, className.lastIndexOf('.')) : ''
            packages.computeIfAbsent(packageName) { new TreeMap<>() }
                    .computeIfAbsent(className) { new TreeMap<>() }
                    .merge(bug.@priority.text(), 1, Integer::sum)
            sourceFiles.putIfAbsent(className, primaryClass.SourceLine[0].@sourcefile.text())
        }

        Map<String, Object> summaryAttributes = bugCollection.FindBugsSummary[0].attributes()
                .findAll { !(it.key ==~ /total_bugs|total_classes|num_packages|priority_\d/) }
        summaryAttributes.putAll(total_classes: packages.values().sum { it.size() }, num_packages: packages.size())
        summaryAttributes.putAll(bugCounters('total_bugs', sumByPriority(packages.values().collectMany { it.values() })))
        List<Map> packageStats = packages.collect { packageName, classes ->
            Map<String, Object> packageAttributes = ['package': packageName, total_types: classes.size(), total_size: 0] +
                    bugCounters('total_bugs', sumByPriority(classes.values()))
            List<Map<String, Object>> classStats = classes.collect { className, byPriority ->
                ['class': className, sourceFile: sourceFiles[className], 'interface': false, size: 0] +
                        bugCounters('bugs', byPriority)
            }
            [attributes: packageAttributes, classes: classStats]
        }

        StringWriter summary = new StringWriter()
        MarkupBuilder builder = new MarkupBuilder(summary)
        builder.doubleQuotes = true
        builder.omitEmptyAttributes = true
        builder.FindBugsSummary(summaryAttributes) {
            packageStats.each { stats ->
                PackageStats(stats.attributes) {
                    stats.classes.each { ClassStats(it) }
                }
            }
            FindBugsProfile()
        }
        String fixed = xml.replaceFirst(/(?s)<FindBugsSummary .*?<\/FindBugsSummary>/,
                Matcher.quoteReplacement(summary.toString()))
        mergedXml.setText(fixed, 'UTF-8')
    }

    private static Map<String, Integer> sumByPriority(Collection<Map<String, Integer>> counters) {
        Map<String, Integer> sum = new TreeMap<>()
        counters.each { it.each { priority, count -> sum.merge(priority, count, Integer::sum) } }
        return sum
    }

    /**
     * Returns the total and the per-priority bug counters the way FindBugsSummary, PackageStats and ClassStats
     * carry them.
     */
    private static Map<String, Integer> bugCounters(String totalName, Map<String, Integer> byPriority) {
        Map<String, Integer> counters = [(totalName): byPriority.values().sum(0) as Integer]
        byPriority.each { priority, count -> counters["priority_${priority}".toString()] = count }
        return counters
    }

    private static void extractSpotbugsStylesheet(FileCollection spotbugsClasspath, File stylesheet) {
        File spotbugsJar = spotbugsClasspath.files.find { it.name ==~ /spotbugs-\d.*\.jar/ }
        new ZipFile(spotbugsJar).withCloseable { zip ->
            stylesheet.bytes = zip.getInputStream(zip.getEntry(stylesheet.name)).bytes
        }
    }

    private static String buildSpotbugsSummary(Map<String, File> reports, List<String> missing,
                                               File mergedXml, File mergedHtml) {
        XmlSlurper parser = new XmlSlurper()
        Map<String, Integer> bugsByProject = reports.collectEntries { name, file ->
            [name, parser.parse(file).FindBugsSummary.@total_bugs.text().toInteger()]
        }.findAll { it.value > 0 }.sort { -it.value }
        String totalAttr = parser.parse(mergedXml).FindBugsSummary.@total_bugs.text()
        int total = totalAttr ? totalAttr.toInteger() : bugsByProject.values().sum(0)

        StringBuilder summary = new StringBuilder()
        summary.append("SpotBugs: ${total} bug(s) in ${bugsByProject.size()} of ${reports.size()} modules\n")
        bugsByProject.each { name, count -> summary.append(String.format('  %-45s %5d%n', name, count)) }
        if (missing) {
            summary.append("No SpotBugs XML report for: ${missing.join(', ')}\n")
        }
        summary.append("Aggregate report: ${mergedHtml.toURI()}")
        return summary.toString()
    }

    private Set<Project> getJavaSubProjects(Project rootProject) {
        rootProject.subprojects.findAll { subproject -> subproject.plugins.hasPlugin(JavaPlugin) }
    }

    private void setupDependencyManagement(Project project) {
        def bomProject = project.rootProject.findProject("bom")
        def bom = bomProject ? project.dependencies.project(path: bomProject.path) : "io.jmix.bom:jmix-bom:${project.version}"
        project.with {
            dependencies {
                api platform(bom)
                //to be able to add the annotationProcessor dependency without defining its version
                annotationProcessor platform(bom)
            }
        }
    }

    private void setupConfigurationMetadataGeneration(Project project) {
        project.dependencies.add('annotationProcessor', 'org.springframework.boot:spring-boot-configuration-processor')
        //to be able to use additional-spring-configuration-metadata.json in jmix modules
//        project.tasks.named('compileJava').get().dependsOn('processResources')
    }
}
