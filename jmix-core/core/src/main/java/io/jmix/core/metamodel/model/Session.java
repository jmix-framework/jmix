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

package io.jmix.core.metamodel.model;

import org.springframework.lang.Nullable;
import java.util.Collection;

/**
 * Meta-model entry point.
 */
public interface Session {

    /**
     * Search MetaClass by its name in the whole metamodel.
     * @param name  entity name
     * @return      MetaClass instance or null if not found
     */
    @Nullable
    MetaClass findClass(String name);

    /**
     * Search MetaClass by its name in the whole metamodel.
     * @param name  entity name
     * @return      MetaClass instance. Throws exception if not found.
     */
    MetaClass getClass(String name);

    /**
     * Search MetaClass by the corresponding Java class in the whole metamodel.
     * @param javaClass Java class defining the entity
     * @return      MetaClass instance or null if not found
     */
    @Nullable
    MetaClass findClass(Class<?> javaClass);

    /**
     * Search MetaClass by the corresponding Java class in the whole metamodel.
     * @param javaClass Java class defining the entity
     * @return      MetaClass instance. Throws exception if not found.
     */
    MetaClass getClass(Class<?> javaClass);

    /**
     * @return collection of all MetaClasses in the whole metamodel
     */
    Collection<MetaClass> getClasses();
}
