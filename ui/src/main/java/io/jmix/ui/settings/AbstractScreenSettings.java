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

package io.jmix.ui.settings;

import io.jmix.core.annotation.Internal;

public abstract class AbstractScreenSettings implements ScreenSettings {

    protected String screenId;

    protected boolean modified = false;

    public AbstractScreenSettings(String screenId) {
        this.screenId = screenId;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    /**
     * INTERNAL. Used by the framework.
     * <p>
     * Commits screen settings to the store.
     */
    @Internal
    public abstract void commit();
}
