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

package io.jmix.chartsflowui.kit.component.model.visualMap;

import io.jmix.chartsflowui.kit.component.model.shared.ItemStyle;

public class ContinuousVisualMap extends AbstractVisualMap<ContinuousVisualMap> {

    protected Boolean calculable;

    protected Integer[] range;

    protected Boolean realtime;

    protected String handleIcon;

    protected String handleSize;

    protected ItemStyle handleStyle;

    protected String indicatorIcon;

    protected String indicatorSize;

    protected ItemStyle indicatorStyle;

    public ContinuousVisualMap() {
        super(VisualMapType.CONTINUOUS);
    }

    public Boolean getCalculable() {
        return calculable;
    }

    public void setCalculable(Boolean calculable) {
        this.calculable = calculable;
        markAsDirty();
    }

    public Integer[] getRange() {
        return range;
    }

    public void setRange(Integer min, Integer max) {
        this.range = new Integer[]{min, max};
        markAsDirty();
    }

    public Boolean getRealtime() {
        return realtime;
    }

    public void setRealtime(Boolean realtime) {
        this.realtime = realtime;
        markAsDirty();
    }

    public String getHandleIcon() {
        return handleIcon;
    }

    public void setHandleIcon(String handleIcon) {
        this.handleIcon = handleIcon;
        markAsDirty();
    }

    public String getHandleSize() {
        return handleSize;
    }

    public void setHandleSize(String handleSize) {
        this.handleSize = handleSize;
        markAsDirty();
    }

    public ItemStyle getHandleStyle() {
        return handleStyle;
    }

    public void setHandleStyle(ItemStyle handleStyle) {
        if (this.handleStyle != null) {
            removeChild(this.handleStyle);
        }

        this.handleStyle = handleStyle;
        addChild(handleStyle);
    }

    public String getIndicatorIcon() {
        return indicatorIcon;
    }

    public void setIndicatorIcon(String indicatorIcon) {
        this.indicatorIcon = indicatorIcon;
        markAsDirty();
    }

    public String getIndicatorSize() {
        return indicatorSize;
    }

    public void setIndicatorSize(String indicatorSize) {
        this.indicatorSize = indicatorSize;
        markAsDirty();
    }

    public ItemStyle getIndicatorStyle() {
        return indicatorStyle;
    }

    public void setIndicatorStyle(ItemStyle indicatorStyle) {
        if (this.indicatorStyle != null) {
            removeChild(this.indicatorStyle);
        }

        this.indicatorStyle = indicatorStyle;
        addChild(indicatorStyle);
    }

    public ContinuousVisualMap withCalculable(Boolean calculable) {
        setCalculable(calculable);
        return this;
    }

    public ContinuousVisualMap withRange(Integer min, Integer max) {
        setRange(min, max);
        return this;
    }

    public ContinuousVisualMap withRealtime(Boolean realtime) {
        setRealtime(realtime);
        return this;
    }

    public ContinuousVisualMap withHandleIcon(String handleIcon) {
        setHandleIcon(handleIcon);
        return this;
    }

    public ContinuousVisualMap withHandleSize(String handleSize) {
        setHandleSize(handleSize);
        return this;
    }

    public ContinuousVisualMap withHandleStyle(ItemStyle handleStyle) {
        setHandleStyle(handleStyle);
        return this;
    }

    public ContinuousVisualMap withIndicatorIcon(String indicatorIcon) {
        setIndicatorIcon(indicatorIcon);
        return this;
    }

    public ContinuousVisualMap withIndicatorSize(String indicatorSize) {
        setIndicatorSize(indicatorSize);
        return this;
    }

    public ContinuousVisualMap withIndicatorStyle(ItemStyle indicatorStyle) {
        setIndicatorStyle(indicatorStyle);
        return this;
    }
}
