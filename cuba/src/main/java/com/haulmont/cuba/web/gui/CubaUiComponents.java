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

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.web.gui.components.*;
import io.jmix.ui.component.Calendar;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.CheckBoxGroup;
import io.jmix.ui.component.ColorPicker;
import io.jmix.ui.component.CurrencyField;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.DatePicker;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Image;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.MaskedField;
import io.jmix.ui.component.OptionsList;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.RadioButtonGroup;
import io.jmix.ui.component.ResizableTextArea;
import io.jmix.ui.component.RichTextArea;
import io.jmix.ui.component.RowsCount;
import io.jmix.ui.component.Slider;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.component.SuggestionField;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.TextArea;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.TimeField;
import io.jmix.ui.component.TokenList;
import io.jmix.ui.component.Tree;
import io.jmix.ui.component.TreeDataGrid;
import io.jmix.ui.component.TreeTable;
import io.jmix.ui.component.TwinColumn;
import io.jmix.ui.component.impl.WebGridLayout;
import io.jmix.ui.sys.WebUiComponents;

public class CubaUiComponents extends WebUiComponents {

    public static final String NAME = "cuba_UiComponents";

    {
        classes.put(RowsCount.NAME, WebRowsCount.class);
        classes.put(Calendar.NAME, WebCalendar.class);
        classes.put(Tree.NAME, WebTree.class);
        classes.put(DataGrid.NAME, WebDataGrid.class);
        classes.put(TreeDataGrid.NAME, WebTreeDataGrid.class);
        classes.put(Table.NAME, WebTable.class);
        classes.put(GroupTable.NAME, WebGroupTable.class);
        classes.put(TreeTable.NAME, WebTreeTable.class);
        classes.put(TokenList.NAME, WebTokenList.class);
        classes.put(TwinColumn.NAME, WebTwinColumn.class);
        classes.put(Image.NAME, WebImage.class);
        classes.put(CheckBoxGroup.NAME, WebCheckBoxGroup.class);
        classes.put(RadioButtonGroup.NAME, WebRadioButtonGroup.class);
        classes.put(SuggestionPickerField.NAME, WebSuggestionPickerField.class);
        classes.put(SuggestionField.NAME, WebSuggestionField.class);
        classes.put(Slider.NAME, WebSlider.class);
        classes.put(CurrencyField.NAME, WebCurrencyField.class);
        classes.put(com.haulmont.cuba.gui.components.FileUploadField.NAME, WebFileUploadField.class);
        classes.put(FileMultiUploadField.NAME, WebFileMultiUploadField.class);
        classes.put(ColorPicker.NAME, WebColorPicker.class);
        classes.put(TimeField.NAME, WebTimeField.class);
        classes.put(RichTextArea.NAME, WebRichTextArea.class);
        classes.put(SourceCodeEditor.NAME, WebSourceCodeEditor.class);
        classes.put(PasswordField.NAME, WebPasswordField.class);
        classes.put(MaskedField.NAME, WebMaskedField.class);
        classes.put(ResizableTextArea.NAME, WebResizableTextArea.class);
        classes.put(TextArea.NAME, WebTextArea.class);
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
        classes.put(SearchPickerField.NAME, WebSearchPickerField.class);
        classes.put(FieldGroup.NAME, WebFieldGroup.class);

        classes.put(Filter.NAME, WebFilter.class);
        classes.put("grid", WebGridLayout.class);
    }
}
