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

package io.jmix.pivottable.widget.events;


import com.vaadin.ui.Component;
import io.jmix.ui.data.DataItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class CellClickEvent extends Component.Event {
    protected Double value;
    protected Map<String, String> filters;
    protected Supplier<List<DataItem>> usedDataItemsRetriever;

    public CellClickEvent(Component source, Double value, Map<String, String> filters,
                          Supplier<List<DataItem>> usedDataItemsRetriever) {
        super(source);
        this.value = value;
        this.filters = filters;
        this.usedDataItemsRetriever = usedDataItemsRetriever;
    }

    @Nullable
    public Double getValue() {
        return value;
    }

    public Map<String, String> getFilters() {
        return filters;
    }

    public Supplier<List<DataItem>> getUsedDataItemsRetriever() {
        return usedDataItemsRetriever;
    }
}
