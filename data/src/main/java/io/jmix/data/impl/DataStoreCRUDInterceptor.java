/*
 * Copyright 2020 Haulmont.
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
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.datastore.BeforeEntityCountEvent;
import io.jmix.core.datastore.BeforeEntityLoadEvent;
import io.jmix.core.datastore.DataStoreInterceptor;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataStoreCRUDInterceptor implements DataStoreInterceptor {

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected ExtendedEntities extendedEntities;

    public void beforeEntityLoad(BeforeEntityLoadEvent beforeLoadEvent) {
        LoadContext<?> context = beforeLoadEvent.getLoadContext();

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyConstraints(entityContext, context.getAccessConstraints());

        if (!entityContext.isReadPermitted()) {
            beforeLoadEvent.setLoadPrevented();
        }
    }

    public void beforeEntityCount(BeforeEntityCountEvent beforeCountEvent) {
        LoadContext<?> context = beforeCountEvent.getLoadContext();

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyConstraints(entityContext, context.getAccessConstraints());

        if (!entityContext.isReadPermitted()) {
            beforeCountEvent.setCountPrevented();
        }
    }
}
