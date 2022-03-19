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

package io.jmix.core.impl;

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.impl.serialization.EntitySerializationImpl;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("core_IdSerialization")
public class IdSerializationImpl implements IdSerialization {

    @Autowired
    private Metadata metadata;

    @Autowired
    private MetadataTools metadataTools;

    @Autowired
    private EntitySerialization entitySerialization;

    private static final EntitySerializationOption[] SERIALIZATION_OPTIONS = {EntitySerializationOption.SERIALIZE_NULLS};

    @Override
    public String idToString(Id<?> entityId) {
        Preconditions.checkNotNullArgument(entityId, "entityId is null");

        MetaClass metaClass = metadata.getClass(entityId.getEntityClass());
        String json = entitySerialization.objectToJson(entityId.getValue(), SERIALIZATION_OPTIONS);
        return metaClass + "." + json;
    }

    @Override
    public <T> Id<T> stringToId(String ref) {
        Preconditions.checkNotEmptyString(ref, "String reference is empty");

        int i = ref.indexOf('.');
        String entityName = ref.substring(0, i);
        String idJson = ref.substring(i + 1);

        MetaClass metaClass = metadata.getClass(entityName);

        MetaProperty pkProp = metadataTools.getPrimaryKeyProperty(metaClass);
        if (pkProp == null) {
            throw new RuntimeException("Cannot determine PK for entity " + metaClass.getName());
        }
        Object id;
        if (pkProp.getRange().isDatatype()) {
            Class<?> javaClass = pkProp.getRange().asDatatype().getJavaClass();
            id = entitySerialization.objectFromJson(idJson, javaClass, SERIALIZATION_OPTIONS);
        } else if (pkProp.getRange().isClass()) {
            id = entitySerialization.entityFromJson(idJson, pkProp.getRange().asClass(), SERIALIZATION_OPTIONS);
        } else {
            throw new UnsupportedOperationException();
        }

        return Id.of(id, metaClass.getJavaClass());
    }
}
