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

import com.vaadin.flow.component.ComponentEvent;
import io.jmix.pivottableflowui.kit.component.JmixPivotTable;
import io.jmix.pivottableflowui.kit.data.DataItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Describes PivotTable cell click event.
 */
public class PivotTableCellClickEvent extends ComponentEvent<JmixPivotTable> {

    protected Double value;
    protected Map<String, String> filters;
    protected List<DataItem> usedDataItems;

    public PivotTableCellClickEvent(JmixPivotTable pivotTable, Double value, Map<String, String> filters,
                                    List<DataItem> usedDataItems) {
        super(pivotTable, false);

        this.value = value;
        this.filters = filters;
        this.usedDataItems = usedDataItems;
    }

    /**
     * @return value of the clicked cell
     */
    @Nullable
    public Double getValue() {
        return value;
    }

    /**
     * @return a map in which keys are localized property names used in columns or rows
     * and values are localized property values
     */
    public Map<String, String> getFilters() {
        return filters;
    }

    /**
     * @return a list of {@link DataItem} used in the clicked cell value generation
     */
    public List<DataItem> getUsedDataItems() {
        return usedDataItems;
    }
}
