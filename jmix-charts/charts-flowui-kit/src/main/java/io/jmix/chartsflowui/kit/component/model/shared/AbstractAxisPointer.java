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
import io.jmix.chartsflowui.kit.component.model.HasEnumId;
import io.jmix.chartsflowui.kit.component.model.HasShadow;
import jakarta.annotation.Nullable;

/**
 * The base class for axis pointer.
 *
 * @param <T> origin axis pointer class type
 */
public class AbstractAxisPointer<T extends AbstractAxisPointer<T>> extends ChartObservableObject {

    protected Boolean show;

    protected IndicatorType type;

    protected Boolean snap;

    protected Integer z;

    protected Label label;

    protected LineStyle lineStyle;

    protected ShadowStyle shadowStyle;

    protected Boolean triggerEmphasis;

    protected Boolean triggerTooltip;

    protected Integer value;

    protected Boolean status;

    protected Handle handle;

    public enum IndicatorType implements HasEnumId {
        LINE("line"),
        SHADOW("shadow"),
        NONE("none");

        private final String id;

        IndicatorType(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static IndicatorType fromId(String id) {
            for (IndicatorType at : IndicatorType.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public static class Handle extends ChartObservableObject
            implements HasShadow<Handle> {

        protected Boolean show;

        protected String icon;

        protected Integer[] size;

        protected Integer margin;

        protected Color color;

        protected Integer throttle;

        protected Integer shadowBlur;

        protected Color shadowColor;

        protected Integer shadowOffsetX;

        protected Integer shadowOffsetY;

        public Boolean getShow() {
            return show;
        }

        public void setShow(Boolean show) {
            this.show = show;
            markAsDirty();
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
            markAsDirty();
        }

        public Integer[] getSize() {
            return size;
        }

        public void setSize(Integer width, Integer height) {
            this.size = new Integer[]{width, height};
            markAsDirty();
        }

        public Integer getMargin() {
            return margin;
        }

        public void setMargin(Integer margin) {
            this.margin = margin;
            markAsDirty();
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
            markAsDirty();
        }

        public Integer getThrottle() {
            return throttle;
        }

        public void setThrottle(Integer throttle) {
            this.throttle = throttle;
            markAsDirty();
        }

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

        public Handle withShow(Boolean show) {
            setShow(show);
            return this;
        }

        public Handle withIcon(String icon) {
            setIcon(icon);
            return this;
        }

        public Handle withSize(Integer width, Integer height) {
            setSize(width, height);
            return this;
        }

        public Handle withMargin(Integer margin) {
            setMargin(margin);
            return this;
        }

        public Handle withColor(Color color) {
            setColor(color);
            return this;
        }

        public Handle withThrottle(Integer throttle) {
            setThrottle(throttle);
            return this;
        }
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public IndicatorType getType() {
        return type;
    }

    public void setType(IndicatorType type) {
        this.type = type;
        markAsDirty();
    }

    public Boolean getSnap() {
        return snap;
    }

    public void setSnap(Boolean snap) {
        this.snap = snap;
        markAsDirty();
    }

    public Integer getZ() {
        return z;
    }

    public void setZ(Integer z) {
        this.z = z;
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

    public ShadowStyle getShadowStyle() {
        return shadowStyle;
    }

    public void setShadowStyle(ShadowStyle shadowStyle) {
        if (this.shadowStyle != null) {
            removeChild(this.shadowStyle);
        }

        this.shadowStyle = shadowStyle;
        addChild(this.shadowStyle);
    }

    public Boolean getTriggerEmphasis() {
        return triggerEmphasis;
    }

    public void setTriggerEmphasis(Boolean triggerEmphasis) {
        this.triggerEmphasis = triggerEmphasis;
        markAsDirty();
    }

    public Boolean getTriggerTooltip() {
        return triggerTooltip;
    }

    public void setTriggerTooltip(Boolean triggerTooltip) {
        this.triggerTooltip = triggerTooltip;
        markAsDirty();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
        markAsDirty();
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
        markAsDirty();
    }

    public Handle getHandle() {
        return handle;
    }

    public void setHandle(Handle handle) {
        if (this.handle != null) {
            removeChild(this.handle);
        }

        this.handle = handle;
        addChild(handle);
    }

    @SuppressWarnings("unchecked")
    public T withShow(Boolean show) {
        setShow(show);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withType(IndicatorType type) {
        setType(type);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withSnap(Boolean snap) {
        setSnap(snap);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withZ(Integer z) {
        setZ(z);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLabel(Label label) {
        setLabel(label);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withLineStyle(LineStyle lineStyle) {
        setLineStyle(lineStyle);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withShadowStyle(ShadowStyle shadowStyle) {
        setShadowStyle(shadowStyle);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withTriggerEmphasis(Boolean triggerEmphasis) {
        setTriggerEmphasis(triggerEmphasis);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withTriggerTooltip(Boolean triggerTooltip) {
        setTriggerTooltip(triggerTooltip);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withValue(Integer value) {
        setValue(value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withStatus(Boolean status) {
        setStatus(status);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withHandle(Handle handle) {
        setHandle(handle);
        return (T) this;
    }
}