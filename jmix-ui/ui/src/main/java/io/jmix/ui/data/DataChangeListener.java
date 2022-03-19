/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.data;

import java.io.Serializable;

/**
 * Listener to data provider item change events.
 */
public interface DataChangeListener extends Serializable {

    /**
     * Enclosed collection changed.
     *
     * @param e event with information about changes of data items
     */
    void dataItemsChanged(DataItemsChangeEvent e);
}
