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

import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.util.Set;

/**
 * Used in {@link EnhancingTask} to run provider-specific enhancing logic.
 * <p>
 * Runs at task execution time, so it must not use {@code Project}: everything it needs is passed as parameters.
 */
public interface PersistenceProviderEnhancing {

    /**
     * @param logger           logger of the enhancing task
     * @param sourceSetName    name of the enhanced source set
     * @param compileClasspath root of the resolved compile classpath dependency graph of the source set
     * @param weavingClasspath classpath for running the provider's weaver
     * @param workDir          work directory of the enhancing task, contains the generated
     *                         {@code [store-]persistence/META-INF/persistence.xml} files
     * @param enhancedDir      directory with the classes to enhance in place
     * @param allStores        names of the data stores to enhance the classes for
     * @param debug            whether to start the weaver in debug mode
     */
    void run(Logger logger, String sourceSetName, ResolvedComponentResult compileClasspath,
             FileCollection weavingClasspath, File workDir, String enhancedDir, Set<String> allStores,
             boolean debug);
}
