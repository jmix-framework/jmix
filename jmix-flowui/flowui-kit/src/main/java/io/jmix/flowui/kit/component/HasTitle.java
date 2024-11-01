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

package io.jmix.flowui.kit.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.HasElement;

import jakarta.annotation.Nullable;

/**
 * Mixin interface for components that displays text in a tooltip popup when the mouse is over it.
 */
public interface HasTitle extends HasElement {

    /**
     * Returns the text usually displayed in a tooltip popup when the mouse is over the
     * field.
     *
     * @return the {@code title} property from the web-component
     */
    @Nullable
    default String getTitle() {
        return getElement().getProperty("title");
    }

    /**
     * Sets the text usually displayed in a tooltip popup when the mouse is over the field.
     * The default implementations also sets {@code aria-label} HTML attribute to the root element.
     *
     * @param title the title to set
     */
    default void setTitle(@Nullable String title) {
        String titleValue = Strings.nullToEmpty(title);

        getElement().setProperty("title", titleValue);
        getElement().setAttribute("aria-label", titleValue);
    }
}
