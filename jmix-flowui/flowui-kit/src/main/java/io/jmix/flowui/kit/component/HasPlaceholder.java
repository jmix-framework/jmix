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

package io.jmix.flowui.kit.component;

import com.google.common.base.Strings;
import jakarta.annotation.Nullable;

/**
 * A component which supports a placeholder.
 * <p>
 * A placeholder is a text that should be displayed in the input element, when
 * the user has not entered a value.
 *
 * @deprecated use {@link com.vaadin.flow.component.HasPlaceholder} instead.
 */
// FIXME: don't forget to remove corresponding spotbugs exclusion
@Deprecated(since = "2.2", forRemoval = true)
public interface HasPlaceholder extends com.vaadin.flow.component.HasPlaceholder {

    String PLACEHOLDER_PROPERTY_NAME = "placeholder";

    /**
     * {@inheritDoc}
     */
    @Nullable
    default String getPlaceholder() {
        return getElement().getProperty(PLACEHOLDER_PROPERTY_NAME);
    }

    /**
     * {@inheritDoc}
     */
    default void setPlaceholder(@Nullable String placeholder) {
        getElement().setProperty(PLACEHOLDER_PROPERTY_NAME, Strings.nullToEmpty(placeholder));
    }
}
