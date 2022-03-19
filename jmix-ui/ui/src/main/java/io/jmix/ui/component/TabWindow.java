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
package io.jmix.ui.component;

import io.jmix.ui.UiComponents;

/**
 * Tab window UI component.
 */
public interface TabWindow extends Window {
    /**
     * Name that is used to register a client type specific screen implementation in {@link UiComponents}
     */
    String NAME = "tabWindow";

    /**
     * @return formatted tab caption
     */
    String formatTabCaption();

    /**
     * @return formatted tab tooltip text
     */
    String formatTabDescription();

    /**
     * Returns how the managed main TabSheet switches a tab with this window: hides or unloads its content.
     *
     * @return one of the {@link ContentSwitchMode} enum values
     */
    ContentSwitchMode getContentSwitchMode();

    /**
     * Sets how the managed main TabSheet switches a tab with this window: hides or unloads its content.
     * <p>
     * Note that: a method invocation will take effect only if {@code jmix.ui.component.mainTabSheetMode} property
     * is set to 'MANAGED'.
     *
     * @param mode one of the {@link ContentSwitchMode} enum values
     */
    void setContentSwitchMode(ContentSwitchMode mode);
}