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

import io.jmix.core.common.event.Subscription;

import java.util.function.Consumer;

/**
 * @deprecated Use {@link io.jmix.ui.component.PopupButton} instead
 */
@Deprecated
public interface PopupButton extends io.jmix.ui.component.PopupButton {

    /**
     * @return one of width units: {@link Component#UNITS_PIXELS}, {@link Component#UNITS_PERCENTAGE}
     *
     * @deprecated Use {@link #getMenuWidthSizeUnit()}
     */
    @Deprecated
    int getMenuWidthUnits();

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removePopupVisibilityListener(Consumer<PopupVisibilityEvent> listener);
}
