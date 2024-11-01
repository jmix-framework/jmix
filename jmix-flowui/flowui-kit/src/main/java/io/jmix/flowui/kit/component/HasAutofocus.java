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

import com.vaadin.flow.component.HasElement;

/**
 * Interface to be implemented by UI components that can automatically receive focus when the page is loaded.
 */
public interface HasAutofocus extends HasElement {

    /**
     * The 'autofocus' property name.
     */
    String AUTOFOCUS_PROPERTY_NAME = "autofocus";

    /**
     * Returns whether this component should have input focus when the page loads.
     *
     * @return the {@code autofocus} property value
     */
    default boolean isAutofocus() {
        return getElement().getProperty(AUTOFOCUS_PROPERTY_NAME, false);
    }

    /**
     * Sets whether the component should automatically receive focus when
     * the page loads. Defaults to {@code false}.
     *
     * @param autofocus {@code true} component should automatically receive focus
     */
    default void setAutofocus(boolean autofocus) {
        getElement().setProperty(AUTOFOCUS_PROPERTY_NAME, autofocus);
    }
}
