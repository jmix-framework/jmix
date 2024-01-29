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
import io.jmix.chartsflowui.kit.component.model.shared.LineStyle;

/**
 * Options component related minor ticks.
 *
 * @see <a href="https://echarts.apache.org/en/option.html#xAxis.minorTick">MitorTick documentation</a>
 */
public class MinorTick extends ChartObservableObject {

    protected Boolean show;

    protected Integer splitNumber;

    protected Integer length;

    protected LineStyle lineStyle;

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public Integer getSplitNumber() {
        return splitNumber;
    }

    public void setSplitNumber(Integer splitNumber) {
        this.splitNumber = splitNumber;
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

    public MinorTick withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public MinorTick withSplitNumber(Integer splitNumber) {
        setSplitNumber(splitNumber);
        return this;
    }

    public MinorTick withLength(Integer length) {
        setLength(length);
        return this;
    }

    public MinorTick withLineStyle(LineStyle lineStyle) {
        setLineStyle(lineStyle);
        return this;
    }
}
