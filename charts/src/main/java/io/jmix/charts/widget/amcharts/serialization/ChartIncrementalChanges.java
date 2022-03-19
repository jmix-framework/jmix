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

package io.jmix.charts.widget.amcharts.serialization;


import io.jmix.ui.data.DataItem;

import java.util.ArrayList;
import java.util.List;

public class ChartIncrementalChanges {
    protected List<DataItem> addedItems;
    protected List<DataItem> removedItems;
    protected List<DataItem> updatedItems;

    public List<DataItem> getAddedItems() {
        return addedItems;
    }

    public void setAddedItems(List<DataItem> addedItems) {
        this.addedItems = addedItems;
    }

    public List<DataItem> getRemovedItems() {
        return removedItems;
    }

    public void setRemovedItems(List<DataItem> removedItems) {
        this.removedItems = removedItems;
    }

    public List<DataItem> getUpdatedItems() {
        return updatedItems;
    }

    public void setUpdatedItems(List<DataItem> updatedItems) {
        this.updatedItems = updatedItems;
    }

    public void registerAddedItem(List<DataItem> items) {
        if (addedItems == null) {
            addedItems = new ArrayList<>();
        }

        addedItems.addAll(items);
    }

    public void registerRemovedItems(List<DataItem> items) {
        if (removedItems == null) {
            removedItems = new ArrayList<>();
        }

        removedItems.addAll(items);
    }

    public void registerUpdatedItems(List<DataItem> items) {
        if (updatedItems == null) {
            updatedItems = new ArrayList<>();
        }

        updatedItems.addAll(items);
    }

    public boolean isEmpty() {
        return updatedItems == null && addedItems == null && removedItems == null;
    }
}