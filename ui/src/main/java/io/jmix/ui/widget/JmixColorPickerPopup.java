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

public class JmixColorPickerPopup extends ColorPickerPopup {

    public JmixColorPickerPopup(Color initialColor) {
        super(initialColor);
    }

    @Override
    protected VerticalLayout createHistoryOuterContainer(VerticalLayout innerContainer) {
        innerContainer.setMargin(false);
        VerticalLayout historyOuterContainer = super.createHistoryOuterContainer(innerContainer);
        historyOuterContainer.setMargin(new MarginInfo(false, true, false, true));
        return historyOuterContainer;
    }

    public void setConfirmButtonCaption(String caption) {
        ok.setCaption(caption);
    }

    public void setCancelButtonCaption(String caption) {
        cancel.setCaption(caption);
    }

    public void setSwatchesTabCaption(String caption) {
        ((TabSheet) swatchesTab.getParent()).getTab(swatchesTab).setCaption(caption);
    }

    public void setLookupAllCaption(String caption) {
        ((JmixColorPickerSelect) colorSelect).setAllCaption(caption);
    }

    public void setLookupRedCaption(String caption) {
        ((JmixColorPickerSelect) colorSelect).setRedCaption(caption);
    }

    public void setLookupGreenCaption(String caption) {
        ((JmixColorPickerSelect) colorSelect).setGreenCaption(caption);
    }

    public void setLookupBlueCaption(String caption) {
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