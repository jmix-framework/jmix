/*
 * Copyright 2024 Haulmont.
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

package io.jmix.restds.impl;

import io.jmix.core.EntitySerialization;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("restds_RestSerialization")
public class RestSerialization {

    private final EntitySerialization entitySerialization;

    private final Metadata metadata;

    public RestSerialization(EntitySerialization entitySerialization, Metadata metadata) {
        this.entitySerialization = entitySerialization;
        this.metadata = metadata;
    }

    public String toJson(Object entity) {
        return entitySerialization.toJson(entity);
    }

    public String toJson(Object entity, FetchPlan fetchPlan) {
        return entitySerialization.toJson(entity, fetchPlan);
    }

    @Nullable
    public <E> E fromJson(@Nullable String json, Class<E> entityClass) {
        if (json == null) {
            return null;
        }
        MetaClass metaClass = metadata.getClass(entityClass);
        return entitySerialization.entityFromJson(json, metaClass);
    }

    public <E> List<E> fromJsonCollection(String json, Class<E> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        return (List<E>) entitySerialization.entitiesCollectionFromJson(json, metaClass);
    }
}
