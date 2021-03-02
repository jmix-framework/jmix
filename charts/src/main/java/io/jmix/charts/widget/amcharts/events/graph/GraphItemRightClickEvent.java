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

package io.jmix.charts.widget.amcharts.events.graph;

import io.jmix.ui.data.DataItem;
import io.jmix.charts.widget.amcharts.JmixAmchartsScene;
import io.jmix.charts.widget.amcharts.events.AbstractClickEvent;

public class GraphItemRightClickEvent extends AbstractClickEvent {

    private static final long serialVersionUID = 4964415107233336938L;

    private final String graphId;

    private final int itemIndex;
    private final DataItem dataItem;

    public GraphItemRightClickEvent(JmixAmchartsScene scene, String graphId, int itemIndex, DataItem dataItem,
                                    int x, int y, int absoluteX, int absoluteY) {
        super(scene, x, y, absoluteX, absoluteY);
        this.itemIndex = itemIndex;
        this.graphId = graphId;
        this.dataItem = dataItem;
    }
    public String getGraphId() {
        return graphId;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public DataItem getDataItem() {
        return dataItem;
    }
}