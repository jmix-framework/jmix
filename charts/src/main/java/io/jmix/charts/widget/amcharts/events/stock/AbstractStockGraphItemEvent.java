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

package io.jmix.charts.widget.amcharts.events.stock;


import com.vaadin.ui.Component;
import io.jmix.ui.data.DataItem;
import io.jmix.charts.widget.amcharts.JmixAmStockChartScene;

public abstract class AbstractStockGraphItemEvent extends Component.Event {

    private final String panelId;
    private final String graphId;

    private final int x;
    private final int y;
    private final int absoluteX;
    private final int absoluteY;

    private final DataItem dataItem;
    private final int itemIndex;

    protected AbstractStockGraphItemEvent(JmixAmStockChartScene scene, String panelId, String graphId, DataItem dataItem, int itemIndex,
                                          int x, int y, int absoluteX, int absoluteY) {
        super(scene);
        this.panelId = panelId;
        this.dataItem = dataItem;
        this.itemIndex = itemIndex;
        this.absoluteY = absoluteY;
        this.absoluteX = absoluteX;
        this.graphId = graphId;
        this.x = x;
        this.y = y;
    }

    public String getPanelId() {
        return panelId;
    }

    public String getGraphId() {
        return graphId;
    }

    public DataItem getDataItem() {
        return dataItem;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getAbsoluteX() {
        return absoluteX;
    }

    public int getAbsoluteY() {
        return absoluteY;
    }
}