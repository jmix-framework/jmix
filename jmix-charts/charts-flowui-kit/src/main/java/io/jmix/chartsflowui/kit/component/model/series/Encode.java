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

package io.jmix.chartsflowui.kit.component.model.series;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;

/**
 * Define what is encoded to for each dimension of data.<br/>
 * Chart example:
 * <pre>{@code
 *    <charts:chart id="chart" height="100%" width="100%">
 *      <charts:dataSet>
 *          <charts:source dataContainer="stockDataDc" categoryField="date"
 *                         valueFields="open, close, low, high, volume"/>
 *      </charts:dataSet>
 *
 *      // ...
 *
 *      <charts:series>
 *          // Use only the values open, close, low, high for the series.
 *          <charts:candlestick>
 *              <charts:encode x="date" y="open, close, low, high"/>
 *          </charts:candlestick>
 *
 *          // Use only volume value for the series.
 *          <charts:bar>
 *              <charts:encode x="date" y="volume"/>
 *          </charts:bar>
 *      </charts:series>
 *    </charts:chart>
 * }</pre>
 * More detailed information is provided in the documentation.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#series-line.encode">Encode documentation</a>
 */
public class Encode extends ChartObservableObject {

    protected String[] x;

    protected String[] y;

    protected String[] radius;

    protected String[] angle;

    protected String[] value;

    protected String[] tooltip;

    public String[] getX() {
        return x;
    }

    public void setX(String... x) {
        this.x = x;
        markAsDirty();
    }

    public String[] getY() {
        return y;
    }

    public void setY(String... y) {
        this.y = y;
        markAsDirty();
    }

    public String[] getRadius() {
        return radius;
    }

    public void setRadius(String... radius) {
        this.radius = radius;
        markAsDirty();
    }

    public String[] getAngle() {
        return angle;
    }

    public void setAngle(String... angle) {
        this.angle = angle;
        markAsDirty();
    }


    public String[] getValue() {
        return value;
    }

    public void setValue(String... value) {
        this.value = value;
        markAsDirty();
    }

    public String[] getTooltip() {
        return tooltip;
    }

    public void setTooltip(String... tooltip) {
        this.tooltip = tooltip;
        markAsDirty();
    }

    public Encode withX(String... x) {
        setX(x);
        return this;
    }

    public Encode withY(String... y) {
        setY(y);
        return this;
    }

    public Encode withRadius(String... radius) {
        setRadius(radius);
        return this;
    }

    public Encode withAngle(String... angle) {
        setAngle(angle);
        return this;
    }

    public Encode withValue(String... value) {
        setValue(value);
        return this;
    }

    public Encode withTooltip(String... tooltip) {
        setTooltip(tooltip);
        return this;
    }

}
