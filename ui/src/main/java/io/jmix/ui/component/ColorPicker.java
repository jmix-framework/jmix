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

import javax.annotation.Nullable;

public interface ColorPicker extends Field<String>, Component.Focusable {

    String NAME = "colorPicker";

    /**
     * Set caption for the popup window.
     *
     * @param popupCaption caption text.
     */
    void setPopupCaption(@Nullable String popupCaption);
    /**
     * Return caption of the popup window.
     *
     * @return caption text.
     */
    @Nullable
    String getPopupCaption();

    /**
     * Set caption for the confirm button.
     *
     * @param confirmButtonCaption caption text.
     */
    void setConfirmButtonCaption(@Nullable String confirmButtonCaption);
    /**
     * Return caption of the confirm button.
     *
     * @return caption text.
     */
    @Nullable
    String getConfirmButtonCaption();

    /**
     * Set caption for the cancel button.
     *
     * @param cancelButtonCaption caption text.
     */
    void setCancelButtonCaption(@Nullable String cancelButtonCaption);
    /**
     * Return caption of the cancel button.
     *
     * @return caption text.
     */
    @Nullable
    String getCancelButtonCaption();

    /**
     * Set caption for the swatches tab.
     *
     * @param swatchesTabCaption caption text.
     */
    void setSwatchesTabCaption(@Nullable String swatchesTabCaption);
    /**
     * Return caption of the swatches tab.
     *
     * @return caption text.
     */
    @Nullable
    String getSwatchesTabCaption();

    /**
     * Set caption for the all colors in lookup.
     *
     * @param lookupAllCaption caption text.
     */
    void setLookupAllCaption(@Nullable String lookupAllCaption);
    /**
     * Return caption of the all colors in lookup.
     *
     * @return caption text.
     */
    @Nullable
    String getLookupAllCaption();

    /**
     * Set caption for the red colors in lookup.
     *
     * @param lookupRedCaption caption text.
     */
    void setLookupRedCaption(@Nullable String lookupRedCaption);
    /**
     * Return caption of the red colors in lookup.
     *
     * @return caption text.
     */
    @Nullable
    String getLookupRedCaption();

    /**
     * Set caption for the green colors in lookup.
     *
     * @param lookupGreenCaption caption text.
     */
    void setLookupGreenCaption(@Nullable String lookupGreenCaption);
    /**
     * Return caption of the green colors in lookup.
     *
     * @return caption text.
     */
    @Nullable
    String getLookupGreenCaption();

    /**
     * Set caption for the blue colors in lookup.
     *
     * @param lookupBlueCaption caption text.
     */
    void setLookupBlueCaption(@Nullable String lookupBlueCaption);
    /**
     * Return caption of the blue colors in lookup.
     *
     * @return caption text.
     */
    @Nullable
    String getLookupBlueCaption();

    /**
     * Set caption for the slider of red color.
     *
     * @param redSliderCaption caption text.
     */
    void setRedSliderCaption(@Nullable String redSliderCaption);
    /**
     * Return caption of the slider for red color.
     *
     * @return caption text.
     */
    @Nullable
    String getRedSliderCaption();

    /**
     * Set caption for the slider of green color.
     *
     * @param greenSliderCaption caption text.
     */
    void setGreenSliderCaption(@Nullable String greenSliderCaption);
    /**
     * Return caption of the slider for green color.
     *
     * @return caption text.
     */
    @Nullable
    String getGreenSliderCaption();

    /**
     * Set caption for the slider of blue color.
     *
     * @param blueSliderCaption caption text.
     */
    void setBlueSliderCaption(@Nullable String blueSliderCaption);
    /**
     * Return caption of the slider for blue color.
     *
     * @return caption text.
     */
    @Nullable
    String getBlueSliderCaption();

    /**
     * Set caption for the HUE slider.
     *
     * @param hueSliderCaption caption text.
     */
    void setHueSliderCaption(@Nullable String hueSliderCaption);
    /**
     * Return caption of the slider for HUE.
     *
     * @return caption text.
     */
    @Nullable
    String getHueSliderCaption();

    /**
     * Set caption for the saturation slider.
     *
     * @param saturationSliderCaption caption text.
     */
    void setSaturationSliderCaption(@Nullable String saturationSliderCaption);
    /**
     * Return caption of the slider for saturation.
     *
     * @return caption text.
     */
    @Nullable
    String getSaturationSliderCaption();

    /**
     * Set caption for the value slider.
     *
     * @param valueSliderCaption caption text.
     */
    void setValueSliderCaption(@Nullable String valueSliderCaption);
    /**
     * Return caption of the slider for value.
     *
     * @return caption text.
     */
    @Nullable
    String getValueSliderCaption();

    /**
     *  Set visibility for history pane
     *
     *  @param historyVisible pane visibility.
     */
    void setHistoryVisible(boolean historyVisible);
    /**
     * @return true if history pane is visible.
     */
    boolean isHistoryVisible();

    /**
     *  Set visibility for swatches tab
     *
     *  @param swatchesVisible tab visibility.
     */
    void setSwatchesVisible(boolean swatchesVisible);
    /**
     * @return true if swatches tab is visible.
     */
    boolean isSwatchesVisible();

    /**
     *  Set visibility for RGB tab
     *
     *  @param rgbVisible tab visibility.
     */
    void setRGBVisible(boolean rgbVisible);
    /**
     * @return true if RGB tab is visible.
     */
    boolean isRGBVisible();

    /**
     *  Set visibility for HSV tab
     *
     *  @param hsvVisible tab visibility.
     */
    void setHSVVisible(boolean hsvVisible);
    /**
     * @return true if HSV tab is visible.
     */
    boolean isHSVVisible();

    /**
     * Set HEX value as button caption.
     *
     * @param defaultCaptionEnabled true if HEX color is shown as button caption.
     */
    void setDefaultCaptionEnabled(boolean defaultCaptionEnabled);
    /**
     * @return true if caption is shown as HTML.
     */
    boolean isDefaultCaptionEnabled();

    /**
     * Set caption for color picker button.
     *
     * @param buttonCaption caption text.
     */
    void setButtonCaption(String buttonCaption);
    /**
     * Return caption of color picker button.
     *
     * @return caption text.
     */
    String getButtonCaption();
}
