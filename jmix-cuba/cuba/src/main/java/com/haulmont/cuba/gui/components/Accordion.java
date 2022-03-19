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

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * @deprecated Use {@link io.jmix.ui.component.Accordion} instead
 */
@Deprecated
public interface Accordion extends io.jmix.ui.component.Accordion {

    /**
     * Get selected tab. May be null if the tabsheet does not contain tabs at all.
     *
     * @deprecated use {@link #getSelectedTab()}
     */
    @Nullable
    @Deprecated
    default Tab getTab() {
        return getSelectedTab();
    }

    /**
     * Set selected tab.
     *
     * @param tab tab instance
     * @deprecated Use {@link #setSelectedTab(Tab)}
     */
    @Deprecated
    default void setTab(Tab tab) {
        setSelectedTab(tab);
    }

    /**
     * Set selected tab.
     *
     * @param name tab id
     * @deprecated Use {@link #setSelectedTab(String)}
     */
    @Deprecated
    default void setTab(String name) {
        setSelectedTab(name);
    }

    /**
     * Remove previously added SelectedTabChangeListener.
     *
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeSelectedTabChangeListener(Consumer<SelectedTabChangeEvent> listener);
}
