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

package io.jmix.ui.component;

import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

/**
 * Interface to be implemented by a component that requires a value
 */
public interface Requirable {

    /**
     * @return whether the component must contain a non-null value
     */
    boolean isRequired();

    /**
     * Sets whether the component must contain a non-null value.
     *
     * @param required required
     */
    @StudioProperty(defaultValue = "false")
    void setRequired(boolean required);

    /**
     * @return a message that will be displayed to user if the component is required but has null value
     */
    @Nullable
    String getRequiredMessage();

    /**
     * Sets a message that will be displayed to user if the field is required but has null value.
     *
     * @param msg message
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setRequiredMessage(@Nullable String msg);
}
