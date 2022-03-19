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

package io.jmix.charts.widget.amcharts.events;


import io.jmix.ui.data.DataItem;
import io.jmix.charts.widget.amcharts.JmixAmchartsScene;

import javax.annotation.Nullable;

public abstract class AbstractLegendItemEvent extends com.vaadin.ui.Component.Event {

    private static final long serialVersionUID = -2258433755537180205L;

    private final int itemIndex;
    private final DataItem dataItem;

    public AbstractLegendItemEvent(JmixAmchartsScene scene, int itemIndex, DataItem dataItem) {
        super(scene);
        this.itemIndex = itemIndex;
        this.dataItem = dataItem;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    @Nullable
    public DataItem getDataItem() {
        return dataItem;
    }

    public DataItem getDataItemNN() {
        if (dataItem == null) {
            throw new IllegalStateException("LegendItemEvent dataItem is null");
        }
        return dataItem;
    }
}