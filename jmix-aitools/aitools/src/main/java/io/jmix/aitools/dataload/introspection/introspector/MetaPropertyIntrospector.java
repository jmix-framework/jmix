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

package io.jmix.aitools.dataload.introspection.introspector;

import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.aitools.dataload.introspection.model.EntityPropertyDescriptor;
import org.jspecify.annotations.Nullable;

public interface MetaPropertyIntrospector {

    /**
     * Checks if this introspector can handle the given {@link MetaProperty} type.
     *
     * @param property the {@link MetaProperty} to check
     * @return true if this introspector can handle this property type
     */
    boolean supports(MetaProperty property);

    /**
     * Introspects a {@link MetaProperty} to an AI-optimized property descriptor.
     *
     * @param property the {@link MetaProperty} to introspect
     * @return {@link EntityPropertyDescriptor} representation of this property or {@code null} if this introspector
     * cannot handle it
     */
    @Nullable
    EntityPropertyDescriptor introspect(MetaProperty property);
}
