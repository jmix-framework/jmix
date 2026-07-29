/*
 * Copyright 2024 Haulmont.
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

package io.jmix.flowui.kit.meta.component.preview;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
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
import io.jmix.flowui.kit.component.usermenu.HasActionMenuItems;
import io.jmix.flowui.kit.component.usermenu.HasTextMenuItems;
import io.jmix.flowui.kit.component.usermenu.JmixUserMenu;
import io.jmix.flowui.kit.component.usermenu.TextUserMenuItem;
import io.jmix.flowui.kit.component.valuepicker.MultiValuePicker;
import io.jmix.flowui.kit.component.valuepicker.ValuePicker;
import io.jmix.flowui.kit.meta.StudioXmlElements;
import io.jmix.flowui.kit.meta.component.preview.loader.PreviewActionSupport;
import io.jmix.flowui.kit.xml.layout.support.BaseComponentLoaderSupport;
import io.jmix.flowui.kit.xml.layout.support.BaseLoaderSupport;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

import static io.jmix.flowui.kit.component.usermenu.JmixUserMenu.BUTTON_CONTENT_CLASS_NAME;

public final class StudioStandardComponentsPreviewLoader implements StudioPreviewComponentLoader {

    /** Item tags the preview can actually render; used to decide XML items vs. fallback placeholders. */
    private static final Set<String> RENDERABLE_ITEM_NAMES = Set.of(
            StudioXmlElements.TEXT_ITEM, StudioXmlElements.ACTION_ITEM,
            StudioXmlElements.VIEW_ITEM, StudioXmlElements.SEPARATOR);

    private static final Map<String, Supplier<Component>> FACTORIES = Map.<String, Supplier<Component>>ofEntries(
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
            Map.entry(StudioXmlElements.ENTITY_COMBO_BOX, ComboBoxPicker::new),
            Map.entry("comboBoxPicker", ComboBoxPicker::new),

            Map.entry(StudioXmlElements.AVATAR, Avatar::new),
            Map.entry(StudioXmlElements.BUTTON, JmixButton::new),
            Map.entry(StudioXmlElements.PROGRESS_BAR, ProgressBar::new),
            Map.entry(StudioXmlElements.DRAWER_TOGGLE, DrawerToggle::new),
            Map.entry(StudioXmlElements.LIST_MENU, ListMenu::new),
            Map.entry(StudioXmlElements.USER_INDICATOR, UserIndicator::new),
            Map.entry(StudioXmlElements.VIRTUAL_LIST, VirtualList::new),

            Map.entry(StudioXmlElements.LOGIN_FORM, EnhancedLoginForm::new),
            Map.entry(StudioXmlElements.LOGIN_OVERLAY, LoginOverlay::new),

            Map.entry(StudioXmlElements.SIMPLE_PAGINATION, JmixSimplePagination::new),
            Map.entry(StudioXmlElements.FILE_UPLOAD_FIELD, JmixFileUploadField::new),
            Map.entry(StudioXmlElements.FILE_STORAGE_UPLOAD_FIELD, JmixFileStorageUploadField::new),
            Map.entry(StudioXmlElements.UPLOAD, Upload::new),
            Map.entry(StudioXmlElements.CODE_EDITOR, JmixCodeEditor::new),
            Map.entry(StudioXmlElements.RICH_TEXT_EDITOR, JmixRichTextEditor::new),
            Map.entry(StudioXmlElements.MARKDOWN_EDITOR, JmixMarkdownEditor::new),
            Map.entry(StudioXmlElements.TWIN_COLUMN, JmixTwinColumn::new),
            Map.entry(StudioXmlElements.HORIZONTAL_MENU, JmixMenuBar::new),

            Map.entry(StudioXmlElements.MENU_FILTER_FIELD, TextField::new),
            Map.entry(StudioXmlElements.PROPERTY_FILTER, TextField::new),
            Map.entry(StudioXmlElements.JPQL_FILTER, TextField::new),
            Map.entry(StudioXmlElements.GROUP_FILTER, VerticalLayout::new),
            Map.entry(StudioXmlElements.GENERIC_FILTER, Details::new)
    );

    /**
     * Elements needing extra attribute handling on top of the common ones.
     */
    private static final Map<String, Function<Element, Component>> SPECIALS = Map.of(
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

    /**
     * Used by tests to iterate the full supported set.
     */
    public static Set<String> supportedElements() {
        Set<String> names = new HashSet<>(FACTORIES.keySet());
        names.addAll(SPECIALS.keySet());
        return names;
    }

    @Override
    public boolean isSupported(Element element) {
        return isFragment(element) || isUserMenu(element) || isFactoryElement(element);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement) {
        return load(componentElement, viewElement, StudioPreviewEnvironment.NOOP);
    }

    @Nullable
    @Override
    public Component load(Element componentElement, Element viewElement, StudioPreviewEnvironment environment) {
        if (isFragment(componentElement)) {
            return loadFragment(componentElement);
        } else if (isUserMenu(componentElement)) {
            return loadUserMenu(componentElement, viewElement, environment);
        } else if (isFactoryElement(componentElement)) {
            return loadFactoryComponent(componentElement, environment);
        } else {
            return null;
        }
    }

    private boolean isFactoryElement(Element element) {
        return hasViewOrFragmentSchema(element)
                && (FACTORIES.containsKey(element.getName()) || SPECIALS.containsKey(element.getName()));
    }

    private Component loadFactoryComponent(Element componentElement, StudioPreviewEnvironment environment) {
        String name = componentElement.getName();
        Component component = SPECIALS.containsKey(name)
                ? SPECIALS.get(name).apply(componentElement)
                : FACTORIES.get(name).get();

        loadComponentBaseAttributes(component, componentElement);
        if (environment != StudioPreviewEnvironment.NOOP) {
            fillPlaceholders(component);
        }
        return component;
    }

    @SuppressWarnings("unchecked")
    private void fillPlaceholders(Component component) {
        if (component instanceof HasListDataView && !(component instanceof Grid)) {
            ((HasListDataView<Object, ?>) component).setItems(List.of("Item 1", "Item 2", "Item 3"));
        } else if (component instanceof HasMenuItemsEnhanced menuItems) {
            PreviewActionSupport.addPlaceholderItems((id, text) -> menuItems.addItem(text));
        }
    }

    private static Optional<String> inlineContent(Element element) {
        return element.elements().stream()
                .filter(child -> StudioXmlElements.CONTENT.equals(child.getName()))
                .findFirst()
                .map(child -> child.getText().trim());
    }

    private boolean isFragment(Element element) {
        return hasViewOrFragmentSchema(element)
                && StudioXmlElements.FRAGMENT.equals(element.getName());
    }

    private Component loadFragment(Element fragment) {
        if (FRAGMENT_SCHEMA.equals(fragment.getNamespaceURI())) {
            return new VerticalLayout();
        } else {
            return new Image("icons/studio-fragment-preview.svg", "FRAGMENT");
        }
    }

    private boolean isUserMenu(Element element) {
        return hasViewOrFragmentSchema(element)
                && StudioXmlElements.USER_MENU.equals(element.getName());
    }

    private Component loadUserMenu(Element userMenuElement, Element viewElement, StudioPreviewEnvironment environment) {
        JmixUserMenu<String> userMenu = new JmixUserMenu<>();
        userMenu.setUser("admin");

        Element itemsElement = userMenuElement.element(StudioXmlElements.ITEMS);
        if (hasRenderableItem(itemsElement)) {
            loadItems(userMenu, itemsElement, viewElement, environment, true);
        } else {
            PreviewActionSupport.addPlaceholderItems(userMenu::addTextItem);
        }

        userMenu.setButtonRenderer(user -> {
            Div wrapper = new Div();
            wrapper.setClassName(BUTTON_CONTENT_CLASS_NAME);

            Avatar avatar = new Avatar();
            avatar.setName(user);
            avatar.getElement().setAttribute("tabindex", "-1");
            avatar.setClassName(BUTTON_CONTENT_CLASS_NAME + "-user-avatar");

            Span name = new Span();
            name.setText(user);
            name.setClassName(BUTTON_CONTENT_CLASS_NAME + "-user-name");

            wrapper.add(avatar, name);
            return wrapper;
        });

        return userMenu;
    }

    private boolean hasRenderableItem(@Nullable Element itemsElement) {
        if (itemsElement == null) {
            return false;
        }
        for (Element child : itemsElement.elements()) {
            if (RENDERABLE_ITEM_NAMES.contains(child.getName())) {
                return true;
            }
        }
        return false;
    }

    private <M extends HasTextMenuItems & HasActionMenuItems> void loadItems(
            M menu, Element itemsElement, Element viewElement, StudioPreviewEnvironment environment,
            boolean nestingAllowed) {
        for (Element childElement : itemsElement.elements()) {
            switch (childElement.getName()) {
                case StudioXmlElements.TEXT_ITEM ->
                        loadTextItem(menu, childElement, viewElement, environment, nestingAllowed);
                case StudioXmlElements.ACTION_ITEM -> loadActionItem(menu, childElement, viewElement, environment);
                case StudioXmlElements.VIEW_ITEM -> loadViewItem(menu, childElement, environment);
                case StudioXmlElements.SEPARATOR -> menu.addSeparator();
                case StudioXmlElements.COMPONENT_ITEM -> {

                }
                default -> {
                    // unknown items child: skipped silently in preview
                }
            }
        }
    }

    private <M extends HasTextMenuItems & HasActionMenuItems> void loadTextItem(
            M menu, Element itemElement, Element viewElement, StudioPreviewEnvironment environment,
            boolean nestingAllowed) {
        String id = loadString(itemElement, "id").orElse(null);
        if (id == null) {
            // Runtime throws without an id, preview skips silently.
            return;
        }

        String text = loadString(itemElement, "text")
                .map(value -> PreviewActionSupport.resolveText(environment, value))
                .orElse(null);
        if (text == null) {
            // Runtime throws without resolvable text; JmixUserMenu#addTextItem also requires
            // non-null text (unlike dropdownButton's items), so preview skips silently too.
            return;
        }

        TextUserMenuItem item = BaseComponentLoaderSupport.loadIconSetIcon(itemElement)
                .<TextUserMenuItem>map(icon -> menu.addTextItem(id, text, icon))
                .orElseGet(() -> menu.addTextItem(id, text));

        if (nestingAllowed) {
            Element nestedItemsElement = itemElement.element(StudioXmlElements.ITEMS);
            if (hasRenderableItem(nestedItemsElement)) {
                loadItems(item.getSubMenu(), nestedItemsElement, viewElement, environment, false);
            }
        }
    }

    private <M extends HasTextMenuItems & HasActionMenuItems> void loadActionItem(
            M menu, Element itemElement, Element viewElement, StudioPreviewEnvironment environment) {
        PreviewActionSupport.loadActionItem(itemElement, viewElement, environment,
                menu::addActionItem, menu::addTextItem);
    }

    private <M extends HasTextMenuItems> void loadViewItem(M menu, Element itemElement,
                                                           StudioPreviewEnvironment environment) {
        String id = loadString(itemElement, "id").orElse(null);
        if (id == null) {
            // Runtime throws without an id, preview skips silently.
            return;
        }

        String text = loadString(itemElement, "text")
                .map(value -> PreviewActionSupport.resolveText(environment, value))
                .or(() -> loadString(itemElement, "viewId"))
                .orElse(id);

        BaseComponentLoaderSupport.loadIconSetIcon(itemElement)
                .ifPresentOrElse(
                        icon -> menu.addTextItem(id, text, icon),
                        () -> menu.addTextItem(id, text));
    }
}
