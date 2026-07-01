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

package io.jmix.gradle;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.tasks.SourceSet;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EclipselinkEnhancing implements PersistenceProviderEnhancing {

    private final ExecOperations execOperations;

    @Inject
    public EclipselinkEnhancing(ExecOperations execOperations) {
        this.execOperations = execOperations;
    }

    public void run(Project project, SourceSet sourceSet, String enhancedDir, Set<String> allStores) {
        for (String storeName : allStores) {
            Configuration conf = project.getConfigurations().findByName(sourceSet.getCompileClasspathConfigurationName());
            if (!findJpaDependencies(conf.getResolvedConfiguration().getFirstLevelModuleDependencies(), new HashSet<>())) {
                project.getLogger().info("Jpa dependencies not found in classpath, EclipseLink enhancer will not run");
                return;
            }

            project.getLogger().lifecycle("Running EclipseLink enhancer in {} for {}", project, sourceSet);

            execOperations.javaexec(javaExecSpec -> {
                javaExecSpec.getMainClass().set("org.eclipse.persistence.tools.weaving.jpa.StaticWeave");

                javaExecSpec.setClasspath(project.files(
                        project.getConfigurations().getByName("enhancing").getAsFileTree(),
                        sourceSet.getCompileClasspath().getFiles(),
                        project.files(new File(enhancedDir)).getAsFileTree()));

                javaExecSpec.args("-loglevel", "INFO", "-persistenceinfo",
                        project.getBuildDir() + "/tmp/entitiesEnhancing/" + sourceSet.getName() + "/" + (("main".equals(storeName) ? "" : (storeName + '-')) + "persistence"),
                        enhancedDir,
                        enhancedDir
                );
                javaExecSpec.setDebug(project.hasProperty("debugEnhancing") && Boolean.parseBoolean((String) project.property("debugEnhancing")));
            });
        }
    }

    protected boolean findJpaDependencies(Set<ResolvedDependency> deps, Set visited) {
        for (ResolvedDependency dep : deps) {
            if (!visited.contains(dep)) {
                if ("org.eclipse.persistence".equals(dep.getModuleGroup()) && "org.eclipse.persistence.core".equals(dep.getModuleName()))
                    return true;
                visited.add(dep);
                if (findJpaDependencies(dep.getChildren(), visited))
                    return true;
            }
        }
        return false;
    }
}
