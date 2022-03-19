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

package io.jmix.charts.widget.client.amstockcharts;

import com.vaadin.shared.AbstractComponentState;

public class JmixAmStockChartSceneState extends AbstractComponentState {
    {
        primaryStyleName = "jmix-amcharts-stockchart";
    }

    public static final String CHART_CLICK_EVENT = "scc";
    public static final String CHART_RIGHT_CLICK_EVENT = "scrc";
    public static final String STOCK_EVENT_CLICK_EVENT = "sec";
    public static final String STOCK_EVENT_ROLL_OUT_EVENT = "serout";
    public static final String STOCK_EVENT_ROLL_OVER_EVENT = "sero";
    public static final String STOCK_ZOOM_EVENT = "sz";
    public static final String PERIOD_SELECTOR_CHANGE_EVENT = "psc";
    public static final String DATA_SET_SELECTOR_COMPARE_EVENT = "dssc";
    public static final String DATA_SET_SELECTOR_SELECT_EVENT = "dsss";
    public static final String DATA_SET_SELECTOR_UNCOMPARE_EVENT = "dssu";
    public static final String STOCK_GRAPH_CLICK_EVENT = "sgc";
    public static final String STOCK_GRAPH_ROLL_OUT_EVENT = "sgrout";
    public static final String STOCK_GRAPH_ROLL_OVER_EVENT = "sgro";
    public static final String STOCK_GRAPH_ITEM_CLICK_EVENT = "sgic";
    public static final String STOCK_GRAPH_ITEM_RIGHT_CLICK_EVENT = "sgirc";
    public static final String STOCK_GRAPH_ITEM_ROLL_OUT_EVENT = "sgirout";
    public static final String STOCK_GRAPH_ITEM_ROLL_OVER_EVENT = "sgiro";

    public String json;
}
