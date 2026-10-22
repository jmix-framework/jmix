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

package io.jmix.flowui.action;

import org.jspecify.annotations.NullMarked;

/**
 * Interface to be implemented by field actions, such as picker actions, that may stay visible
 * when the field they belong to is read-only.
 * <p>
 * Actions of a read-only field are hidden by default. Implement this interface and return {@code true}
 * from {@link #isVisibleWhenFieldReadOnly()} for an action that does not change the field value,
 * for example, an action that shows the selected entity.
 */
@NullMarked
public interface AdjustWhenFieldReadOnly {

    /**
     * @return whether this action stays visible when the field it belongs to is read-only
     */
    default boolean isVisibleWhenFieldReadOnly() {
        return false;
    }
}
