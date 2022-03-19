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

package io.jmix.charts.widget.amcharts.events.axis;


import com.vaadin.ui.Component;
import io.jmix.charts.widget.amcharts.JmixAmchartsScene;

public class AxisZoomEvent extends Component.Event {

    private final String axisId;
    private final double startValue;
    private final double endValue;

    public AxisZoomEvent(JmixAmchartsScene source, String axisId, double startValue, double endValue) {
        super(source);
        this.axisId = axisId;
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public String getAxisId() {
        return axisId;
    }

    public double getEndValue() {
        return endValue;
    }

    public double getStartValue() {
        return startValue;
    }
}