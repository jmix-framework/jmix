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
import io.jmix.chartsflowui.kit.component.model.Grid;
import io.jmix.chartsflowui.kit.component.model.shared.JsFunction;
import io.jmix.chartsflowui.kit.component.model.shared.LineStyle;

/**
 * Split line of axis in {@link Grid} area.
 */
public class SplitLine extends ChartObservableObject {

    protected Boolean show;

    protected Integer interval;

    protected JsFunction intervalFunction;

    protected LineStyle lineStyle;

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
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

    public SplitLine withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public SplitLine withInterval(Integer interval) {
        setInterval(interval);
        return this;
    }

    public SplitLine withIntervalFunction(JsFunction intervalFunction) {
        setIntervalFunction(intervalFunction);
        return this;
    }

    public SplitLine withIntervalFunction(String intervalFunction) {
        setIntervalFunction(intervalFunction);
        return this;
    }

    public SplitLine withLineStyle(LineStyle lineStyle) {
        setLineStyle(lineStyle);
        return this;
    }
}
