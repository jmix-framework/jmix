/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.component;

import io.jmix.flowui.UiComponentProperties;

/**
 * A component that trims spaces at the beginning and at the end of the entered string.
 */
public interface HasTrimming {

    /**
     * @return {@code true} if trims spaces at the beginning and at the end of the entered string,
     * {@code false} otherwise
     */
    boolean isTrimEnabled();

    /**
     * Sets whether to trim spaces at the beginning and at the end of the entered string. The default value
     * depends on {@link UiComponentProperties#isDefaultTrimEnabled()}.
     *
     * @param trimEnabled whether to trim spaces at the beginning and at the end of the entered string
     */
    void setTrimEnabled(boolean trimEnabled);
}
