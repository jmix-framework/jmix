/*
 * Copyright 2019 Haulmont.
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

package io.jmix.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Delete
import org.yaml.snakeyaml.Yaml

import java.util.jar.Manifest

class JmixPlugin implements Plugin<Project> {

    public static final String PROVIDED_RUNTIME_CONFIGURATION_NAME = 'providedRuntime'
    public static final String PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME = 'productionRuntimeClasspath'

    @Override
    void apply(Project project) {
        project.extensions.create('jmix', JmixExtension, project)

        project.afterEvaluate {
            if (isJmixApp(project) && project.jmix.useBom) {
                String bomVersion = project.jmix.bomVersion ?: getBomVersion()
                def platform = project.dependencies.platform("io.jmix.bom:jmix-bom:$bomVersion")
                project.dependencies.add('implementation', platform)

                if (project.plugins.hasPlugin('war')) {
                    project.dependencies.add(PROVIDED_RUNTIME_CONFIGURATION_NAME, platform)
                }

                if (project.plugins.hasPlugin('org.springframework.boot')) {
                    project.dependencies.add(PRODUCTION_RUNTIME_CLASSPATH_CONFIGURATION_NAME, platform)
                }
            }

            if (project.plugins.hasPlugin('org.springframework.boot')) {
                project.with {
                    springBoot {
                        buildInfo()
                    }
                }
            }

            def kotlinPlugin = project.plugins.findPlugin("org.jetbrains.kotlin.jvm")
            def javaPlugin = project.plugins.findPlugin(JavaPlugin.class)

            if (kotlinPlugin) {
                setupKotlinOutputDir(project, kotlinPlugin.pluginVersion)
            }

            if (project.jmix.entitiesEnhancing.enabled) {
                project.configurations.create('enhancing')
                project.dependencies.add('enhancing', 'org.eclipse.persistence:org.eclipse.persistence.jpa:5.0.0-1-jmix')

                def enhanceMainTask = project.tasks.register('enhanceJmixMain') {
                    doLast(new EnhancingAction('main'))
                }
                def enhanceTestTask = project.tasks.register('enhanceJmixTest') {
                    doLast(new EnhancingAction('test'))
                }

                def mainCompileTasks = []
                def testCompileTasks = []

                if (javaPlugin) {
                    mainCompileTasks.add(project.tasks.named('compileJava'))
                    testCompileTasks.add(project.tasks.named('compileTestJava'))
                }

                /**
                 * If project 100% kotlin, compileJava will not execute and EnhancingAction will not run.
                 * So we need run EnhancingAction after compileKotlin.
                 */
                if (kotlinPlugin) {
                    mainCompileTasks.add(project.tasks.named('compileKotlin'))
                    testCompileTasks.add(project.tasks.named('compileTestKotlin'))
                }

                def enhancedMainDir = project.file(EnhancingAction.enhancedClassesDir(project, 'main'))
                def enhancedTestDir = project.file(EnhancingAction.enhancedClassesDir(project, 'test'))

                enhanceMainTask.configure {
                    dependsOn(mainCompileTasks)
                    dependsOnClasspathArtifacts(project, it, 'main')
                    inputs.files(project.sourceSets.main.java.classesDirectory)
                            .withPropertyName('compiledClasses')
                            .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
                    inputs.files(project.sourceSets.main.compileClasspath)
                            .withNormalizer(org.gradle.api.tasks.ClasspathNormalizer)
                    outputs.dir(enhancedMainDir).withPropertyName('enhancedClasses')
                    outputs.dir(project.file(EnhancingAction.generatedDescriptorsDir(project, 'main'))).withPropertyName('descriptors')
                    outputs.cacheIf { true }
                }
                enhanceTestTask.configure {
                    dependsOn(testCompileTasks)
                    dependsOnClasspathArtifacts(project, it, 'test')
                    inputs.files(project.sourceSets.test.java.classesDirectory)
                            .withPropertyName('compiledClasses')
                            .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
                    inputs.files(project.sourceSets.test.compileClasspath)
                            .withNormalizer(org.gradle.api.tasks.ClasspathNormalizer)
                    outputs.dir(enhancedTestDir).withPropertyName('enhancedClasses')
                    outputs.dir(project.file(EnhancingAction.generatedDescriptorsDir(project, 'test'))).withPropertyName('descriptors')
                    outputs.cacheIf { true }
                }

                project.sourceSets.main.output.classesDirs.setFrom(enhancedMainDir)
                project.sourceSets.main.output.classesDirs.builtBy(enhanceMainTask)
                project.tasks.named(project.sourceSets.main.classesTaskName) { dependsOn(enhanceMainTask) }

                project.sourceSets.test.output.classesDirs.setFrom(enhancedTestDir)
                project.sourceSets.test.output.classesDirs.builtBy(enhanceTestTask)
                project.tasks.named(project.sourceSets.test.classesTaskName) { dependsOn(enhanceTestTask) }

                registerGeneratedDescriptors(project, 'main', enhanceMainTask)
                registerGeneratedDescriptors(project, 'test', enhanceTestTask)

                if (kotlinPlugin) {
                    redirectKotlinArchiveOutput(project, 'main', enhancedMainDir)
                }
            }

            if (isJmixApp(project)) {
                def configurations = project.configurations.collect()

                //todo SB3 do we still need to exclude org.slf4j?
                // Exclude second logger to prevent collisions with Logback
                configurations.each {
                    it.exclude(group: 'org.slf4j', module: 'slf4j-jdk14')
                }
            }
        }

        project.task([type: ZipProject], 'zipProject')

        registerCleanConfTask(project)
    }

    /**
     * Kotlin classes output dir should be the same as java output dir.
     * Otherwise the current implementation of entities enhancing doesn't work properly
     */
    private void setupKotlinOutputDir(Project project, String kotlinPluginVersion) {
        try {
            def kotlinPluginVersionNumbers = kotlinPluginVersion.split('\\.')
                    .collect { it.toInteger() }
            int majorVersion = kotlinPluginVersionNumbers[0]
            int minorVersion = kotlinPluginVersionNumbers[1]
            project.tasks.getByName('compileKotlin', {task ->
                if (majorVersion == 1 && minorVersion < 7) {
                    task.destinationDir = project.sourceSets.main.java.destinationDirectory.get()
                } else {
                    task.destinationDirectory = project.sourceSets.main.java.destinationDirectory
                }
            })
            project.tasks.getByName('compileTestKotlin', {task ->
                if (majorVersion == 1 && minorVersion < 7) {
                    task.destinationDir = project.sourceSets.test.java.destinationDirectory.get()
                } else {
                    task.destinationDirectory = project.sourceSets.test.java.destinationDirectory
                }
            })
        } catch (UnknownTaskException ignored) {
            project.logger.debug("Unable to setup output directory for Kotlin. " + ignored.message)
        }
    }

    private static void registerGeneratedDescriptors(Project project, String sourceSetName, enhanceTask) {
        def sourceSet = project.sourceSets.getByName(sourceSetName)
        def generatedDir = project.file(EnhancingAction.generatedDescriptorsDir(project, sourceSetName))

        // Generated persistence.xml/orm.xml are registered as a first-class generated output of the source
        // set: they reach the runtime classpath and the jar without being reprocessed by processResources.
        sourceSet.output.dir([builtBy: enhanceTask], generatedDir)
    }

    /**
     * The Kotlin JVM plugin adds the raw compile output directory to archive tasks (e.g. 'jar')
     * through a source path that bypasses sourceSet.output. Since enhancement produces a separate
     * enhanced output directory, that raw path would put un-enhanced (and duplicate) classes into
     * the archive. Redirect any archive source that points at the raw compiled-classes directory
     * to the enhanced directory.
     */
    private static void redirectKotlinArchiveOutput(Project project, String sourceSetName, File enhancedDir) {
        def sourceSet = project.sourceSets.getByName(sourceSetName)
        def rawDir = sourceSet.java.classesDirectory.get().asFile

        def archiveTaskNames = [sourceSet.jarTaskName]
        if (project.plugins.hasPlugin('org.springframework.boot')) {
            archiveTaskNames.add('bootJar')
        }

        archiveTaskNames.each { taskName ->
            try {
                project.tasks.named(taskName).configure { task ->
                    redirectArchiveSources(task, rawDir, enhancedDir)
                }
            } catch (UnknownTaskException ignored) {
                // No such archive task in this project.
            }
        }
    }

    private static void redirectArchiveSources(task, File rawDir, File enhancedDir) {
        // Walk the archive's CopySpec tree and redirect directory sources pointing at the raw dir.
        // Relies on the internal CopySpec structure (rootSpec/children/sourcePaths); guarded so that
        // a non-directory or read-only source is skipped rather than failing the build.
        def walk
        walk = { spec ->
            spec.sourcePaths.each { sourcePath ->
                try {
                    if (sourcePath.respondsTo('getOrNull') && sourcePath.getOrNull()?.asFile == rawDir) {
                        sourcePath.set(enhancedDir)
                    }
                } catch (ignored) {
                    // Source path is not a redirectable directory property.
                }
            }
            spec.children.each { child -> walk(child) }
        }
        walk(task.rootSpec)
    }

    private static void dependsOnClasspathArtifacts(Project project, task, String sourceSetName) {
        def sourceSet = project.sourceSets.getByName(sourceSetName)

        [
                sourceSet.compileClasspathConfigurationName,
                sourceSet.runtimeClasspathConfigurationName
        ].each { configurationName ->
            task.dependsOn(project.configurations.getByName(configurationName))
        }
    }

    private boolean isJmixApp(Project project) {
        !project.plugins.hasPlugin('io.jmix.build')
    }

    String getBomVersion() {
        String result = null
        Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF")
        while (resources.hasMoreElements()) {
            try {
                Manifest manifest = new Manifest(resources.nextElement().openStream())
                String bomVersion = manifest.mainAttributes.getValue('Jmix-BOM-Version')
                if (bomVersion) {
                    if (result && result != bomVersion) {
                        throw new IllegalStateException("More than one manifest in plugin's classpath define Jmix-BOM-Version, and they are different." +
                                " Set jmix.bomVersion property in the project.")
                    }
                    result = bomVersion
                }
            } catch (IOException e) {
                throw new RuntimeException(e)
            }
        }
        return result ?: 'unspecified'
    }

    private static void registerCleanConfTask(Project project) {
        project.tasks.register('cleanConf', Delete) {
            doFirst {
                if (project.jmix.confDirCleanupEnabled) {
                    def resources = project.file("src/main/resources/")
                    if (resources.exists() && resources.isDirectory()) {
                        def mainProperties = loadProperties(project)
                        def confDir = resolveConfDir(project, mainProperties)

                        project.logger.lifecycle("Delete directory: {}", confDir)
                        delete "${confDir}"
                    } else {
                        project.logger.lifecycle("Resource directory not found")
                        return
                    }
                } else {
                    project.logger.lifecycle("'conf' directory cleanup is disabled")
                }
            }
        }
        project.pluginManager.withPlugin("org.springframework.boot") {
            project.tasks.named("bootRun") {
                dependsOn("cleanConf")
            }
        }
    }

    private static Properties loadProperties(Project project, String profileName = null) {
        project.logger.lifecycle("Load properties (profile = $profileName)")

        def appPropertiesFile = getAppPropertiesFile(project, profileName)
        def yamlPropertiesFile = getYamlPropertiesFile(project, profileName)

        if (appPropertiesFile.exists()) {
            project.logger.lifecycle("Found file: {}", appPropertiesFile.getName())
            return loadPropertiesFromAppPropertiesFile(appPropertiesFile)
        } else if (yamlPropertiesFile.exists()) {
            project.logger.lifecycle("Found file: {}", yamlPropertiesFile.getName())
            return loadPropertiesFromYamlPropertiesFile(yamlPropertiesFile)
        } else {
            project.logger.lifecycle("File with properties is not found")
            return new Properties()
        }
    }

    private static File getAppPropertiesFile(Project project, String profileName = null) {
        def preparedProfileName = (profileName?.trim() ?: "").with { name -> name ? "-$name" : "" }
        def fileName = "src/main/resources/application%s.properties".formatted(preparedProfileName)
        return project.file(fileName)
    }

    private static File getYamlPropertiesFile(Project project, String profileName = null) {
        def basePath = "src/main/resources"
        def templates = ["application%s.yaml", "application%s.yml"]
                .collect { template ->
                    def preparedProfileName = (profileName?.trim() ?: "").with { name -> name ? "-$name" : "" }
                    def fileName = "$template".formatted(preparedProfileName)
                    project.file("$basePath/$fileName")
                }

        return templates.find { it.exists() } ?: templates.first()
    }

    private static Properties loadPropertiesFromAppPropertiesFile(File appPropsFile) {
        Properties properties = new Properties()
        appPropsFile.withInputStream { properties.load(it) }
        return properties;
    }

    private static Properties loadPropertiesFromYamlPropertiesFile(File yamlPropsFile) {
        Yaml yaml = new Yaml()
        Map<String, Object> yamlMap
        yamlPropsFile.withInputStream { yamlMap = yaml.load(it) }

        Properties properties = new Properties()
        flattenPropertiesMap("", yamlMap, properties)
        return properties;
    }

    private static void flattenPropertiesMap(String prefix, Map<String, Object> map, Properties props) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                flattenPropertiesMap(key, (Map<String, Object>) value, props);
            } else {
                props.put(key, value.toString());
            }
        }
    }

    private static String resolveConfDir(Project project, Properties mainProperties) {
        def profilesList = resolveActiveProfiles(project, mainProperties)

        def confDir = null
        if (!profilesList.isEmpty()) {
            for (def profileName : profilesList) {
                project.logger.lifecycle("Check profile: {}", profileName)
                def profileProperties = loadProperties(project, profileName)
                confDir = profileProperties.getProperty("jmix.core.conf-dir") ?: profileProperties.getProperty("jmix.core.confDir") ?: null
                if (confDir != null) {
                    break
                }
            }
        }

        if (confDir == null) {
            confDir = mainProperties.getProperty("jmix.core.conf-dir") ?: mainProperties.getProperty("jmix.core.confDir") ?: "${project.rootDir}/.jmix/conf"
        }

        return confDir
    }

    private static List<String> resolveActiveProfiles(Project project, Properties mainProperties) {
        String profiles
        if (project.hasProperty("spring.profiles.active")) {
            profiles = project.property("spring.profiles.active")
        } else {
            profiles = mainProperties.getProperty("spring.profiles.active") ?: null
        }

        def profilesList = []
        if (profiles != null) {
            def split = profiles.trim().replaceAll(/^\[(.*)\]$/, '$1').split(",")
            profilesList = Arrays.stream(split).map { s -> s.trim().toLowerCase() }.toList()
        }
        return profilesList
    }
}
