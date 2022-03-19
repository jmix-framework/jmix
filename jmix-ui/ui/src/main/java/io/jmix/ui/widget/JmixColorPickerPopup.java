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

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.colorpicker.Color;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.colorpicker.ColorPickerPopup;

import javax.annotation.Nullable;

public class JmixColorPickerPopup extends ColorPickerPopup {

    public JmixColorPickerPopup(Color initialColor) {
        super(initialColor);
    }

    public void setJTestIds(String colorPickerPrefix) {
        setJTestId(colorPickerPrefix + "_colorPickerPopup");

        ok.setJTestId(colorPickerPrefix + "_ok");
        cancel.setJTestId(colorPickerPrefix + "_cancel");
        resize.setJTestId(colorPickerPrefix + "_resize");

        redSlider.setJTestId(colorPickerPrefix + "_redSlider");
        greenSlider.setJTestId(colorPickerPrefix + "_greenSlider");
        blueSlider.setJTestId(colorPickerPrefix + "_blueSlider");
        hueSlider.setJTestId(colorPickerPrefix + "_hueSlider");
        saturationSlider.setJTestId(colorPickerPrefix + "_saturationSlider");
        valueSlider.setJTestId(colorPickerPrefix + "_valueSlider");

        rgbPreview.setJTestId(colorPickerPrefix + "_rgbPreview");
        rgbPreview.getField().setJTestId(colorPickerPrefix + "_rgbPreview_textField");
        hsvPreview.setJTestId(colorPickerPrefix + "_hsvPreview");
        hsvPreview.getField().setJTestId(colorPickerPrefix + "_hsvPreview_textField");
        selPreview.setJTestId(colorPickerPrefix + "_selPreview");
        selPreview.getField().setJTestId(colorPickerPrefix + "_selPreview_textField");

        colorSelect.setJTestId(colorPickerPrefix + "_colorSelect");
    }

    @Override
    protected VerticalLayout createHistoryOuterContainer(VerticalLayout innerContainer) {
        innerContainer.setMargin(false);
        VerticalLayout historyOuterContainer = super.createHistoryOuterContainer(innerContainer);
        historyOuterContainer.setMargin(new MarginInfo(false, true, false, true));
        return historyOuterContainer;
    }

    public void setConfirmButtonCaption(@Nullable String caption) {
        ok.setCaption(caption);
    }

    public void setCancelButtonCaption(@Nullable String caption) {
        cancel.setCaption(caption);
    }

    public void setSwatchesTabCaption(@Nullable String caption) {
        ((TabSheet) swatchesTab.getParent()).getTab(swatchesTab).setCaption(caption);
    }

    public void setLookupAllCaption(@Nullable String caption) {
        ((JmixColorPickerSelect) colorSelect).setAllCaption(caption);
    }

    public void setLookupRedCaption(@Nullable String caption) {
        ((JmixColorPickerSelect) colorSelect).setRedCaption(caption);
    }

    public void setLookupGreenCaption(@Nullable String caption) {
        ((JmixColorPickerSelect) colorSelect).setGreenCaption(caption);
    }

    public void setLookupBlueCaption(@Nullable String caption) {
        ((JmixColorPickerSelect) colorSelect).setBlueCaption(caption);
    }

    @Override
    protected Component createSelectTab() {
        VerticalLayout selLayout = new VerticalLayout();
        selLayout.setSpacing(false);
        selLayout.setMargin(new MarginInfo(false, false, true, false));
        selLayout.addComponent(selPreview);
        selLayout.addStyleName("seltab");

        colorSelect = new JmixColorPickerSelect();
        colorSelect.addValueChangeListener(this::colorChanged);

        selLayout.addComponent(colorSelect);
        return selLayout;
    }
}