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

package io.jmix.flowui.facet.settings;

import io.jmix.flowui.facet.settings.component.DataGridSettings;
import org.springframework.lang.Nullable;

/**
 * Base interface for POJO classes that represents component settings.
 * <p>
 * See {@link DataGridSettings} as an example.
 */
public interface Settings {

    /**
     * @return id of settings or {@code null} if not set
     */
    @Nullable
    String getId();

    /**
     * Sets an id of settings.
     *
     * @param id id to set
     */
    void setId(@Nullable String id);

    /**
     * The convenient method for casting current object to specific settings class.
     *
     * @param <T> type of settings
     * @return this object that is cast to specific settings class
     */
    @SuppressWarnings("unchecked")
    default <T extends Settings> T as() {
        return (T) this;
    }
}
