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

import org.gradle.api.Project

/**
 * Defines Jmix plugin parameters.
 */
class JmixExtension {

    private Project project

    /**
     * Jmix BOM version
     */
    String bomVersion

    /**
     * Project id that is used by Jmix Studio.
     */
    String projectId

    /**
     * If true, the Jmix BOM will be added to the project dependencies as a platform.
     */
    boolean useBom = true

    /**
     * Defines entities enhancing parameters.
     */
    EntitiesEnhancing entitiesEnhancing

    JmixExtension(Project project) {
        this.project = project
        entitiesEnhancing = new EntitiesEnhancing()
    }

    /**
     * Defines entities enhancing parameters.
     */
    void entitiesEnhancing(Closure closure) {
        project.configure(entitiesEnhancing, closure)
    }

    /**
     * Entity enhancing can be disabled in the project's build.gradle as follows:
     * <pre>
     * jmix {
     *     entitiesEnhancing {
     *         enabled = false
     *     }
     * }
     * </pre>
     * If the project's entities refer to JPA converters defined in another module, provide class names of the converters as follows:
     * <pre>
     * jmix {
     *     entitiesEnhancing {
     *         jpaConverters = ['com.company.module.entity.SomeConverter']
     *     }
     * }
     * </pre>
     */
    class EntitiesEnhancing {

        /**
         * Use this property to disable entity enhancing.
         */
        boolean enabled = true

        /**
         * If the project's entities refer to JPA converters defined in another module, provide class names of the converters as follows:
         * <pre>
         * entitiesEnhancing {
         *     jpaConverters = ['com.company.module.entity.SomeConverter']
         * }
         * </pre>
         */
        List<String> jpaConverters = []

        /**
         * The property indicates whether entity classes enhancing should be skipped in case there were no modifications
         * in entities since the last build. If set to "true" the plugin will check whether entity classes have been
         * changed since last build, and if they haven't been changed then enhancing step will be skipped. If set to
         * false, then entities enhancing steps will be executed on each classes compilation.
         */
        boolean skipUnmodifiedEntitiesEnhancing = true
    }
}
