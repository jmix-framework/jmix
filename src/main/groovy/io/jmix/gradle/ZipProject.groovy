/*
 * Copyright 2020 Haulmont.
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


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * Create ZIP archive with the current project including HSQL database.
 */
class ZipProject extends DefaultTask {

    @Input
    List<String> excludeFromZip = []

    @Input
    List<String> includeToZip = []

    @Option(option = "zipDir", description = "Where to place resulting ZIP")
    @Internal
    // don't respect the dir in up-to-date checks
    def zipDir = "${project.rootDir}"

    @Option(option = "zipFileName", description = "Resulting ZIP file name with extension")
    @Internal
    def zipFileName = "${project.name}.zip"

    @TaskAction
    def zipProject() {

        def tmpDir = "${project.buildDir}/zip"
        def tmpRootDir = "${project.buildDir}/zip/${project.name}"

        def includeToZip = []
        includeToZip += this.includeToZip

        def excludeFromZip = [
                'build',
                'deploy',
                'bower_components',
                'node_modules',
                '.iml'
        ]
        excludeFromZip += this.excludeFromZip

        String zipFilePath = "${zipDir}/${zipFileName}"

        project.logger.info("[ZipProject] Deleting old archive")
        // to exclude recursive packing
        project.delete(zipFilePath)

        project.logger.info("[ZipProject] Packing files from: ${project.rootDir}")
        project.copy {
            from '.'
            into tmpRootDir
            exclude { details ->
                String name = details.file.name
                if (isFileMatched(name, includeToZip)) return false
                // eclipse project files, gradle, git, idea (directory based), Mac OS files
                if (name.startsWith(".")) return true
                return isFileMatched(name, excludeFromZip)
            }
        }
        project.copy {
            from '.jmix/hsqldb'
            into "$tmpRootDir/.jmix/hsqldb"
        }

        ant.zip(destfile: zipFilePath, basedir: tmpDir)

        println("Zip archive has been created at '${project.file(zipFilePath).absolutePath}'")

        project.delete(tmpDir)
    }

    protected static boolean isFileMatched(String name, def rules) {
        for (String rule : rules) {
            if (rule.startsWith(".")) {     // extension
                if (name.endsWith(rule)) {
                    return true
                }
            } else {                        // file name
                if (name == rule) {
                    return true
                }
            }
        }
        return false
    }
}
