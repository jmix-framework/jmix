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

package io.jmix.ui.widget;

import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;

import javax.annotation.Nullable;
import java.util.Objects;

public class JmixColorPickerWrapper extends CustomField<Color> {

    @SuppressWarnings("UnusedAssignment")
    protected JmixColorPicker field = null;

    // The internalValue is used to store the 'null' value,
    // because the ColorPicker doesn't accept null values
    protected Color internalValue;

    public JmixColorPickerWrapper() {
        field = createColorPicker();
        initColorPicker(field);
        // We need to sync 'internalValue' with the default field value, otherwise,
        // the first time we set 'null', it doesn't change the color to Black
        setInternalValue(field.getValue());
        setFocusDelegate(field);
        setPrimaryStyleName("jmix-color-picker");
        setWidthUndefined();
    }

    private JmixColorPicker createColorPicker() {
        return new JmixColorPicker();
    }

    protected void initColorPicker(JmixColorPicker field) {
        field.addValueChangeListener((ValueChangeListener<Color>) event -> {
            setInternalValue(event.getValue());
            fireEvent(createValueChange(event.getOldValue(), event.isUserOriginated()));
        });
        field.setCaption(null);
        field.setModal(true);
    }

    @Override
    protected Component initContent() {
        return field;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        if (field != null) {
            field.setReadOnly(readOnly);
        }
    }

    @Override
    protected void doSetValue(Color value) {
        if (!Objects.equals(field.getValue(), value)) {
            field.setValue(value);
        }
        setInternalValue(value);
    }

    @Override
    public Color getValue() {
        return getInternalValue();
    }

    public Color getInternalValue() {
        return internalValue;
    }

    public void setInternalValue(Color internalValue) {
        if (!Objects.equals(this.internalValue, internalValue)) {
            this.internalValue = internalValue;
            markAsDirty();
        }
    }

    @Override
    public void setWidth(float width, Unit unit) {
        super.setWidth(width, unit);

        if (field != null) {
            if (width < 0) {
                field.setWidthUndefined();
            } else {
                field.setWidth("100%");
            }
        }
    }

    @Override
    public void setHeight(float height, Unit unit) {
        super.setHeight(height, unit);

        if (field != null) {
            if (height < 0) {
                field.setHeightUndefined();
            } else {
                field.setHeight("100%");
            }
        }
    }

    public void setDefaultCaptionEnabled(boolean value) {
        field.setDefaultCaptionEnabled(value);

        if (value) {
            removeStyleName("color-maxwidth");
        } else {
            addStyleName("color-maxwidth");
        }
    }

    public boolean isDefaultCaptionEnabled() {
        return field.isDefaultCaptionEnabled();
    }

    public void setButtonCaption(String value) {
        field.setCaption(value);
    }

    public String getButtonCaption() {
        return field.getCaption();
    }

    public void setHistoryVisible(boolean value) {
        field.setHistoryVisibility(value);
    }

    public boolean isHistoryVisible() {
        return field.getHistoryVisibility();
    }

    public void setSwatchesVisible(boolean value) {
        field.setSwatchesVisibility(value);
    }

    public boolean isSwatchesVisible() {
        return field.getSwatchesVisibility();
    }

    public void setRGBVisible(boolean value) {
        field.setRGBVisibility(value);
    }

    public boolean isRGBVisible() {
        return field.getRGBVisibility();
    }

    public void setHSVVisible(boolean value) {
        field.setHSVVisibility(value);
    }

    public boolean isHSVVisible() {
        return field.getHSVVisibility();
    }

    public void setPopupCaption(@Nullable String popupCaption) {
        field.setWindowCaption(popupCaption);
    }

    @Nullable
    public String getPopupCaption() {
        return field.getWindowCaption();
    }

    public void setConfirmButtonCaption(@Nullable String caption) {
        field.setConfirmButtonCaption(caption);
    }

    @Nullable
    public String getConfirmButtonCaption() {
        return field.getConfirmButtonCaption();
    }

    public void setCancelButtonCaption(@Nullable String caption) {
        field.setCancelButtonCaption(caption);
    }

    @Nullable
    public String getCancelButtonCaption() {
        return field.getCancelButtonCaption();
    }

    public void setSwatchesTabCaption(@Nullable String caption) {
        field.setSwatchesTabCaption(caption);
    }

    @Nullable
    public String getSwatchesTabCaption() {
        return field.getSwatchesTabCaption();
    }

    public void setLookupAllCaption(@Nullable String lookupAllCaption) {
        field.setLookupAllCaption(lookupAllCaption);
    }

    @Nullable
    public String getLookupAllCaption() {
        return field.getLookupAllCaption();
    }

    public void setLookupRedCaption(@Nullable String lookupRedCaption) {
        field.setLookupRedCaption(lookupRedCaption);
    }

    @Nullable
    public String getLookupRedCaption() {
        return field.getLookupRedCaption();
    }

    public void setLookupGreenCaption(@Nullable String lookupGreenCaption) {
        field.setLookupGreenCaption(lookupGreenCaption);
    }

    @Nullable
    public String getLookupGreenCaption() {
        return field.getLookupGreenCaption();
    }

    public void setLookupBlueCaption(@Nullable String lookupBlueCaption) {
        field.setLookupBlueCaption(lookupBlueCaption);
    }

    @Nullable
    public String getLookupBlueCaption() {
        return field.getLookupBlueCaption();
    }

    public void setRedSliderCaption(@Nullable String redSliderCaption) {
        field.setRedSliderCaption(redSliderCaption);
    }

    @Nullable
    public String getRedSliderCaption() {
        return field.getRedSliderCaption();
    }

    public void setGreenSliderCaption(@Nullable String greenSliderCaption) {
        field.setGreenSliderCaption(greenSliderCaption);
    }

    @Nullable
    public String getGreenSliderCaption() {
        return field.getGreenSliderCaption();
    }

    public void setBlueSliderCaption(@Nullable String blueSliderCaption) {
        field.setBlueSliderCaption(blueSliderCaption);
    }

    @Nullable
    public String getBlueSliderCaption() {
        return field.getBlueSliderCaption();
    }

    public void setHueSliderCaption(@Nullable String hueSliderCaption) {
        field.setHueSliderCaption(hueSliderCaption);
    }

    @Nullable
    public String getHueSliderCaption() {
        return field.getHueSliderCaption();
    }

    public void setSaturationSliderCaption(@Nullable String saturationSliderCaption) {
        field.setSaturationSliderCaption(saturationSliderCaption);
    }

    @Nullable
    public String getSaturationSliderCaption() {
        return field.getSaturationSliderCaption();
    }

    public void setValueSliderCaption(@Nullable String valueSliderCaption) {
        field.setValueSliderCaption(valueSliderCaption);
    }

    @Nullable
    public String getValueSliderCaption() {
        return field.getValueSliderCaption();
    }
}