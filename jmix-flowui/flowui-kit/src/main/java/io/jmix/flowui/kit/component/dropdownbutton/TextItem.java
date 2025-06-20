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

/**
 * Represents a text-based item that can be included in a dropdown button component.
 * The {@code TextItem} interface allows setting and retrieving the textual content
 * associated with an item in the dropdown menu.
 */
public interface TextItem extends DropdownButtonItem {

    /**
     * Sets the text displayed on the component.
     *
     * @param text the text to be displayed
     */
    void setText(String text);

    /**
     * Returns the text associated with this component.
     *
     * @return the current text of the component
     */
    String getText();
}
