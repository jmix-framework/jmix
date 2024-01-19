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

package io.jmix.chartsflowui.kit.component.model;

import io.jmix.chartsflowui.kit.component.model.shared.*;
import jakarta.annotation.Nullable;

public class Title extends ChartObservableObject
        implements HasShadow<Title>, HasPosition<Title>, HasPadding<Title>, HasBorder<Title> {

    protected String id;

    protected Boolean show;

    protected String text;

    protected String link;

    protected Target target;

    protected TextStyle textStyle;

    protected String subtext;

    protected String sublink;

    protected Target subtarget;

    protected SubtextStyle subtextStyle;

    protected TextAlign textAlign;

    protected TextVerticalAlign textVerticalAlign;

    protected Boolean triggerEvent;

    protected Padding padding;

    protected Integer itemGap;

    protected Integer zLevel;

    protected Integer z;

    protected String left;

    protected String top;

    protected String right;

    protected String bottom;

    protected Color backgroundColor;

    protected Color borderColor;

    protected Integer borderWidth;

    protected Integer borderRadius;

    protected Integer shadowBlur;

    protected Color shadowColor;

    protected Integer shadowOffsetX;

    protected Integer shadowOffsetY;

    public Title() {
    }

    public Title(String text) {
        this.text = text;
    }

    public static class TextStyle extends AbstractRichText<TextStyle> {
    }

    public static class SubtextStyle extends AbstractRichText<SubtextStyle>
            implements HasAlign<SubtextStyle> {

        protected Align align;

        protected VerticalAlign verticalAlign;

        @Override
        public Align getAlign() {
            return align;
        }

        @Override
        public void setAlign(Align align) {
            this.align = align;
            markAsDirty();
        }

        @Override
        public VerticalAlign getVerticalAlign() {
            return verticalAlign;
        }

        @Override
        public void setVerticalAlign(VerticalAlign verticalAlign) {
            this.verticalAlign = verticalAlign;
            markAsDirty();
        }
    }

    public enum Target implements HasEnumId {
        SELF("self"),
        BLANK("blank");

        private final String id;

        Target(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static Target fromId(String id) {
            for (Target at : Target.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public enum TextAlign implements HasEnumId {
        AUTO("auto"),
        LEFT("left"),
        RIGHT("right"),
        CENTER("center");

        private final String id;

        TextAlign(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static TextAlign fromId(String id) {
            for (TextAlign at : TextAlign.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public enum TextVerticalAlign implements HasEnumId {
        AUTO("auto"),
        TOP("top"),
        BOTTOM("bottom"),
        MIDDLE("middle");

        private final String id;

        TextVerticalAlign(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }

        @Nullable
        public static TextVerticalAlign fromId(String id) {
            for (TextVerticalAlign at : TextVerticalAlign.values()) {
                if (at.getId().equals(id)) {
                    return at;
                }
            }
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        markAsDirty();
    }

    public Boolean isShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
        markAsDirty();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        markAsDirty();
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
        markAsDirty();
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
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

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
        markAsDirty();
    }

    public String getSublink() {
        return sublink;
    }

    public void setSublink(String sublink) {
        this.sublink = sublink;
        markAsDirty();
    }

    public Target getSubtarget() {
        return subtarget;
    }

    public void setSubtarget(Target subtarget) {
        this.subtarget = subtarget;
        markAsDirty();
    }

    public SubtextStyle getSubtextStyle() {
        return subtextStyle;
    }

    public void setSubtextStyle(SubtextStyle subtextStyle) {
        if (this.subtextStyle != null) {
            removeChild(this.subtextStyle);
        }

        this.subtextStyle = subtextStyle;
        addChild(subtextStyle);
    }

    public TextAlign getTextAlign() {
        return textAlign;
    }

    public void setTextAlign(TextAlign textAlign) {
        this.textAlign = textAlign;
        markAsDirty();
    }

    public TextVerticalAlign getTextVerticalAlign() {
        return textVerticalAlign;
    }

    public void setTextVerticalAlign(TextVerticalAlign textVerticalAlign) {
        this.textVerticalAlign = textVerticalAlign;
        markAsDirty();
    }

    public Boolean isTriggerEvent() {
        return triggerEvent;
    }

    public void setTriggerEvent(Boolean triggerEvent) {
        this.triggerEvent = triggerEvent;
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

    public Integer getItemGap() {
        return itemGap;
    }

    public void setItemGap(Integer itemGap) {
        this.itemGap = itemGap;
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

    @Override
    public String getLeft() {
        return left;
    }

    @Override
    public void setLeft(String left) {
        this.left = left;
        markAsDirty();
    }

    @Override
    public String getTop() {
        return top;
    }

    @Override
    public void setTop(String top) {
        this.top = top;
        markAsDirty();
    }

    @Override
    public String getRight() {
        return right;
    }

    @Override
    public void setRight(String right) {
        this.right = right;
        markAsDirty();
    }

    @Override
    public String getBottom() {
        return bottom;
    }

    @Override
    public void setBottom(String bottom) {
        this.bottom = bottom;
        markAsDirty();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        markAsDirty();
    }

    @Override
    public Color getBorderColor() {
        return borderColor;
    }

    @Override
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        markAsDirty();
    }

    @Override
    public Integer getBorderWidth() {
        return borderWidth;
    }

    @Override
    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;
        markAsDirty();
    }

    @Override
    public Integer getBorderRadius() {
        return borderRadius;
    }

    @Override
    public void setBorderRadius(Integer borderRadius) {
        this.borderRadius = borderRadius;
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

    public Title withId(String id) {
        setId(id);
        return this;
    }

    public Title withShow(Boolean show) {
        setShow(show);
        return this;
    }

    public Title withText(String text) {
        setText(text);
        return this;
    }

    public Title withLink(String link) {
        setLink(link);
        return this;
    }

    public Title withTarget(Target target) {
        setTarget(target);
        return this;
    }

    public Title withTextStyle(TextStyle textStyle) {
        setTextStyle(textStyle);
        return this;
    }

    public Title withSubtext(String subtext) {
        setSubtext(subtext);
        return this;
    }

    public Title withSublink(String sublink) {
        setSublink(sublink);
        return this;
    }

    public Title withSubtarget(Target subtarget) {
        setSubtarget(subtarget);
        return this;
    }

    public Title withSubtextStyle(SubtextStyle subtextStyle) {
        setSubtextStyle(subtextStyle);
        return this;
    }

    public Title withTextAlign(TextAlign textAlign) {
        setTextAlign(textAlign);
        return this;
    }

    public Title withTextVerticalAlign(TextVerticalAlign textVerticalAlign) {
        setTextVerticalAlign(textVerticalAlign);
        return this;
    }

    public Title withTriggerEvent(Boolean triggerEvent) {
        setTriggerEvent(triggerEvent);
        return this;
    }

    public Title withItemGap(Integer itemGap) {
        setItemGap(itemGap);
        return this;
    }

    public Title withZLevel(Integer zLevel) {
        setZLevel(zLevel);
        return this;
    }

    public Title withZ(Integer z) {
        setZ(z);
        return this;
    }

    public Title withBackgroundColor(Color backgroundColor) {
        setBackgroundColor(backgroundColor);
        return this;
    }
}
