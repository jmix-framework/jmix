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

package io.jmix.charts.widget.client.amcharts.state;

import com.vaadin.shared.AbstractComponentState;

public class JmixAmchartsSceneState extends AbstractComponentState {
    {
        primaryStyleName = "jmix-amcharts-chart";
    }

    public static final String CHART_CLICK_EVENT = "cc";
    public static final String CHART_RIGHT_CLICK_EVENT = "crc";

    public static final String GRAPH_CLICK_EVENT = "gc";
    public static final String GRAPH_ITEM_CLICK_EVENT = "gic";
    public static final String GRAPH_ITEM_RIGHT_CLICK_EVENT = "girc";

    public static final String ZOOM_EVENT = "z";

    public static final String SLICE_CLICK_EVENT = "sc";
    public static final String SLICE_RIGHT_CLICK_EVENT = "src";
    public static final String SLICE_PULL_IN_EVENT = "spi";
    public static final String SLICE_PULL_OUT_EVENT = "spo";
    public static final String SLICE_ROLL_OUT_EVENT = "srou";
    public static final String SLICE_ROLL_OVER_EVENT = "srov";

    public static final String LEGEND_LABEL_CLICK_EVENT = "llc";
    public static final String LEGEND_MARKER_CLICK_EVENT = "lmc";
    public static final String LEGEND_ITEM_SHOW_EVENT = "lsi";
    public static final String LEGEND_ITEM_HIDE_EVENT = "lhi";

    public static final String CURSOR_ZOOM_EVENT = "cz";
    public static final String CURSOR_PERIOD_SELECT_EVENT = "cps";

    public static final String VALUE_AXIS_ZOOM_EVENT = "vaz";

    public static final String CATEGORY_ITEM_CLICK_EVENT = "cic";

    public static final String ROLL_OUT_GRAPH_EVENT = "roug";
    public static final String ROLL_OUT_GRAPH_ITEM_EVENT = "rougi";
    public static final String ROLL_OVER_GRAPH_EVENT = "rovg";
    public static final String ROLL_OVER_GRAPH_ITEM_EVENT = "rovgi";

    // Custom JSON chart configuration
    public String json;
}