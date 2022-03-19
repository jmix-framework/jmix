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
import io.jmix.charts.widget.amcharts.JmixAmStockChartScene;

import java.util.Date;

public abstract class AbstractStockEventEvent extends Component.Event {

    private static final long serialVersionUID = 275215935877518123L;

    private final String graphId;
    private final Date date;
    private final String stockEventId;

    protected AbstractStockEventEvent(JmixAmStockChartScene scene, String graphId, Date date, String stockEventId) {
        super(scene);
        this.graphId = graphId;
        this.date = date;
        this.stockEventId = stockEventId;
    }

    public String getGraphId() {
        return graphId;
    }

    public Date getDate() {
        return date;
    }

    public String getStockEventId() {
        return stockEventId;
    }
}