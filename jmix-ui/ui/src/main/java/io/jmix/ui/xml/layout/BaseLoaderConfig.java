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

package io.jmix.ui.xml.layout;

import io.jmix.ui.component.*;
import io.jmix.ui.component.mainwindow.*;
import io.jmix.ui.xml.layout.loader.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseLoaderConfig {

    protected Map<String, Class<? extends ComponentLoader>> loaders = new ConcurrentHashMap<>();

    public BaseLoaderConfig() {
        initStandardLoaders();
    }

    protected void initStandardLoaders() {
        loaders.put(HBoxLayout.NAME, HBoxLayoutLoader.class);
        loaders.put(VBoxLayout.NAME, VBoxLayoutLoader.class);
        loaders.put(GridLayout.NAME, GridLayoutLoader.class);
        loaders.put(ScrollBoxLayout.NAME, ScrollBoxLayoutLoader.class);
        loaders.put(GroupBoxLayout.NAME, GroupBoxLayoutLoader.class);
        loaders.put(HtmlBoxLayout.NAME, HtmlBoxLayoutLoader.class);
        loaders.put(FlowBoxLayout.NAME, FlowBoxLayoutLoader.class);
        loaders.put(CssLayout.NAME, CssLayoutLoader.class);
        loaders.put(ResponsiveGridLayout.NAME, ResponsiveGridLayoutLoader.class);

        loaders.put(Button.NAME, ButtonLoader.class);
        loaders.put(LinkButton.NAME, LinkButtonLoader.class);
        loaders.put(CheckBox.NAME, CheckBoxLoader.class);
        loaders.put(Label.NAME, LabelLoader.class);
        loaders.put(Link.NAME, LinkLoader.class);
        loaders.put(Slider.NAME, SliderLoader.class);

        loaders.put(TextField.NAME, TextFieldLoader.class);
        loaders.put(MaskedField.NAME, MaskedFieldLoader.class);
        loaders.put(TextArea.NAME, TextAreaLoader.class);
        loaders.put(ResizableTextArea.NAME, ResizableTextAreaLoader.class);
        loaders.put(SourceCodeEditor.NAME, SourceCodeEditorLoader.class);
        loaders.put(PasswordField.NAME, PasswordFieldLoader.class);
        loaders.put(RichTextArea.NAME, RichTextAreaLoader.class);

        loaders.put(DateField.NAME, DateFieldLoader.class);
        loaders.put(TimeField.NAME, TimeFieldLoader.class);
        loaders.put(DatePicker.NAME, DatePickerLoader.class);
        loaders.put(ComboBox.NAME, ComboBoxLoader.class);
        loaders.put(SuggestionField.NAME, SuggestionFieldLoader.class);
        loaders.put(EntitySuggestionField.NAME, EntitySuggestionFieldLoader.class);
        loaders.put(ValuePicker.NAME, ValuePickerLoader.class);
        loaders.put(ValuesPicker.NAME, ValuesPickerLoader.class);
        loaders.put(EntityPicker.NAME, EntityPickerLoader.class);
        loaders.put(ColorPicker.NAME, ColorPickerLoader.class);
        loaders.put(EntityComboBox.NAME, EntityComboBoxLoader.class);
        loaders.put(CheckBoxGroup.NAME, CheckBoxGroupLoader.class);
        loaders.put(RadioButtonGroup.NAME, RadioButtonGroupLoader.class);
        loaders.put(MultiSelectList.NAME, MultiSelectListLoader.class);
        loaders.put(SingleSelectList.NAME, SingleSelectListLoader.class);
        loaders.put(FileUploadField.NAME, FileUploadFieldLoader.class);
        loaders.put(FileStorageUploadField.NAME, FileStorageUploadFieldLoader.class);
        loaders.put(FileMultiUploadField.NAME, FileMultiUploadFieldLoader.class);
        loaders.put(CurrencyField.NAME, CurrencyFieldLoader.class);

        loaders.put(Table.NAME, TableLoader.class);
        loaders.put(TreeTable.NAME, TreeTableLoader.class);
        loaders.put(GroupTable.NAME, GroupTableLoader.class);
        loaders.put(DataGrid.NAME, DataGridLoader.class);
        loaders.put(TreeDataGrid.NAME, TreeDataGridLoader.class);

        loaders.put(Calendar.NAME, CalendarLoader.class);
//        loaders.put(RuntimePropertiesFrame.NAME, RuntimePropertiesFrameLoader.class);  // todo dynamic attributes
        loaders.put(SplitPanel.NAME, SplitPanelLoader.class);
        loaders.put(Tree.NAME, TreeLoader.class);
        loaders.put(TabSheet.NAME, TabSheetLoader.class);
        loaders.put(Accordion.NAME, AccordionLoader.class);
        loaders.put(Image.NAME, ImageLoader.class);
        loaders.put(BrowserFrame.NAME, BrowserFrameLoader.class);
        loaders.put(ButtonsPanel.NAME, ButtonsPanelLoader.class);
        loaders.put(PopupButton.NAME, PopupButtonLoader.class);
        loaders.put(PopupView.NAME, PopupViewLoader.class);
        loaders.put(TagPicker.NAME, TagPickerLoader.class);
        loaders.put(TagField.NAME, TagFieldLoader.class);
        loaders.put(TwinColumn.NAME, TwinColumnLoader.class);
        loaders.put(ProgressBar.NAME, ProgressBarLoader.class);
        loaders.put(RelatedEntities.NAME, RelatedEntitiesLoader.class);
        loaders.put(Pagination.NAME, PaginationLoader.class);
        loaders.put(SimplePagination.NAME, SimplePaginationLoader.class);

        loaders.put(CapsLockIndicator.NAME, CapsLockIndicatorLoader.class);

        loaders.put(Form.NAME, FormLoader.class);
        loaders.put(JavaScriptComponent.NAME, JavaScriptComponentLoader.class);

        loaders.put(Filter.NAME, FilterLoader.class);
        loaders.put(GroupFilter.NAME, GroupFilterLoader.class);
        loaders.put(PropertyFilter.NAME, PropertyFilterLoader.class);
        loaders.put(JpqlFilter.NAME, JpqlFilterLoader.class);

        loaders.put(Fragment.NAME, FragmentComponentLoader.class);

        /* Main window components */

        loaders.put(AppMenu.NAME, AppMenuLoader.class);
        loaders.put(AppWorkArea.NAME, AppWorkAreaLoader.class);
        loaders.put(Drawer.NAME, DrawerLoader.class);
        loaders.put(UserActionsButton.NAME, UserActionsButtonLoader.class);
        loaders.put(LogoutButton.NAME, LogoutButtonLoader.class);
        loaders.put(NewWindowButton.NAME, NewWindowButtonLoader.class);
        loaders.put(UserIndicator.NAME, UserIndicatorLoader.class);
//        loaders.put(FtsField.NAME, FtsFieldLoader.class); // todo fts field
        loaders.put(TimeZoneIndicator.NAME, TimeZoneIndicatorLoader.class);
        loaders.put(SideMenu.NAME, SideMenuLoader.class);
    }
}
