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

import com.google.common.reflect.TypeToken;
import com.haulmont.cuba.gui.UiComponents;
import com.haulmont.cuba.gui.components.Accordion;
import com.haulmont.cuba.gui.components.BrowserFrame;
import com.haulmont.cuba.gui.components.ButtonsPanel;
import com.haulmont.cuba.gui.components.CssLayout;
import com.haulmont.cuba.gui.components.FileMultiUploadField;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.FlowBoxLayout;
import com.haulmont.cuba.gui.components.Form;
import com.haulmont.cuba.gui.components.Fragment;
import com.haulmont.cuba.gui.components.GridLayout;
import com.haulmont.cuba.gui.components.HBoxLayout;
import com.haulmont.cuba.gui.components.Image;
import com.haulmont.cuba.gui.components.MaskedField;
import com.haulmont.cuba.gui.components.PopupButton;
import com.haulmont.cuba.gui.components.PopupView;
import com.haulmont.cuba.gui.components.ResizableTextArea;
import com.haulmont.cuba.gui.components.ScrollBoxLayout;
import com.haulmont.cuba.gui.components.SplitPanel;
import com.haulmont.cuba.gui.components.TextArea;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.components.VBoxLayout;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.mainwindow.AppWorkArea;
import com.haulmont.cuba.gui.components.mainwindow.FoldersPane;
import com.haulmont.cuba.web.gui.components.*;
import com.haulmont.cuba.web.gui.components.mainwindow.WebAppWorkArea;
import com.haulmont.cuba.web.gui.components.mainwindow.WebFoldersPane;
import io.jmix.ui.component.Calendar;
import io.jmix.ui.component.CheckBox;
import io.jmix.ui.component.CheckBoxGroup;
import io.jmix.ui.component.ColorPicker;
import io.jmix.ui.component.CurrencyField;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.DateField;
import io.jmix.ui.component.DatePicker;
import io.jmix.ui.component.EntityLinkField;
import io.jmix.ui.component.GroupBoxLayout;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.PasswordField;
import io.jmix.ui.component.RadioButtonGroup;
import io.jmix.ui.component.RelatedEntities;
import io.jmix.ui.component.RichTextArea;
import io.jmix.ui.component.Slider;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.component.SuggestionField;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.TimeField;
import io.jmix.ui.component.TokenList;
import io.jmix.ui.component.Tree;
import io.jmix.ui.component.TreeDataGrid;
import io.jmix.ui.component.TreeTable;
import io.jmix.ui.component.TwinColumn;
import io.jmix.ui.component.*;
import io.jmix.ui.sys.UiComponentsImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class CubaUiComponents extends UiComponentsImpl implements UiComponents {

    public static final String NAME = "cuba_UiComponents";

    {
        classes.put(ButtonsPanel.NAME, WebButtonsPanel.class);
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
        classes.put(BrowserFrame.NAME, WebBrowserFrame.class);
        classes.put(CheckBoxGroup.NAME, WebCheckBoxGroup.class);
        classes.put(RadioButtonGroup.NAME, WebRadioButtonGroup.class);
        classes.put(SuggestionPickerField.NAME, WebSuggestionPickerField.class);
        classes.put(SuggestionField.NAME, WebSuggestionField.class);
        classes.put(Slider.NAME, WebSlider.class);
        classes.put(CurrencyField.NAME, WebCurrencyField.class);
        classes.put(FileUploadField.NAME, WebFileUploadField.class);
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
        classes.put(EntityLinkField.NAME, WebEntityLinkField.class);
        classes.put(RelatedEntities.NAME, WebRelatedEntities.class);
        classes.put(PopupButton.NAME, WebPopupButton.class);
        classes.put(ScrollBoxLayout.NAME, WebScrollBoxLayout.class);
        classes.put(CssLayout.NAME, WebCssLayout.class);
        classes.put(FlowBoxLayout.NAME, WebFlowBoxLayout.class);
        classes.put(VBoxLayout.NAME, WebVBoxLayout.class);
        classes.put(HBoxLayout.NAME, WebHBoxLayout.class);
        classes.put(Fragment.NAME, WebFragment.class);
        classes.put(RootWindow.NAME, WebRootWindow.class);
        classes.put(TabWindow.NAME, WebTabWindow.class);
        classes.put(DialogWindow.NAME, WebDialogWindow.class);

        classes.put(OptionsList.NAME, WebOptionsList.class);
        classes.put(PickerField.NAME, WebPickerField.class);
        classes.put(SearchField.NAME, WebSearchField.class);
        classes.put(SearchPickerField.NAME, WebSearchPickerField.class);
        classes.put(FieldGroup.NAME, WebFieldGroup.class);
        classes.put(Form.NAME, WebForm.class);

        classes.put(Filter.NAME, WebFilter.class);
        classes.put(GridLayout.NAME, WebGridLayout.class);

        classes.put(SplitPanel.NAME, WebSplitPanel.class);
        classes.put(GroupBoxLayout.NAME, WebGroupBox.class);
        classes.put(TabSheet.NAME, WebTabSheet.class);
        classes.put(Accordion.NAME, WebAccordion.class);

        classes.put(PopupView.NAME, WebPopupView.class);
        classes.put(BulkEditor.NAME, WebBulkEditor.class);
        classes.put(ListEditor.NAME, WebListEditor.class);
        classes.put(Embedded.NAME, WebEmbedded.class);

        /* Main window components */
        classes.put(AppWorkArea.NAME, WebAppWorkArea.class);
        classes.put(FoldersPane.NAME, WebFoldersPane.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component> T create(TypeToken<T> type) {
        T t = (T) create((Class) type.getRawType());
        if (t instanceof HasDatatype) {
            Type[] actualTypeArguments = ((ParameterizedType) type.getType()).getActualTypeArguments();
            if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof Class) {
                Class actualTypeArgument = (Class) actualTypeArguments[0];

                ((HasDatatype) t).setDatatype(datatypeRegistry.find(actualTypeArgument));
            }
        }
        return t;
    }
}
