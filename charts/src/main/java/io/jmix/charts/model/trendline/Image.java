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

package io.jmix.charts.model.trendline;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Color;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

/**
 * Image is used to add images to the end/start of trend lines. Allows you to display image anywhere on chart's plot
 * area.
 * <br>
 * See documentation for properties of Image JS Object. <br>
 *
 * <a href="https://docs.amcharts.com/3/javascriptstockchart/Image">https://docs.amcharts.com/3/javascriptstockchart/Image</a>
 */
@StudioElement
public class Image extends AbstractChartObject {

    private static final long serialVersionUID = -1259965238248274458L;

    private Color balloonColor;
    private String balloonText;
    private Color color;
    private Integer height;
    private Integer offsetX;
    private Integer offsetY;
    private Color outlineColor;
    private Integer rotation;
    private String svgPath;
    private String url;
    private Integer width;

    /**
     * @return roll-over balloon color
     */
    public Color getBalloonColor() {
        return balloonColor;
    }

    /**
     * Sets roll-over balloon color. If not set the default value is #000000.
     *
     * @param balloonColor color
     * @return image
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    public Image setBalloonColor(Color balloonColor) {
        this.balloonColor = balloonColor;
        return this;
    }

    /**
     * @return roll-over text
     */
    public String getBalloonText() {
        return balloonText;
    }

    /**
     * Sets roll-over text.
     *
     * @param balloonText text
     * @return image
     */
    @StudioProperty
    public Image setBalloonText(String balloonText) {
        this.balloonText = balloonText;
        return this;
    }

    /**
     * @return color of an image
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets color of an image. Works only if an image is generated using SVG path (svgPath property). If not set the
     * default value is #000000.
     *
     * @param color color
     * @return image
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    public Image setColor(Color color) {
        this.color = color;
        return this;
    }

    /**
     * @return height of an image
     */
    public Integer getHeight() {
        return height;
    }

    /**
     * Sets height of an image. If not set the default value is 20.
     *
     * @param height height
     * @return image
     */
    @StudioProperty(defaultValue = "20")
    public Image setHeight(Integer height) {
        this.height = height;
        return this;
    }

    /**
     * @return horizontal offset
     */
    public Integer getOffsetX() {
        return offsetX;
    }

    /**
     * Sets horizontal offset. If not set the default value is 0.
     *
     * @param offsetX X offset
     * @return image
     */
    @StudioProperty(defaultValue = "0")
    public Image setOffsetX(Integer offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    /**
     * @return vertical offset
     */
    public Integer getOffsetY() {
        return offsetY;
    }

    /**
     * Sets vertical offset. If not set the default value is 0.
     *
     * @param offsetY Y offset
     * @return image
     */
    @StudioProperty(defaultValue = "0")
    public Image setOffsetY(Integer offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    /**
     * @return color of image outline
     */
    public Color getOutlineColor() {
        return outlineColor;
    }

    /**
     * Sets color of image outline. Works only if an image is generated using SVG path (using svgPath property)
     *
     * @param outlineColor color
     * @return image
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    public Image setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
        return this;
    }

    /**
     * @return rotation of an image
     */
    public Integer getRotation() {
        return rotation;
    }

    /**
     * Sets rotation of an image. If not set the default value is 0.
     *
     * @param rotation rotation
     * @return image
     */
    @StudioProperty(defaultValue = "0")
    public Image setRotation(Integer rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * @return svg path of an image
     */
    public String getSvgPath() {
        return svgPath;
    }

    /**
     * Sets svg path of an image. Will not work with IE8.
     *
     * @param svgPath svg path
     * @return image
     */
    @StudioProperty
    public Image setSvgPath(String svgPath) {
        this.svgPath = svgPath;
        return this;
    }

    /**
     * @return the URL of an image
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the URL of an image.
     *
     * @param url the URL
     * @return image
     */
    @StudioProperty
    public Image setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * @return width on an image
     */
    public Integer getWidth() {
        return width;
    }

    /**
     * Sets width on an image. If not set the default value is 20.
     *
     * @param width width
     * @return image
     */
    @StudioProperty(defaultValue = "20")
    public Image setWidth(Integer width) {
        this.width = width;
        return this;
    }
}