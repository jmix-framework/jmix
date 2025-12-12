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
import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component("core_EntityElementCollectionInitializer")
public class EntityElementCollectionInitializer implements EntityInitializer, Ordered {

    private static final Logger log = LoggerFactory.getLogger(EntityElementCollectionInitializer.class);

    @Autowired
    private Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;

    @Override
    public void initEntity(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        for (MetaProperty property : metaClass.getProperties()) {
            if (metadataTools.isElementCollection(property)) {
                if (List.class.isAssignableFrom(property.getJavaType())) {
                    EntityValues.setValue(entity, property.getName(), new ArrayList<>());
                } else if (Set.class.isAssignableFrom(property.getJavaType())) {
                    EntityValues.setValue(entity, property.getName(), new HashSet<>());
                } else {
                    log.warn("Unsupported element collection type {} of property {}.{}",
                            property.getJavaType(), metaClass.getName(), property.getName());
                }
            }
        }
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 25;
    }
}
