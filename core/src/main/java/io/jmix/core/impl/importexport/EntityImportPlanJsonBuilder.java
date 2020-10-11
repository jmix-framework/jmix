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

package io.jmix.core.impl.importexport;

import io.jmix.core.EntityImportPlan;
import io.jmix.core.EntityImportPlanProperty;
import io.jmix.core.ReferenceImportBehaviour;
import io.jmix.core.metamodel.model.MetaClass;

/**
 * Class that is used for building an {@link EntityImportPlan} based on the JSON object that represents an entity.
 */
public interface EntityImportPlanJsonBuilder {

    /**
     * Builds an {@link EntityImportPlan} that contains all fields that are presented in the JSON object.
     * <p>
     * All references will be added to the plan as a {@link ReferenceImportBehaviour#ERROR_ON_MISSING} behavior. All
     * references that have a @Composition annotation will be added to the plan with a property that has a {@link
     * EntityImportPlanProperty}. This means that compositions will be persisted during the import. Absent collection
     * items will be removed from the database.
     * <p>
     * For many-to-many association items corresponding entities will be searched in the database. If any of them is
     * missing, an error will be thrown. Absent collection members will be excluded from the many-to-many association.
     *
     * @param json      a string that represents a JSON object
     * @param metaClass a MetaClass of the entity
     * @return an EntityImportPlan
     */
    EntityImportPlan buildFromJson(String json, MetaClass metaClass);
}
