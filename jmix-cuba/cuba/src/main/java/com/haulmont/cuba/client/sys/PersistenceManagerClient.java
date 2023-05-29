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

import com.haulmont.cuba.gui.components.LookupPickerField;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.EntityComboBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(PersistenceManagerClient.NAME)
public class PersistenceManagerClient {

    public static final String NAME = "cuba_PersistenceManagerClient";

    @Autowired
    protected UiComponentProperties componentProperties;
    @Autowired
    protected UiProperties uiProperties;

    public boolean useLookupScreen(String entityName) {
        String fieldType = componentProperties.getEntityFieldType().get(entityName);
        return fieldType == null || !(fieldType.equals(LookupPickerField.NAME) || fieldType.equals(EntityComboBox.NAME));
    }

    public int getFetchUI(String entityName) {
        return uiProperties.getEntityPageSize(entityName);
    }

    public int getMaxFetchUI(String entityName) {
        return uiProperties.getEntityMaxFetchSize(entityName);
    }
}
