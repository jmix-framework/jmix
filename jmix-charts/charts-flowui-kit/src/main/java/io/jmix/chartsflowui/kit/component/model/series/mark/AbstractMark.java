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

package io.jmix.chartsflowui.kit.component.model.series.mark;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.series.Label;

public abstract class AbstractMark<T extends AbstractMark<T>> extends ChartObservableObject {

    protected Boolean silent;

    protected Label label;

    protected Boolean animation;

    protected Integer animationThreshold;

    protected Integer animationDuration;

    protected String animationEasing;

    protected Integer animationDelay;

    protected Integer animationDurationUpdate;

    protected String animationEasingUpdate;

    protected Integer animationDelayUpdate;

    public Boolean getSilent() {
        return silent;
    }

    public void setSilent(Boolean silent) {
        this.silent = silent;
        markAsDirty();
    }

    public Label getLabel() {
        return label;
    }

    public void setLabel(Label label) {
        if (this.label != null) {
            removeChild(this.label);
        }

        this.label = label;
        addChild(label);
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

    public Integer getAnimationDurationUpdate() {
        return animationDurationUpdate;
    }

    public void setAnimationDurationUpdate(Integer animationDurationUpdate) {
        this.animationDurationUpdate = animationDurationUpdate;
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

    @SuppressWarnings("unchecked")
    public T withSilent(Boolean silent) {
        setSilent(silent);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLabel(Label label) {
        setLabel(label);
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
    public T withAnimationDurationUpdate(Integer animationDurationUpdate) {
        setAnimationDurationUpdate(animationDurationUpdate);
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
}