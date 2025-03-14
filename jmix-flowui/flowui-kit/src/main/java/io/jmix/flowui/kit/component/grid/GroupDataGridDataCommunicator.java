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

package io.jmix.flowui.kit.component.grid;

import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.provider.hierarchy.*;
import com.vaadin.flow.function.*;
import com.vaadin.flow.internal.*;
import elemental.json.*;

public class GroupDataGridDataCommunicator<T> extends HierarchicalDataCommunicator<T> {

    public GroupDataGridDataCommunicator(CompositeDataGenerator<T> dataGenerator, HierarchicalArrayUpdater arrayUpdater, SerializableConsumer<JsonArray> dataUpdater, StateNode stateNode, SerializableSupplier<ValueProvider<T, String>> uniqueKeyProviderSupplier) {
        super(dataGenerator, arrayUpdater, dataUpdater, stateNode, uniqueKeyProviderSupplier);
    }
}
