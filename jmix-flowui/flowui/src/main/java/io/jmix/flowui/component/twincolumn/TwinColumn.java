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

package io.jmix.flowui.component.twincolumn;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.data.binder.HasItemComponents;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import elemental.json.JsonValue;
import io.jmix.core.Messages;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.component.listbox.JmixMultiSelectListBox;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.*;
import io.jmix.flowui.data.items.InMemoryDataProviderWrapper;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

@Tag("jmix-twin-column")
public class TwinColumn<V> extends AbstractField<TwinColumn<V>, Collection<V>>
            implements HasSize, HasHelper, HasAriaLabel, HasLabel, HasRequired, HasTheme,
                       HasDataView<V, Void, TwinColumnDataView<V>>, HasListDataView<V, TwinColumnListDataView<V>>,
                       HasItemComponents<V>, HasSubParts, HasThemeVariant<TwinColumnVariant>,
                       SupportsItemsContainer<V>, SupportsValueSource<Collection<V>>, SupportsItemsEnum<V>,
                       SupportsDataProvider<V>, SupportsValidation<Collection<V>>,
                       ApplicationContextAware, InitializingBean {

    private static final String LIST_BOX_DEFAULT_HEIGHT = "16em";
    private static final String COMPONENT_DEFAULT_WIDTH = "32em";
    private static final String LIST_BOX_MIN_WIDTH = "12em";
    private static final String LIST_BOX_MIN_HEIGHT = "14.6em";
    private static final String JS_SCROLL_TOP_VARIABLE = "this._scrollerElement.scrollTop";

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;
    protected Messages messages;

    protected TwinColumnDelegate<TwinColumn<V>, Collection<V>, Collection<V>> fieldDelegate;
    protected DataViewDelegate<TwinColumn<V>, V> dataViewDelegate;

    protected JmixMultiSelectListBox<V> selected;
    protected JmixMultiSelectListBox<V> options;

    protected VerticalLayout contentWrapper;
    protected VerticalLayout buttonsPanel;
    protected HorizontalLayout listBoxesLabelsLayout;

    protected ListBoxListDataView<V> optionsDataView;
    protected ListBoxListDataView<V> selectedDataView;

    protected Span componentLabel;
    protected Div optionsColumnLabel;
    protected Div selectedItemsColumnLabel;
    protected Span helperLabel;
    protected Span errorLabel;

    protected JmixButton selectItems;
    protected JmixButton selectAllItems;
    protected JmixButton deselectItems;
    protected JmixButton deselectAllItems;
    protected JmixButton clearListBoxesSelection;

    protected List<Component> subParts;
    protected List<V> optionsData;
    protected Map<Element, Integer> optionsListeners = new HashMap<>();
    protected Map<Element, Integer> selectedListeners = new HashMap<>();
    protected Map<JmixMultiSelectListBox<V>, Integer> listToScrollValue = new HashMap<>();
    protected Set<JmixMultiSelectListBox<V>> listBoxesWithSavedScrollPosition = new HashSet<>();

    protected Boolean reorderable = false;
    protected Boolean selectAllButtonsVisible = false;
    protected Boolean clearListBoxesSelectionButtonVisible = false;
    protected Boolean moveJustPerformed = false;

    //change in tests using reflection
    private final Boolean saveAndRestoreListBoxesScrollTopPosition = true;

    public TwinColumn() {
        super(null);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        autowireDependencies();
        initComponent();
    }

    @Nullable
    @Override
    public Object getSubPart(String name) {
        return subParts
                .stream()
                .filter(component -> sameId(component, name))
                .findAny()
                .orElse(null);
    }

    public void setReorderable(Boolean reorderable) {
        this.reorderable = reorderable;

        if (optionsData != null) {
            updateListBoxesReorderableComparator();
            updateListBoxesListenersAndStyles();
        }
    }

    public Boolean isReorderable() {
        return reorderable;
    }

    public void setSelectAllButtonsVisible(Boolean allBtnEnabled) {
        selectAllItems.setVisible(allBtnEnabled);
        deselectAllItems.setVisible(allBtnEnabled);
        this.selectAllButtonsVisible = allBtnEnabled;

        updateHiddenButtonsStyles();
    }

    public Boolean isSelectAllButtonsVisible() {
        return selectAllButtonsVisible;
    }

    @Override
    public void setLabel(String label) {
        HasLabel.super.setLabel(label);

        componentLabel.setVisible(!StringUtils.isEmpty(label));
        componentLabel.setText(label);
    }

    @Override
    public void setWidth(String width) {
        HasSize.super.setWidth(width);

        contentWrapper.setWidth(width);
    }

    @Override
    public void setHeight(String height) {
        HasSize.super.setHeight(height);

        contentWrapper.setHeight(height);

        if (options == null) {
            return;
        }

        if (height == null) {
            options.setHeight(LIST_BOX_DEFAULT_HEIGHT);
            selected.setHeight(LIST_BOX_DEFAULT_HEIGHT);
        } else if ("100%".equals(height)){
            options.setHeight("100%");
            selected.setHeight("100%");
        }
    }

    @Override
    public void setMinWidth(String minWidth) {
        HasSize.super.setMinWidth(minWidth);

        contentWrapper.setMinWidth(minWidth);
    }

    @Override
    public void setMinHeight(String minHeight) {
        HasSize.super.setMinHeight(minHeight);

        contentWrapper.setMinHeight(minHeight);
    }

    @Override
    public void setMaxWidth(String maxWidth) {
        HasSize.super.setMaxWidth(maxWidth);

        contentWrapper.setMaxWidth(maxWidth);
    }

    @Override
    public void setMaxHeight(String maxHeight) {
        HasSize.super.setMaxHeight(maxHeight);

        contentWrapper.setMaxHeight(maxHeight);
    }

    @Override
    public void setHelperText(String helperText) {
        HasHelper.super.setHelperText(helperText);

        helperLabel.setText(helperText);
    }

    @Override
    public void setValueSource(ValueSource<Collection<V>> valueSource) {
        fieldDelegate.setValueSource(valueSource);
    }

    @Override
    public ValueSource<Collection<V>> getValueSource() {
        return fieldDelegate.getValueSource();
    }

    @Override
    public void setItems(CollectionContainer<V> container) {
        dataViewDelegate.setItems(container);
    }

    @Override
    public void setItems(Class<V> itemsEnum) {
        dataViewDelegate.setItems(itemsEnum);
    }

    @Override
    public TwinColumnDataView<V> setItems(DataProvider<V, Void> dataProvider) {
        bindDataProvider(dataProvider);
        dataProvider.addDataProviderListener((DataProviderListener<V>) event -> {
            List<V> newOptions = event.getSource()
                    .fetch(DataViewUtils.getQuery(TwinColumn.this))
                    .toList();
            recreateOptions(newOptions);
            if (fieldDelegate != null) {
                fieldDelegate.updateInvalidState();
            }
        });
        return getGenericDataView();
    }

    @Override
    public TwinColumnDataView<V> setItems(InMemoryDataProvider<V> dataProvider) {
        InMemoryDataProviderWrapper<V> wrapper = new InMemoryDataProviderWrapper<>(dataProvider);
        return setItems(wrapper);
    }

    @Override
    public TwinColumnListDataView<V> setItems(ListDataProvider<V> dataProvider) {
        bindDataProvider(dataProvider);
        recreateOptions(dataProvider.getItems());
        if (fieldDelegate != null) {
            fieldDelegate.updateInvalidState();
        }
        return getListDataView();
    }

    @Override
    public TwinColumnListDataView<V> getListDataView() {
        return new TwinColumnListDataView<>(this::getDataProvider, this, (filter, sorting) -> {});
    }

    @Nullable
    @Override
    public DataProvider<V, ?> getDataProvider() {
        return dataViewDelegate.getDataProvider();
    }

    @Override
    public TwinColumnDataView<V> getGenericDataView() {
        return new TwinColumnDataView<>(this::getDataProvider, this);
    }

    @Override
    public Registration addValidator(Validator<? super Collection<V>> validator) {
        return fieldDelegate.addValidator(validator);
    }

    @Override
    public void executeValidators() throws ValidationException {
        fieldDelegate.executeValidators();
    }

    @Override
    public void setInvalid(boolean invalid) {
        if (fieldDelegate != null) {
            fieldDelegate.setInvalid(invalid);
            updateValidationVisibleElements(invalid, isRequired(), fieldDelegate.getErrorMessage());
        } else {
            updateValidationVisibleElements(false, false, "");
        }
    }

    @Override
    public boolean isInvalid() {
        return fieldDelegate.isInvalid();
    }

    @Nullable
    @Override
    public String getRequiredMessage() {
        return fieldDelegate.getRequiredMessage();
    }

    @Override
    public void setRequiredMessage(@Nullable String requiredMessage) {
        fieldDelegate.setRequiredMessage(requiredMessage);

        errorLabel.setText(requiredMessage);
    }

    @Override
    public void setRequired(boolean required) {
        HasRequired.super.setRequired(required);

        fieldDelegate.updateInvalidState();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);

        if (fieldDelegate != null) {
            fieldDelegate.updateInvalidState();
        }
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty() || CollectionUtils.isEmpty(getValue());
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return super.isRequiredIndicatorVisible();
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        fieldDelegate.setErrorMessage(errorMessage);

        errorLabel.setText(errorMessage);
    }

    @Override
    public String getErrorMessage() {
        return fieldDelegate.getErrorMessage();
    }

    public void setOptionsColumnLabel(String optionsColumnLabel) {
        this.optionsColumnLabel.setText(optionsColumnLabel);

        updateListBoxesLabelsLayoutVisibility();
    }

    public String getOptionsColumnLabel() {
        return optionsColumnLabel.getText();
    }

    public void setSelectedItemsColumnLabel(String selectedItemsColumnLabel) {
        this.selectedItemsColumnLabel.setText(selectedItemsColumnLabel);

        updateListBoxesLabelsLayoutVisibility();
    }

    public String getSelectedItemsColumnLabel() {
        return selectedItemsColumnLabel.getText();
    }

    public void setClearListBoxesSelectionButtonVisible(Boolean clearListBoxesSelectionButtonVisible) {
        this.clearListBoxesSelection.setVisible(clearListBoxesSelectionButtonVisible);
        this.clearListBoxesSelectionButtonVisible = clearListBoxesSelectionButtonVisible;

        updateHiddenButtonsStyles();
    }

    public Boolean isClearListBoxesSelectionButtonVisible() {
        return clearListBoxesSelectionButtonVisible;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        options.setReadOnly(readOnly);
        selected.setReadOnly(readOnly);

        fieldDelegate.updateInvalidState();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        options.setEnabled(enabled);
        selected.setEnabled(enabled);

        selectItems.setEnabled(enabled);
        selectAllItems.setEnabled(enabled);
        deselectItems.setEnabled(enabled);
        deselectAllItems.setEnabled(enabled);

        fieldDelegate.updateInvalidState();
    }

    @Override
    public void setValue(Collection<V> value) {
        super.setValue(value);

        updateListBoxesInAccordanceWithNewValue(value);
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        messages = applicationContext.getBean(Messages.class);
    }

    @Override
    protected void setPresentationValue(Collection<V> newPresentationValue) {
        updateListBoxesInAccordanceWithNewValue(newPresentationValue);
    }

    protected void initComponent() {
        createContent();

        fieldDelegate = createFieldDelegate();
        dataViewDelegate = createDataViewDelegate();

        createValueChangeListener();
    }

    protected void createValueChangeListener() {
        addValueChangeListener((ValueChangeListener<ComponentValueChangeEvent<TwinColumn<V>, Collection<V>>>) event -> {
            fieldDelegate.updateInvalidState();

            updateValidationVisibleElements(fieldDelegate.isInvalid(), isRequired(), fieldDelegate.getErrorMessage());
        });
    }

    protected void createContent() {
        setThemeName(TwinColumnVariant.GRID.getVariantName());

        initContentWrapper();
        createComponentLabel();
        createListBoxLabels();
        createListBoxes();
        createButtonsPanel();
        createListBoxesLayout();
        createHelper();
        createErrorLabel();

        updateValidationVisibleElements(false, false, "");

        createSubParts();
    }

    protected void initContentWrapper() {
        contentWrapper = uiComponents.create(VerticalLayout.class);
        add(contentWrapper);

        contentWrapper.setSpacing(false);

        if (getWidth() == null) {
            contentWrapper.setWidth(COMPONENT_DEFAULT_WIDTH);
        }
    }

    protected void createComponentLabel() {
        componentLabel = uiComponents.create(Span.class);
        componentLabel.setId("label");
        componentLabel.setClassName("label");
        componentLabel.setVisible(false);

        contentWrapper.add(componentLabel);
    }

    protected void createListBoxLabels() {
        selectedItemsColumnLabel = createListBoxLabel("selectedLabel");
        optionsColumnLabel = createListBoxLabel("optionsLabel");

        listBoxesLabelsLayout = uiComponents.create(HorizontalLayout.class);
        contentWrapper.add(listBoxesLabelsLayout);

        listBoxesLabelsLayout.setPadding(false);
        listBoxesLabelsLayout.setWidth("100%");

        JmixButton stub = uiComponents.create(JmixButton.class);
        stub.addClassName("stub");
        stub.setIcon(new Icon(VaadinIcon.THUMBS_UP));
        stub.setHeight("1px");

        listBoxesLabelsLayout.add(optionsColumnLabel);
        listBoxesLabelsLayout.add(stub);
        listBoxesLabelsLayout.add(selectedItemsColumnLabel);
    }

    protected void createListBoxes() {
        options = createListBox("options");
        selected = createListBox("selected");

        options.addSelectionListener((MultiSelectionListener<MultiSelectListBox<V>, V>) event -> {
            updateListBoxSelectionStyles(options, optionsDataView);
        });
        selected.addSelectionListener((MultiSelectionListener<MultiSelectListBox<V>, V>) event -> {
            updateListBoxSelectionStyles(selected, selectedDataView);
        });

        optionsDataView = options.setItems(new LinkedList<>());
        selectedDataView = selected.setItems(new LinkedList<>());

        updateListBoxesLabelsLayoutVisibility();
    }

    protected void createButtonsPanel() {
        buttonsPanel = uiComponents.create(VerticalLayout.class);
        buttonsPanel.setAlignItems(FlexComponent.Alignment.BASELINE);
        buttonsPanel.setClassName(LumoUtility.Padding.SMALL);
        buttonsPanel.setWidth("AUTO");
        buttonsPanel.setSpacing(false);

        selectItems = createButton(
                "selectItems",
                "button-first",
                new Icon(VaadinIcon.ANGLE_RIGHT),
                "twinColumn.selectItems",
                true,
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(options, selected, false));
        selectAllItems = createButton(
                "selectAllItems",
                "button-middle",
                new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT),
                "twinColumn.selectAllItems",
                isSelectAllButtonsVisible(),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(options, selected, true));
        deselectItems = createButton(
                "deselectItems",
                "button-middle",
                new Icon(VaadinIcon.ANGLE_LEFT),
                "twinColumn.deselectItems",
                true,
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(selected, options, false));
        deselectAllItems = createButton(
                "deselectAllItems",
                "button-middle",
                new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT),
                "twinColumn.deselectAllItems",
                isSelectAllButtonsVisible(),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(selected, options, true));
        clearListBoxesSelection = createButton(
                "clearListBoxesSelection",
                "button-last",
                new Icon(VaadinIcon.CLOSE_SMALL),
                "twinColumn.clearListBoxesSelection",
                isClearListBoxesSelectionButtonVisible(),
                (ComponentEventListener<ClickEvent<Button>>) event -> {
                    options.deselect(optionsDataView.getItems().toList());
                    selected.deselect(selectedDataView.getItems().toList());
                }
        );
        updateHiddenButtonsStyles();
        buttonsPanel.add(selectItems, selectAllItems, deselectItems, deselectAllItems, clearListBoxesSelection);
    }

    protected void createListBoxesLayout() {
        HorizontalLayout horizontalLayout = uiComponents.create(HorizontalLayout.class);
        horizontalLayout.setSpacing(false);
        horizontalLayout.setWidth("100%");
        horizontalLayout.setHeight("100%");

        horizontalLayout.add(options);
        horizontalLayout.add(buttonsPanel);
        horizontalLayout.add(selected);

        contentWrapper.add(horizontalLayout);
    }

    protected void createHelper() {
        helperLabel = uiComponents.create(Span.class);
        helperLabel.setClassName("helper");

        contentWrapper.add(helperLabel);
    }

    protected void createErrorLabel() {
        errorLabel = uiComponents.create(Span.class);
        errorLabel.setClassName("error-label");

        contentWrapper.add(errorLabel);
    }

    protected void createSubParts() {
        subParts = new LinkedList<>();

        subParts.add(componentLabel);
        subParts.add(selectedItemsColumnLabel);
        subParts.add(optionsColumnLabel);
        subParts.add(options);
        subParts.add(selected);
        subParts.add(selectItems);
        subParts.add(selectAllItems);
        subParts.add(deselectItems);
        subParts.add(deselectAllItems);
        subParts.add(clearListBoxesSelection);
    }

    protected Div createListBoxLabel(String id) {
        Div label = uiComponents.create(Div.class);
        label.setId(id);
        label.addClassName(LumoUtility.Margin.Top.AUTO);
        label.setWidth("100%");
        label.setClassName("label");
        return label;
    }

    protected JmixMultiSelectListBox<V> createListBox(String id) {
        JmixMultiSelectListBox<V> listBox = uiComponents.create(JmixMultiSelectListBox.class);
        listBox.setId(id);
        listBox.setMinWidth(LIST_BOX_MIN_WIDTH);
        listBox.setMinHeight(LIST_BOX_MIN_HEIGHT);
        listBox.setWidth("50%");
        listBox.setHeight("100%");
        return listBox;
    }

    protected JmixButton createButton(String id, String className, Icon icon, String tooltipKey, Boolean visible,
                                      ComponentEventListener<ClickEvent<Button>> clickListener) {
        JmixButton button = uiComponents.create(JmixButton.class);

        button.setId(id);
        button.setClassName(className);
        button.setIcon(icon);
        button.setTooltipText(messages.getMessage(tooltipKey));
        button.setVisible(visible);
        button.addClickListener(clickListener);

        return button;
    }

    protected void bindDataProvider(DataProvider<V, ?> dataProvider) {
        // One of binding methods is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
    }

    protected TwinColumnDelegate<TwinColumn<V>, Collection<V>, Collection<V>>  createFieldDelegate() {
        return applicationContext.getBean(TwinColumnDelegate.class, this);
    }

    protected DataViewDelegate<TwinColumn<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }

    protected void listBoxItemDoubleClicked(JmixMultiSelectListBox<V> from,
                                            JmixMultiSelectListBox<V> to,
                                            Integer itemIndex) {
        moveItems(from, to, Collections.singletonList(from.getListDataView().getItem(itemIndex)));
    }

    protected void recreateOptions(Collection<V> newOptions) {
        if (optionsData == null) {
            optionsData = new LinkedList<>();
        }

        optionsDataView.removeItems(optionsData);
        optionsData.clear();

        optionsDataView.addItems(newOptions);
        optionsData.addAll(newOptions);

        updateListBoxesReorderableComparator();
        updateListBoxesListenersAndStyles();
    }

    private void moveItems(JmixMultiSelectListBox<V> from, JmixMultiSelectListBox<V> to, boolean moveAllItems) {
        List<V> selectedItems = moveAllItems ?
                from.getListDataView().getItems().toList() :
                from.getSelectedItems().stream().toList();

        moveItems(from, to, selectedItems);
    }

    private void moveItems(JmixMultiSelectListBox<V> from, JmixMultiSelectListBox<V> to, List<V> selectedItems) {
        Runnable moveItems = () -> {
            from.select(Collections.emptySet());
            from.getListDataView().removeItems(selectedItems);

            to.getListDataView().addItems(selectedItems);
            to.select(selectedItems);

            setModelValue(selected.getListDataView().getItems().collect(Collectors.toList()),false);

            updateListBoxesListenersAndStyles();

            moveJustPerformed = true;
            listBoxesWithSavedScrollPosition.clear();

            if (saveAndRestoreListBoxesScrollTopPosition) {
                restoreScrollPosition(options);
                restoreScrollPosition(selected);
            }
        };

        if (saveAndRestoreListBoxesScrollTopPosition) {
            Consumer<JmixMultiSelectListBox<V>> moveItemsIfScrollPositionsSaved = listBox -> {
                listBoxesWithSavedScrollPosition.add(listBox);
                if (listBoxesWithSavedScrollPosition.size() == 2) {
                    moveItems.run();
                }
            };

            saveScrollPosition(options, moveItemsIfScrollPositionsSaved);
            saveScrollPosition(selected, moveItemsIfScrollPositionsSaved);
        } else {
            moveItems.run();
        }
    }

    private void saveScrollPosition(JmixMultiSelectListBox<V> listBox, Consumer<JmixMultiSelectListBox<V>> consumer) {
        if (listBox.getElement().isEnabled()) {
            listBox.getElement().executeJs("return " + JS_SCROLL_TOP_VARIABLE)
                    .then(Integer.class, (SerializableConsumer<Integer>) scrollTop -> {
                        listToScrollValue.put(listBox, scrollTop);
                        consumer.accept(listBox);
                    });
        } else {
            consumer.accept(listBox);
        }
    }

    private void restoreScrollPosition(JmixMultiSelectListBox<V> listBox) {
        if (listBox.getElement().isEnabled()) {
            listBox.getElement()
                    .executeJs(String.format(JS_SCROLL_TOP_VARIABLE + " = %s", listToScrollValue.getOrDefault(listBox, 0)))
                    .then((SerializableConsumer<JsonValue>) jsonValue -> moveJustPerformed = false);
        } else {
            moveJustPerformed = false;
        }
    }

    private void updateListBoxesListenersAndStyles() {
        updateDoubleClickListeners();
        updateListBoxSelectionStyles(options, optionsDataView);
        updateListBoxSelectionStyles(selected, selectedDataView);
    }

    private void updateListBoxesReorderableComparator() {
        SerializableComparator<V> listBoxReorderableComparator = (value1, value2) ->
                Integer.compare(optionsData.indexOf(value1), optionsData.indexOf(value2));
        optionsDataView.setSortComparator(isReorderable() ? listBoxReorderableComparator : null);
        selectedDataView.setSortComparator(isReorderable() ? listBoxReorderableComparator : null);
    }

    private void updateHiddenButtonsStyles() {
        List<Component> buttonsList = buttonsPanel.getChildren()
                .filter(component -> component instanceof JmixButton && component.isVisible())
                .toList();
        for (int i = 0; i < buttonsList.size(); i++) {
            Component button = buttonsList.get(i);
            button.removeClassNames("button-first", "button-middle", "button-last");
            if (i == 0) {
                button.addClassName("button-first");
            } else if (i == buttonsList.size() - 1) {
                button.addClassName("button-last");
            } else {
                button.addClassName("button-middle");
            }
        }
    }

    private void updateListBoxesLabelsLayoutVisibility() {
        listBoxesLabelsLayout.setVisible(
                StringUtils.isNotEmpty(optionsColumnLabel.getText()) ||
                        StringUtils.isNotEmpty(selectedItemsColumnLabel.getText()));
    }

    private void updateDoubleClickListeners() {
        optionsListeners.clear();
        selectedListeners.clear();

        updateDoubleClickListeners(options, selected, optionsListeners);
        updateDoubleClickListeners(selected, options, selectedListeners);
    }

    private void updateDoubleClickListeners(JmixMultiSelectListBox<V> from,
                                            JmixMultiSelectListBox<V> to,
                                            Map<Element, Integer> doubleClickListeners) {
        int itemIndex = 0;
        for (Component child : from.getChildren().toList())  {
            doubleClickListeners.put(child.getElement(), itemIndex++);

            child.getElement().addEventListener("dblclick", (DomEventListener) event ->
                    listBoxItemDoubleClicked(from, to, doubleClickListeners.get(event.getSource()))
            );
        }
    }

    private void updateValidationVisibleElements(boolean invalid, boolean required, String errorMessage) {
        if (invalid) {
            componentLabel.addClassName("invalid");
        } else {
            componentLabel.removeClassName("invalid");
        }
        if (required) {
            componentLabel.addClassName("required");
        } else {
            componentLabel.removeClassName("required");
        }
        errorLabel.setText(errorMessage);
        errorLabel.setVisible(invalid);
    }

    private void updateListBoxesInAccordanceWithNewValue(Collection<V> value) {
        List<V> selectedListBoxItems = selectedDataView.getItems().toList();
        List<V> itemsToAdd = value != null
                ? value.stream().filter(item -> !selectedListBoxItems.contains(item)).toList()
                : new LinkedList<>();
        List<V> itemsToRemove = selectedDataView.getItems()
                .filter(item -> value == null || !value.contains(item))
                .toList();

        if (itemsToAdd.isEmpty() && itemsToRemove.isEmpty()) {
            return;
        }

        selectedDataView.addItems(itemsToAdd);
        selectedDataView.removeItems(itemsToRemove);

        optionsDataView.addItems(itemsToRemove);
        optionsDataView.removeItems(itemsToAdd);

        updateListBoxesListenersAndStyles();
    }

    private void updateListBoxSelectionStyles(JmixMultiSelectListBox<V> listBox, ListBoxListDataView<V> listBoxDataView) {
        List<Component> list = listBox.getChildren().toList();
        for (int i = 0; i < list.size(); i++) {
            list.get(i).getElement().removeProperty("position");

            if (list.get(i).hasClassName("single")) {
                list.get(i).removeClassName("single");
            }
            if (list.get(i).hasClassName("first")) {
                list.get(i).removeClassName("first");
            }
            if (list.get(i).hasClassName("middle")) {
                list.get(i).removeClassName("middle");
            }
            if (list.get(i).hasClassName("last")) {
                list.get(i).removeClassName("last");
            }

            if (i >= listBoxDataView.getItemCount() || !listBox.isSelected(listBoxDataView.getItem(i))) {
                continue;
            }

            boolean prevSelected = i > 0 && listBox.isSelected(listBoxDataView.getItem(i - 1));
            boolean nextSelected = i < list.size() - 1 && listBox.isSelected(listBoxDataView.getItem(i + 1));

            String className = getListBoxComponentClassName(prevSelected, nextSelected);
            list.get(i).addClassName(className);

            String position = getPosition(prevSelected, nextSelected);
            list.get(i).getElement().setProperty("position", position);
        }
    }

    private String getPosition(boolean prevSelected, boolean nextSelected) {
        if (!prevSelected && !nextSelected) {
            return "0";
        } else if (!prevSelected) {
            return "1";
        } else if (!nextSelected) {
            return "-1";
        }
        return "0";
    }

    private String getListBoxComponentClassName(boolean prevSelected, boolean nextSelected) {
        if (!prevSelected && !nextSelected) {
            return "single";
        } else if (!prevSelected) {
            return "first";
        } else if (!nextSelected) {
            return "last";
        }
        return "middle";
    }
}