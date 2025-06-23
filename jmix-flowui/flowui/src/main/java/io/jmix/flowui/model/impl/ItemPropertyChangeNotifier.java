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

package io.jmix.flowui.model.impl;

import io.jmix.core.entity.EntityPropertyChangeEvent;

/**
 * Interface that defines a contract for handling changes to properties of a particular item.
 * <p>
 * Implementations of this interface allow the monitoring and reacting to changes in the properties
 * of entities, facilitating dynamic updates and maintaining consistency between state and data.
 */
public interface ItemPropertyChangeNotifier {

    /**
     * Handles changes to a specific property of an item.
     * This method is called whenever a property of an item changes, providing details about
     * the item, the property, and its old and new values.
     *
     * @param e an {@link EntityPropertyChangeEvent} object containing information about
     *          the changed property
     */
    void itemPropertyChanged(EntityPropertyChangeEvent e);
}
