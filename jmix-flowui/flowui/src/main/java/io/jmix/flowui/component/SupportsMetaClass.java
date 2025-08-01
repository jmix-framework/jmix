/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component;

import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.lang.Nullable;

/**
 * Provides a contract for components that support working with a {@link MetaClass}.
 */
public interface SupportsMetaClass {

    /**
     * Returns the {@link MetaClass} associated with this component or entity.
     *
     * @return the {@link MetaClass} object, or {@code null} if no MetaClass is associated
     */
    @Nullable
    MetaClass getMetaClass();

    /**
     * Sets the {@link MetaClass} for this component or entity.
     *
     * @param metaClass the {@link MetaClass} to associate with this component or entity,
     *                  or {@code null} to disassociate any currently set MetaClass
     */
    void setMetaClass(@Nullable MetaClass metaClass);
}
