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

package io.jmix.chartsflowui.kit.component.model.axis;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.shared.JsFunction;
import io.jmix.chartsflowui.kit.component.model.shared.LineStyle;

/**
 * Options component related to axis tick.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#xAxis.axisTick">AxisTick documentation</a>
 */
public class AxisTick extends ChartObservableObject {

    protected Boolean show;

    protected Boolean alignWithLabel;

    protected Integer interval;

    protected JsFunction intervalFunction;

    protected Boolean inside;

    protected Integer length;

    protected LineStyle lineStyle;

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public Boolean getAlignWithLabel() {
        return alignWithLabel;
    }

    public void setAlignWithLabel(Boolean alignWithLabel) {
        this.alignWithLabel = alignWithLabel;
        markAsDirty();
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
        markAsDirty();
    }

    public JsFunction getIntervalFunction() {
        return intervalFunction;
    }

    public void setIntervalFunction(JsFunction intervalFunction) {
        this.intervalFunction = intervalFunction;
        markAsDirty();
    }

    public void setIntervalFunction(String intervalFunction) {
        this.intervalFunction = new JsFunction(intervalFunction);
        markAsDirty();
    }

    public Boolean getInside() {
        return inside;
    }

    public void setInside(Boolean inside) {
        this.inside = inside;
        markAsDirty();
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
        markAsDirty();
    }

    public LineStyle getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(LineStyle lineStyle) {
        if (this.lineStyle != null) {
            removeChild(this.lineStyle);
        }

        this.lineStyle = lineStyle;
        addChild(lineStyle);
    }

    public AxisTick withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public AxisTick withAlignWithLabel(Boolean alignWithLabel) {
        setAlignWithLabel(alignWithLabel);
        return this;
    }

    public AxisTick withInterval(Integer interval) {
        setInterval(interval);
        return this;
    }

    public AxisTick withIntervalFunction(JsFunction intervalFunction) {
        setIntervalFunction(intervalFunction);
        return this;
    }

    public AxisTick withIntervalFunction(String intervalFunction) {
        setIntervalFunction(intervalFunction);
        return this;
    }

    public AxisTick withInside(Boolean inside) {
        setInside(inside);
        return this;
    }

    public AxisTick withLength(Integer length) {
        setLength(length);
        return this;
    }

    public AxisTick withLineStyle(LineStyle lineStyle) {
        setLineStyle(lineStyle);
        return this;
    }
}
