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

package io.jmix.charts.widget.amcharts.events.zoom;


import io.jmix.charts.widget.amcharts.JmixAmchartsScene;

import java.util.Date;

public class ZoomEvent extends com.vaadin.ui.Component.Event {

    private static final long serialVersionUID = 3375717365928857628L;

    private final int startIndex;
    private final int endIndex;
    private final Date startDate;
    private final Date endDate;
    private final String startValue;
    private final String endValue;

    public ZoomEvent(JmixAmchartsScene source, int startIndex, int endIndex,
                     Date startDate, Date endDate, String startValue, String endValue) {
        super(source);
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public Date getEndDate() {
        return endDate;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public String getEndValue() {
        return endValue;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public String getStartValue() {
        return startValue;
    }
}