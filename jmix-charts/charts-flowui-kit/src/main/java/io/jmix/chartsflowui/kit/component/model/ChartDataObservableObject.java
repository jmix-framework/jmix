/*
 * Copyright 2023 Haulmont.
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

package io.jmix.chartsflowui.kit.component.model;

import io.jmix.chartsflowui.kit.component.JmixChart;

/**
 * Base class for data-aware chart options.
 */
public abstract class ChartDataObservableObject extends ChartObservableObject {

    protected JmixChart chart;

    protected void setChart(JmixChart chart) {
        if (this.chart == null) {
            this.chart = chart;
        } else {
            String message = String.format("A %s can only be attached to one chart", getClass().getSimpleName());
            throw new IllegalStateException(message);
        }

        afterChartSetup();
    }

    protected abstract void afterChartSetup();
}
