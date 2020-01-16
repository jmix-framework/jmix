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

package io.jmix.ui.components.impl;

import io.jmix.core.Messages;
import io.jmix.ui.components.ColorPicker;
import io.jmix.ui.components.data.ConversionException;
import io.jmix.ui.widgets.CubaColorPickerWrapper;
import com.vaadin.shared.ui.colorpicker.Color;
import io.jmix.ui.widgets.CubaColorPickerWrapper;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;

public class WebColorPicker extends WebV8AbstractField<CubaColorPickerWrapper, Color, String>
        implements ColorPicker, InitializingBean {

    /* Beans */
    protected Messages messages;

    public WebColorPicker() {
        component = createComponent();
        attachValueChangeListener(component);
    }

    protected CubaColorPickerWrapper createComponent() {
        return new CubaColorPickerWrapper();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initComponent(component);
    }

    protected void initComponent(CubaColorPickerWrapper component) {
        setHSVVisible(false);
        setSwatchesVisible(false);
        setHistoryVisible(false);

        setCaptions(messages);
    }

    @Inject
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    protected void setCaptions(Messages messages) {
        component.setPopupCaption(messages.getMessage("colorPicker.popupCaption"));
        component.setSwatchesTabCaption(messages.getMessage("colorPicker.swatchesTabCaption"));
        component.setConfirmButtonCaption(messages.getMessage("colorPicker.confirmButtonCaption"));
        component.setCancelButtonCaption(messages.getMessage("colorPicker.cancelButtonCaption"));

        component.setLookupAllCaption(messages.getMessage("colorPicker.lookupAll"));
        component.setLookupRedCaption(messages.getMessage("colorPicker.lookupRed"));
        component.setLookupGreenCaption(messages.getMessage("colorPicker.lookupGreen"));
        component.setLookupBlueCaption(messages.getMessage("colorPicker.lookupBlue"));

        component.setRedSliderCaption(messages.getMessage("colorPicker.redSliderCaption"));
        component.setGreenSliderCaption(messages.getMessage("colorPicker.greenSliderCaption"));
        component.setBlueSliderCaption(messages.getMessage("colorPicker.blueSliderCaption"));
        component.setHueSliderCaption(messages.getMessage("colorPicker.hueSliderCaption"));
        component.setSaturationSliderCaption(messages.getMessage("colorPicker.saturationSliderCaption"));
        component.setValueSliderCaption(messages.getMessage("colorPicker.valueSliderCaption"));
    }

    @Override
    public void setSwatchesVisible(boolean value) {
        component.setSwatchesVisible(value);
    }

    @Override
    public boolean isSwatchesVisible() {
        return component.isSwatchesVisible();
    }

    @Override
    public void setRGBVisible(boolean value) {
        component.setRGBVisible(value);
    }

    @Override
    public boolean isRGBVisible() {
        return component.isRGBVisible();
    }

    @Override
    public void setHSVVisible(boolean value) {
        component.setHSVVisible(value);
    }

    @Override
    public boolean isHSVVisible() {
        return component.isHSVVisible();
    }

    @Override
    public void setDefaultCaptionEnabled(boolean defaultCaptionEnabled) {
        component.setDefaultCaptionEnabled(defaultCaptionEnabled);
    }

    @Override
    public boolean isDefaultCaptionEnabled() {
        return component.isDefaultCaptionEnabled();
    }

    @Override
    public void setButtonCaption(String value) {
        component.setButtonCaption(value);
    }

    @Override
    public String getButtonCaption() {
        return component.getButtonCaption();
    }

    @Override
    protected String convertToModel(Color componentRawValue) throws ConversionException {
        if (componentRawValue == null) {
            return null;
        }

        String redString = Integer.toHexString(componentRawValue.getRed());
        redString = redString.length() < 2 ? "0" + redString : redString;

        String greenString = Integer.toHexString(componentRawValue.getGreen());
        greenString = greenString.length() < 2 ? "0" + greenString : greenString;

        String blueString = Integer.toHexString(componentRawValue.getBlue());
        blueString = blueString.length() < 2 ? "0" + blueString : blueString;

        return redString + greenString + blueString;
    }

    @Override
    protected Color convertToPresentation(String modelValue) throws ConversionException {
        if (modelValue == null) {
            return null;
        }

        if (modelValue.startsWith("#")) {
            modelValue = modelValue.substring(1, modelValue.length());
        }

        try {
            switch (modelValue.length()) {
                case 3:
                    return new Color(Integer.valueOf(modelValue.substring(0, 1), 16),
                            Integer.valueOf(modelValue.substring(1, 2), 16),
                            Integer.valueOf(modelValue.substring(2, 3), 16));
                case 6:
                    return new Color(Integer.valueOf(modelValue.substring(0, 2), 16),
                            Integer.valueOf(modelValue.substring(2, 4), 16),
                            Integer.valueOf(modelValue.substring(4, 6), 16));
                default:
                    throw new ConversionException(String.format("Value '%s' must be 3 or 6 characters in length",
                            modelValue));
            }
        } catch (NumberFormatException e) {
            throw new ConversionException(String.format("Value '%s' is not valid", modelValue));
        }
    }

    @Override
    public void setPopupCaption(String popupCaption) {
        component.setPopupCaption(popupCaption);
    }

    @Override
    public String getPopupCaption() {
        return component.getPopupCaption();
    }

    @Override
    public void setConfirmButtonCaption(String caption) {
        component.setConfirmButtonCaption(caption);
    }

    @Override
    public String getConfirmButtonCaption() {
        return component.getConfirmButtonCaption();
    }

    @Override
    public void setCancelButtonCaption(String caption) {
        component.setCancelButtonCaption(caption);
    }

    @Override
    public String getCancelButtonCaption() {
        return component.getCancelButtonCaption();
    }

    @Override
    public void setSwatchesTabCaption(String caption) {
        component.setSwatchesTabCaption(caption);
    }

    @Override
    public String getSwatchesTabCaption() {
        return component.getSwatchesTabCaption();
    }

    @Override
    public void setLookupAllCaption(String caption) {
        component.setLookupAllCaption(caption);
    }

    @Override
    public String getLookupAllCaption() {
        return component.getLookupAllCaption();
    }

    @Override
    public void setLookupRedCaption(String caption) {
        component.setLookupRedCaption(caption);
    }

    @Override
    public String getLookupRedCaption() {
        return component.getLookupRedCaption();
    }

    @Override
    public void setLookupGreenCaption(String caption) {
        component.setLookupGreenCaption(caption);
    }

    @Override
    public String getLookupGreenCaption() {
        return component.getLookupGreenCaption();
    }

    @Override
    public void setLookupBlueCaption(String caption) {
        component.setLookupBlueCaption(caption);
    }

    @Override
    public String getLookupBlueCaption() {
        return component.getLookupBlueCaption();
    }

    @Override
    public void setRedSliderCaption(String caption) {
        component.setRedSliderCaption(caption);
    }

    @Override
    public String getRedSliderCaption() {
        return component.getRedSliderCaption();
    }

    @Override
    public void setGreenSliderCaption(String caption) {
        component.setGreenSliderCaption(caption);
    }

    @Override
    public String getGreenSliderCaption() {
        return component.getGreenSliderCaption();
    }

    @Override
    public void setBlueSliderCaption(String caption) {
        component.setBlueSliderCaption(caption);
    }

    @Override
    public String getBlueSliderCaption() {
        return component.getBlueSliderCaption();
    }

    @Override
    public void setHueSliderCaption(String caption) {
        component.setHueSliderCaption(caption);
    }

    @Override
    public String getHueSliderCaption() {
        return component.getHueSliderCaption();
    }

    @Override
    public void setSaturationSliderCaption(String caption) {
        component.setSaturationSliderCaption(caption);
    }

    @Override
    public String getSaturationSliderCaption() {
        return component.getSaturationSliderCaption();
    }

    @Override
    public void setValueSliderCaption(String caption) {
        component.setValueSliderCaption(caption);
    }

    @Override
    public String getValueSliderCaption() {
        return component.getValueSliderCaption();
    }

    @Override
    public void setHistoryVisible(boolean historyVisible) {
        component.setHistoryVisible(historyVisible);
    }

    @Override
    public boolean isHistoryVisible() {
        return component.isHistoryVisible();
    }

    @Override
    public void focus() {
        component.focus();
    }

    @Override
    public int getTabIndex() {
        return component.getTabIndex();
    }

    @Override
    public void setTabIndex(int tabIndex) {
        component.setTabIndex(tabIndex);
    }
}
