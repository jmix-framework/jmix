/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.facet.settings;

import com.vaadin.flow.component.Component;

/**
 * Abstract base class that partially implements the {@link UiComponentSettings} interface
 * and provides common functionality related to managing settings for a specific {@link Component}.
 */
public abstract class AbstractUiComponentSettings<S extends UiComponentSettings<S>>
        implements UiComponentSettings<S> {

    protected String ownerId;
    protected boolean modified = false;

    protected AbstractUiComponentSettings(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public boolean isModified() {
        return modified;
    }
}
