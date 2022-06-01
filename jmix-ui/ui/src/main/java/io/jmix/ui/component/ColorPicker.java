/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.ui.meta.CanvasBehaviour;
import io.jmix.ui.meta.PropertiesConstraint;
import io.jmix.ui.meta.PropertiesGroup;
import io.jmix.ui.meta.PropertyType;
import io.jmix.ui.meta.StudioComponent;
import io.jmix.ui.meta.StudioProperties;
import io.jmix.ui.meta.StudioProperty;

import javax.annotation.Nullable;

@StudioComponent(
        caption = "ColorPicker",
        category = "Components",
        xmlElement = "colorPicker",
        icon = "io/jmix/ui/icon/component/colorPicker.svg",
        canvasBehaviour = CanvasBehaviour.COLOR_PICKER,
        unsupportedProperties = {"buffered"},
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/color-picker.html"
)
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, options = "string"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING,
                        properties = {"dataContainer", "property"})
        }
)
public interface ColorPicker extends Field<String>, Component.Focusable, Buffered {

    String NAME = "colorPicker";

    /**
     * Sets caption for the popup window.
     *
     * @param popupCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.popupCaption")
    void setPopupCaption(@Nullable String popupCaption);

    /**
     * Returns caption of the popup window.
     *
     * @return caption text.
     */
    @Nullable
    String getPopupCaption();

    /**
     * Sets caption for the confirm button.
     *
     * @param confirmButtonCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.confirmButtonCaption")
    void setConfirmButtonCaption(@Nullable String confirmButtonCaption);

    /**
     * Returns caption of the confirm button.
     *
     * @return caption text.
     */
    @Nullable
    String getConfirmButtonCaption();

    /**
     * Sets caption for the cancel button.
     *
     * @param cancelButtonCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.cancelButtonCaption")
    void setCancelButtonCaption(@Nullable String cancelButtonCaption);

    /**
     * Returns caption of the cancel button.
     *
     * @return caption text.
     */
    @Nullable
    String getCancelButtonCaption();

    /**
     * Sets caption for the swatches tab.
     *
     * @param swatchesTabCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.swatchesTabCaption")
    void setSwatchesTabCaption(@Nullable String swatchesTabCaption);

    /**
     * Returns caption of the swatches tab.
     *
     * @return caption text.
     */
    @Nullable
    String getSwatchesTabCaption();

    /**
     * Sets caption for the all colors in lookup.
     *
     * @param lookupAllCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.lookupAll")
    void setLookupAllCaption(@Nullable String lookupAllCaption);

    /**
     * Returns caption of the all colors in lookup.
     *
     * @return caption text.
     */
    @Nullable
    String getLookupAllCaption();

    /**
     * Sets caption for the red colors in lookup.
     *
     * @param lookupRedCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.lookupRed")
    void setLookupRedCaption(@Nullable String lookupRedCaption);

    /**
     * Returns caption of the red colors in lookup.
     *
     * @return caption text.
     */
    @Nullable
    String getLookupRedCaption();

    /**
     * Sets caption for the green colors in lookup.
     *
     * @param lookupGreenCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.lookupGreen")
    void setLookupGreenCaption(@Nullable String lookupGreenCaption);

    /**
     * Returns caption of the green colors in lookup.
     *
     * @return caption text.
     */
    @Nullable
    String getLookupGreenCaption();

    /**
     * Sets caption for the blue colors in lookup.
     *
     * @param lookupBlueCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.lookupBlue")
    void setLookupBlueCaption(@Nullable String lookupBlueCaption);

    /**
     * Returns caption of the blue colors in lookup.
     *
     * @return caption text.
     */
    @Nullable
    String getLookupBlueCaption();

    /**
     * Sets caption for the slider of red color.
     *
     * @param redSliderCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.redSliderCaption")
    void setRedSliderCaption(@Nullable String redSliderCaption);

    /**
     * Returns caption of the slider for red color.
     *
     * @return caption text.
     */
    @Nullable
    String getRedSliderCaption();

    /**
     * Sets caption for the slider of green color.
     *
     * @param greenSliderCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.greenSliderCaption")
    void setGreenSliderCaption(@Nullable String greenSliderCaption);

    /**
     * Returns caption of the slider for green color.
     *
     * @return caption text.
     */
    @Nullable
    String getGreenSliderCaption();

    /**
     * Sets caption for the slider of blue color.
     *
     * @param blueSliderCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.blueSliderCaption")
    void setBlueSliderCaption(@Nullable String blueSliderCaption);

    /**
     * Returns caption of the slider for blue color.
     *
     * @return caption text.
     */
    @Nullable
    String getBlueSliderCaption();

    /**
     * Sets caption for the HUE slider.
     *
     * @param hueSliderCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.hueSliderCaption")
    void setHueSliderCaption(@Nullable String hueSliderCaption);

    /**
     * Returns caption of the slider for HUE.
     *
     * @return caption text.
     */
    @Nullable
    String getHueSliderCaption();

    /**
     * Sets caption for the saturation slider.
     *
     * @param saturationSliderCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///colorPicker.saturationSliderCaption")
    void setSaturationSliderCaption(@Nullable String saturationSliderCaption);

    /**
     * Returns caption of the slider for saturation.
     *
     * @return caption text.
     */
    @Nullable
    String getSaturationSliderCaption();

    /**
     * Sets caption for the value slider.
     *
     * @param valueSliderCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING, defaultValue = "msg:///valueSliderCaption")
    void setValueSliderCaption(@Nullable String valueSliderCaption);

    /**
     * Returns caption of the slider for value.
     *
     * @return caption text.
     */
    @Nullable
    String getValueSliderCaption();

    /**
     * Sets visibility for history pane
     *
     * @param historyVisible pane visibility.
     */
    @StudioProperty(defaultValue = "false")
    void setHistoryVisible(boolean historyVisible);

    /**
     * @return true if history pane is visible.
     */
    boolean isHistoryVisible();

    /**
     * Sets visibility for swatches tab
     *
     * @param swatchesVisible tab visibility.
     */
    @StudioProperty(defaultValue = "false")
    void setSwatchesVisible(boolean swatchesVisible);

    /**
     * @return true if swatches tab is visible.
     */
    boolean isSwatchesVisible();

    /**
     * Sets visibility for RGB tab
     *
     * @param rgbVisible tab visibility.
     */
    @StudioProperty(name = "rgbVisible", defaultValue = "true")
    void setRGBVisible(boolean rgbVisible);

    /**
     * @return true if RGB tab is visible.
     */
    boolean isRGBVisible();

    /**
     * Sets visibility for HSV tab
     *
     * @param hsvVisible tab visibility.
     */
    @StudioProperty(name = "hsvVisible", defaultValue = "false")
    void setHSVVisible(boolean hsvVisible);

    /**
     * @return true if HSV tab is visible.
     */
    boolean isHSVVisible();

    /**
     * Sets HEX value as button caption.
     *
     * @param defaultCaptionEnabled true if HEX color is shown as button caption.
     */
    @StudioProperty(defaultValue = "false")
    void setDefaultCaptionEnabled(boolean defaultCaptionEnabled);

    /**
     * @return true if caption is shown as HTML.
     */
    boolean isDefaultCaptionEnabled();

    /**
     * Sets caption for color picker button.
     *
     * @param buttonCaption caption text.
     */
    @StudioProperty(type = PropertyType.LOCALIZED_STRING)
    void setButtonCaption(String buttonCaption);

    /**
     * Returns caption of color picker button.
     *
     * @return caption text.
     */
    String getButtonCaption();
}
