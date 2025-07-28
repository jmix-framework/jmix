/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.listener;

import io.jmix.core.Id;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.DynamicAttributes;

import java.util.Set;

public interface DependentEntitiesResolver {
    Set<Id<?>> getEntityIdsDependentOnUpdatedEntity(Id<?> updatedEntityId, MetaClass metaClass, AttributeChanges changes);

    Set<Id<?>> getEntityIdsDependentOnUpdatedEntity(Id<Object> updatedEntityId, MetaClass metaClass, DynamicAttributes dynamicAttributes);

    Set<Id<?>> getEntityIdsDependentOnRemovedEntity(Id<?> removedEntityId, MetaClass metaClass);
}
