/*
 * Copyright 2021 Haulmont.
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

package io.jmix.charts.model.balloon;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Align;
import io.jmix.charts.model.Color;
import io.jmix.charts.model.cursor.Cursor;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Creates the balloons (tooltips) of the chart, It follows the mouse cursor when you roll-over the data items.
 * <br>
 * See documentation for properties of AmBalloon JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptcharts/AmBalloon">http://docs.amcharts.com/3/javascriptcharts/AmBalloon</a>
 */
@StudioElement(
        caption = "Balloon",
        xmlElement = "balloon",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class Balloon extends AbstractChartObject {

    private static final long serialVersionUID = -4143720120608274912L;

    private Boolean adjustBorderColor;

    private Double animationDuration;

    private Double borderAlpha;

    private Color borderColor;

    private Integer borderThickness;

    private Color color;

    private Integer cornerRadius;

    private Boolean disableMouseEvents;

    private Boolean drop;

    private Boolean enabled;

    private Double fadeOutDuration;

    private Double fillAlpha;

    private Color fillColor;

    private Boolean fixedPosition;

    private Integer fontSize;

    private Integer horizontalPadding;

    private Integer maxWidth;

    private Integer offsetX;

    private Integer offsetY;

    private PointerOrientation pointerOrientation;

    private Integer pointerWidth;

    private Double shadowAlpha;

    private Color shadowColor;

    private Boolean showBullet;

    private Align textAlign;

    private Integer verticalPadding;

    /**
     * @return true if border color will be changed when user rolls-over the slice, graph, etc instead of background
     * color
     */
    public Boolean getAdjustBorderColor() {
        return adjustBorderColor;
    }

    /**
     * Set adjustBorderColor to true if you want that the border color will be changed when user rolls-over the
     * slice, graph, etc instead of background color. If not set the default value is true.
     *
     * @param adjustBorderColor adjust border color option
     * @return balloon
     */
    @StudioProperty(defaultValue = "true")
    public Balloon setAdjustBorderColor(Boolean adjustBorderColor) {
        this.adjustBorderColor = adjustBorderColor;
        return this;
    }

    /**
     * @return animation duration in seconds
     */
    public Double getAnimationDuration() {
        return animationDuration;
    }

    /**
     * Sets duration of balloon movement from previous point to current point, in seconds. If not set the default
     * value is 0.3.
     *
     * @param animationDuration animation duration in seconds
     * @return balloon
     */
    @StudioProperty(defaultValue = "0.3")
    public Balloon setAnimationDuration(Double animationDuration) {
        this.animationDuration = animationDuration;
        return this;
    }

    /**
     * @return border opacity
     */
    public Double getBorderAlpha() {
        return borderAlpha;
    }

    /**
     * Sets balloon border opacity. Value range is 0 - 1. If not set the default value is 1.
     *
     * @param borderAlpha border opacity
     * @return balloon
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public Balloon setBorderAlpha(Double borderAlpha) {
        this.borderAlpha = borderAlpha;
        return this;
    }

    /**
     * @return border color
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets balloon border color. Will only be used of adjustBorderColor is false. If not set the default value is
     * #FFFFFF.
     *
     * @param borderColor border color
     * @return balloon
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#FFFFFF")
    public Balloon setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    /**
     * @return border thickness
     */
    public Integer getBorderThickness() {
        return borderThickness;
    }

    /**
     * Sets balloon border thickness. If not set the default value is 2.
     *
     * @param borderThickness border thickness
     * @return balloon
     */
    @StudioProperty(defaultValue = "2")
    public Balloon setBorderThickness(Integer borderThickness) {
        this.borderThickness = borderThickness;
        return this;
    }

    /**
     * @return color of text
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets color of text in the balloon. If not set the default value is #000000.
     *
     * @param color color of the text
     * @return balloon
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    public Balloon setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return balloon corner radius
     */
    public Integer getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets balloon corner radius. If not set the default value is 0.
     *
     * @param cornerRadius corner radius
     * @return balloon
     */
    @StudioProperty(defaultValue = "0")
    public Balloon setCornerRadius(Integer cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }

    /**
     * @return duration of a fade out animation, in seconds
     */
    public Double getFadeOutDuration() {
        return fadeOutDuration;
    }

    /**
     * Sets duration of a fade out animation, in seconds. If not set the default value is 0.3.
     *
     * @param fadeOutDuration duration in seconds
     * @return balloon
     */
    @StudioProperty(defaultValue = "0.3")
    public Balloon setFadeOutDuration(Double fadeOutDuration) {
        this.fadeOutDuration = fadeOutDuration;
        return this;
    }

    /**
     * @return background opacity
     */
    public Double getFillAlpha() {
        return fillAlpha;
    }

    /**
     * Sets balloon background opacity. If not set the default value is 0.8.
     *
     * @param fillAlpha background opacity
     * @return balloon
     */
    @StudioProperty(defaultValue = "0.8")
    @Max(1)
    @Min(0)
    public Balloon setFillAlpha(Double fillAlpha) {
        this.fillAlpha = fillAlpha;
        return this;
    }

    /**
     * @return background color
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Sets balloon background color. Usually balloon background color is set by the chart. Only if adjustBorderColor
     * is true this color will be used. If not set the default value is #FFFFFF.
     *
     * @param fillColor background color
     * @return balloon
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#FFFFFF")
    public Balloon setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    /**
     * @return true if balloon stays in fixed position; false if follows mouse when hovering the slice/column/bullet
     */
    public Boolean getFixedPosition() {
        return fixedPosition;
    }

    /**
     * Specifies if balloon should follow mouse when hovering the slice/column/bullet or stay in fixed position
     * (this does not affect balloon behavior if ChartCursor is used). If not set the default value is true.
     *
     * @param fixedPosition fixed position option
     * @return balloon
     */
    @StudioProperty(defaultValue = "true")
    public Balloon setFixedPosition(Boolean fixedPosition) {
        this.fixedPosition = fixedPosition;
        return this;
    }

    /**
     * @return size of text in the balloon
     */
    public Integer getFontSize() {
        return fontSize;
    }

    /**
     * Sets size of text in the balloon. Chart's fontSize is used by default.
     *
     * @param fontSize font size
     * @return balloon
     */
    @StudioProperty
    public Balloon setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * @return horizontal padding
     */
    public Integer getHorizontalPadding() {
        return horizontalPadding;
    }

    /**
     * Sets horizontal padding of the balloon. If not set the default value is 8.
     *
     * @param horizontalPadding horizontal padding
     * @return balloon
     */
    @StudioProperty(defaultValue = "8")
    public Balloon setHorizontalPadding(Integer horizontalPadding) {
        this.horizontalPadding = horizontalPadding;
        return this;
    }

    /**
     * @return maximum width
     */
    public Integer getMaxWidth() {
        return maxWidth;
    }

    /**
     * Sets maximum width of a balloon.
     *
     * @param maxWidth maximum width
     * @return balloon
     */
    @StudioProperty
    public Balloon setMaxWidth(Integer maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    /**
     * @return horizontal distance from mouse pointer to balloon pointer
     */
    public Integer getOffsetX() {
        return offsetX;
    }

    /**
     * Sets horizontal distance from mouse pointer to balloon pointer. If you set it to a small value, the balloon might
     * flicker, as mouse might lose focus on hovered object. Note, this setting is ignored unless fixedPosition is set
     * to false or Chart Cursor is enabled. If not set the default value is 1.
     *
     * @param offsetX horizontal distance from mouse pointer to balloon pointer
     * @return balloon
     */
    @StudioProperty(defaultValue = "1")
    public Balloon setOffsetX(Integer offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    /**
     * @return vertical distance from mouse pointer to balloon pointer
     */
    public Integer getOffsetY() {
        return offsetY;
    }

    /**
     * Sets vertical distance from mouse pointer to balloon pointer. If you set it to a small value, the balloon might
     * flicker, as mouse might lose focus on hovered object. Note, this setting is ignored unless fixedPosition is set
     * to false or Chart Cursor is enabled. If not set the default value is 6.
     *
     * @param offsetY vertical distance from mouse pointer to balloon pointer
     * @return balloon
     */
    @StudioProperty(defaultValue = "6")
    public Balloon setOffsetY(Integer offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    /**
     * @return pointer width
     */
    public Integer getPointerWidth() {
        return pointerWidth;
    }

    /**
     * Specifies direction of a pointer. Works only if balloon drop set to true. If not set the default value is 6.
     *
     * @param pointerWidth pointer width
     * @return balloon
     */
    @StudioProperty(defaultValue = "6")
    public Balloon setPointerWidth(Integer pointerWidth) {
        this.pointerWidth = pointerWidth;
        return this;
    }

    /**
     * @return opacity of a shadow
     */
    public Double getShadowAlpha() {
        return shadowAlpha;
    }

    /**
     * Sets opacity of a shadow. If not set the default value is 0.4.
     *
     * @param shadowAlpha opacity of a shadow
     * @return balloon
     */
    @StudioProperty(defaultValue = "0.4")
    @Max(1)
    @Min(0)
    public Balloon setShadowAlpha(Double shadowAlpha) {
        this.shadowAlpha = shadowAlpha;
        return this;
    }

    /**
     * @return shadow color
     */
    public Color getShadowColor() {
        return shadowColor;
    }

    /**
     * Sets color of a shadow. If not set the default value is #000000.
     *
     * @param shadowColor shadow color
     * @return balloon
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    public Balloon setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
        return this;
    }

    /**
     * @return true if showBullet is enabled
     */
    public Boolean getShowBullet() {
        return showBullet;
    }

    /**
     * If cornerRadius of a balloon is greater than 0, showBullet is set to true for value balloons when {@link Cursor}
     * is used. If you don't want the bullet near the balloon, set it to false. If not set the default value is false.
     *
     * @param showBullet show bullet option
     * @return balloon
     */
    @StudioProperty(defaultValue = "false")
    public Balloon setShowBullet(Boolean showBullet) {
        this.showBullet = showBullet;
        return this;
    }

    /**
     * @return text alignment
     */
    public Align getTextAlign() {
        return textAlign;
    }

    /**
     * Sets text alignment, possible values "left", "middle" and "right". If not set the default value is CENTER.
     *
     * @param textAlign text alignment
     * @return balloon
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "CENTER")
    public Balloon setTextAlign(Align textAlign) {
        this.textAlign = textAlign;
        return this;
    }

    /**
     * @return vertical padding
     */
    public Integer getVerticalPadding() {
        return verticalPadding;
    }

    /**
     * Sets vertical padding of the balloon. If not set the default value is 4.
     *
     * @param verticalPadding vertical padding
     * @return balloon
     */
    @StudioProperty(defaultValue = "4")
    public Balloon setVerticalPadding(Integer verticalPadding) {
        this.verticalPadding = verticalPadding;
        return this;
    }

    /**
     * @return true if links in the balloon text is not clickable
     */
    public Boolean getDisableMouseEvents() {
        return disableMouseEvents;
    }

    /**
     * Set disableMouseEvents to false if your balloon has links and you want those links to be clickable. If not set
     * the default value is true.
     *
     * @param disableMouseEvents disable mouse eventes option
     * @return balloon
     */
    @StudioProperty(defaultValue = "true")
    public Balloon setDisableMouseEvents(Boolean disableMouseEvents) {
        this.disableMouseEvents = disableMouseEvents;
        return this;
    }

    /**
     * @return true if drop is enabled
     */
    public Boolean getDrop() {
        return drop;
    }

    /**
     * Allows having drop-shaped balloons. Note, these balloons will not check for overlapping with other balloons,
     * or if they go outside plot area. It also does not change pointer orientation automatically based on its
     * vertical position like regular balloons do. You can use pointerOrientation property if you want it to point to
     * different direction. Not supported by IE8. If not set the default value is false.
     *
     * @param drop drop option
     * @return balloon
     */
    @StudioProperty(defaultValue = "false")
    public Balloon setDrop(Boolean drop) {
        this.drop = drop;
        return this;
    }

    /**
     * @return true if balloons are enabled for certain value axes
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Set enabled to false if you want to disable balloons for certain value axes. If not set the default value is
     * true.
     *
     * @param enabled enabled option
     * @return balloon
     */
    @StudioProperty(defaultValue = "true")
    public Balloon setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * @return pointer orientation
     */
    public PointerOrientation getPointerOrientation() {
        return pointerOrientation;
    }

    /**
     * Specifies direction of a pointer. Works only if {@link Balloon#drop} set to true. If not set the default value
     * is DOWN.
     *
     * @param pointerOrientation pointer orientation
     * @return balloon
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "DOWN")
    public Balloon setPointerOrientation(PointerOrientation pointerOrientation) {
        this.pointerOrientation = pointerOrientation;
        return this;
    }
}