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

package io.jmix.widgetsaddon;

import com.vaadin.ui.Button;
import io.jmix.widgetsaddon.client.CustomButtonState;

import java.util.Objects;

public class CustomButton extends Button {

    @Override
    protected CustomButtonState getState() {
        return (CustomButtonState) super.getState();
    }

    @Override
    protected CustomButtonState getState(boolean markAsDirty) {
        return (CustomButtonState) super.getState(markAsDirty);
    }

    public String getColor() {
        return getState(false).color;
    }

    public void setColor(String color) {
        if (!Objects.equals(getState(false).color, color)) {
            getState().color = color;
        }
    }
}