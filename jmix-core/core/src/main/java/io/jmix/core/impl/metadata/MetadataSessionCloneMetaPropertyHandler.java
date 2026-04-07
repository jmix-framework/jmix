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

package io.jmix.core.impl.metadata;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import org.jspecify.annotations.Nullable;

/**
 * Extension point for cloning specialized {@link MetaProperty} implementations.
 * <p>
 * Intended for modules that contribute custom meta-property types which cannot be cloned by
 * the built-in logic of {@link MetadataSessionCloneSupport}.
 */
public interface MetadataSessionCloneMetaPropertyHandler {

    /**
     * Returns whether this handler can clone the given meta property type.
     *
     * @param metaProperty source meta property from the published metadata session
     * @return {@code true} if this handler should clone the property
     */
    boolean supports(MetaProperty metaProperty);

    /**
     * Creates a detached clone skeleton for the given source property.
     *
     * @param sourceMetaProperty source property being cloned
     * @param targetDomain cloned owning meta class
     * @param targetRange range remapped to cloned metadata objects
     * @return cloned meta property skeleton
     */
    MetaProperty cloneMetaProperty(MetaProperty sourceMetaProperty, MetaClass targetDomain, Range targetRange);

    /**
     * Copies handler-specific state that can be resolved only after all properties are cloned.
     *
     * @param sourceMetaProperty source property from the published session
     * @param targetMetaProperty cloned target property
     * @param targetInverse inverse property remapped to the cloned session, or {@code null}
     */
    void copyMetaPropertyState(MetaProperty sourceMetaProperty,
                               MetaProperty targetMetaProperty,
                               @Nullable MetaProperty targetInverse);
}
