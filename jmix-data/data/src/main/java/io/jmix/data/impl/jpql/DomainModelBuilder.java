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
import io.jmix.core.MessageTools;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.data.impl.jpql.model.EntityBuilder;
import io.jmix.data.impl.jpql.model.JpqlEntityModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * INTERNAL.
 * Generates domain model for use in JPQL parser.
 */
@Internal
@Component("data_DomainModelBuilder")
@Qualifier("regular")
public class DomainModelBuilder {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected ExtendedEntities extendedEntities;

    protected boolean loadCaptions;

    public DomainModel produce() {
        Collection<MetaClass> classes = metadata.getSession().getClasses();
        DomainModel result = new DomainModel(extendedEntities, metadata);

        EntityBuilder builder = EntityBuilder.create();
        for (MetaClass aClass : classes) {
            builder.startNewEntity(aClass.getName());

            Collection<MetaProperty> props = aClass.getProperties();
            for (MetaProperty prop : props) {
                if (metadataTools.isJpa(prop))
                    addProperty(builder, aClass, prop);
            }

            JpqlEntityModel entity = builder.produce();
            result.add(entity);
        }
        return result;
    }

    private void addProperty(EntityBuilder builder, MetaClass metaClass, MetaProperty prop) {
        String name = prop.getName();
        String userFriendlyName = null;
        if (loadCaptions) {
            userFriendlyName = messageTools.getPropertyCaption(metaClass, prop.getName());
        }
        boolean isEmbedded = metadataTools.isEmbedded(prop);
        MetaProperty.Type type = prop.getType();
        Class<?> javaType = prop.getJavaType();
        Range range = prop.getRange();
        switch (type) {
            case EMBEDDED:
            case COMPOSITION:
            case ASSOCIATION:
                if (range.isClass()) {
                    MetaClass rangeClass = range.asClass();
                    if (range.getCardinality().isMany()) {
                        builder.addCollectionReferenceAttribute(name, rangeClass.getName(), userFriendlyName);
                    } else {
                        builder.addReferenceAttribute(name, rangeClass.getName(), userFriendlyName, isEmbedded);
                    }
                } else {
                    builder.addSingleValueAttribute(javaType, name, userFriendlyName);
                }
                break;
            case ENUM:
                builder.addSingleValueAttribute(javaType, name, userFriendlyName);
                break;
            case DATATYPE:
                builder.addSingleValueAttribute(javaType, name, userFriendlyName);
                break;
        }
    }
}