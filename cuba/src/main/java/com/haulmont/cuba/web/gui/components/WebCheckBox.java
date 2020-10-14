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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.CheckBox;
import com.haulmont.cuba.gui.components.FieldGroup;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Form;
import io.jmix.ui.component.impl.CheckBoxImpl;
import io.jmix.ui.widget.JmixCheckBox;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Deprecated
public class WebCheckBox extends CheckBoxImpl implements CheckBox {

    @Override
    public void setParent(@Nullable Component parent) {
        super.setParent(parent);

        if (parent instanceof FieldGroup) {
            ((JmixCheckBox) component).setCaptionManagedByLayout(true);
        }
    }

    @Override
    public void addValidator(Consumer<? super Boolean> validator) {
        addValidator(validator::accept);
    }

    @Override
    public void removeValidator(Consumer<Boolean> validator) {
        removeValidator(validator::accept);
    }
}
