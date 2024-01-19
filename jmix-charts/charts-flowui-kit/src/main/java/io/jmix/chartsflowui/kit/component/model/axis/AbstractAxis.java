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
import io.jmix.chartsflowui.kit.component.model.shared.AbstractAxisPointer;
import io.jmix.chartsflowui.kit.component.model.shared.JsFunction;

public abstract class AbstractAxis<T extends AbstractAxis<T>> extends ChartObservableObject {

    protected String id;

    protected AxisType type;

    protected BoundaryGap boundaryGap;

    protected String min;

    protected JsFunction minFunction;

    protected String max;

    protected JsFunction maxFunction;

    protected Boolean scale;

    protected Integer splitNumber;

    protected Integer minInterval;

    protected Integer maxInterval;

    protected Integer interval;

    protected Integer logBase;

    protected Boolean silent;

    protected Boolean triggerEvent;

    protected AxisLine axisLine;

    protected AxisTick axisTick;

    protected MinorTick minorTick;

    protected AxisLabel axisLabel;

    protected SplitLine splitLine;

    protected MinorSplitLine minorSplitLine;

    protected SplitArea splitArea;

    protected AxisPointer axisPointer;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDuration;

    protected JsFunction animationDurationFunction;

    protected String animationEasing;

    protected Integer animationDelay;

    protected JsFunction animationDelayFunction;

    protected Integer animationDurationUpdate;

    protected JsFunction animationDurationUpdateFunction;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    protected JsFunction animationDelayUpdateFunction;

    protected Integer zLevel;

    protected Integer z;

    protected AbstractAxis(AxisType type) {
        this.type = type;
    }

    public static class BoundaryGap {

        protected Boolean categoryGap;

        protected String[] nonCategoryGap;

        public BoundaryGap(Boolean categoryGap) {
            this.categoryGap = categoryGap;
        }

        public BoundaryGap(String min, String max) {
            nonCategoryGap = new String[]{min, max};
        }

        public Boolean getCategoryGap() {
            return categoryGap;
        }

        public String[] getNonCategoryGap() {
            return nonCategoryGap;
        }
    }

    public static class AxisPointer extends AbstractAxisPointer<AxisPointer> {
    }

    public AxisType getType() {
        return type;
    }

    public void setType(AxisType type) {
        this.type = type;
        markAsDirty();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public BoundaryGap getBoundaryGap() {
        return boundaryGap;
    }

    public void setBoundaryGap(Boolean categoryGap) {
        this.boundaryGap = new BoundaryGap(categoryGap);
        markAsDirty();
    }

    public void setBoundaryGap(String min, String max) {
        this.boundaryGap = new BoundaryGap(min, max);
        markAsDirty();
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
        markAsDirty();
    }

    public JsFunction getMinFunction() {
        return minFunction;
    }

    public void setMinFunction(JsFunction minFunction) {
        this.minFunction = minFunction;
        markAsDirty();
    }

    public void setMinFunction(String minFunction) {
        this.minFunction = new JsFunction(minFunction);
        markAsDirty();
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
        markAsDirty();
    }

    public JsFunction getMaxFunction() {
        return maxFunction;
    }

    public void setMaxFunction(JsFunction maxFunction) {
        this.maxFunction = maxFunction;
        markAsDirty();
    }

    public void setMaxFunction(String maxFunction) {
        this.maxFunction = new JsFunction(maxFunction);
        markAsDirty();
    }

    public Boolean getScale() {
        return scale;
    }

    public void setScale(Boolean scale) {
        this.scale = scale;
        markAsDirty();
    }

    public Integer getSplitNumber() {
        return splitNumber;
    }

    public void setSplitNumber(Integer splitNumber) {
        this.splitNumber = splitNumber;
        markAsDirty();
    }

    public Integer getMinInterval() {
        return minInterval;
    }

    public void setMinInterval(Integer minInterval) {
        this.minInterval = minInterval;
        markAsDirty();
    }

    public Integer getMaxInterval() {
        return maxInterval;
    }

    public void setMaxInterval(Integer maxInterval) {
        this.maxInterval = maxInterval;
        markAsDirty();
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
        markAsDirty();
    }

    public Integer getLogBase() {
        return logBase;
    }

    public void setLogBase(Integer logBase) {
        this.logBase = logBase;
        markAsDirty();
    }

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(Boolean silent) {
        this.silent = silent;
        markAsDirty();
    }

    public Boolean getTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(Boolean triggerEvent) {
        this.triggerEvent = triggerEvent;
        markAsDirty();
    }

    public AxisLine getAxisLine() {
        return axisLine;
    }

    public void setAxisLine(AxisLine axisLine) {
        if (this.axisLine != null) {
            removeChild(this.axisLine);
        }

        this.axisLine = axisLine;
        addChild(axisLine);
    }

    public AxisTick getAxisTick() {
        return axisTick;
    }

    public void setAxisTick(AxisTick axisTick) {
        if (this.axisTick != null) {
            removeChild(this.axisTick);
        }

        this.axisTick = axisTick;
        addChild(axisTick);
    }

    public MinorTick getMinorTick() {
        return minorTick;
    }

    public void setMinorTick(MinorTick minorTick) {
        if (this.minorTick != null) {
            removeChild(this.minorTick);
        }

        this.minorTick = minorTick;
        addChild(minorTick);
    }

    public AxisLabel getAxisLabel() {
        return axisLabel;
    }

    public void setAxisLabel(AxisLabel axisLabel) {
        if (this.axisLabel != null) {
            removeChild(this.axisLabel);
        }

        this.axisLabel = axisLabel;
        addChild(axisLabel);
    }

    public SplitLine getSplitLine() {
        return splitLine;
    }

    public void setSplitLine(SplitLine splitLine) {
        if (this.splitLine != null) {
            removeChild(this.splitLine);
        }

        this.splitLine = splitLine;
        addChild(splitLine);
    }

    public MinorSplitLine getMinorSplitLine() {
        return minorSplitLine;
    }

    public void setMinorSplitLine(MinorSplitLine minorSplitLine) {
        if (this.minorSplitLine != null) {
            removeChild(this.minorSplitLine);
        }

        this.minorSplitLine = minorSplitLine;
        addChild(minorSplitLine);
    }

    public SplitArea getSplitArea() {
        return splitArea;
    }

    public void setSplitArea(SplitArea splitArea) {
        if (this.splitArea != null) {
            removeChild(this.splitArea);
        }

        this.splitArea = splitArea;
        addChild(splitArea);
    }

    public AxisPointer getAxisPointer() {
        return axisPointer;
    }

    public void setAxisPointer(AxisPointer axisPointer) {
        if (this.axisPointer != null) {
            removeChild(this.axisPointer);
        }

        this.axisPointer = axisPointer;
        addChild(axisPointer);
    }

    public Boolean getAnimation() {
        return animation;
    }

    public void setAnimation(Boolean animation) {
        this.animation = animation;
        markAsDirty();
    }

    public Integer getAnimationThreshold() {
        return animationThreshold;
    }

    public void setAnimationThreshold(Integer animationThreshold) {
        this.animationThreshold = animationThreshold;
        markAsDirty();
    }

    public Integer getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(Integer animationDuration) {
        this.animationDuration = animationDuration;
        markAsDirty();
    }

    public JsFunction getAnimationDurationFunction() {
        return animationDurationFunction;
    }

    public void setAnimationDurationFunction(JsFunction animationDurationFunction) {
        this.animationDurationFunction = animationDurationFunction;
        markAsDirty();
    }

    public void setAnimationDurationFunction(String animationDurationFunction) {
        this.animationDurationFunction = new JsFunction(animationDurationFunction);
        markAsDirty();
    }

    public String getAnimationEasing() {
        return animationEasing;
    }

    public void setAnimationEasing(String animationEasing) {
        this.animationEasing = animationEasing;
        markAsDirty();
    }

    public Integer getAnimationDelay() {
        return animationDelay;
    }

    public void setAnimationDelay(Integer animationDelay) {
        this.animationDelay = animationDelay;
        markAsDirty();
    }

    public JsFunction getAnimationDelayFunction() {
        return animationDelayFunction;
    }

    public void setAnimationDelayFunction(JsFunction animationDelayFunction) {
        this.animationDelayFunction = animationDelayFunction;
        markAsDirty();
    }

    public void setAnimationDelayFunction(String animationDelayFunction) {
        this.animationDelayFunction = new JsFunction(animationDelayFunction);
    }

    public Integer getAnimationDurationUpdate() {
        return animationDurationUpdate;
    }

    public void setAnimationDurationUpdate(Integer animationDurationUpdate) {
        this.animationDurationUpdate = animationDurationUpdate;
        markAsDirty();
    }

    public JsFunction getAnimationDurationUpdateFunction() {
        return animationDurationUpdateFunction;
    }

    public void setAnimationDurationUpdateFunction(JsFunction animationDurationUpdateFunction) {
        this.animationDurationUpdateFunction = animationDurationUpdateFunction;
        markAsDirty();
    }

    public void setAnimationDurationUpdateFunction(String animationDurationUpdateFunction) {
        this.animationDurationUpdateFunction = new JsFunction(animationDurationUpdateFunction);
        markAsDirty();
    }

    public String getAnimationEasingUpdate() {
        return animationEasingUpdate;
    }

    public void setAnimationEasingUpdate(String animationEasingUpdate) {
        this.animationEasingUpdate = animationEasingUpdate;
        markAsDirty();
    }

    public Integer getAnimationDelayUpdate() {
        return animationDelayUpdate;
    }

    public void setAnimationDelayUpdate(Integer animationDelayUpdate) {
        this.animationDelayUpdate = animationDelayUpdate;
        markAsDirty();
    }

    public JsFunction getAnimationDelayUpdateFunction() {
        return animationDelayUpdateFunction;
    }

    public void setAnimationDelayUpdateFunction(JsFunction animationDelayUpdateFunction) {
        this.animationDelayUpdateFunction = animationDelayUpdateFunction;
        markAsDirty();
    }

    public void setAnimationDelayUpdateFunction(String animationDelayUpdateFunction) {
        this.animationDelayUpdateFunction = new JsFunction(animationDelayUpdateFunction);
        markAsDirty();
    }

    public Integer getZLevel() {
        return zLevel;
    }

    public void setZLevel(Integer zLevel) {
        this.zLevel = zLevel;
        markAsDirty();
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
        markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public T withType(AxisType type) {
        setType(type);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withId(String id) {
        setId(id);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBoundaryGap(Boolean categoryGap) {
        setBoundaryGap(categoryGap);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBoundaryGap(String min, String max) {
        setBoundaryGap(min, max);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMin(String min) {
        setMin(min);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMinFunction(JsFunction minFunction) {
        setMinFunction(minFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMinFunction(String minFunction) {
        setMinFunction(minFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMax(String max) {
        setMax(max);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMaxFunction(JsFunction maxFunction) {
        setMaxFunction(maxFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMaxFunction(String maxFunction) {
        setMaxFunction(maxFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withScale(Boolean scale) {
        setScale(scale);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSplitNumber(Integer splitNumber) {
        setSplitNumber(splitNumber);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMinInterval(Integer minInterval) {
        setMinInterval(minInterval);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMaxInterval(Integer maxInterval) {
        setMaxInterval(maxInterval);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withInterval(Integer interval) {
        setInterval(interval);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLogBase(Integer logBase) {
        setLogBase(logBase);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSilent(Boolean silent) {
        setSilent(silent);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withTriggerEvent(Boolean triggerEvent) {
        setTriggerEvent(triggerEvent);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAxisLine(AxisLine axisLine) {
        setAxisLine(axisLine);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAxisTick(AxisTick axisTick) {
        setAxisTick(axisTick);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMinorTick(MinorTick minorTick) {
        setMinorTick(minorTick);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAxisLabel(AxisLabel axisLabel) {
        setAxisLabel(axisLabel);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSplitLine(SplitLine splitLine) {
        setSplitLine(splitLine);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withMinorSplitLine(MinorSplitLine minorSplitLine) {
        setMinorSplitLine(minorSplitLine);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSplitArea(SplitArea splitArea) {
        setSplitArea(splitArea);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAxisPointer(AxisPointer axisPointer) {
        setAxisPointer(axisPointer);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimation(Boolean animation) {
        setAnimation(animation);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationThreshold(Integer animationThreshold) {
        setAnimationThreshold(animationThreshold);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDuration(Integer animationDuration) {
        setAnimationDuration(animationDuration);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDurationFunction(JsFunction animationDurationFunction) {
        setAnimationDurationFunction(animationDurationFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDurationFunction(String animationDurationFunction) {
        setAnimationDurationFunction(animationDurationFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationEasing(String animationEasing) {
        setAnimationEasing(animationEasing);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDelay(Integer animationDelay) {
        setAnimationDelay(animationDelay);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDelayFunction(JsFunction animationDelayFunction) {
        setAnimationDelayFunction(animationDelayFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDelayFunction(String animationDelayFunction) {
        setAnimationDelayFunction(animationDelayFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDurationUpdateFunction(JsFunction animationDurationUpdateFunction) {
        setAnimationDurationUpdateFunction(animationDurationUpdateFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDurationUpdateFunction(String animationDurationUpdateFunction) {
        setAnimationDurationUpdateFunction(animationDurationUpdateFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationEasingUpdate(String animationEasingUpdate) {
        setAnimationEasingUpdate(animationEasingUpdate);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDelayUpdate(Integer animationDelayUpdate) {
        setAnimationDelayUpdate(animationDelayUpdate);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDelayUpdateFunction(JsFunction animationDelayUpdateFunction) {
        setAnimationDelayUpdateFunction(animationDelayUpdateFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAnimationDelayUpdateFunction(String animationDelayUpdateFunction) {
        setAnimationDelayUpdateFunction(animationDelayUpdateFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withZLevel(Integer zLevel) {
        setZLevel(zLevel);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withZ(Integer z) {
        setZ(z);
        return (T) this;
    }
}
