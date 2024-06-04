/*
 * Copyright 2023 Haulmont.
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

import io.jmix.flowui.component.virtuallist.JmixVirtualList;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.support.DataLoaderSupport;

public class VirtualListLoader extends AbstractComponentLoader<JmixVirtualList<?>> {

    protected DataLoaderSupport dataLoaderSupport;

    @Override
    protected JmixVirtualList<?> createComponent() {
        return factory.create(JmixVirtualList.class);
    }

    @Override
    public void loadComponent() {
        getDataLoaderSupport().loadItems(resultComponent, element);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);
    }

    protected DataLoaderSupport getDataLoaderSupport() {
        if (dataLoaderSupport == null) {
            dataLoaderSupport = applicationContext.getBean(DataLoaderSupport.class, context);
        }
        return dataLoaderSupport;
    }
}
