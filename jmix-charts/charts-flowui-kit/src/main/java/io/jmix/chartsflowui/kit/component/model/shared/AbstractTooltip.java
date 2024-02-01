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
import jakarta.annotation.Nullable;

/**
 * The base class for tooltip components.
 *
 * @param <T> origin tooltip class type
 */
public abstract class AbstractTooltip<T extends ChartObservableObject> extends ChartObservableObject
        implements HasPadding<T> {

    protected Boolean show;

    protected AbstractTooltip.Trigger trigger;

    protected AbstractTooltip.Position position;

    protected AxisPointer axisPointer;

    protected String formatter;

    protected JsFunction formatterFunction;

    protected String valueFormatter;

    protected JsFunction valueFormatterFunction;

    protected Color backgroundColor;

    protected Color borderColor;

    protected Integer borderWidth;

    protected TextStyle textStyle;

    protected String extraCssText;

    protected Padding padding;

    public enum Trigger implements HasEnumId {
        ITEM("item"),
        AXIS("axis"),
        NONE("none");

        private final String id;

        Trigger(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Trigger fromId(String id) {
            for (Trigger at : Trigger.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }

            return null;
        }
    }

    public static class Position {

        protected String[] coordinates;

        protected ItemTriggerPosition itemTriggerPosition;

        public Position(String horizontalPosition, String verticalPosition) {
            coordinates = new String[]{horizontalPosition, verticalPosition};
        }

        public Position(ItemTriggerPosition itemTriggerPosition) {
            this.itemTriggerPosition = itemTriggerPosition;
        }

        public enum ItemTriggerPosition implements HasEnumId {
            INSIDE("inside"),
            TOP("top"),
            LEFT("left"),
            RIGHT("right"),
            BOTTOM("bottom");

            private final String id;

            ItemTriggerPosition(String id) {
                this.id = id;
            }

            @Override
            public String getId() {
                return id;
            }

            @Nullable
            public static ItemTriggerPosition fromId(String id) {
                for (ItemTriggerPosition at : ItemTriggerPosition.values()) {
                    if (at.getId().equals(id)) {
                        return at;
                    }
                }
                return null;
            }
        }

        public String[] getCoordinates() {
            return coordinates;
        }

        public ItemTriggerPosition getItemTriggerPosition() {
            return itemTriggerPosition;
        }
    }

    public static class AxisPointer extends ChartObservableObject {

        protected IndicatorType type;

        protected AxisType axis;

        protected Boolean snap;

        protected Integer z;

        protected Label label;

        protected LineStyle lineStyle;

        protected ShadowStyle shadowStyle;

        protected LineStyle crossStyle;

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

        public enum IndicatorType implements HasEnumId {
            LINE("line"),
            SHADOW("shadow"),
            NONE("none"),
            CROSS("cross");

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

        public enum AxisType implements HasEnumId {
            X("x"),
            Y("y"),
            RADIUS("radius"),
            ANGLE("angle");

            private final String id;

            AxisType(String id) {
                this.id = id;
            }

            @Override
            public String getId() {
                return id;
            }

            @Nullable
            public static AxisType fromId(String id) {
                for (AxisType at : AxisType.values()) {
                    if (at.getId().equals(id)) {
                        return at;
                    }
                }
                return null;
            }
        }

        public IndicatorType getType() {
            return type;
        }

        public void setType(IndicatorType type) {
            this.type = type;
            markAsDirty();
        }

        public AxisType getAxis() {
            return axis;
        }

        public void setAxis(AxisType axis) {
            this.axis = axis;
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
            addChild(shadowStyle);
        }

        public LineStyle getCrossStyle() {
            return crossStyle;
        }

        public void setCrossStyle(LineStyle crossStyle) {
            if (this.crossStyle != null) {
                removeChild(this.crossStyle);
            }

            this.crossStyle = crossStyle;
            addChild(crossStyle);
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

        public AxisPointer withType(IndicatorType type) {
            setType(type);
            return this;
        }

        public AxisPointer withAxis(AxisType axis) {
            setAxis(axis);
            return this;
        }

        public AxisPointer withSnap(Boolean snap) {
            setSnap(snap);
            return this;
        }

        public AxisPointer withZ(Integer z) {
            setZ(z);
            return this;
        }

        public AxisPointer withLabel(Label label) {
            setLabel(label);
            return this;
        }

        public AxisPointer withLineStyle(LineStyle lineStyle) {
            setLineStyle(lineStyle);
            return this;
        }

        public AxisPointer withShadowStyle(ShadowStyle shadowStyle) {
            setShadowStyle(shadowStyle);
            return this;
        }

        public AxisPointer withCrossStyle(LineStyle crossStyle) {
            setCrossStyle(crossStyle);
            return this;
        }

        public AxisPointer withAnimation(Boolean animation) {
            setAnimation(animation);
            return this;
        }

        public AxisPointer withAnimationThreshold(Integer animationThreshold) {
            setAnimationThreshold(animationThreshold);
            return this;
        }

        public AxisPointer withAnimationDuration(Integer animationDuration) {
            setAnimationDuration(animationDuration);
            return this;
        }

        public AxisPointer withAnimationDurationFunction(JsFunction animationDurationFunction) {
            setAnimationDurationFunction(animationDurationFunction);
            return this;
        }

        public AxisPointer withAnimationDurationFunction(String animationDurationFunction) {
            setAnimationDurationFunction(animationDurationFunction);
            return this;
        }

        public AxisPointer withAnimationEasing(String animationEasing) {
            setAnimationEasing(animationEasing);
            return this;
        }

        public AxisPointer withAnimationDelay(Integer animationDelay) {
            setAnimationDelay(animationDelay);
            return this;
        }

        public AxisPointer withAnimationDelayFunction(JsFunction animationDelayFunction) {
            setAnimationDelayFunction(animationDelayFunction);
            return this;
        }

        public AxisPointer withAnimationDelayFunction(String animationDelayFunction) {
            setAnimationDelayFunction(animationDelayFunction);
            return this;
        }

        public AxisPointer withAnimationDurationUpdate(Integer animationDurationUpdate) {
            setAnimationDurationUpdate(animationDurationUpdate);
            return this;
        }

        public AxisPointer withAnimationDurationUpdateFunction(JsFunction animationDurationUpdateFunction) {
            setAnimationDurationUpdateFunction(animationDurationUpdateFunction);
            return this;
        }

        public AxisPointer withAnimationDurationUpdateFunction(String animationDurationUpdateFunction) {
            setAnimationDurationUpdateFunction(animationDurationUpdateFunction);
            return this;
        }

        public AxisPointer withAnimationEasingUpdate(String animationEasingUpdate) {
            setAnimationEasingUpdate(animationEasingUpdate);
            return this;
        }

        public AxisPointer withAnimationDelayUpdate(Integer animationDelayUpdate) {
            setAnimationDelayUpdate(animationDelayUpdate);
            return this;
        }

        public AxisPointer withAnimationDelayUpdateFunction(JsFunction animationDelayUpdateFunction) {
            setAnimationDelayUpdateFunction(animationDelayUpdateFunction);
            return this;
        }

        public AxisPointer withAnimationDelayUpdateFunction(String animationDelayUpdateFunction) {
            setAnimationDelayUpdateFunction(animationDelayUpdateFunction);
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

    public AbstractTooltip.Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(AbstractTooltip.Trigger trigger) {
        this.trigger = trigger;
        markAsDirty();
    }

    public AbstractTooltip.Position getPosition() {
        return position;
    }

    public void setPosition(String horizontalPosition, String verticalPosition) {
        this.position = new AbstractTooltip.Position(horizontalPosition, verticalPosition);
        markAsDirty();
    }

    public void setPosition(AbstractTooltip.Position.ItemTriggerPosition position) {
        this.position = new AbstractTooltip.Position(position);
        markAsDirty();
    }

    public AbstractTooltip.AxisPointer getAxisPointer() {
        return axisPointer;
    }

    public void setAxisPointer(AbstractTooltip.AxisPointer axisPointer) {
        if (this.axisPointer != null) {
            removeChild(this.axisPointer);
        }

        this.axisPointer = axisPointer;
        addChild(axisPointer);
    }

    public String getFormatter() {
        return formatter;
    }

    public void setFormatter(String formatter) {
        this.formatter = formatter;
        markAsDirty();
    }

    public JsFunction getFormatterFunction() {
        return formatterFunction;
    }

    public void setFormatterFunction(JsFunction formatterFunction) {
        this.formatterFunction = formatterFunction;
        markAsDirty();
    }

    public void setFormatterFunction(String formatterFunction) {
        this.formatterFunction = new JsFunction(formatterFunction);
        markAsDirty();
    }

    public String getValueFormatter() {
        return valueFormatter;
    }

    public void setValueFormatter(String valueFormatter) {
        this.valueFormatter = valueFormatter;
        markAsDirty();
    }

    public JsFunction getValueFormatterFunction() {
        return valueFormatterFunction;
    }

    public void setValueFormatterFunction(JsFunction valueFormatterFunction) {
        this.valueFormatterFunction = valueFormatterFunction;
        markAsDirty();
    }

    public void setValueFormatterFunction(String valueFormatterFunction) {
        this.valueFormatterFunction = new JsFunction(valueFormatterFunction);
        markAsDirty();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markAsDirty();
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        markAsDirty();
    }

    public Integer getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;
        markAsDirty();
    }

    public TextStyle getTextStyle() {
        return textStyle;
    }

    public void setTextStyle(TextStyle textStyle) {
        if (this.textStyle != null) {
            removeChild(this.textStyle);
        }

        this.textStyle = textStyle;
        addChild(textStyle);
    }

    public String getExtraCssText() {
        return extraCssText;
    }

    public void setExtraCssText(String extraCssText) {
        this.extraCssText = extraCssText;
        markAsDirty();
    }

    @Override
    public Padding getPadding() {
        return padding;
    }

    @Override
    public void setPadding(Integer padding) {
        this.padding = new Padding(padding);
        markAsDirty();
    }

    @Override
    public void setPadding(Integer vertical, Integer horizontal) {
        this.padding = new Padding(vertical, horizontal);
        markAsDirty();
    }

    @Override
    public void setPadding(Integer top, Integer right, Integer bottom, Integer left) {
        this.padding = new Padding(top, right, bottom, left);
        markAsDirty();
    }

    @SuppressWarnings("unchecked")
    public T withShow(Boolean show) {
        setShow(show);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withTrigger(AbstractTooltip.Trigger trigger) {
        setTrigger(trigger);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withPosition(String horizontalPosition, String verticalPosition) {
        setPosition(horizontalPosition, verticalPosition);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withPosition(AbstractTooltip.Position.ItemTriggerPosition position) {
        setPosition(position);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withAxisPointer(AbstractTooltip.AxisPointer axisPointer) {
        setAxisPointer(axisPointer);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFormatter(String formatter) {
        setFormatter(formatter);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFormatterFunction(JsFunction formatterFunction) {
        setFormatterFunction(formatterFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withFormatterFunction(String formatterFunction) {
        setFormatterFunction(formatterFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withValueFormatter(String valueFormatter) {
        setValueFormatter(valueFormatter);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withValueFormatterFunction(JsFunction valueFormatterFunction) {
        setValueFormatterFunction(valueFormatterFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withValueFormatterFunction(String valueFormatterFunction) {
        setValueFormatterFunction(valueFormatterFunction);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBorderColor(Color borderColor) {
        setBorderColor(borderColor);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withBorderWidth(Integer borderWidth) {
        setBorderWidth(borderWidth);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withTextStyle(TextStyle textStyle) {
        setTextStyle(textStyle);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withExtraCssText(String extraCssText) {
        setExtraCssText(extraCssText);
        return (T) this;
    }
}
