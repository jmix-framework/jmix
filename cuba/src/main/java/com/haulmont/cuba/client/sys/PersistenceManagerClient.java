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

package com.haulmont.cuba.client.sys;

import io.jmix.ui.UiProperties;
import io.jmix.ui.component.EntityComboBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(PersistenceManagerClient.NAME)
public class PersistenceManagerClient {

    public static final String NAME = "cuba_PersistenceManagerClient";

    @Autowired
    protected UiProperties properties;

    public boolean useLookupScreen(String entityName) {
        String fieldType = properties.getEntityFieldType().get(entityName);
        return fieldType == null || !fieldType.equals(EntityComboBox.NAME);
    }

    public int getFetchUI(String entityName) {
        return properties.getEntityPageSize(entityName);
    }

    public int getMaxFetchUI(String entityName) {
        return properties.getEntityMaxFetchSize(entityName);
    }
}
