/*
 * Copyright 2021 Haulmont.
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

package io.jmix.graphql.modifier;

import io.jmix.core.EntityImportPlan;
import io.jmix.core.metamodel.model.MetaClass;

import java.util.List;

public class GraphQLUpsertEntityDataFetcherContext<E> {
    private MetaClass metaClass;
    private List<E> entities;
    private EntityImportPlan entityImportPlan;

    public GraphQLUpsertEntityDataFetcherContext(MetaClass metaClass, List<E> entities, EntityImportPlan entityImportPlan) {
        this.metaClass = metaClass;
        this.entities = entities;
        this.entityImportPlan = entityImportPlan;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    public List<E> getEntities() {
        return entities;
    }

    public void setEntities(List<E> entities) {
        this.entities = entities;
    }

    public EntityImportPlan getEntityImportPlan() {
        return entityImportPlan;
    }

    public void setEntityImportPlan(EntityImportPlan entityImportPlan) {
        this.entityImportPlan = entityImportPlan;
    }
}
