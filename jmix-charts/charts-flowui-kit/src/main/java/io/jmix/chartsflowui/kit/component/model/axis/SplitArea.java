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
import io.jmix.chartsflowui.kit.component.model.shared.AreaStyle;
import io.jmix.chartsflowui.kit.component.model.shared.JsFunction;

public class SplitArea extends ChartObservableObject {

    protected Integer interval;

    protected JsFunction intervalFunction;

    protected Boolean show;

    protected AreaStyle areaStyle;

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

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public AreaStyle getAreaStyle() {
        return areaStyle;
    }

    public void setAreaStyle(AreaStyle areaStyle) {
        if (this.areaStyle != null) {
            removeChild(this.areaStyle);
        }

        this.areaStyle = areaStyle;
        addChild(areaStyle);
    }

    public SplitArea withInterval(Integer interval) {
        setInterval(interval);
        return this;
    }

    public SplitArea withIntervalFunction(JsFunction intervalFunction) {
        setIntervalFunction(intervalFunction);
        return this;
    }

    public SplitArea withIntervalFunction(String intervalFunction) {
        setIntervalFunction(intervalFunction);
        return this;
    }

    public SplitArea withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public SplitArea withAreaStyle(AreaStyle areaStyle) {
        setAreaStyle(areaStyle);
        return this;
    }
}
