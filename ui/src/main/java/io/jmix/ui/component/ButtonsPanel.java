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

/**
 * A container that streamlines the use and placement of the components (usually, buttons) for data management in a
 * components that implement {@link HasButtonsPanel} interface.
 */
public interface ButtonsPanel extends FlowBoxLayout {

    String NAME = "buttonsPanel";

    /**
     * Sets whether or not buttons panel is always displayed on the lookup screen. If the attribute value is true,
     * the buttons panel is not hidden. Default value is {@code false}.
     *
     * @param alwaysVisible specifies whether buttons panel is always displayed on the lookup screen
     */
    void setAlwaysVisible(boolean alwaysVisible);

    /**
     * Returns whether buttons panel is always displayed on the lookup screen. Default value is {@code false}.
     *
     * @return {code true} if the buttons panel is always displayed on the lookup screen
     */
    boolean isAlwaysVisible();
}