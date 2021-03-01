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

package io.jmix.data.impl.jpql;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.data.impl.jpql.model.JpqlEntityModel;
import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NotThreadSafe
public class DomainModel {
    protected Map<String, JpqlEntityModel> entities = new HashMap<>();
    protected ExtendedEntities extendedEntities;
    protected Metadata metadata;

    public DomainModel(ExtendedEntities extendedEntities, Metadata metadata, JpqlEntityModel... initialEntities) {
        this(initialEntities);
        this.extendedEntities = extendedEntities;
        this.metadata = metadata;
    }

    public DomainModel(JpqlEntityModel... initialEntities) {
        for (JpqlEntityModel initialEntity : initialEntities) {
            add(initialEntity);
        }
    }

    public void add(JpqlEntityModel entity) {
        if (entity == null)
            throw new NullPointerException("No entity passed");

        entities.put(entity.getName(), entity);
    }

    public List<JpqlEntityModel> findEntitiesStartingWith(String lastWord) {
        List<JpqlEntityModel> result = entities.values().stream()
                .filter(entity -> entity.getName().startsWith(lastWord))
                .collect(Collectors.toList());
        return result;
    }

    public JpqlEntityModel getEntityByName(String requiredEntityName) throws UnknownEntityNameException {
        if (extendedEntities != null) {
            if (metadata.getSession().findClass(requiredEntityName) == null) {
                throw new UnknownEntityNameException(requiredEntityName);
            }
            MetaClass effectiveMetaClass = extendedEntities.getEffectiveMetaClass(requiredEntityName);
            requiredEntityName = effectiveMetaClass.getName();
        }

        JpqlEntityModel entity = entities.get(requiredEntityName);
        if (entity == null) {
            throw new UnknownEntityNameException(requiredEntityName);
        } else {
            return entity;
        }
    }
}
