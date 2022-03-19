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

import com.vaadin.ui.Component;
import com.vaadin.ui.components.colorpicker.ColorPickerSelect;

import javax.annotation.Nullable;

public class JmixColorPickerSelect extends ColorPickerSelect {

    protected String allCaption;
    protected String redCaption;
    protected String greenCaption;
    protected String blueCaption;

    @Override
    protected Component initContent() {
        Component component = super.initContent();

        range.setTextInputAllowed(false);
        range.setItemCaptionGenerator(item -> {
            switch (item) {
                case ALL:
                    return allCaption;
                case RED:
                    return redCaption;
                case GREEN:
                    return greenCaption;
                case BLUE:
                    return blueCaption;
            }
            return null;
        });

        return component;
    }

    public void setAllCaption(@Nullable String allCaption) {
        this.allCaption = allCaption;
        updateSelectedItemCaption();
    }

    public void setRedCaption(@Nullable String redCaption) {
        this.redCaption = redCaption;
        updateSelectedItemCaption();
    }

    public void setGreenCaption(@Nullable String greenCaption) {
        this.greenCaption = greenCaption;
        updateSelectedItemCaption();
    }

    public void setBlueCaption(@Nullable String blueCaption) {
        this.blueCaption = blueCaption;
        updateSelectedItemCaption();
    }

    protected void updateSelectedItemCaption() {
        if (range != null && range.getSelectedItem().isPresent()) {
            range.updateSelectedItemCaption(range.getSelectedItem().get());
        }
    }
}