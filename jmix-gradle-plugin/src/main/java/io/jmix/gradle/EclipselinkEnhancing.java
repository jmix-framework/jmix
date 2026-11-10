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

import org.gradle.api.artifacts.ModuleVersionIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class EclipselinkEnhancing implements PersistenceProviderEnhancing {

    private final ExecOperations execOperations;

    @Inject
    public EclipselinkEnhancing(ExecOperations execOperations) {
        this.execOperations = execOperations;
    }

    @Override
    public void run(Logger logger, String sourceSetName, ResolvedComponentResult compileClasspath,
                    FileCollection weavingClasspath, File workDir, String enhancedDir, Set<String> allStores,
                    boolean debug) {
        for (String storeName : allStores) {
            if (!findJpaDependencies(compileClasspath, new HashSet<>())) {
                logger.info("Jpa dependencies not found in classpath, EclipseLink enhancer will not run");
                return;
            }

            logger.lifecycle("Running EclipseLink enhancer for source set '{}'", sourceSetName);

            execOperations.javaexec(javaExecSpec -> {
                javaExecSpec.getMainClass().set("org.eclipse.persistence.tools.weaving.jpa.StaticWeave");

                javaExecSpec.setClasspath(weavingClasspath);

                javaExecSpec.args("-loglevel", "INFO", "-persistenceinfo",
                        new File(workDir, ("main".equals(storeName) ? "" : (storeName + '-')) + "persistence").getPath(),
                        enhancedDir,
                        enhancedDir
                );
                javaExecSpec.setDebug(debug);
            });
        }
    }

    /**
     * Returns {@code true} if the dependency graph below the given component contains the EclipseLink core module.
     */
    protected boolean findJpaDependencies(ResolvedComponentResult component, Set<ResolvedComponentResult> visited) {
        if (!visited.add(component)) {
            return false;
        }
        ModuleVersionIdentifier module = component.getModuleVersion();
        if (module != null
                && "org.eclipse.persistence".equals(module.getGroup())
                && "org.eclipse.persistence.core".equals(module.getName())) {
            return true;
        }
        for (DependencyResult dependency : component.getDependencies()) {
            if (dependency instanceof ResolvedDependencyResult
                    && findJpaDependencies(((ResolvedDependencyResult) dependency).getSelected(), visited)) {
                return true;
            }
        }
        return false;
    }
}
