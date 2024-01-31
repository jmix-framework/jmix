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

package io.jmix.chartsflowui.kit.component.model.shared;

import io.jmix.chartsflowui.kit.component.model.ChartObservableObject;
import io.jmix.chartsflowui.kit.component.model.HasShadow;

/**
 * The base class for area style.
 *
 * @param <T> origin area style class type
 */
public abstract class AbstractAreaStyle<T extends AbstractAreaStyle<T>> extends ChartObservableObject
        implements HasShadow<T> {

    protected Integer shadowBlur;

    protected Color shadowColor;

    protected Integer shadowOffsetX;

    protected Integer shadowOffsetY;

    protected Double opacity;

    @Override
    public Integer getShadowBlur() {
        return shadowBlur;
    }

    @Override
    public void setShadowBlur(Integer shadowBlur) {
        this.shadowBlur = shadowBlur;
        markAsDirty();
    }

    @Override
    public Color getShadowColor() {
        return shadowColor;
    }

    @Override
    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
        markAsDirty();
    }

    @Override
    public Integer getShadowOffsetX() {
        return shadowOffsetX;
    }

    @Override
    public void setShadowOffsetX(Integer shadowOffsetX) {
        this.shadowOffsetX = shadowOffsetX;
        markAsDirty();
    }

    @Override
    public Integer getShadowOffsetY() {
        return shadowOffsetY;
    }

    @Override
    public void setShadowOffsetY(Integer shadowOffsetY) {
        this.shadowOffsetY = shadowOffsetY;
        markAsDirty();
    }

    public Double getOpacity() {
        return opacity;
    }

    public void setOpacity(Double opacity) {
        this.opacity = opacity;
        markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public T withOpacity(Double opacity) {
        setOpacity(opacity);
        return (T) this;
    }
}
