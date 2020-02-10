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

package com.haulmont.cuba.web.gui;

import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.web.components.CheckBox;
import com.haulmont.cuba.web.components.DateField;
import com.haulmont.cuba.web.components.DatePicker;
import com.haulmont.cuba.web.components.Label;
import com.haulmont.cuba.web.components.OptionsList;
import com.haulmont.cuba.web.gui.components.*;
import io.jmix.ui.components.LookupField;
import io.jmix.ui.components.LookupPickerField;
import io.jmix.ui.components.OptionsGroup;
import io.jmix.ui.components.PickerField;
import io.jmix.ui.components.SearchField;
import io.jmix.ui.components.TextField;
import io.jmix.ui.sys.WebUiComponents;

public class CubaUiComponents extends WebUiComponents {

    public static final String NAME = "cuba_UiComponents";

    {
        classes.put(TextField.NAME, WebTextField.class);
        classes.put(CheckBox.NAME, WebCheckBox.class);
        classes.put(DateField.NAME, WebDateField.class);
        classes.put(DatePicker.NAME, WebDatePicker.class);
        classes.put(Label.NAME, WebLabel.class);
        classes.put(LookupField.NAME, WebLookupField.class);
        classes.put(LookupPickerField.NAME, WebLookupPickerField.class);
        classes.put(OptionsGroup.NAME, WebOptionsGroup.class);
        classes.put(OptionsList.NAME, WebOptionsList.class);

        classes.put(PickerField.NAME, WebPickerField.class);
        classes.put(SearchField.NAME, WebSearchField.class);
        classes.put(FieldGroup.NAME, WebFieldGroup.class);
    }
}
