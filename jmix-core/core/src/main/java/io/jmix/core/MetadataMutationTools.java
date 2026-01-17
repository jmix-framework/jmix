/*
 * Copyright 2025 Haulmont.
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

package io.jmix.core;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Store;
import io.jmix.core.metamodel.model.impl.MetaClassImpl;
import io.jmix.core.metamodel.model.impl.MetaPropertyImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides utility methods for changing metadata.
 */
@Component("core_MetadataMutationTools")
public class MetadataMutationTools {

    @Autowired
    private Stores stores;

    /**
     * Sets data store for the given metaclass.
     *
     * @param metaClass entity metaclass
     * @param storeName store name
     */
    public void setStore(MetaClass metaClass, String storeName) {
        Store store = stores.get(storeName);
        ((MetaClassImpl) metaClass).setStore(store);
        for (MetaProperty property : metaClass.getProperties()) {
            ((MetaPropertyImpl) property).setStore(store);
        }
    }
}
