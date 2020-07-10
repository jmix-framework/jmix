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

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.FileMultiUploadField;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.xml.layout.loaders.*;
import io.jmix.ui.component.*;
import io.jmix.ui.component.Calendar;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.CheckBoxGroup;
import io.jmix.ui.component.ColorPicker;
import io.jmix.ui.component.CurrencyField;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.DatePicker;
import io.jmix.ui.component.GroupBoxLayout;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Image;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.MaskedField;
import io.jmix.ui.component.OptionsList;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.RadioButtonGroup;
import io.jmix.ui.component.ResizableTextArea;
import io.jmix.ui.component.RichTextArea;
import io.jmix.ui.component.Slider;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.component.SplitPanel;
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
import io.jmix.ui.xml.layout.BaseLoaderConfig;
import io.jmix.ui.xml.layout.ComponentLoader;
import io.jmix.ui.xml.layout.LoaderConfig;
import io.jmix.ui.xml.layout.loader.ButtonLoader;
import io.jmix.ui.xml.layout.loader.FileMultiUploadFieldLoader;
import io.jmix.ui.xml.layout.loader.FragmentLoader;
import io.jmix.ui.xml.layout.loader.GridLayoutLoader;
import io.jmix.ui.xml.layout.loader.WindowLoader;
import org.dom4j.Element;
import org.springframework.stereotype.Component;

@SuppressWarnings("rawtypes")
@Component(CubaLoaderConfig.NAME)
public class CubaLoaderConfig extends BaseLoaderConfig implements LoaderConfig {

    public static final String NAME = "cuba_LegacyLoaderConfig";

    protected Class<? extends WindowLoader> windowLoader = CubaWindowLoader.class;
    protected Class<? extends FragmentLoader> fragmentLoader = CubaFragmentLoader.class;

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
    public Class<? extends ComponentLoader> getFragmentLoader(Element root) {
        if (isLegacyScreen(root))
            return fragmentLoader;

        return null;
    }

    @Override
    public Class<? extends ComponentLoader> getWindowLoader(Element root) {
        if (isLegacyScreen(root))
            return windowLoader;

        return null;
    }

    @Override
    protected void initStandardLoaders() {
        super.initStandardLoaders();

        loaders.put("frame", CubaFragmentComponentLoader.class);

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
        loaders.put(Button.NAME, CubaButtonLoader.class);
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
        loaders.put("grid", GridLayoutLoader.class);
        loaders.put(FileUploadField.NAME, CubaFileUploadFieldLoader.class);
        loaders.put(FileMultiUploadField.NAME, FileMultiUploadFieldLoader.class);
        loaders.put(GroupBoxLayout.NAME, CubaGroupBoxLayoutLoader.class);
        loaders.put(SplitPanel.NAME, CubaSplitPanelLoader.class);
    }

    protected boolean isLegacyScreen(Element element) {
        if ("window".equals(element.getName())) {
            return element.attribute("class") != null;
        }

        Element parent = element.getParent();

        while (parent != null
                && !"window".equals(parent.getName())) {
            parent = parent.getParent();
        }

        return parent != null
                && parent.attribute("class") != null;
    }
}
