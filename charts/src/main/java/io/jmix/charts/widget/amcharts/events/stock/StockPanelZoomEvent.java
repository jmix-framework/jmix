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

public class StockPanelZoomEvent extends Component.Event {

    private static final long serialVersionUID = -8555462390239325142L;

    private final Date startDate;
    private final Date endDate;
    private final String period;

    public StockPanelZoomEvent(JmixAmStockChartScene scene, Date startDate, Date endDate, String period) {
        super(scene);
        this.startDate = startDate;
        this.endDate = endDate;
        this.period = period;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getPeriod() {
        return period;
    }
}
