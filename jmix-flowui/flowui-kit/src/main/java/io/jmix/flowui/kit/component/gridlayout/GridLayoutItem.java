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

package io.jmix.flowui.kit.component.gridlayout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.data.binder.HasItemComponents;

/**
 * Server-side component for the {@code grid-layout-item} element, used to represent individual items
 * in a {@link JmixGridLayout}.
 *
 * @param <T> type of the item represented by this component
 */
@Tag("jmix-grid-layout-item")
@JsModule("./src/grid-layout/jmix-grid-layout-item.js")
public class GridLayoutItem<T> extends Component implements HasItemComponents.ItemComponent<T>, HasComponents {

    protected final T item;

    /**
     * Constructs the component with the given item renderer as a String.
     *
     * @param item the item to be displayed by this component
     */
    public GridLayoutItem(T item) {
        this.item = item;
    }

    @Override
    public T getItem() {
        return item;
    }
}
