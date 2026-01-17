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

package io.jmix.flowui.component.groupgrid;

import org.springframework.lang.Nullable;

/**
 * Represents a group of items in the {@link GroupListDataComponent}.
 */
public interface GroupInfo {

    /**
     * @return parent group or {@code null} if this is a root group
     */
    @Nullable
    GroupInfo getParent();

    /**
     * @return group property
     */
    GroupProperty getProperty();

    /**
     * Returns the value by group property. If the group is a child, it also provides property values
     * of the parent groups.
     *
     * @param property group property
     * @param <T>      property value type
     * @return group property value or {@code null}
     */
    @Nullable
    <T> T getPropertyValue(GroupProperty property);

    /**
     * Returns the value of the group property.
     *
     * @param <T> property value type
     * @return group property value
     */
    @Nullable
    <T> T getValue();
}
