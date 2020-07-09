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

import io.jmix.core.EntityInitializer;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.annotation.EmbeddedParameters;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Autowired;

@Component(EntityEmbeddedInitializer.NAME)
public class EntityEmbeddedInitializer implements EntityInitializer, Ordered {
    public static final String NAME = "core_EntityEmbeddedInitializer";

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;

    @Override
    public void initEntity(JmixEntity entity) {
        MetaClass metaClass = metadata.getClass(entity.getClass());
        for (MetaProperty property : metaClass.getProperties()) {
            if (property.getRange().isClass() && metadataTools.isEmbedded(property)) {
                EmbeddedParameters embeddedParameters = property.getAnnotatedElement().getAnnotation(EmbeddedParameters.class);
                if (embeddedParameters != null && !embeddedParameters.nullAllowed()) {
                    MetaClass embeddableMetaClass = property.getRange().asClass();
                    JmixEntity embeddableEntity = metadata.create(embeddableMetaClass);
                    EntityValues.setValue(entity, property.getName(), embeddableEntity);
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 20;
    }
}
