/*
 * Copyright (c) 2008-2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.html.container;

import com.vaadin.flow.component.html.OrderedList;

public class OrderedListLoader extends AbstractHtmlContainerLoader<OrderedList> {

    @Override
    protected OrderedList createComponent() {
        return factory.create(OrderedList.class);
    }

    @Override
    public void loadComponent() {
        super.loadComponent();

        loadEnum(element, OrderedList.NumberingType.class, "numberingType", resultComponent::setType);
    }
}
