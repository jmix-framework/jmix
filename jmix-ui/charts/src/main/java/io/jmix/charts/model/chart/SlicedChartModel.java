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

package io.jmix.charts.model.chart;

import io.jmix.charts.model.*;
import io.jmix.charts.model.animation.AnimationEffect;
import io.jmix.charts.model.animation.HasStartEffect;
import io.jmix.charts.model.chart.impl.AbstractChart;
import io.jmix.charts.model.chart.impl.SlicedChartModelImpl;
import io.jmix.charts.model.label.Label;
import io.jmix.ui.meta.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@StudioProperties(properties = {
        @StudioProperty(name = "marginBottom", type = PropertyType.INTEGER, defaultValue = "0"),
        @StudioProperty(name = "marginLeft", type = PropertyType.INTEGER, defaultValue = "0"),
        @StudioProperty(name = "marginRight", type = PropertyType.INTEGER, defaultValue = "0"),
        @StudioProperty(name = "marginTop", type = PropertyType.INTEGER, defaultValue = "0"),
        @StudioProperty(name = "startEffect", type = PropertyType.ENUMERATION,
                options = {"@link io.jmix.charts.model.animation.AnimationEffect"}, defaultValue = "BOUNCE"),
        @StudioProperty(name = "startDuration", type = PropertyType.DOUBLE, defaultValue = "1")
},
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "alphaField"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "colorField"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "descriptionField"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "patternField"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "pulledField"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "titleField"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "urlField"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "valueField"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "visibleInLegendField"}),
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "classNameField"})
        })
public interface SlicedChartModel<T extends SlicedChartModel>
        extends ChartModel<T>, HasMargins<T>, HasStartEffect<T>, HasColors<T> {
    /**
     * @return opacity of all slices
     */
    Double getAlpha();

    /**
     * Sets opacity of all slices. If not set the default value is 1.
     *
     * @param alpha opacity of all slices
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    T setAlpha(Double alpha);

    /**
     * @return alpha field name
     */
    String getAlphaField();

    /**
     * Sets alpha field name from data provider which holds slice's alpha.
     *
     * @param alphaField alpha field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setAlphaField(String alphaField);

    /**
     * @return color of the first slice
     */
    Color getBaseColor();

    /**
     * Sets color of the first slice. All the other will be colored with darker or brighter colors. Use
     * brightnessStep to set intensity of color change for each subsequent slice. If not set the default value is 20.
     *
     * @param baseColor color of the first slice
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    T setBaseColor(Color baseColor);

    /**
     * @return color field
     */
    String getColorField();

    /**
     * Sets name of the field from data provider which holds slice's color.
     *
     * @param colorField color field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setColorField(String colorField);

    /**
     * @return description field
     */
    String getDescriptionField();

    /**
     * Sets name of the field from data provider which holds a string with description.
     *
     * @param descriptionField description field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setDescriptionField(String descriptionField);

    /**
     * @return list of gradient ratio
     */
    List<Double> getGradientRatio();

    /**
     * Sets the list of gradient ratio. Will make slices to be filled with color gradients. Negative value means the
     * color will be darker than the original, and positive number means the color will be lighter.
     *
     * @param gradientRatio list of gradient ratio
     * @return sliced chart model
     */
    @StudioCollection(xmlElement = "gradientRatios",
            itemXmlElement = "ratio",
            itemCaption = "Gradient Ratio",
            itemProperties = {
                    @StudioProperty(name = "value", type = PropertyType.DOUBLE)
            },
            documentationURL = "https://docs.jmix.io/jmix/%VERSION%/backoffice-ui/charts.html")
    T setGradientRatio(List<Double> gradientRatio);

    /**
     * Adds gradient ratios.
     *
     * @param ratios the ratios
     * @return sliced chart model
     */
    T addGradientRatio(Double... ratios);

    /**
     * @return opacity of the group slice
     */
    Double getGroupedAlpha();

    /**
     * Sets opacity of the group slice. Value range is 0 - 1. If not set the default value is 1.
     *
     * @param groupedAlpha opacity of the group slice
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    T setGroupedAlpha(Double groupedAlpha);

    /**
     * @return grouped color
     */
    Color getGroupedColor();

    /**
     * Sets the color of the group slice. The default value is not set - this means the next available color from
     * "colors" list will be used.
     *
     * @param groupedColor grouped color
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.OPTIONS)
    T setGroupedColor(Color groupedColor);

    /**
     * @return grouped description
     */
    String getGroupedDescription();

    /**
     * Sets description of the group slice.
     *
     * @param groupedDescription grouped description string
     * @return sliced chart model
     */
    @StudioProperty
    T setGroupedDescription(String groupedDescription);

    /**
     * @return true if grouped pulled is enabled
     */
    Boolean getGroupedPulled();

    /**
     * If groupedPulled set to true, the group slice will be pulled out when the chart loads. If not set the default
     * value is false.
     *
     * @param groupedPulled grouped pulled option
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "false")
    T setGroupedPulled(Boolean groupedPulled);

    /**
     * @return grouped title
     */
    String getGroupedTitle();

    /**
     * Sets title of the group slice. If not set the default value is "Other".
     *
     * @param groupedTitle grouped title string
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "Other")
    T setGroupedTitle(String groupedTitle);

    Double getGroupPercent();

    /**
     * Sets group percent. If there is more than one slice whose percentage of the pie is less than this number,
     * those slices will be grouped together into one slice. This is the "other" slice. It will always be the last
     * slice in a pie. If not set the default value is 0.
     *
     * @param groupPercent the group percent
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "0")
    T setGroupPercent(Double groupPercent);

    /**
     * @return hide labels percent
     */
    Double getHideLabelsPercent();

    /**
     * Sets hide labels percent. Slices with percent less then hideLabelsPercent won't display labels. This is useful
     * to avoid cluttering up the chart, if you have a lot of small slices. 0 means all labels will be shown. If not
     * set the default value is 0.
     *
     * @param hideLabelsPercent the hide labels percent
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "0")
    T setHideLabelsPercent(Double hideLabelsPercent);

    /**
     * @return opacity of a hovered slice
     */
    Double getHoverAlpha();

    /**
     * Sets opacity of a hovered slice. Value range is 0 - 1. If not set the default value is 1.
     *
     * @param hoverAlpha opacity of a hovered slice
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "1")
    @Max(1)
    @Min(0)
    T setHoverAlpha(Double hoverAlpha);

    /**
     * @return true if data labels are visible
     */
    Boolean getLabelsEnabled();

    /**
     * Set labelsEnabled to true if data labels should be visible. If not set the default value is true.
     *
     * @param labelsEnabled labels enabled option
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "true")
    T setLabelsEnabled(Boolean labelsEnabled);

    /**
     * @return {@link Label label} tick opacity
     */
    Double getLabelTickAlpha();

    /**
     * Sets {@link Label label} tick opacity. Value range is 0 - 1. If not set the default value is 0.2.
     *
     * @param labelTickAlpha label tick opacity
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "0.2")
    @Max(1)
    @Min(0)
    T setLabelTickAlpha(Double labelTickAlpha);

    /**
     * @return {@link Label label} tick color
     */
    Color getLabelTickColor();

    /**
     * Sets	{@link Label label} tick color. If not set the default value is #000000.
     *
     * @param labelTickColor label tick color
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    T setLabelTickColor(Color labelTickColor);

    /**
     * @return outline opacity
     */
    Double getOutlineAlpha();

    /**
     * Sets outline opacity. Value range is 0 - 1. If not set the default value is 0.
     *
     * @param outlineAlpha outline opacity
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "0")
    @Max(1)
    @Min(0)
    T setOutlineAlpha(Double outlineAlpha);

    /**
     * @return outline color
     */
    Color getOutlineColor();

    /**
     * Sets outline color. If not set the default value is #FFFFFF.
     *
     * @param outlineColor the outline color
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#FFFFFF")
    T setOutlineColor(Color outlineColor);

    /**
     * @return outline thickness
     */
    Integer getOutlineThickness();

    /**
     * Sets pie outline thickness. If not set the default value is 1.
     *
     * @param outlineThickness the outline thickness
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "1")
    T setOutlineThickness(Integer outlineThickness);

    /**
     * @return pattern field name
     */
    String getPatternField();

    /**
     * Sets field name from your data provider which holds pattern information.
     *
     * @param patternField pattern field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setPatternField(String patternField);

    /**
     * @return pulled field name
     */
    String getPulledField();

    /**
     * Sets name of the field in chart's data provider which holds a boolean value telling the chart whether this
     * slice must be pulled or not.
     *
     * @param pulledField pulled field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF, options = "boolean")
    T setPulledField(String pulledField);

    /**
     * @return pull out duration in seconds
     */
    Integer getPullOutDuration();

    /**
     * Sets pull out duration in seconds. If not set the default value is 1.
     *
     * @param pullOutDuration pull out duration in seconds
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "1")
    T setPullOutDuration(Integer pullOutDuration);

    /**
     * @return pull out effect
     */
    AnimationEffect getPullOutEffect();

    /**
     * Sets pull out effect. Possible values are: easeOutSine, easeInSine, elastic, bounce. If not set the default
     * value is BOUNCE.
     *
     * @param pullOutEffect the pull out effect
     * @return sliced chart model
     */
    T setPullOutEffect(AnimationEffect pullOutEffect);

    /**
     * @return true if only one slice can be pulled out at a time
     */
    Boolean getPullOutOnlyOne();

    /**
     * If pullOutOnlyOne set to true, only one slice can be pulled out at a time. If the viewer clicks on a slice,
     * any other pulled-out slice will be pulled in. If not set the default value is false.
     *
     * @param pullOutOnlyOne pull out only one option
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "false")
    T setPullOutOnlyOne(Boolean pullOutOnlyOne);

    /**
     * @return true if the animation should be sequenced
     */
    Boolean getSequencedAnimation();

    /**
     * Specifies whether the animation should be sequenced or all slices should appear at once. If not set the
     * default value is true.
     *
     * @param sequencedAnimation sequenced animation option
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "true")
    T setSequencedAnimation(Boolean sequencedAnimation);

    /**
     * @return initial opacity of all slices
     */
    Double getStartAlpha();

    /**
     * Sets initial opacity of all slices. Slices will fade in from startAlpha. If not set the default value is 0.
     *
     * @param startAlpha initial opacity of all slices
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "0")
    T setStartAlpha(Double startAlpha);

    /**
     * @return title field name
     */
    String getTitleField();

    /**
     * Sets name of the field from data provider which holds slice's title.
     *
     * @param titleField title field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setTitleField(String titleField);

    /**
     * @return the URL field name
     */
    String getUrlField();

    /**
     * Sets name of the field from data provider which holds URL which would be accessed if the user clicks on a slice.
     *
     * @param urlField the URL field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setUrlField(String urlField);

    /**
     * @return the URL target
     */
    String getUrlTarget();

    /**
     * If URL is specified for a slice, it will be opened when user clicks on it. urlTarget specifies target of this
     * URL. Use "_blank" if you want URL to be opened in a new window. If not set the default value is "_self".
     *
     * @param urlTarget the URL target string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.OPTIONS, options = {"_blank", "_parent", "_self", "_top"},
            defaultValue = "_self")
    T setUrlTarget(String urlTarget);

    /**
     * @return value field name
     */
    String getValueField();

    /**
     * Sets name of the field from data provider which holds slice's value.
     *
     * @param valueField value field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setValueField(String valueField);

    /**
     * @return visible in legend field
     */
    String getVisibleInLegendField();

    /**
     * Use this field to selectively specify which slice is shown in legend. It should be set to a boolean field in
     * data (that holds either true or false). For example if you set visibleInLegendField to "showInLegend", all
     * slices that have "showInLegend: false" in their data will not be shown in the legend.
     *
     * @param visibleInLegendField visible in legend field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF, options = "boolean")
    T setVisibleInLegendField(String visibleInLegendField);

    /**
     * @return label function
     */
    JsFunction getLabelFunction();

    /**
     * Sets label function that can format data labels in any way you want. Chart will call this method and will pass
     * Slice object and formatted text as attributes. This function should return string which will be displayed as
     * label.
     *
     * @param labelFunction label function
     * @return sliced chart model
     */
    T setLabelFunction(JsFunction labelFunction);

    /**
     * @return maximum label width
     */
    Integer getMaxLabelWidth();

    /**
     * Sets maximum label width. If width of the label is bigger than maxLabelWidth, it will be wrapped. If not set
     * the default value is 200.
     *
     * @param maxLabelWidth maximum label width
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "200")
    T setMaxLabelWidth(Integer maxLabelWidth);

    /**
     * @return class name field
     */
    String getClassNameField();

    /**
     * Sets field name from your data provider which holds CSS class name. If this field is set and
     * {@link AbstractChart#addClassNames} is enabled, the slice element will have this class name set.
     *
     * @param classNameField class name field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.PROPERTY_PATH_REF)
    T setClassNameField(String classNameField);

    /**
     * @return true if the chart displays outlines (if visible) and labels for slices even if their value is 0.
     */
    Boolean getShowZeroSlices();

    /**
     * Set showZeroSlices to true if you want the chart should display outlines (if visible) and labels for slices
     * even if their value is 0. If not set the default value is false.
     *
     * @param showZeroSlices show zero slices option
     * @return sliced chart model
     */
    @StudioProperty(defaultValue = "false")
    T setShowZeroSlices(Boolean showZeroSlices);

    /**
     * @return accessible label text
     */
    String getAccessibleLabel();

    /**
     * Sets accessible label text. It will be read by screen readers if user rolls-over the slice or sets focus using
     * tab key (this is possible only if {@link SlicedChartModelImpl#tabIndex} is set to some number). Text is added as
     * aria-label tag. Note, not all screen readers and browsers support this. If not set the default value is
     * "[[title]]: [[percents]]% [[value]] [[description]]".
     *
     * @param accessibleLabel accessible label text
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING,
            defaultValue = "[[title]]: [[percents]]% [[value]] [[description]]")
    T setAccessibleLabel(String accessibleLabel);

    /**
     * @return label color field
     */
    Color getLabelColorField();

    /**
     * Sets a field from your data provider which holds color value for the tick. Use it to set color of the label
     * for each slice individually. If not set the default value is #000000.
     *
     * @param labelColorField label color field string
     * @return sliced chart model
     */
    @StudioProperty(type = PropertyType.OPTIONS, defaultValue = "#000000")
    T setLabelColorField(Color labelColorField);

    /**
     * @return tab index
     */
    Integer getTabIndex();

    /**
     * In case you set it to some number, the chart will set focus on a slice (starting from first) when user clicks
     * tab key. When a focus is set, screen readers like NVDA Screen reader will read label which is set using
     * {@link SlicedChartModelImpl#accessibleLabel}. Note, not all browsers and readers support this.
     *
     * @param tabIndex the tab index
     * @return sliced chart model
     */
    @StudioProperty
    T setTabIndex(Integer tabIndex);
}