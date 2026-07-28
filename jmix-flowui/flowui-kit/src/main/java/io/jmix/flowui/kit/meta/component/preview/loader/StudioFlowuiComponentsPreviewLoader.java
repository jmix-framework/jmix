/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.kit.meta.component.preview.loader;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.FontIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.provider.HasListDataView;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.checkbox.JmixSwitch;
import io.jmix.flowui.kit.component.codeeditor.JmixCodeEditor;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.kit.component.gridlayout.JmixGridLayout;
import io.jmix.flowui.kit.component.loginform.EnhancedLoginForm;
import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.kit.component.main.UserIndicator;
import io.jmix.flowui.kit.component.markdowneditor.JmixMarkdownEditor;
import io.jmix.flowui.kit.component.menubar.HasMenuItemsEnhanced;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.multiselectcomboboxpicker.MultiSelectComboBoxPicker;
import io.jmix.flowui.kit.component.pagination.JmixSimplePagination;
import io.jmix.flowui.kit.component.richtexteditor.JmixRichTextEditor;
import io.jmix.flowui.kit.component.sidepanellayout.JmixSidePanelLayout;
import io.jmix.flowui.kit.component.sidepanellayout.JmixSidePanelLayoutCloser;
import io.jmix.flowui.kit.component.twincolumn.JmixTwinColumn;
import io.jmix.flowui.kit.component.upload.JmixFileStorageUploadField;
import io.jmix.flowui.kit.component.upload.JmixFileUploadField;
import io.jmix.flowui.kit.component.valuepicker.MultiValuePicker;
import io.jmix.flowui.kit.component.valuepicker.ValuePicker;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewEnvironment;
import io.jmix.flowui.kit.xml.layout.support.BaseComponentLoaderSupport;
import io.jmix.flowui.kit.xml.layout.support.BaseLoaderSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Studio preview loader for the core flowui components: containers, fields, buttons, grids and misc elements.
 * <p>
 * With a real {@link StudioPreviewEnvironment} (Studio now gates its own structural post-init on it),
 * this loader fills the same placeholders Studio used to add itself: 3 rows for any
 * {@link HasListDataView} (comboBox, listBox, checkboxGroup, virtualList, etc. — not grids, which
 * route to {@link StudioGridPreviewLoader}), and 5 items for {@code horizontalMenu}
 * ({@link HasMenuItemsEnhanced}). {@link StudioPreviewEnvironment#NOOP} (old Studio, via the 2-arg
 * {@link #load(Element, Element) load}) gets none, since old Studio still adds its own.
 */
public class StudioFlowuiComponentsPreviewLoader implements StudioPreviewComponentLoader {

    protected static final Map<String, Supplier<Component>> FACTORIES = Map.ofEntries(
            // containers
            Map.entry(StudioXmlElements.HBOX, HorizontalLayout::new),
            Map.entry(StudioXmlElements.VBOX, VerticalLayout::new),
            Map.entry(StudioXmlElements.FLEX_LAYOUT, FlexLayout::new),
            Map.entry(StudioXmlElements.SCROLLER, Scroller::new),
            Map.entry(StudioXmlElements.ACCORDION, Accordion::new),
            Map.entry(StudioXmlElements.ACCORDION_PANEL, AccordionPanel::new),
            Map.entry(StudioXmlElements.TAB_SHEET, TabSheet::new),
            Map.entry(StudioXmlElements.TABS, Tabs::new),
            Map.entry(StudioXmlElements.TAB, Tab::new),
            Map.entry(StudioXmlElements.DETAILS, Details::new),
            Map.entry(StudioXmlElements.SPLIT, SplitLayout::new),
            Map.entry(StudioXmlElements.FORM_LAYOUT, FormLayout::new),
            Map.entry(StudioXmlElements.FORM_ITEM, FormLayout.FormItem::new),
            Map.entry(StudioXmlElements.FORM_ROW, FormLayout.FormRow::new),
            Map.entry(StudioXmlElements.CARD, Card::new),
            Map.entry(StudioXmlElements.GRID_LAYOUT, JmixGridLayout::new),
            Map.entry(StudioXmlElements.SIDE_PANEL_LAYOUT, JmixSidePanelLayout::new),
            Map.entry(StudioXmlElements.SIDE_PANEL_LAYOUT_CLOSER, JmixSidePanelLayoutCloser::new),
            // buttons
            Map.entry(StudioXmlElements.BUTTON, JmixButton::new),
            // fields
            Map.entry(StudioXmlElements.TEXT_FIELD, TextField::new),
            Map.entry(StudioXmlElements.EMAIL_FIELD, EmailField::new),
            Map.entry(StudioXmlElements.NUMBER_FIELD, NumberField::new),
            Map.entry(StudioXmlElements.PASSWORD_FIELD, PasswordField::new),
            Map.entry(StudioXmlElements.BIG_DECIMAL_FIELD, BigDecimalField::new),
            Map.entry(StudioXmlElements.INTEGER_FIELD, IntegerField::new),
            Map.entry(StudioXmlElements.TEXT_AREA, TextArea::new),
            Map.entry(StudioXmlElements.CHECKBOX, Checkbox::new),
            Map.entry(StudioXmlElements.SWITCH, JmixSwitch::new),
            Map.entry(StudioXmlElements.CHECKBOX_GROUP, CheckboxGroup::new),
            Map.entry(StudioXmlElements.RADIO_BUTTON_GROUP, RadioButtonGroup::new),
            Map.entry(StudioXmlElements.LIST_BOX, ListBox::new),
            Map.entry(StudioXmlElements.MULTI_SELECT_LIST_BOX, MultiSelectListBox::new),
            Map.entry(StudioXmlElements.COMBO_BOX, ComboBox::new),
            Map.entry(StudioXmlElements.MULTI_SELECT_COMBO_BOX, MultiSelectComboBox::new),
            Map.entry(StudioXmlElements.MULTI_SELECT_COMBO_BOX_PICKER, MultiSelectComboBoxPicker::new),
            Map.entry(StudioXmlElements.SELECT, Select::new),
            Map.entry(StudioXmlElements.TIME_PICKER, TimePicker::new),
            Map.entry(StudioXmlElements.DATE_PICKER, DatePicker::new),
            Map.entry(StudioXmlElements.DATE_TIME_PICKER, DateTimePicker::new),
            Map.entry(StudioXmlElements.VALUE_PICKER, ValuePicker::new),
            Map.entry(StudioXmlElements.ENTITY_PICKER, ValuePicker::new),
            Map.entry(StudioXmlElements.MULTI_VALUE_PICKER, MultiValuePicker::new),
            // comboBoxPicker: no matching StudioXmlElements constant, kept as a local literal.
            Map.entry("comboBoxPicker", ComboBoxPicker::new),
            Map.entry(StudioXmlElements.ENTITY_COMBO_BOX, ComboBoxPicker::new),
            // misc display
            Map.entry(StudioXmlElements.AVATAR, Avatar::new),
            Map.entry(StudioXmlElements.PROGRESS_BAR, ProgressBar::new),
            Map.entry(StudioXmlElements.DRAWER_TOGGLE, DrawerToggle::new),
            Map.entry(StudioXmlElements.LIST_MENU, ListMenu::new),
            Map.entry(StudioXmlElements.USER_INDICATOR, UserIndicator::new),
            // grids
            Map.entry(StudioXmlElements.VIRTUAL_LIST, VirtualList::new),
            // login
            Map.entry(StudioXmlElements.LOGIN_FORM, EnhancedLoginForm::new),
            Map.entry(StudioXmlElements.LOGIN_OVERLAY, LoginOverlay::new),
            // pagination / upload / editors
            Map.entry(StudioXmlElements.SIMPLE_PAGINATION, JmixSimplePagination::new),
            Map.entry(StudioXmlElements.FILE_UPLOAD_FIELD, JmixFileUploadField::new),
            Map.entry(StudioXmlElements.FILE_STORAGE_UPLOAD_FIELD, JmixFileStorageUploadField::new),
            Map.entry(StudioXmlElements.UPLOAD, Upload::new),
            Map.entry(StudioXmlElements.CODE_EDITOR, JmixCodeEditor::new),
            Map.entry(StudioXmlElements.RICH_TEXT_EDITOR, JmixRichTextEditor::new),
            Map.entry(StudioXmlElements.MARKDOWN_EDITOR, JmixMarkdownEditor::new),
            Map.entry(StudioXmlElements.TWIN_COLUMN, JmixTwinColumn::new),
            Map.entry(StudioXmlElements.HORIZONTAL_MENU, JmixMenuBar::new),
            // structural placeholders (spring composites / UI.getCurrent()-touching components)
            Map.entry(StudioXmlElements.MENU_FILTER_FIELD, TextField::new),
            Map.entry(StudioXmlElements.PROPERTY_FILTER, TextField::new),
            Map.entry(StudioXmlElements.JPQL_FILTER, TextField::new),
            Map.entry(StudioXmlElements.GROUP_FILTER, VerticalLayout::new),
            Map.entry(StudioXmlElements.GENERIC_FILTER, Details::new)
    );

    /** Elements needing extra attribute handling on top of the common ones. */
    protected static final Map<String, Function<Element, Component>> SPECIALS = Map.of(
            StudioXmlElements.ICON, element -> BaseComponentLoaderSupport.loadIconSetIcon(element).orElseGet(Icon::new),
            StudioXmlElements.SVG_ICON, element -> {
                SvgIcon svgIcon = new SvgIcon();
                BaseLoaderSupport.loadString(element, "src", svgIcon::setSrc);
                return svgIcon;
            },
            StudioXmlElements.FONT_ICON, element -> new FontIcon(),
            StudioXmlElements.MARKDOWN, element -> new Markdown(inlineContent(element)
                    .or(() -> BaseLoaderSupport.loadString(element, StudioXmlElements.CONTENT))
                    .orElse("")),
            StudioXmlElements.IMAGE, element -> {
                Image image = new Image();
                BaseLoaderSupport.loadString(element, "src")
                        .filter(src -> src.startsWith("http"))
                        .ifPresent(image::setSrc);
                return image;
            }
    );

    /** Used by tests to iterate the full supported set. */
    public static Set<String> supportedElements() {
        Set<String> names = new HashSet<>(FACTORIES.keySet());
        names.addAll(SPECIALS.keySet());
        return names;
    }

    @Override
    public boolean isSupported(Element element) {
        return hasViewOrFragmentSchema(element)
                && (FACTORIES.containsKey(element.getName()) || SPECIALS.containsKey(element.getName()));
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        String name = componentElement.getName();
        Component component = SPECIALS.containsKey(name)
                ? SPECIALS.get(name).apply(componentElement)
                : FACTORIES.get(name).get();
        loadComponentBaseAttributes(component, componentElement);
        return component;
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        Component component = load(componentElement, viewElement);
        if (component != null && environment != StudioPreviewEnvironment.NOOP) {
            fillPlaceholders(component);
        }
        return component;
    }

    /**
     * Mirrors Studio's old {@code postInitHasListDataView}/{@code postInitHasMenuItems}: since
     * Studio now gates those on the framework-owns-content probe, this loader builds the same
     * placeholders itself so a real environment still sees live content.
     */
    @SuppressWarnings("unchecked")
    protected void fillPlaceholders(Component component) {
        if (component instanceof HasListDataView && !(component instanceof Grid)) {
            ((HasListDataView<Object, ?>) component).setItems(List.of("Item 1", "Item 2", "Item 3"));
        } else if (component instanceof HasMenuItemsEnhanced menuItems) {
            for (int i = 0; i < 5; i++) {
                menuItems.addItem("Menu item " + i);
            }
        }
    }

    protected static Optional<String> inlineContent(Element element) {
        return element.elements().stream()
                .filter(child -> StudioXmlElements.CONTENT.equals(child.getName()))
                .findFirst()
                .map(child -> child.getText().trim());
    }
}
