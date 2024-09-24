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

package io.jmix.restds.impl.security;

import io.jmix.core.LoadContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.datastore.security.BaseEntityAttributesEraser;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("restds_RestEntityAttributesEraser")
public class RestEntityAttributesEraser extends BaseEntityAttributesEraser {

    @Autowired
    protected UnconstrainedDataManager dataManager;

    @Override
    protected Object getEntityReference(MetaClass entityMetaClass, Object id) {
        // TODO optimize - ideally to save instances instead of ids in security state
        Object ref = dataManager.load(new LoadContext<>(entityMetaClass).setId(id));
        if (ref == null)
            throw new RuntimeException("Unable to load %s with id=%s".formatted(entityMetaClass.getName(), id));
        return ref;
    }

    @Override
    protected boolean isPropertyToCheck(MetaProperty metaProperty) {
        return metaProperty.getRange().isClass();
    }
}
