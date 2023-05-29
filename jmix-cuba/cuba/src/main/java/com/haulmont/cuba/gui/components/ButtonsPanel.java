/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.gui.components;

import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasButtonsPanel;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @deprecated Use a {@link io.jmix.ui.component.ButtonsPanel} instead
 */
@Deprecated
public interface ButtonsPanel extends FlowBoxLayout, io.jmix.ui.component.ButtonsPanel {

    String NAME = io.jmix.ui.component.ButtonsPanel.NAME;

    /**
     * @deprecated use {@link HasButtonsPanel} instead
     */
    @Deprecated
    interface Provider extends Supplier<Collection<Component>> {
        @Override
        default Collection<Component> get() {
            return getButtons();
        }

        Collection<Component> getButtons();
    }
}
