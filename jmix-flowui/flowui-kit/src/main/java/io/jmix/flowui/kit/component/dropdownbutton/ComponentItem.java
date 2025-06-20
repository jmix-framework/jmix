/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.kit.component.dropdownbutton;

import com.vaadin.flow.component.Component;

/**
 * Represents an item in a dropdown button that can display a custom component.
 * The {@code ComponentItem} interface allows associating a UI component with
 * the dropdown item and provides methods to manage its content.
 */
public interface ComponentItem extends DropdownButtonItem {

    /**
     * Sets the content component for the dropdown item.
     *
     * @param content the {@link Component} to be set as the content
     */
    void setContent(Component content);

    /**
     * Returns the content component associated with this dropdown item.
     *
     * @return the {@link Component} currently set as the content
     */
    Component getContent();
}
