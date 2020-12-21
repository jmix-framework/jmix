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

package io.jmix.data;

import io.jmix.core.Entity;
import io.jmix.core.EntityEntryExtraState;
import io.jmix.core.EntityValuesProvider;
import io.jmix.data.impl.EntityAttributeChanges;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ExtraAttributesHelper {

    /**
     * @return {@code entity} changes summary for regular and extra attributes
     */
    public static EntityAttributeChanges getChanges(Entity entity) {
        EntityAttributeChanges changes = new EntityAttributeChanges();
        changes.addChanges(entity);
        return changes;
    }

    /**
     * @return true if attribute with specified {@code name} belongs to one of extra states of current {@code entity}
     */
    public static boolean isExtraAttribute(String name, Object entity) {
        if (entity instanceof Entity) {
            return ((Entity) entity).__getEntityEntry().getAllExtraState().stream()
                    .anyMatch(state -> state instanceof EntityValuesProvider && ((EntityValuesProvider) state).supportAttribute(name));
        }
        return false;
    }

    /**
     * Examines extra attributes loaded in specified {@code entity}.
     *
     * @return set of extra attribute names for <b>current</b> entity. Empty set if entity loaded without extra state.
     */
    public static Set<String> getExtraAttributes(Object entity) {
        Set<String> extra = new HashSet<>();
        if (entity instanceof Entity) {
            Collection<EntityEntryExtraState> extraStates = ((Entity) entity).__getEntityEntry().getAllExtraState();
            for (EntityEntryExtraState state : extraStates) {
                if (state instanceof EntityValuesProvider) {
                    for (String attribute : ((EntityValuesProvider) state).getAttributes()) {
                        extra.add('+' + attribute);
                    }
                }
            }
        }
        return extra;
    }
}
