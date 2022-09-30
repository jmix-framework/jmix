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

package io.jmix.data.impl;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.List;
import java.util.stream.Collectors;

@Component("data_GeneratedNumberIdEntityInitializer")
public class GeneratedNumberIdEntityInitializer implements EntityInitializer, Ordered {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected NumberIdSource numberIdSource;
    @Autowired
    protected CoreProperties coreProperties;

    @Override
    public void initEntity(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        if (!coreProperties.isIdGenerationForEntitiesInAdditionalDataStoresEnabled()
                && !Stores.MAIN.equals(metaClass.getStore().getName())) {
            return;
        }

        metaClass.getProperties().stream()
                .filter(property -> property.getAnnotations().get(JmixGeneratedValue.class.getName()) != null && isNumberType(property))
                .findFirst()
                .ifPresent(property -> {
                    if (EntityValues.getValue(entity, property.getName()) == null) {
                        String entityName = getEntityNameForIdGeneration(metaClass);
                        if (property.getRange().asDatatype().getJavaClass().equals(Long.class)) {
                            EntityValues.setValue(entity, property.getName(), numberIdSource.createLongId(entityName));
                        } else {
                            EntityValues.setValue(entity, property.getName(), numberIdSource.createIntegerId(entityName));
                        }
                    }
                });
    }

    private boolean isNumberType(MetaProperty metaProperty) {
        if (metaProperty.getRange().isDatatype()) {
            Class javaClass = metaProperty.getRange().asDatatype().getJavaClass();
            return javaClass.equals(Long.class) || javaClass.equals(Integer.class);
        }
        return false;
    }

    private String getEntityNameForIdGeneration(MetaClass metaClass) {
        List<MetaClass> persistentAncestors = metaClass.getAncestors().stream()
                .filter(mc -> metadataTools.isJpaEntity(mc)) // filter out all mapped superclasses
                .collect(Collectors.toList());
        if (persistentAncestors.size() > 0) {
            MetaClass root = persistentAncestors.get(persistentAncestors.size() - 1);
            Class<?> javaClass = root.getJavaClass();
            Inheritance inheritance = javaClass.getAnnotation(Inheritance.class);
            if (inheritance == null || inheritance.strategy() != InheritanceType.TABLE_PER_CLASS) {
                // use root of inheritance tree if the strategy is JOINED or SINGLE_TABLE because ID is stored in the root table
                return root.getName();
            }
        }
        return metaClass.getName();
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 5;
    }
}
