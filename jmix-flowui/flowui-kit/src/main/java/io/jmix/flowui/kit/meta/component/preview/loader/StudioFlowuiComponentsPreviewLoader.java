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
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.checkbox.JmixSwitch;
import io.jmix.flowui.kit.component.codeeditor.JmixCodeEditor;
import io.jmix.flowui.kit.component.combobox.ComboBoxPicker;
import io.jmix.flowui.kit.component.gridlayout.JmixGridLayout;
import io.jmix.flowui.kit.component.loginform.EnhancedLoginForm;
import io.jmix.flowui.kit.component.main.ListMenu;
import io.jmix.flowui.kit.component.main.UserIndicator;
import io.jmix.flowui.kit.component.markdowneditor.JmixMarkdownEditor;
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
import io.jmix.flowui.kit.meta.component.preview.StudioPreviewComponentLoader;
import io.jmix.flowui.kit.xml.layout.support.BaseComponentLoaderSupport;
import io.jmix.flowui.kit.xml.layout.support.BaseLoaderSupport;
import org.dom4j.Element;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Studio preview loader for the core flowui components: containers, fields, buttons, grids and misc elements.
 */
public class StudioFlowuiComponentsPreviewLoader implements StudioPreviewComponentLoader {

    protected static final Map<String, Supplier<Component>> FACTORIES = Map.ofEntries(
            // containers
            Map.entry("hbox", HorizontalLayout::new),
            Map.entry("vbox", VerticalLayout::new),
            Map.entry("flexLayout", FlexLayout::new),
            Map.entry("scroller", Scroller::new),
            Map.entry("accordion", Accordion::new),
            Map.entry("accordionPanel", AccordionPanel::new),
            Map.entry("tabSheet", TabSheet::new),
            Map.entry("tabs", Tabs::new),
            Map.entry("tab", Tab::new),
            Map.entry("details", Details::new),
            Map.entry("split", SplitLayout::new),
            Map.entry("formLayout", FormLayout::new),
            Map.entry("formItem", FormLayout.FormItem::new),
            Map.entry("formRow", FormLayout.FormRow::new),
            Map.entry("card", Card::new),
            Map.entry("gridLayout", JmixGridLayout::new),
            Map.entry("sidePanelLayout", JmixSidePanelLayout::new),
            Map.entry("sidePanelLayoutCloser", JmixSidePanelLayoutCloser::new),
            // buttons
            Map.entry("button", JmixButton::new),
            // fields
            Map.entry("textField", TextField::new),
            Map.entry("emailField", EmailField::new),
            Map.entry("numberField", NumberField::new),
            Map.entry("passwordField", PasswordField::new),
            Map.entry("bigDecimalField", BigDecimalField::new),
            Map.entry("integerField", IntegerField::new),
            Map.entry("textArea", TextArea::new),
            Map.entry("checkbox", Checkbox::new),
            Map.entry("switch", JmixSwitch::new),
            Map.entry("checkboxGroup", CheckboxGroup::new),
            Map.entry("radioButtonGroup", RadioButtonGroup::new),
            Map.entry("listBox", ListBox::new),
            Map.entry("multiSelectListBox", MultiSelectListBox::new),
            Map.entry("comboBox", ComboBox::new),
            Map.entry("multiSelectComboBox", MultiSelectComboBox::new),
            Map.entry("multiSelectComboBoxPicker", MultiSelectComboBoxPicker::new),
            Map.entry("select", Select::new),
            Map.entry("timePicker", TimePicker::new),
            Map.entry("datePicker", DatePicker::new),
            Map.entry("dateTimePicker", DateTimePicker::new),
            Map.entry("valuePicker", ValuePicker::new),
            Map.entry("entityPicker", ValuePicker::new),
            Map.entry("multiValuePicker", MultiValuePicker::new),
            Map.entry("comboBoxPicker", ComboBoxPicker::new),
            Map.entry("entityComboBox", ComboBoxPicker::new),
            // misc display
            Map.entry("avatar", Avatar::new),
            Map.entry("progressBar", ProgressBar::new),
            Map.entry("drawerToggle", DrawerToggle::new),
            Map.entry("listMenu", ListMenu::new),
            Map.entry("userIndicator", UserIndicator::new),
            // grids
            Map.entry("virtualList", VirtualList::new),
            // login
            Map.entry("loginForm", EnhancedLoginForm::new),
            Map.entry("loginOverlay", LoginOverlay::new),
            // pagination / upload / editors
            Map.entry("simplePagination", JmixSimplePagination::new),
            Map.entry("fileUploadField", JmixFileUploadField::new),
            Map.entry("fileStorageUploadField", JmixFileStorageUploadField::new),
            Map.entry("upload", Upload::new),
            Map.entry("codeEditor", JmixCodeEditor::new),
            Map.entry("richTextEditor", JmixRichTextEditor::new),
            Map.entry("markdownEditor", JmixMarkdownEditor::new),
            Map.entry("twinColumn", JmixTwinColumn::new),
            Map.entry("horizontalMenu", JmixMenuBar::new),
            // structural placeholders (spring composites / UI.getCurrent()-touching components)
            Map.entry("menuFilterField", TextField::new),
            Map.entry("propertyFilter", TextField::new),
            Map.entry("jpqlFilter", TextField::new),
            Map.entry("groupFilter", VerticalLayout::new),
            Map.entry("genericFilter", Details::new)
    );

    /** Elements needing extra attribute handling on top of the common ones. */
    protected static final Map<String, Function<Element, Component>> SPECIALS = Map.of(
            "icon", element -> BaseComponentLoaderSupport.loadIconSetIcon(element).orElseGet(Icon::new),
            "svgIcon", element -> {
                SvgIcon svgIcon = new SvgIcon();
                BaseLoaderSupport.loadString(element, "src", svgIcon::setSrc);
                return svgIcon;
            },
            "fontIcon", element -> new FontIcon(),
            "markdown", element -> new Markdown(inlineContent(element).orElse("")),
            "image", element -> {
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

    protected static Optional<String> inlineContent(Element element) {
        return element.elements().stream()
                .filter(child -> "content".equals(child.getName()))
                .findFirst()
                .map(child -> child.getText().trim());
    }
}
