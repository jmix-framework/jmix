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


import io.jmix.charts.widget.amcharts.JmixAmStockChartScene;

public abstract class AbstractStockGraphEvent extends com.vaadin.ui.Component.Event {

    private final String panelId;
    private final String graphId;

    private final int x;
    private final int y;
    private final int absoluteX;
    private final int absoluteY;

    protected AbstractStockGraphEvent(JmixAmStockChartScene scene, String panelId, String graphId,
                                      int x, int y, int absoluteX, int absoluteY) {
        super(scene);
        this.panelId = panelId;
        this.graphId = graphId;
        this.x = x;
        this.y = y;
        this.absoluteX = absoluteX;
        this.absoluteY = absoluteY;
    }

    public String getPanelId() {
        return panelId;
    }

    public String getGraphId() {
        return graphId;
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