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

package io.jmix.ui.widgets;

import io.jmix.ui.widgets.client.radiobuttongroup.CubaRadioButtonGroupState;
import com.vaadin.shared.ui.Orientation;
import com.vaadin.ui.RadioButtonGroup;

public class CubaRadioButtonGroup<T> extends RadioButtonGroup<T> {

    @Override
    protected CubaRadioButtonGroupState getState() {
        return ((CubaRadioButtonGroupState) super.getState());
    }

    @Override
    protected CubaRadioButtonGroupState getState(boolean markAsDirty) {
        return ((CubaRadioButtonGroupState) super.getState(markAsDirty));
    }

    public Orientation getOrientation() {
        return getState(false).orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (orientation != getOrientation()) {
            getState().orientation = orientation;
        }
    }
}
