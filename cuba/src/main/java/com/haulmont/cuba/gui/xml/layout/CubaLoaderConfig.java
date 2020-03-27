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

package com.haulmont.cuba.gui.xml.layout;

import com.haulmont.cuba.gui.components.BulkEditor;
import com.haulmont.cuba.gui.components.FieldGroup;
import com.haulmont.cuba.gui.components.Filter;
import com.haulmont.cuba.gui.xml.layout.loaders.*;
import io.jmix.ui.components.*;
import io.jmix.ui.xml.layout.BaseLoaderConfig;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LoaderConfig;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

@SuppressWarnings("rawtypes")
@Component(CubaLoaderConfig.NAME)
public class CubaLoaderConfig extends BaseLoaderConfig implements LoaderConfig {

    public static final String NAME = "cuba_LegacyLoaderConfig";

    @Override
    public boolean supports(Element element) {
        return isLegacyScreen(element)
                && loaders.containsKey(element.getName());
    }

    @Override
    public Class<? extends ComponentLoader> getLoader(Element element) {
        return loaders.get(element.getName());
    }

    @Override
    protected void initStandardLoaders() {
        super.initStandardLoaders();

        loaders.put(Calendar.NAME, CubaCalendarLoader.class);
        loaders.put(Tree.NAME, CubaTreeLoader.class);
        loaders.put(DataGrid.NAME, CubaDataGridLoader.class);
        loaders.put(TreeDataGrid.NAME, CubaTreeDataGridLoader.class);
        loaders.put(Table.NAME, CubaTableLoader.class);
        loaders.put(GroupTable.NAME, CubaGroupTableLoader.class);
        loaders.put(TreeTable.NAME, CubaTreeTableLoader.class);
        loaders.put(TokenList.NAME, CubaTokenListLoader.class);
        loaders.put(TwinColumn.NAME, CubaTwinColumnLoader.class);
        loaders.put(Image.NAME, CubaImageLoader.class);
        loaders.put(SearchPickerField.NAME, CubaSearchPickerFieldLoader.class);
        loaders.put(SearchField.NAME, CubaSearchFieldLoader.class);
        loaders.put(CheckBox.NAME, CubaCheckBoxLoader.class);
        loaders.put(Label.NAME, CubaLabelLoader.class);
        loaders.put(CheckBoxGroup.NAME, CubaCheckBoxGroupLoader.class);
        loaders.put(RadioButtonGroup.NAME, CubaRadioButtonGroupLoader.class);
        loaders.put(OptionsList.NAME, CubaOptionsListLoader.class);
        loaders.put(OptionsGroup.NAME, CubaOptionsGroupLoader.class);
        loaders.put(SuggestionPickerField.NAME, CubaSuggestionPickerFieldLoader.class);
        loaders.put(SuggestionField.NAME, CubaSuggestionFieldLoader.class);
        loaders.put(Slider.NAME, CubaSliderLoader.class);
        loaders.put(CurrencyField.NAME, CubaCurrencyFieldLoader.class);
        loaders.put(ColorPicker.NAME, CubaColorPickerLoader.class);
        loaders.put(DateField.NAME, CubaDateFieldLoader.class);
        loaders.put(DatePicker.NAME, CubaDatePickerLoader.class);
        loaders.put(TimeField.NAME, CubaTimeFieldLoader.class);
        loaders.put(LookupField.NAME, CubaLookupFieldLoader.class);
        loaders.put(LookupPickerField.NAME, CubaLookupPickerFieldLoader.class);
        loaders.put(PickerField.NAME, CubaPickerFieldLoader.class);
        loaders.put(PasswordField.NAME, CubaPasswordFieldLoader.class);
        loaders.put(RichTextArea.NAME, CubaRichTextAreaLoader.class);
        loaders.put(SourceCodeEditor.NAME, CubaSourceCodeEditorLoader.class);
        loaders.put(MaskedField.NAME, CubaMaskedFieldLoader.class);
        loaders.put(ResizableTextArea.NAME, CubaResizableTextAreaLoader.class);
        loaders.put(TextArea.NAME, CubaResizableTextAreaLoader.class);
        loaders.put(TextField.NAME, CubaTextFieldLoader.class);
        loaders.put(FieldGroup.NAME, FieldGroupLoader.class);
        loaders.put(BulkEditor.NAME, BulkEditorLoader.class);
        loaders.put(Filter.NAME, FilterLoader.class);
    }

    protected boolean isLegacyScreen(Element element) {
        Element parent = element.getParent();

        while (parent != null
                && !"window".equals(parent.getName())) {
            parent = parent.getParent();
        }

        return parent != null
                && parent.attribute("class") != null;
    }
}
