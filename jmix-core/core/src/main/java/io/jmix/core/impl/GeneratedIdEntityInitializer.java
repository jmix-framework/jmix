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

package io.jmix.core.impl;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("core_GeneratedIdEntityInitializer")
public class GeneratedIdEntityInitializer implements EntityInitializer, Ordered {

    @Autowired
    private Metadata metadata;

    @Override
    public void initEntity(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        metaClass.getProperties().stream()
                .filter(property -> property.getRange().isDatatype()
                        && property.getRange().asDatatype().getJavaClass().equals(UUID.class)
                        && property.getAnnotations().get(JmixGeneratedValue.class.getName()) != null)
                .forEach(property -> {
                    if (EntityValues.getValue(entity, property.getName()) == null) {
                        EntityValues.setValue(entity, property.getName(), UuidProvider.createUuid());
                    }
                });
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE;
    }
}
