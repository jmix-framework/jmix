/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.facet.settings.component;

import io.jmix.flowui.component.sidepanellayout.SidePanelLayout;
import io.jmix.flowui.facet.settings.Settings;
import org.jspecify.annotations.Nullable;

/**
 * Represents user settings for a {@link SidePanelLayout} component: the user-chosen side panel size.
 */
public class SidePanelLayoutSettings implements Settings {

    @Nullable
    protected String id;

    @Nullable
    protected String horizontalSize;

    @Nullable
    protected String verticalSize;

    @Nullable
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(@Nullable String id) {
        this.id = id;
    }

    /**
     * Returns the stored side panel width for horizontal positions.
     *
     * @return the stored horizontal size, or {@code null} if none is stored
     */
    @Nullable
    public String getHorizontalSize() {
        return horizontalSize;
    }

    /**
     * Sets the side panel width to store for horizontal positions.
     *
     * @param horizontalSize the horizontal size to store, or {@code null} to store none
     */
    public void setHorizontalSize(@Nullable String horizontalSize) {
        this.horizontalSize = horizontalSize;
    }

    /**
     * Returns the stored side panel height for vertical positions.
     *
     * @return the stored vertical size, or {@code null} if none is stored
     */
    @Nullable
    public String getVerticalSize() {
        return verticalSize;
    }

    /**
     * Sets the side panel height to store for vertical positions.
     *
     * @param verticalSize the vertical size to store, or {@code null} to store none
     */
    public void setVerticalSize(@Nullable String verticalSize) {
        this.verticalSize = verticalSize;
    }
}
