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

package io.jmix.fullcalendarflowui.kit.component.serialization.model;

import io.jmix.fullcalendarflowui.kit.component.data.ItemChangeOperation;

import java.io.Serializable;
import java.util.Collection;

public class IncrementalData implements Serializable {

    protected String sourceId;
    protected ItemChangeOperation operation;
    protected Collection<?> items;

    public IncrementalData() {
    }

    public IncrementalData(String sourceId, ItemChangeOperation operation, Collection<?> items) {
        this.sourceId = sourceId;
        this.operation = operation;
        this.items = items;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public ItemChangeOperation getOperation() {
        return operation;
    }

    public void setOperation(ItemChangeOperation operation) {
        this.operation = operation;
    }

    public Collection<?> getItems() {
        return items;
    }

    public void setItems(Collection<?> items) {
        this.items = items;
    }
}
