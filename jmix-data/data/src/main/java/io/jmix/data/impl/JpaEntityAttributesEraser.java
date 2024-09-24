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

package io.jmix.data.impl;

import io.jmix.core.MetadataTools;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.datastore.security.BaseEntityAttributesEraser;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("data_JpaEntityAttributesEraser")
public class JpaEntityAttributesEraser extends BaseEntityAttributesEraser {

    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected MetadataTools metadataTools;

    @Override
    protected Object getEntityReference(MetaClass entityMetaClass, Object id) {
        return dataManager.getReference((entityMetaClass.getJavaClass()), id);
    }

    @Override
    protected boolean isPropertyToCheck(MetaProperty metaProperty) {
        return metaProperty.getRange().isClass() && metadataTools.isJpa(metaProperty);
    }
}
