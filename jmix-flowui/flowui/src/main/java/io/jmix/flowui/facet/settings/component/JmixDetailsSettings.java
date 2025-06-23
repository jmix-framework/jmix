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

package io.jmix.flowui.facet.settings.component;

import com.vaadin.flow.component.details.Details;
import io.jmix.flowui.facet.settings.Settings;
import org.springframework.lang.Nullable;

/**
 * Represents component settings for {@link Details} component.
 */
public class JmixDetailsSettings implements Settings {

    protected String id;
    protected Boolean opened;

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
     * Returns the opened state of the {@link Details} component settings.
     *
     * @return {@code true} if the component is opened, {@code false} if it is closed,
     * or {@code null} if the state is not defined
     */
    @Nullable
    public Boolean getOpened() {
        return opened;
    }

    /**
     * Sets the opened state of the {@link Details} component settings.
     *
     * @param opened the new opened state; {@code true} if the component is opened,
     *               {@code false} if it is closed
     */
    public void setOpened(Boolean opened) {
        this.opened = opened;
    }
}
