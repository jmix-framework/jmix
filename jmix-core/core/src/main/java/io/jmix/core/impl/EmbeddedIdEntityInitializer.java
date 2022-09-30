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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component("core_EmbeddedIdEntityInitializer")
public class EmbeddedIdEntityInitializer implements EntityInitializer, Ordered {

    @Autowired
    private Metadata metadata;
    @Autowired
    private MetadataTools metadataTools;

    @Override
    public void initEntity(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
        if (primaryKeyProperty != null && metadataTools.isEmbedded(primaryKeyProperty)) {
            // create an instance of embedded ID
            Object key = metadata.create(primaryKeyProperty.getRange().asClass());
            EntityValues.setId(entity, key);
        }
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE;
    }
}
