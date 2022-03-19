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

package io.jmix.charts.model.stock;

import io.jmix.charts.model.AbstractChartObject;
import io.jmix.charts.model.Color;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioElement;
import io.jmix.ui.meta.StudioProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Defines set of properties for all StockEvents.
 * <br>
 * See documentation for properties of StockEventsSettings JS object. <br>
 *
 * <a href="http://docs.amcharts.com/3/javascriptstockchart/StockEventsSettings">http://docs.amcharts.com/3/javascriptstockchart/StockEventsSettings</a>
 */
@StudioElement(
        caption = "StockEventsSettings",
        xmlElement = "stockEventsSettings",
        xmlns = "http://jmix.io/schema/ui/charts",
        xmlnsAlias = "chart")
public class StockEventsSettings extends AbstractChartObject {

    private static final long serialVersionUID = 6413770909562353029L;

    private Double backgroundAlpha;

    private Color backgroundColor;

    private Color balloonColor;

    private Double borderAlpha;

    private Color borderColor;

    private Color rollOverColor;

    private String showAt;

    private StockEventType type;

    /**
     * @return opacity of bullet background
     */
    public Double getBackgroundAlpha() {
        return backgroundAlpha;
    }

    /**
     * Sets opacity of bullet background. If not set the default value is 1.
     *
     * @param backgroundAlpha opacity
     * @return stock event settings
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public StockEventsSettings setBackgroundAlpha(Double backgroundAlpha) {
        this.backgroundAlpha = backgroundAlpha;
        return this;
    }

    /**
     * @return color of bullet background
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets color of bullet background. If not set the default value is #DADADA.
     *
     * @param backgroundColor color
     * @return stock event settings
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#DADADA")
    public StockEventsSettings setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    /**
     * @return color for a roll-over balloon
     */
    public Color getBalloonColor() {
        return balloonColor;
    }

    /**
     * Sets color for a roll-over balloon. If not set the default value is #CC0000.
     *
     * @param balloonColor balloon color
     * @return stock event settings
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#CC0000")
    public StockEventsSettings setBalloonColor(Color balloonColor) {
        this.balloonColor = balloonColor;
        return this;
    }

    /**
     * @return opacity of bullet border
     */
    public Double getBorderAlpha() {
        return borderAlpha;
    }

    /**
     * Sets opacity of bullet border. If not set the default value is 1.
     *
     * @param borderAlpha opacity
     * @return stock event settings
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    public StockEventsSettings setBorderAlpha(Double borderAlpha) {
        this.borderAlpha = borderAlpha;
        return this;
    }

    /**
     * @return bullet border color
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets bullet border color. If not set the default value is #888888.
     *
     * @param borderColor color
     * @return stock event settings
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#888888")
    public StockEventsSettings setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    /**
     * @return roll-over background color
     */
    public Color getRollOverColor() {
        return rollOverColor;
    }

    /**
     * Sets roll-over background color. If not set the default value is #CC0000.
     *
     * @param rollOverColor color
     * @return stock event settings
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#CC0000")
    public StockEventsSettings setRollOverColor(Color rollOverColor) {
        this.rollOverColor = rollOverColor;
        return this;
    }

    /**
     * @return showAt string
     */
    public String getShowAt() {
        return showAt;
    }

    /**
     * Allows placing event bullets at "open", "close", "low", "high" values. If not set the default value is "close".
     *
     * @param showAt showAt string
     * @return stock event settings
     */
    @StudioProperty(type = PropertyType.OPTIONS, options = {"open", "close", "low", "high"}, defaultValue = "close")
    public StockEventsSettings setShowAt(String showAt) {
        this.showAt = showAt;
        return this;
    }

    /**
     * @return type of bullet
     */
    public StockEventType getType() {
        return type;
    }

    /**
     * Sets type of bullet. If not set the default value is SIGN.
     *
     * @param type type
     * @return stock event settings
     */
    @StudioProperty(type = PropertyType.ENUMERATION, defaultValue = "SIGN")
    public StockEventsSettings setType(StockEventType type) {
        this.type = type;
        return this;
    }
}
