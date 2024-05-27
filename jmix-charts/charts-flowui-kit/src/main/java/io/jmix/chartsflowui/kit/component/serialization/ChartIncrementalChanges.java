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

package io.jmix.chartsflowui.kit.component.serialization;

import io.jmix.chartsflowui.kit.component.model.DataSet;
import io.jmix.chartsflowui.kit.data.chart.DataItem;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class ChartIncrementalChanges<T extends DataItem> {

    protected Collection<T> addedItems;
    protected Collection<T> removedItems;
    protected Collection<T> updatedItems;

    protected DataSet.Source<?> source;

    @Nullable
    public Collection<T> getAddedItems() {
        return addedItems;
    }

    public void addAddedItems(Collection<T> items) {
        if (addedItems == null) {
            addedItems = new ArrayList<>();
        }

        addedItems.addAll(items);
    }

    @Nullable
    public Collection<T> getRemovedItems() {
        return removedItems;
    }

    public void addRemovedItems(Collection<T> items) {
        if (removedItems == null) {
            removedItems = new ArrayList<>();
        }

        removedItems.addAll(items);
    }

    @Nullable
    public Collection<T> getUpdatedItems() {
        return updatedItems;
    }

    public void addUpdatedItems(Collection<T> items) {
        if (updatedItems == null) {
            updatedItems = new ArrayList<>();
        }

        updatedItems.addAll(items);
    }

    public DataSet.Source<?> getSource() {
        return source;
    }

    public void setSource(DataSet.Source<?> source) {
        this.source = source;
    }
}
