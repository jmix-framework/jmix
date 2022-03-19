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

package io.jmix.charts.model.settings;

import io.jmix.charts.model.AbstractChartObject;

/**
 * See documentation for properties of AmCharts JS Object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmCharts">http://docs.amcharts.com/3/javascriptcharts/AmCharts</a>
 */
public class Settings extends AbstractChartObject {

    private static final long serialVersionUID = 8224937687234516612L;

    private ChartTheme theme;

    private Boolean baseHref;

    private Integer processDelay;

    private Boolean useUTC;

    public Boolean getBaseHref() {
        return baseHref;
    }

    public Settings setBaseHref(Boolean baseHref) {
        this.baseHref = baseHref;
        return this;
    }

    public ChartTheme getTheme() {
        return theme;
    }

    public Settings setTheme(ChartTheme theme) {
        this.theme = theme;
        return this;
    }

    public Boolean getUseUTC() {
        return useUTC;
    }

    public Settings setUseUTC(Boolean useUTC) {
        this.useUTC = useUTC;
        return this;
    }

    public Integer getProcessDelay() {
        return processDelay;
    }

    public Settings setProcessDelay(Integer processDelay) {
        this.processDelay = processDelay;
        return this;
    }
}