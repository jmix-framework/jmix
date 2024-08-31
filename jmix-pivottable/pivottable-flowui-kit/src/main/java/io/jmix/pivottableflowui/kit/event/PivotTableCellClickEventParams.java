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

package io.jmix.pivottableflowui.kit.event;

import io.jmix.pivottableflowui.kit.data.DataItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PivotTableCellClickEventParams {

    protected Double value;
    protected Map<String, String> filters;
    protected List<DataItem> usedDataItems;
    protected Supplier<List<DataItem>> usedDataItemsRetriever;

    /**
     * @return value of the clicked cell
     */
    @Nullable
    public Double getValue() {
        return value;
    }

    void setValue(@Nullable Double value) {
        this.value = value;
    }

    /**
     * @return a map in which keys are localized property names used in columns or rows
     * and values are localized property values
     */
    public Map<String, String> getFilters() {
        return filters;
    }

    void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    /**
     * @return a list of {@link DataItem} used in the clicked cell value generation
     */
    public List<DataItem> getUsedDataItems() {
        if (usedDataItems == null) {
            usedDataItems = usedDataItemsRetriever.get();
        }
        return usedDataItems;
    }


}
