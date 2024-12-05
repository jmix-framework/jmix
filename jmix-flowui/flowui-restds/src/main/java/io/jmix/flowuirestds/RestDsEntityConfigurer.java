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

package io.jmix.flowuirestds;

import com.google.common.base.Strings;
import io.jmix.core.JmixOrder;
import io.jmix.core.MetadataPostProcessor;
import io.jmix.core.Stores;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.Store;
import io.jmix.core.metamodel.model.impl.MetaClassImpl;
import io.jmix.core.metamodel.model.impl.MetaPropertyImpl;
import io.jmix.flowuirestds.genericfilter.FilterConfiguration;
import io.jmix.flowuirestds.settings.UserSettingsItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Internal
@Component("flowui_RestDsEntityConfigurer")
@Order(JmixOrder.LOWEST_PRECEDENCE - 100)
public class RestDsEntityConfigurer implements MetadataPostProcessor {

    @Value("${jmix.restds.ui-config-store:}")
    private String uiConfigStore;

    @Autowired
    protected Stores stores;

    @Override
    public void process(Session session) {
        if (Strings.isNullOrEmpty(uiConfigStore))
            return;

        Store store = stores.get(uiConfigStore);

        configureEntity(session, UserSettingsItem.class, store);
        configureEntity(session, FilterConfiguration.class, store);
    }

    private void configureEntity(Session session, Class<?> entityClass, Store store) {
        MetaClass userSettingsClass = session.getClass(entityClass);
        ((MetaClassImpl) userSettingsClass).setStore(store);

        for (MetaProperty property : userSettingsClass.getProperties()) {
            ((MetaPropertyImpl) property).setStore(store);
        }
    }
}
