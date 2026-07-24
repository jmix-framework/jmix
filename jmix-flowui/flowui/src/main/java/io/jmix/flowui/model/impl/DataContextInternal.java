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

package io.jmix.flowui.model.impl;

import io.jmix.flowui.model.DataContext;
import io.jmix.flowui.model.DataContextChanges;
import org.jspecify.annotations.NullMarked;

import java.util.Set;

/**
 * Marker interface of {@link DataContext} implementations that can form hierarchies
 * using {@link DataContext#setParent(DataContext)}.
 */
@NullMarked
public interface DataContextInternal extends DataContext, DataContextChanges {

    /**
     * Merges an entity saved by a child context into this (parent) context.
     * <p>
     * Unlike a plain {@link #merge(Object)}, the child's dirty attributes override this
     * context's own unsaved edits of the same attributes (the child's later intent wins),
     * and are then registered as dirty here so the parent's eventual save carries them.
     *
     * @param entity               the entity instance saved by the child context
     * @param childDirtyAttributes attributes the child tracked as changed for this entity
     * @return the managed instance of this context
     */
    default Object mergeFromChild(Object entity, Set<String> childDirtyAttributes) {
        return merge(entity);
    }
}
