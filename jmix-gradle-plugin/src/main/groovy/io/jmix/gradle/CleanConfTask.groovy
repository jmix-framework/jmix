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

package io.jmix.gradle

import org.gradle.api.file.DeleteSpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.yaml.snakeyaml.Yaml

import javax.inject.Inject

/**
 * Deletes the directory that the application uses as {@code jmix.core.conf-dir} at runtime.
 * <p>
 * The directory is found when the task runs, from the application properties of the project. The task never uses
 * {@link org.gradle.api.Project} at execution time, so it is compatible with the Gradle configuration cache.
 * The properties are set by {@link JmixPlugin}.
 */
@DisableCachingByDefault(because = "Deletes files and produces no output")
abstract class CleanConfTask extends Delete {

    /**
     * See {@link JmixExtension#confDirCleanupEnabled}.
     */
    @Internal
    abstract Property<Boolean> getCleanupEnabled()

    /**
     * Directory of the project. The application properties are loaded from its {@code src/main/resources}
     * directory, and relative conf dir paths are resolved against it.
     */
    @Internal
    abstract DirectoryProperty getProjectDirectory()

    /**
     * Value of the {@code spring.profiles.active} Gradle property, if it is set. It overrides the active profiles
     * from the application properties.
     */
    @Internal
    abstract Property<String> getActiveProfiles()

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations()

    @TaskAction
    @Override
    protected void clean() throws IOException {
        boolean confDirDeleted = false
        if (cleanupEnabled.get()) {
            def resources = resolveFile("src/main/resources/")
            if (resources.exists() && resources.isDirectory()) {
                def mainProperties = loadProperties()
                def confDir = resolveConfDir(mainProperties)

                logger.lifecycle("Delete directory: {}", confDir)
                // The task's own delete targets are not changed at execution time, so the directory is deleted here.
                confDirDeleted = fileSystemOperations.delete { DeleteSpec spec -> spec.delete(confDir) }.didWork
            } else {
                logger.lifecycle("Resource directory not found")
            }
        } else {
            logger.lifecycle("'conf' directory cleanup is disabled")
        }

        // Delete the paths added to the task in the build script, if any.
        super.clean()
        didWork = didWork || confDirDeleted
    }

    /**
     * Resolves the path against the project directory, like {@code Project.file()}.
     */
    protected File resolveFile(String path) {
        File file = new File(path)
        if (!file.isAbsolute()) {
            file = new File(projectDirectory.get().asFile, path)
        }
        return file.toPath().normalize().toFile()
    }

    protected Properties loadProperties(String profileName = null) {
        logger.lifecycle("Load properties (profile = $profileName)")

        def appPropertiesFile = appPropertiesFile(profileName)
        def yamlPropertiesFile = yamlPropertiesFile(profileName)

        if (appPropertiesFile.exists()) {
            logger.lifecycle("Found file: {}", appPropertiesFile.getName())
            return loadPropertiesFromAppPropertiesFile(appPropertiesFile)
        } else if (yamlPropertiesFile.exists()) {
            logger.lifecycle("Found file: {}", yamlPropertiesFile.getName())
            return loadPropertiesFromYamlPropertiesFile(yamlPropertiesFile)
        } else {
            logger.lifecycle("File with properties is not found")
            return new Properties()
        }
    }

    protected File appPropertiesFile(String profileName) {
        def preparedProfileName = (profileName?.trim() ?: "").with { name -> name ? "-$name" : "" }
        def fileName = "src/main/resources/application%s.properties".formatted(preparedProfileName)
        return resolveFile(fileName)
    }

    protected File yamlPropertiesFile(String profileName) {
        def basePath = "src/main/resources"
        def templates = ["application%s.yaml", "application%s.yml"]
                .collect { template ->
                    def preparedProfileName = (profileName?.trim() ?: "").with { name -> name ? "-$name" : "" }
                    def fileName = "$template".formatted(preparedProfileName)
                    resolveFile("$basePath/$fileName")
                }

        return templates.find { it.exists() } ?: templates.first()
    }

    protected static Properties loadPropertiesFromAppPropertiesFile(File appPropsFile) {
        Properties properties = new Properties()
        appPropsFile.withInputStream { properties.load(it) }
        return properties;
    }

    protected static Properties loadPropertiesFromYamlPropertiesFile(File yamlPropsFile) {
        Yaml yaml = new Yaml()
        Map<String, Object> yamlMap
        yamlPropsFile.withInputStream { yamlMap = yaml.load(it) }

        Properties properties = new Properties()
        flattenPropertiesMap("", yamlMap, properties)
        return properties;
    }

    protected static void flattenPropertiesMap(String prefix, Map<String, Object> map, Properties props) {
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

    /**
     * Resolves the directory used by the application as 'jmix.core.conf-dir' at runtime.
     *
     * The runtime resolves this property against its working directory, which is the project directory
     * both for the Gradle 'bootRun' task and for Studio run configurations. So relative paths and the
     * '${user.dir}' placeholder are resolved against the project directory, not the build root directory:
     * these are different directories when the application is a subproject with a custom 'projectDir'.
     */
    protected File resolveConfDir(Properties mainProperties) {
        def profilesList = resolveActiveProfiles(mainProperties)

        def confDir = null
        if (!profilesList.isEmpty()) {
            for (def profileName : profilesList) {
                logger.lifecycle("Check profile: {}", profileName)
                def profileProperties = loadProperties(profileName)
                confDir = getConfDirProperty(profileProperties)
                if (confDir != null) {
                    break
                }
            }
        }

        if (confDir == null) {
            confDir = getConfDirProperty(mainProperties) ?: JmixPlugin.DEFAULT_CONF_DIR
        }

        return resolveFile(expandPathPlaceholders(confDir))
    }

    protected static String getConfDirProperty(Properties properties) {
        return properties.getProperty("jmix.core.conf-dir") ?: properties.getProperty("jmix.core.confDir") ?: null
    }

    /**
     * Replaces the placeholders that the running application resolves in path properties.
     * '${user.dir}' becomes the project directory because that is the working directory of the application.
     */
    protected String expandPathPlaceholders(String path) {
        return path.replace('${user.dir}', projectDirectory.get().asFile.absolutePath)
                .replace('${user.home}', System.getProperty('user.home'))
    }

    protected List<String> resolveActiveProfiles(Properties mainProperties) {
        String profiles
        if (activeProfiles.isPresent()) {
            profiles = activeProfiles.get()
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
