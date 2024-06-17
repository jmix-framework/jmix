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

package io.jmix.flowui.xml.layout.loader.component;

import io.jmix.flowui.component.twincolumn.TwinColumn;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class TwinColumnLoader extends AbstractComponentLoader<TwinColumn> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected TwinColumn createComponent() {
        return factory.create(TwinColumn.class);
    }

    @Override
    public void loadComponent() {
        loadBoolean(element, "selectAllButtonsVisible",
                resultComponent::setSelectAllButtonsVisible);
        loadBoolean(element, "reorderable", resultComponent::setReorderable);
        loadResourceString(element, "itemsColumnLabel",
                context.getMessageGroup(), resultComponent::setItemsColumnLabel);
        loadResourceString(element, "selectedItemsColumnLabel",
                context.getMessageGroup(), resultComponent::setSelectedItemsColumnLabel);

        getDataLoaderSupport().loadData(resultComponent, element);
        getDataLoaderSupport().loadItems(resultComponent, element);

        componentLoader().loadAriaLabel(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadHelperText(resultComponent, element);
        componentLoader().loadLabel(resultComponent, element);
        componentLoader().loadRequired(resultComponent, element, context);
        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadValueAndElementAttributes(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
