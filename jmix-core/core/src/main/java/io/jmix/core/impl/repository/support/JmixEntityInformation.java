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

package io.jmix.core.impl.repository.support;

import io.jmix.core.EntityStates;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import org.springframework.data.repository.core.support.AbstractEntityInformation;

import java.util.Objects;

public class JmixEntityInformation<T, ID> extends AbstractEntityInformation<T, ID> {

    protected EntityStates entityStates;
    protected MetadataTools metadataTools;

    public JmixEntityInformation(Class<T> domainClass, EntityStates entityStates, MetadataTools metadataTools) {
        super(domainClass);
        this.entityStates = entityStates;
        this.metadataTools = metadataTools;
    }

    @Override
    public ID getId(T object) {
        Objects.requireNonNull(object, "entity is null");

        //noinspection unchecked
        return (ID) EntityValues.getId(object);
    }

    @Override
    public Class<ID> getIdType() {
        //noinspection unchecked
        return (Class<ID>) metadataTools.getPrimaryKeyProperty(getJavaType()).getJavaType();
    }

    @Override
    public boolean isNew(T entity) {
        return entityStates.isNew(entity);
    }
}
