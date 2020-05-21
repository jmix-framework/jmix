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

import com.vaadin.shared.ui.Orientation;
import com.vaadin.ui.CheckBoxGroup;
import io.jmix.ui.widget.client.checkboxgroup.JmixCheckBoxGroupState;

public class JmixCheckBoxGroup<T> extends CheckBoxGroup<T> {

    @Override
    protected JmixCheckBoxGroupState getState() {
        return ((JmixCheckBoxGroupState) super.getState());
    }

    @Override
    protected JmixCheckBoxGroupState getState(boolean markAsDirty) {
        return ((JmixCheckBoxGroupState) super.getState(markAsDirty));
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
