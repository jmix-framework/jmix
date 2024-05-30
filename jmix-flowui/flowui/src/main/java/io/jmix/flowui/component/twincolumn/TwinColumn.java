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
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.data.binder.HasItemComponents;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
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
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Tag("jmix-twin-column")
@JsModule("./src/twin-column/jmix-twin-column.js")
public class TwinColumn<V> extends AbstractField<TwinColumn<V>, Collection<V>>
            implements HasSize, HasHelper, HasAriaLabel, HasLabel, HasRequired,
                       HasDataView<V, Void, TwinColumnDataView<V>>, HasListDataView<V, TwinColumnListDataView<V>>,
                       HasItemComponents<V>, HasThemeVariant<TwinColumnVariant>,
                       SupportsItemsContainer<V>, SupportsValueSource<Collection<V>>, SupportsItemsEnum<V>,
                       SupportsDataProvider<V>, SupportsValidation<Collection<V>>,
                       ApplicationContextAware, InitializingBean {

    private static final String JS_SCROLL_TOP_VARIABLE = "this._scrollerElement.scrollTop";

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;
    protected Messages messages;

    protected TwinColumnDelegate<TwinColumn<V>, Collection<V>, Collection<V>> fieldDelegate;
    protected DataViewDelegate<TwinColumn<V>, V> dataViewDelegate;

    protected NativeLabel optionsColumnLabel;
    protected NativeLabel selectedItemsColumnLabel;

    protected JmixMultiSelectListBox<V> options;
    protected JmixMultiSelectListBox<V> selected;

    protected VerticalLayout actionsPanel;

    protected JmixButton selectItems;
    protected JmixButton selectAllItems;
    protected JmixButton deselectItems;
    protected JmixButton deselectAllItems;

    protected List<V> optionsData;
    protected Map<Element, Integer> optionsListeners = new HashMap<>();
    protected Map<Element, Integer> selectedListeners = new HashMap<>();
    protected Map<JmixMultiSelectListBox<V>, Integer> columnToScrollValue = new HashMap<>();
    protected Set<JmixMultiSelectListBox<V>> columnsWithSavedScrollPosition = new HashSet<>();

    protected boolean reorderable = false;

    //change in tests using reflection
    private final boolean saveAndRestoreColumnsScrollTopPosition = true;

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

    /**
     * Setting <code>true</code> keeps original items order in both columns.
     * Setting <code>false</code> organize items in order of their selection or deselection.
     * @param reorderable keep original order or not
     */
    public void setReorderable(Boolean reorderable) {
        this.reorderable = reorderable;

        if (optionsData != null) {
            updateColumnsReorderableComparator();
            updateColumnsListeners();
        }
    }

    /**
     * @return <code>true</code> if component keeps original order of items in both columns
     */
    public Boolean isReorderable() {
        return reorderable;
    }

    /**
     * There are two buttons to select or deselect all items. Setting false hides these buttons.
     * @param selectAllButtonsVisible
     */
    public void setSelectAllButtonsVisible(Boolean selectAllButtonsVisible) {
        if (selectAllButtonsVisible) {
            actionsPanel.addComponentAtIndex(actionsPanel.indexOf(selectItems) + 1, selectAllItems);
            actionsPanel.addComponentAtIndex(actionsPanel.indexOf(deselectItems) + 1, deselectAllItems);
        } else {
            actionsPanel.remove(selectAllItems, deselectAllItems);
        }
    }

    /**
     * @return <code>true</code> if the buttons for selecting and deselection all items are visible, <code>false</code> otherwise
     */
    public Boolean isSelectAllButtonsVisible() {
        return selectAllItems.isVisible();
    }

    /**
     * Label text for the options column. It is placed under the options column
     */
    public void setOptionsColumnLabel(String optionsColumnLabel) {
        this.optionsColumnLabel.setText(optionsColumnLabel);

        updateColumnsLabelsLayoutVisibility();
    }

    /**
     * @return the options column label text
     */
    public String getOptionsColumnLabel() {
        return optionsColumnLabel.getText();
    }

    /**
     * Label text for the selected items column. It is placed under the selected items column
     */
    public void setSelectedItemsColumnLabel(String selectedItemsColumnLabel) {
        this.selectedItemsColumnLabel.setText(selectedItemsColumnLabel);

        updateColumnsLabelsLayoutVisibility();
    }

    /**
     * @return the selected items column label text
     */
    public String getSelectedItemsColumnLabel() {
        return selectedItemsColumnLabel.getText();
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
            updateInvalidState();
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
        updateInvalidState();
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
    }

    @Override
    public void setRequired(boolean required) {
        HasRequired.super.setRequired(required);

        updateInvalidState();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
        super.setRequiredIndicatorVisible(requiredIndicatorVisible);

        updateInvalidState();
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
    }

    @Override
    public String getErrorMessage() {
        return fieldDelegate.getErrorMessage();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        options.setReadOnly(readOnly);
        selected.setReadOnly(readOnly);

        actionsPanel.getChildren()
                .filter(component -> component instanceof HasEnabled)
                .forEach(component -> ((HasEnabled) component).setEnabled(!readOnly));

        updateInvalidState();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        updateInvalidState();
    }

    @Override
    public void setValue(Collection<V> value) {
        super.setValue(value);

        updateValueInternal(value);
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        messages = applicationContext.getBean(Messages.class);
    }

    @Override
    protected void setPresentationValue(Collection<V> newPresentationValue) {
        updateValueInternal(newPresentationValue);
    }

    protected void initComponent() {
        createContent();

        fieldDelegate = createFieldDelegate();
        dataViewDelegate = createDataViewDelegate();

        createValueChangeListener();
    }

    protected void createValueChangeListener() {
        addValueChangeListener((ValueChangeListener<ComponentValueChangeEvent<TwinColumn<V>, Collection<V>>>) event -> {
            updateInvalidState();
        });
    }

    protected void createContent() {
        createColumnLabels();
        createColumns();
        createButtonsPanel();
    }

    protected void createColumnLabels() {
        optionsColumnLabel = createColumnLabel();
        selectedItemsColumnLabel = createColumnLabel();

        optionsColumnLabel.addClassName("jmix-twin-column-options-column-label");
        selectedItemsColumnLabel.addClassName("jmix-twin-column-selected-column-label");

        SlotUtils.addToSlot(this, "options-label", optionsColumnLabel);
        SlotUtils.addToSlot(this, "selected-label", selectedItemsColumnLabel);
    }

    protected void createColumns() {
        options = createColumn();
        selected = createColumn();

        options.setItems(new LinkedList<>());
        selected.setItems(new LinkedList<>());

        SlotUtils.addToSlot(this, "options", options);
        SlotUtils.addToSlot(this, "selected", selected);

        options.addClassName("jmix-twin-column-options-column");
        selected.addClassName("jmix-twin-column-selected-column");

        updateColumnsLabelsLayoutVisibility();
    }

    protected void createButtonsPanel() {
        actionsPanel = uiComponents.create(VerticalLayout.class);
        actionsPanel.setAlignItems(FlexComponent.Alignment.BASELINE);
        actionsPanel.setClassName(LumoUtility.Padding.SMALL);
        actionsPanel.setWidth("AUTO");
        actionsPanel.setSpacing(false);

        selectItems = createButton(
                "jmix-twin-column-select-items-action",
                new Icon(VaadinIcon.ANGLE_RIGHT),
                "twinColumn.selectItems",
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(options, selected, false));
        selectAllItems = createButton(
                "jmix-twin-column-select-all-items-action",
                new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT),
                "twinColumn.selectAllItems",
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(options, selected, true));
        deselectItems = createButton(
                "jmix-twin-column-deselect-items-action",
                new Icon(VaadinIcon.ANGLE_LEFT),
                "twinColumn.deselectItems",
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(selected, options, false));
        deselectAllItems = createButton(
                "jmix-twin-column-deselect-all-items-action",
                new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT),
                "twinColumn.deselectAllItems",
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(selected, options, true));

        actionsPanel.addClassName("jmix-twin-column-actions-panel");
        actionsPanel.add(selectItems, deselectItems);

        SlotUtils.addToSlot(this, "actions", actionsPanel);
    }

    protected NativeLabel createColumnLabel() {
        return uiComponents.create(NativeLabel.class);
    }

    protected JmixMultiSelectListBox<V> createColumn() {
        return uiComponents.create(JmixMultiSelectListBox.class);
    }

    protected JmixButton createButton(String className, Icon icon, String tooltipKey,
                                      ComponentEventListener<ClickEvent<Button>> clickListener) {
        JmixButton button = uiComponents.create(JmixButton.class);
        button.setClassName(className);
        button.setIcon(icon);
        button.setTooltipText(messages.getMessage(tooltipKey));
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

    protected void columnItemDoubleClicked(JmixMultiSelectListBox<V> from,
                                           JmixMultiSelectListBox<V> to,
                                           Integer itemIndex) {
        moveItems(from, to, Collections.singletonList(from.getListDataView().getItem(itemIndex)));
    }

    protected void recreateOptions(Collection<V> newOptions) {
        if (optionsData == null) {
            optionsData = new LinkedList<>();
        }

        options.getListDataView().removeItems(optionsData);
        optionsData.clear();

        options.getListDataView().addItems(newOptions);
        optionsData.addAll(newOptions);

        updateColumnsReorderableComparator();
        updateColumnsListeners();
    }

    private void updateInvalidState() {
        if (fieldDelegate != null) {
            fieldDelegate.updateInvalidState();
        }
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

            updateColumnsListeners();

            columnsWithSavedScrollPosition.clear();

            if (saveAndRestoreColumnsScrollTopPosition) {
                restoreScrollPosition(options);
                restoreScrollPosition(selected);
            }
        };

        if (saveAndRestoreColumnsScrollTopPosition) {
            Consumer<JmixMultiSelectListBox<V>> moveItemsIfScrollPositionsSaved = column -> {
                columnsWithSavedScrollPosition.add(column);
                if (columnsWithSavedScrollPosition.size() == 2) {
                    moveItems.run();
                }
            };

            saveScrollPosition(options, moveItemsIfScrollPositionsSaved);
            saveScrollPosition(selected, moveItemsIfScrollPositionsSaved);
        } else {
            moveItems.run();
        }
    }

    private void saveScrollPosition(JmixMultiSelectListBox<V> column, Consumer<JmixMultiSelectListBox<V>> consumer) {
        if (column.getElement().isEnabled()) {
            column.getElement().executeJs("return " + JS_SCROLL_TOP_VARIABLE)
                    .then(Integer.class, (SerializableConsumer<Integer>) scrollTop -> {
                        columnToScrollValue.put(column, scrollTop);
                        consumer.accept(column);
                    });
        } else {
            consumer.accept(column);
        }
    }

    private void restoreScrollPosition(JmixMultiSelectListBox<V> column) {
        if (column.getElement().isEnabled()) {
            column.getElement()
                    .executeJs(String.format(JS_SCROLL_TOP_VARIABLE + " = %s", columnToScrollValue.getOrDefault(column, 0)));
        }
    }

    private void updateColumnsListeners() {
        updateDoubleClickListeners();
    }

    private void updateColumnsReorderableComparator() {
        SerializableComparator<V> columnReorderableComparator = null;
        if (isReorderable()) {
            columnReorderableComparator = (value1, value2) ->
                    Integer.compare(optionsData.indexOf(value1), optionsData.indexOf(value2));
        }
        Set<V> selectedItems = options.getValue();
        options.getListDataView().setSortComparator(columnReorderableComparator);
        //after comparator setting - the value of the component stays the same, but items order is changed and
        // column selections show incorrect items, so need to change the value to update the component
        options.setValue(Collections.emptySet());
        options.setValue(selectedItems);

        selectedItems = selected.getValue();
        selected.getListDataView().setSortComparator(columnReorderableComparator);
        selected.setValue(Collections.emptySet());
        selected.setValue(selectedItems);
    }

    private void updateColumnsLabelsLayoutVisibility() {
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
                    columnItemDoubleClicked(from, to, doubleClickListeners.get(event.getSource()))
            );
        }
    }

    private void updateValueInternal(Collection<V> value) {
        List<V> selectedColumnItems = selected.getListDataView().getItems().toList();
        List<V> itemsToAdd = value != null
                ? value.stream().filter(item -> !selectedColumnItems.contains(item)).toList()
                : new LinkedList<>();
        List<V> itemsToRemove = selected.getListDataView().getItems()
                .filter(item -> value == null || !value.contains(item))
                .toList();

        if (itemsToAdd.isEmpty() && itemsToRemove.isEmpty()) {
            return;
        }

        selected.getListDataView().addItems(itemsToAdd);
        selected.getListDataView().removeItems(itemsToRemove);

        options.getListDataView().addItems(itemsToRemove);
        options.getListDataView().removeItems(itemsToAdd);

        updateColumnsListeners();
    }
}