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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.component.slider.DecimalRangeSlider;
import com.vaadin.flow.component.slider.DecimalRangeSliderValue;
import com.vaadin.flow.component.slider.DecimalSlider;
import com.vaadin.flow.component.slider.IntegerRangeSlider;
import com.vaadin.flow.component.slider.IntegerRangeSliderValue;
import com.vaadin.flow.component.slider.IntegerSlider;
import com.vaadin.flow.component.icon.FontIcon;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.login.LoginOverlay;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
import com.vaadin.flow.component.textfield.TextFieldBase;
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
import io.jmix.flowui.kit.component.upload.AbstractSingleUploadField;
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
import io.jmix.flowui.kit.xml.layout.support.ComponentLoaderUtils;
import io.jmix.flowui.kit.xml.layout.support.LoaderUtils;
import org.jspecify.annotations.Nullable;
import org.dom4j.Element;

import static io.jmix.flowui.kit.component.usermenu.JmixUserMenu.BUTTON_CONTENT_CLASS_NAME;

final class StudioStandardComponentsPreviewLoader implements StudioPreviewComponentLoader {

    /** Item tags the preview can actually render; used to decide XML items vs. fallback placeholders. */
    private static final Set<String> RENDERABLE_ITEM_NAMES = Set.of(
            StudioXmlElements.TEXT_ITEM, StudioXmlElements.ACTION_ITEM,
            StudioXmlElements.VIEW_ITEM, StudioXmlElements.SEPARATOR);

    private static final String COMBO_BOX_PICKER = "comboBoxPicker";

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
            // `content` is an element, not a component, but two of its four meanings (fragment, card)
            // hold real children, so it needs a visible container - the `Div` Studio's meta declares.
            Map.entry(StudioXmlElements.CONTENT, Div::new),
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
            Map.entry(StudioXmlElements.INTEGER_SLIDER, IntegerSlider::new),
            Map.entry(StudioXmlElements.DECIMAL_SLIDER, DecimalSlider::new),
            Map.entry(StudioXmlElements.INTEGER_RANGE_SLIDER, IntegerRangeSlider::new),
            Map.entry(StudioXmlElements.DECIMAL_RANGE_SLIDER, DecimalRangeSlider::new),
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
            Map.entry(COMBO_BOX_PICKER, ComboBoxPicker::new),

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
    private static final Map<String, BiFunction<Element, StudioPreviewEnvironment, Component>> SPECIALS = Map.of(
            // Standard icon sets load directly; project custom icon sets only exist as frontend
            // sources, so their SVG comes through the environment.
            StudioXmlElements.ICON, (element, environment) ->
                    ComponentLoaderUtils.loadIconSetIcon(element)
                            .<Component>map(icon -> icon)
                            .orElseGet(() -> LoaderUtils.loadString(element, "icon")
                                    .map(environment::resolveIconSvg)
                                    .<Component>map(SvgIcon::new)
                                    .orElseGet(Icon::new)),
            // Both source attributes are named as the runtime SvgIconLoader reads them; there is no
            // "src" attribute in layout.xsd, so reading one would never match a valid view.
            StudioXmlElements.SVG_ICON, (element, environment) -> {
                SvgIcon svgIcon = new SvgIcon();
                LoaderUtils.loadString(element, "symbol", svgIcon::setSymbol);
                LoaderUtils.loadString(element, "resource")
                        .map(resource -> resolveResource(resource, environment))
                        .ifPresent(svgIcon::setSrc);
                return svgIcon;
            },
            StudioXmlElements.FONT_ICON, (element, environment) -> new FontIcon(),
            StudioXmlElements.MARKDOWN, (element, environment) -> new Markdown(inlineContent(element)
                    .or(() -> LoaderUtils.loadString(element, StudioXmlElements.CONTENT))
                    .orElse("")),
            // "resource" is the attribute the runtime ImageLoader maps onto setSrc. Absolute URLs are
            // applied as-is; classpath/theme resources are resolved to data URLs by the environment.
            // Unlike svgIcon, an unresolvable classpath resource is NOT applied as a raw src:
            // a broken-image placeholder is worse than an empty img (pre-3.1 behavior kept).
            StudioXmlElements.IMAGE, (element, environment) -> {
                Image image = new Image();
                LoaderUtils.loadString(element, "resource")
                        .map(resource -> resource.startsWith("http")
                                ? resource
                                : environment.resolveStaticResource(resource))
                        .ifPresent(image::setSrc);
                LoaderUtils.loadString(element, "alternateText").ifPresent(image::setAlt);
                return image;
            }
    );

    /**
     * A {@code resource} attribute value as a src: absolute URLs as-is, classpath/theme paths
     * resolved to a data URL by the environment, the raw path when it can't (keeps the pre-3.1
     * svgIcon behavior under {@link StudioPreviewEnvironment#NOOP}).
     */
    private static String resolveResource(String resource, StudioPreviewEnvironment environment) {
        if (resource.startsWith("http")) {
            return resource;
        }
        String resolved = environment.resolveStaticResource(resource);
        return resolved != null ? resolved : resource;
    }

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
        return isFragment(element) || isGenericComponent(element)
                || isUserMenu(element) || isFactoryElement(element);
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
            return loadFragment(componentElement, environment);
        } else if (isGenericComponent(componentElement)) {
            return loadGenericComponent(componentElement);
        } else if (isUserMenu(componentElement)) {
            return loadUserMenu(componentElement, viewElement, environment);
        } else if (isFactoryElement(componentElement)) {
            return loadFactoryComponent(componentElement, viewElement, environment);
        } else {
            return null;
        }
    }

    private boolean isFactoryElement(Element element) {
        return hasViewOrFragmentSchema(element)
                && (FACTORIES.containsKey(element.getName()) || SPECIALS.containsKey(element.getName()));
    }

    private Component loadFactoryComponent(Element componentElement, Element viewElement,
                                           StudioPreviewEnvironment environment) {
        String name = componentElement.getName();
        Component component = SPECIALS.containsKey(name)
                ? SPECIALS.get(name).apply(componentElement, environment)
                : FACTORIES.get(name).get();

        loadComponentBaseAttributes(component, componentElement);
        loadFieldAttributes(component, componentElement, environment);
        loadFactoryComponentAttributes(name, component, componentElement, viewElement, environment);
        if (environment != StudioPreviewEnvironment.NOOP && !hasComponentChildren(componentElement)) {
            fillPlaceholders(component, componentElement, environment);
        }
        return component;
    }

    /** Sub-elements that are settings, not children, and so don't disable placeholder items. */
    private static final Set<String> NON_COMPONENT_CHILD_NAMES = Set.of(
            "tooltip", "prefix", "suffix", "actions", "validators", "formatter", "itemsQuery");

    private boolean hasComponentChildren(Element element) {
        return element.elements().stream()
                .anyMatch(child -> !NON_COMPONENT_CHILD_NAMES.contains(child.getName()));
    }

    private void loadFactoryComponentAttributes(String name, Component component, Element element,
                                                Element viewElement, StudioPreviewEnvironment environment) {
        switch (name) {
            case StudioXmlElements.BUTTON ->
                    loadButtonAttributes((JmixButton) component, element, viewElement, environment);
            case StudioXmlElements.SPAN ->
                    loadLocalizedString(element, "text", environment, ((Span) component)::setText);
            case StudioXmlElements.TEXT_FIELD ->
                    loadStringFieldAttributes((TextField) component, element, environment);
            case StudioXmlElements.EMAIL_FIELD ->
                    loadStringFieldAttributes((EmailField) component, element, environment);
            case StudioXmlElements.PASSWORD_FIELD ->
                    loadPasswordFieldAttributes((PasswordField) component, element, environment);
            case StudioXmlElements.TEXT_AREA ->
                    loadStringFieldAttributes((TextArea) component, element, environment);
            case StudioXmlElements.TIME_PICKER ->
                    loadTimePickerAttributes((TimePicker) component, element);
            case StudioXmlElements.SCROLLER ->
                    loadEnum(element, Scroller.ScrollDirection.class, "scrollBarsDirection",
                            ((Scroller) component)::setScrollDirection);
            case StudioXmlElements.FLEX_LAYOUT -> loadFlexLayoutAttributes((FlexLayout) component, element);
            case StudioXmlElements.CHECKBOX -> loadCheckboxAttributes((Checkbox) component, element);
            // Tab is not HasLabel: its label is the element text
            case StudioXmlElements.TAB ->
                    loadLocalizedString(element, "label", environment, ((Tab) component)::setLabel);
            case StudioXmlElements.DETAILS, StudioXmlElements.ACCORDION_PANEL ->
                    loadDetailsAttributes((Details) component, element, environment);
            case StudioXmlElements.CARD -> loadCardAttributes((Card) component, element, environment);
            case StudioXmlElements.FORM_ITEM ->
                    loadLocalizedString(element, "label", environment,
                            label -> SlotUtils.addToSlot((FormLayout.FormItem) component,
                                    "label", new NativeLabel(label)));
            case StudioXmlElements.PROGRESS_BAR -> loadProgressBarAttributes((ProgressBar) component, element);
            case StudioXmlElements.NUMBER_FIELD -> loadNumberFieldAttributes((NumberField) component, element);
            case StudioXmlElements.INTEGER_FIELD -> loadIntegerFieldAttributes((IntegerField) component, element);
            case StudioXmlElements.INTEGER_SLIDER -> loadIntegerSliderAttributes((IntegerSlider) component,
                    element);
            case StudioXmlElements.DECIMAL_SLIDER -> loadDecimalSliderAttributes((DecimalSlider) component,
                    element);
            case StudioXmlElements.INTEGER_RANGE_SLIDER ->
                    loadIntegerRangeSliderAttributes((IntegerRangeSlider) component, element, environment);
            case StudioXmlElements.DECIMAL_RANGE_SLIDER ->
                    loadDecimalRangeSliderAttributes((DecimalRangeSlider) component, element, environment);
            case StudioXmlElements.BIG_DECIMAL_FIELD ->
                    loadString(element, "value").ifPresent(value ->
                            parse(value, BigDecimal::new, ((BigDecimalField) component)::setValue));
            case StudioXmlElements.DATE_PICKER -> loadDatePickerAttributes((DatePicker) component, element);
            case StudioXmlElements.DATE_TIME_PICKER ->
                    loadDateTimePickerAttributes((DateTimePicker) component, element);
            case StudioXmlElements.COMBO_BOX, StudioXmlElements.MULTI_SELECT_COMBO_BOX,
                 StudioXmlElements.MULTI_SELECT_COMBO_BOX_PICKER, StudioXmlElements.ENTITY_COMBO_BOX,
                 COMBO_BOX_PICKER -> loadComboBoxBaseAttributes((ComboBoxBase<?, ?, ?>) component, element);
            case StudioXmlElements.TWIN_COLUMN -> loadTwinColumnAttributes((JmixTwinColumn<?>) component,
                    element, environment);
            case StudioXmlElements.FILE_UPLOAD_FIELD, StudioXmlElements.FILE_STORAGE_UPLOAD_FIELD ->
                    loadLocalizedString(element, "uploadText", environment,
                            ((AbstractSingleUploadField<?, ?, ?>) component)::setUploadText);
            case StudioXmlElements.GENERIC_FILTER ->
                    loadGenericFilterAttributes((Details) component, element, environment);
            case StudioXmlElements.PROPERTY_FILTER, StudioXmlElements.JPQL_FILTER ->
                    loadSingleFilterAttributes((TextField) component, element);
            case StudioXmlElements.GROUP_FILTER -> loadGroupFilterAttributes((VerticalLayout) component, element);
            case StudioXmlElements.AVATAR -> loadAvatarAttributes((Avatar) component, element, environment);
            // The runtime shows the logged-in user; the preview has none, so show a placeholder
            case StudioXmlElements.USER_INDICATOR ->
                    component.getElement().appendChild(new Span("[admin]").getElement());
            // The runtime shows the loaded row count here
            case StudioXmlElements.SIMPLE_PAGINATION ->
                    findDescendant(component, child -> child instanceof Span span
                            && span.hasClassName(JmixSimplePagination.STATUS_SPAN_CLASS_NAME))
                            .ifPresent(span -> ((Span) span).setText("0 rows"));
            default -> {
                // No additional attributes are loaded for this component yet.
            }
        }
    }

    private void loadButtonAttributes(JmixButton button, Element element, Element viewElement,
                                      StudioPreviewEnvironment environment) {
        loadLocalizedString(element, "text", environment, button::setText);
        loadLocalizedString(element, "title", environment, button::setTitle);
        loadBoolean(element, "autofocus", button::setAutofocus);
        loadBoolean(element, "iconAfterText", button::setIconAfterText);
        loadBoolean(element, "disableOnClick", button::setDisableOnClick);
        ComponentLoaderUtils.loadWhiteSpace(button, element);
        ComponentLoaderUtils.loadIconSetIcon(element).ifPresent(button::setIcon);
        loadString(element, "action").ifPresent(actionRef ->
                PreviewActionSupport.applyButtonAction(button, actionRef, viewElement, environment));
    }

    private void loadStringFieldAttributes(TextFieldBase<?, String> field, Element element,
                                           StudioPreviewEnvironment environment) {
        // label/placeholder/helperText/readOnly/required come from the shared loadFieldAttributes pass
        loadLocalizedString(element, "title", environment, field::setTitle);
        loadString(element, "value", field::setValue);
        loadBoolean(element, "clearButtonVisible", field::setClearButtonVisible);
        loadBoolean(element, "autofocus", field::setAutofocus);
        loadBoolean(element, "autoselect", field::setAutoselect);
        ComponentLoaderUtils.loadValueChangeMode(field, element);
        ComponentLoaderUtils.loadAutocomplete(field, element);
        ComponentLoaderUtils.loadAutocapitalize(field, element);
        ComponentLoaderUtils.loadAutocorrect(field, element);

        if (field instanceof TextField textField) {
            loadTextLengthAndPattern(element,
                    textField::setMaxLength, textField::setMinLength, textField::setPattern);
        } else if (field instanceof EmailField emailField) {
            loadTextLengthAndPattern(element,
                    emailField::setMaxLength, emailField::setMinLength, emailField::setPattern);
        } else if (field instanceof PasswordField passwordField) {
            loadTextLengthAndPattern(element,
                    passwordField::setMaxLength, passwordField::setMinLength, passwordField::setPattern);
        } else if (field instanceof TextArea textArea) {
            loadTextLengthAndPattern(element,
                    textArea::setMaxLength, textArea::setMinLength, textArea::setPattern);
        }
    }

    private void loadPasswordFieldAttributes(PasswordField field, Element element,
                                             StudioPreviewEnvironment environment) {
        loadStringFieldAttributes(field, element, environment);
        loadBoolean(element, "revealButtonVisible", field::setRevealButtonVisible);
    }

    private void loadTextLengthAndPattern(Element element, Consumer<Integer> maxLengthSetter,
                                          Consumer<Integer> minLengthSetter, Consumer<String> patternSetter) {
        loadInteger(element, "maxLength", maxLengthSetter);
        loadInteger(element, "minLength", minLengthSetter);
        loadString(element, "pattern", patternSetter);
    }

    private void loadTimePickerAttributes(TimePicker timePicker, Element element) {
        loadBoolean(element, "clearButtonVisible", timePicker::setClearButtonVisible);
        loadBoolean(element, "autoOpen", timePicker::setAutoOpen);
        ComponentLoaderUtils.loadDuration(element, "step").ifPresent(timePicker::setStep);
    }

    private void loadCheckboxAttributes(Checkbox checkbox, Element element) {
        loadBoolean(element, "value", checkbox::setValue);
        loadBoolean(element, "indeterminate", checkbox::setIndeterminate);
    }

    private void loadDetailsAttributes(Details details, Element element, StudioPreviewEnvironment environment) {
        loadLocalizedString(element, "summaryText", environment, details::setSummaryText);
        loadBoolean(element, "opened", details::setOpened);
    }

    private void loadCardAttributes(Card card, Element element, StudioPreviewEnvironment environment) {
        loadLocalizedString(element, "title", environment, card::setTitle);
        loadLocalizedString(element, "subtitle", environment, card::setSubtitle);
        loadInteger(element, "titleHeadingLevel", card::setTitleHeadingLevel);
    }

    private void loadProgressBarAttributes(ProgressBar progressBar, Element element) {
        loadDouble(element, "min", progressBar::setMin);
        loadDouble(element, "max", progressBar::setMax);
        loadDouble(element, "value", progressBar::setValue);
        loadBoolean(element, "indeterminate", progressBar::setIndeterminate);
    }

    private void loadNumberFieldAttributes(NumberField field, Element element) {
        loadDouble(element, "value", field::setValue);
        loadDouble(element, "min", field::setMin);
        loadDouble(element, "max", field::setMax);
        loadDouble(element, "step", field::setStep);
        loadBoolean(element, "stepButtonsVisible", field::setStepButtonsVisible);
    }

    private void loadIntegerFieldAttributes(IntegerField field, Element element) {
        loadInteger(element, "value", field::setValue);
        loadInteger(element, "min", field::setMin);
        loadInteger(element, "max", field::setMax);
        loadInteger(element, "step", field::setStep);
        loadBoolean(element, "stepButtonsVisible", field::setStepButtonsVisible);
    }

    private void loadIntegerSliderAttributes(IntegerSlider slider, Element element) {
        loadInteger(element, "min", slider::setMin);
        loadInteger(element, "max", slider::setMax);
        loadInteger(element, "step").filter(step -> step > 0).ifPresent(slider::setStep);
        loadInteger(element, "value", slider::setValue);
        loadSliderAppearanceAttributes(element, slider::setValueAlwaysVisible, slider::setMinMaxVisible);
    }

    private void loadDecimalSliderAttributes(DecimalSlider slider, Element element) {
        loadDouble(element, "min", slider::setMin);
        loadDouble(element, "max", slider::setMax);
        loadDouble(element, "step").filter(step -> step > 0).ifPresent(slider::setStep);
        loadDouble(element, "value", slider::setValue);
        loadSliderAppearanceAttributes(element, slider::setValueAlwaysVisible, slider::setMinMaxVisible);
    }

    private void loadIntegerRangeSliderAttributes(IntegerRangeSlider slider, Element element,
                                                  StudioPreviewEnvironment environment) {
        loadInteger(element, "min", slider::setMin);
        loadInteger(element, "max", slider::setMax);
        loadInteger(element, "step").filter(step -> step > 0).ifPresent(slider::setStep);

        Optional<Integer> startValue = loadInteger(element, "startValue");
        Optional<Integer> endValue = loadInteger(element, "endValue");
        if (startValue.isPresent() || endValue.isPresent()) {
            int start = startValue.orElseGet(slider::getMin);
            int end = endValue.orElseGet(slider::getMax);
            if (start <= end) {
                slider.setValue(new IntegerRangeSliderValue(start, end));
            }
        }

        loadSliderAppearanceAttributes(element, slider::setValueAlwaysVisible, slider::setMinMaxVisible);
        loadRangeSliderAccessibleNames(element, environment, slider::setAccessibleNameStart,
                slider::setAccessibleNameEnd);
    }

    private void loadDecimalRangeSliderAttributes(DecimalRangeSlider slider, Element element,
                                                  StudioPreviewEnvironment environment) {
        loadDouble(element, "min", slider::setMin);
        loadDouble(element, "max", slider::setMax);
        loadDouble(element, "step").filter(step -> step > 0).ifPresent(slider::setStep);

        Optional<Double> startValue = loadDouble(element, "startValue");
        Optional<Double> endValue = loadDouble(element, "endValue");
        if (startValue.isPresent() || endValue.isPresent()) {
            double start = startValue.orElseGet(slider::getMin);
            double end = endValue.orElseGet(slider::getMax);
            if (start <= end) {
                slider.setValue(new DecimalRangeSliderValue(start, end));
            }
        }

        loadSliderAppearanceAttributes(element, slider::setValueAlwaysVisible, slider::setMinMaxVisible);
        loadRangeSliderAccessibleNames(element, environment, slider::setAccessibleNameStart,
                slider::setAccessibleNameEnd);
    }

    private void loadSliderAppearanceAttributes(Element element, Consumer<Boolean> valueAlwaysVisibleSetter,
                                                Consumer<Boolean> minMaxVisibleSetter) {
        loadBoolean(element, "valueAlwaysVisible", valueAlwaysVisibleSetter);
        loadBoolean(element, "minMaxVisible", minMaxVisibleSetter);
    }

    private void loadRangeSliderAccessibleNames(Element element, StudioPreviewEnvironment environment,
                                                Consumer<String> startSetter, Consumer<String> endSetter) {
        loadLocalizedString(element, "accessibleNameStart", environment, startSetter);
        loadLocalizedString(element, "accessibleNameEnd", environment, endSetter);
    }

    private void loadDatePickerAttributes(DatePicker datePicker, Element element) {
        loadBoolean(element, "clearButtonVisible", datePicker::setClearButtonVisible);
        loadBoolean(element, "autoOpen", datePicker::setAutoOpen);
        loadBoolean(element, "weekNumbersVisible", datePicker::setWeekNumbersVisible);
        loadString(element, "min").ifPresent(min -> parse(min, LocalDate::parse, datePicker::setMin));
        loadString(element, "max").ifPresent(max -> parse(max, LocalDate::parse, datePicker::setMax));
    }

    private void loadDateTimePickerAttributes(DateTimePicker dateTimePicker, Element element) {
        loadBoolean(element, "autoOpen", dateTimePicker::setAutoOpen);
        loadBoolean(element, "weekNumbersVisible", dateTimePicker::setWeekNumbersVisible);
        loadString(element, "min").ifPresent(min -> parse(min, LocalDateTime::parse, dateTimePicker::setMin));
        loadString(element, "max").ifPresent(max -> parse(max, LocalDateTime::parse, dateTimePicker::setMax));
    }

    private void loadComboBoxBaseAttributes(ComboBoxBase<?, ?, ?> comboBox, Element element) {
        loadBoolean(element, "allowCustomValue", comboBox::setAllowCustomValue);
        loadBoolean(element, "autoOpen", comboBox::setAutoOpen);
        loadBoolean(element, "clearButtonVisible", comboBox::setClearButtonVisible);
        loadInteger(element, "pageSize", comboBox::setPageSize);
    }

    private void loadTwinColumnAttributes(JmixTwinColumn<?> twinColumn, Element element,
                                          StudioPreviewEnvironment environment) {
        loadLocalizedString(element, "itemsColumnLabel", environment, twinColumn::setItemsColumnLabel);
        loadLocalizedString(element, "selectedItemsColumnLabel", environment,
                twinColumn::setSelectedItemsColumnLabel);
    }

    private void loadAvatarAttributes(Avatar avatar, Element element, StudioPreviewEnvironment environment) {
        loadLocalizedString(element, "name", environment, avatar::setName);
        loadString(element, "abbreviation", avatar::setAbbreviation);
        loadInteger(element, "colorIndex", avatar::setColorIndex);
    }

    /**
     * The runtime GenericFilter is a Details with a control header and condition rows.
     * The preview approximates the header so the component doesn't collapse to a bare chevron.
     */
    private void loadGenericFilterAttributes(Details details, Element element, StudioPreviewEnvironment environment) {
        loadLocalizedString(element, "summaryText", environment, details::setSummaryText);
        if (details.getSummaryText() == null || details.getSummaryText().isEmpty()) {
            details.setSummaryText("Filter");
        }
        details.setOpened(loadBoolean(element, "opened").orElse(true));

        JmixButton refresh = new JmixButton();
        refresh.setText("Refresh");
        refresh.setIcon(new Icon("lumo", "reload"));

        JmixButton settings = new JmixButton();
        settings.setIcon(new Icon("lumo", "cog"));

        JmixButton addCondition = new JmixButton();
        addCondition.setText("Add search condition");
        addCondition.getThemeNames().add("tertiary-inline");

        HorizontalLayout header = new HorizontalLayout(refresh, settings, addCondition);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        details.add(header);
    }

    /**
     * The runtime propertyFilter/jpqlFilter render label + operation text + value field.
     * The preview shows the operation as a prefix inside the value field.
     */
    private void loadSingleFilterAttributes(TextField field, Element element) {
        if (loadBoolean(element, "operationTextVisible").orElse(true)) {
            loadString(element, "operation").ifPresent(operation -> {
                Span operationSpan = new Span(operationCaption(operation));
                operationSpan.getStyle().set("color", "var(--lumo-secondary-text-color)");
                field.setPrefixComponent(operationSpan);
            });
        }
    }

    private void loadGroupFilterAttributes(VerticalLayout layout, Element element) {
        loadString(element, "operation").ifPresent(operation ->
                layout.add(new Span(operationCaption(operation))));
    }

    private String operationCaption(String operation) {
        return switch (operation) {
            case "EQUAL" -> "=";
            case "NOT_EQUAL" -> "<>";
            case "GREATER" -> ">";
            case "GREATER_OR_EQUAL" -> ">=";
            case "LESS" -> "<";
            case "LESS_OR_EQUAL" -> "<=";
            case "AND" -> "And";
            case "OR" -> "Or";
            default -> operation.toLowerCase(java.util.Locale.ROOT).replace('_', ' ');
        };
    }

    /** First component in the subtree (depth-first, self excluded) matching the predicate. */
    private static Optional<Component> findDescendant(Component root,
                                                      java.util.function.Predicate<Component> predicate) {
        return root.getChildren()
                .flatMap(child -> predicate.test(child)
                        ? java.util.stream.Stream.of(child)
                        : findDescendant(child, predicate).stream())
                .findFirst();
    }

    /** Applies {@code parser}'s result, silently skipping values the preview cannot parse. */
    private static <T> void parse(String value, Function<String, T> parser, Consumer<T> setter) {
        try {
            setter.accept(parser.apply(value));
        } catch (RuntimeException ignored) {
            // malformed design-time value: the preview shows the component without it
        }
    }

    private void loadFlexLayoutAttributes(FlexLayout layout, Element element) {
        loadEnum(element, FlexLayout.ContentAlignment.class, "contentAlignment", layout::setAlignContent);
        loadEnum(element, FlexLayout.FlexDirection.class, "flexDirection", layout::setFlexDirection);
        loadEnum(element, FlexLayout.FlexWrap.class, "flexWrap", layout::setFlexWrap);
    }

    @SuppressWarnings("unchecked")
    private void fillPlaceholders(Component component, Element element, StudioPreviewEnvironment environment) {
        if (component instanceof HasListDataView && !(component instanceof Grid)) {
            // enum-bound fields show the real enum constants, everything else the generic items
            List<String> enumItems = PreviewActionSupport.enumPlaceholderItems(element, environment);
            // List<Object>: a List<String> would match the setItems(T...) overload as a single item
            List<Object> items = enumItems.isEmpty()
                    ? List.of("Item 1", "Item 2", "Item 3")
                    : List.copyOf(enumItems);
            ((HasListDataView<Object, ?>) component).setItems(items);
        } else if (component instanceof HasMenuItemsEnhanced menuItems) {
            // horizontalMenu: the real main menu when Studio can provide it, placeholders otherwise
            List<PreviewActionSupport.MenuEntry> menuEntries =
                    PreviewActionSupport.parseMenuItems(environment.mainMenuItems());
            if (menuEntries.isEmpty()) {
                PreviewActionSupport.addPlaceholderItems((id, text) -> menuItems.addItem(text));
            } else {
                menuEntries.forEach(entry -> menuItems.addItem(entry.title()));
            }
        } else if (component instanceof ListMenu listMenu) {
            List<PreviewActionSupport.MenuEntry> menuEntries =
                    PreviewActionSupport.parseMenuItems(environment.mainMenuItems());
            if (menuEntries.isEmpty()) {
                PreviewActionSupport.addPlaceholderItems((id, text) ->
                        listMenu.addMenuItem(new ListMenu.MenuItem(id).withTitle(text)));
            } else {
                menuEntries.forEach(entry -> listMenu.addMenuItem(toListMenuItem(entry)));
            }
        }
    }

    private ListMenu.MenuItem toListMenuItem(PreviewActionSupport.MenuEntry entry) {
        if (entry.items().isEmpty()) {
            return new ListMenu.MenuItem(entry.id()).withTitle(entry.title());
        }
        ListMenu.MenuBarItem menuBar = ListMenu.MenuItem.createMenuBar(entry.id());
        menuBar.withTitle(entry.title());
        entry.items().forEach(child -> menuBar.addChildItem(toListMenuItem(child)));
        return menuBar;
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

    private Component loadFragment(Element fragment, StudioPreviewEnvironment environment) {
        Component component;
        if (FRAGMENT_SCHEMA.equals(fragment.getNamespaceURI())) {
            component = new VerticalLayout();
        } else {
            // a <fragment class=…> used inside a view: render the real fragment content when
            // Studio can hand over its descriptor, a placeholder image otherwise
            component = loadString(fragment, "class")
                    .map(environment::resolveFragmentDescriptor)
                    .map(descriptorXml ->
                            StudioPreviewSubtreeBuilder.buildFragmentContent(descriptorXml, environment))
                    .orElseGet(() -> new Image("icons/studio-fragment-preview.svg", "FRAGMENT"));
        }
        loadComponentBaseAttributes(component, fragment);
        return component;
    }

    private boolean isGenericComponent(Element element) {
        return hasViewOrFragmentSchema(element)
                && StudioXmlElements.COMPONENT.equals(element.getName());
    }

    /**
     * The generic {@code <component class=…>} names an arbitrary application class the spring-free kit
     * cannot instantiate, so it previews as a labelled placeholder - the same treatment
     * {@link #loadFragment} gives a fragment declared outside a fragment descriptor.
     */
    private Component loadGenericComponent(Element element) {
        Image component = new Image("icons/studio-generic-component-preview.svg", "COMPONENT");
        loadComponentBaseAttributes(component, element);
        return component;
    }

    private boolean isUserMenu(Element element) {
        return hasViewOrFragmentSchema(element)
                && StudioXmlElements.USER_MENU.equals(element.getName());
    }

    private Component loadUserMenu(Element userMenuElement, Element viewElement, StudioPreviewEnvironment environment) {
        JmixUserMenu<String> userMenu = new JmixUserMenu<>();
        loadComponentBaseAttributes(userMenu, userMenuElement);
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
            // the runtime formats a user without a display name as "[login]"
            name.setText("[" + user + "]");
            name.setClassName(BUTTON_CONTENT_CLASS_NAME + "-user-name");

            wrapper.add(avatar, name);
            return wrapper;
        });

        return userMenu;
    }

    private boolean hasRenderableItem(@Nullable Element itemsElement) {
        return PreviewActionSupport.hasRenderableItem(itemsElement, RENDERABLE_ITEM_NAMES);
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

        TextUserMenuItem item = ComponentLoaderUtils.loadIconSetIcon(itemElement)
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
                .or(() -> loadString(itemElement, "viewClass").map(environment::viewTitle))
                .or(() -> loadString(itemElement, "viewId").map(viewId ->
                        Optional.ofNullable(environment.viewTitle(viewId)).orElse(viewId)))
                .orElse(id);

        ComponentLoaderUtils.loadIconSetIcon(itemElement)
                .ifPresentOrElse(
                        icon -> menu.addTextItem(id, text, icon),
                        () -> menu.addTextItem(id, text));
    }
}
