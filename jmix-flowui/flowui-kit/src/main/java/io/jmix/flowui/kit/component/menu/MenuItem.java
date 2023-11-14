/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.kit.component.menu;

import jakarta.annotation.Nullable;

/**
 * Represents generic item of a menu (for example, {@link io.jmix.flowui.kit.component.main.ListMenu})
 */
public interface MenuItem {

    /**
     * @return the label of this menu item or null if no label has been set
     */
    @Nullable
    String getLabel();

    /**
     * Set a textual label for the item.
     *
     * @param label the label text to set or null to remove the label
     */
    void setLabel(@Nullable String label);
}
