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

package io.jmix.flowui.kit.component.twincolumn;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.component.shared.SlotUtils;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.component.ComponentUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * JmixTwinColumn is a customizable UI component designed for selecting multiple items
 * from one column to another. It is particularly useful in scenarios where users need
 * to manage a large collection of items while keeping the selection process organized.
 * <p>
 * The component consists of:
 * - An "items column" containing the available items.
 * - A "selected items column" displaying the chosen items.
 * - Buttons or actions for transferring items between the two columns.
 *
 * @param <V> the type of the items managed by the component
 */
@Tag("jmix-twin-column")
@JsModule("./src/twin-column/jmix-twin-column.js")
public class JmixTwinColumn<V> extends AbstractField<JmixTwinColumn<V>, Collection<V>>
        implements HasSize, HasHelper, HasAriaLabel, HasLabel, HasThemeVariant<TwinColumnVariant>,
        HasListDataView<V, TwinColumnListDataView<V>>, HasDataView<V, Void, TwinColumnDataView<V>> {

    private static final String JS_SCROLL_TOP_VARIABLE = "this._scrollerElement.scrollTop";
    private static final String HAS_WIDTH_ATTRIBUTE_NAME = "has-width";
    private static final String HAS_HEIGHT_ATTRIBUTE_NAME = "has-height";

    protected NativeLabel itemsColumnLabel;
    protected NativeLabel selectedItemsColumnLabel;

    protected MultiSelectListBox<V> items;
    protected MultiSelectListBox<V> selectedItems;

    protected VerticalLayout actionsPanel;

    protected Button selectItems;
    protected Button selectAllItems;
    protected Button deselectItems;
    protected Button deselectAllItems;

    protected List<V> itemsData;
    protected Map<Element, Integer> itemsListeners = new HashMap<>();
    protected Map<Element, Integer> selectedItemsListeners = new HashMap<>();
    protected Map<MultiSelectListBox<V>, Integer> columnToScrollValue = new HashMap<>();
    protected Set<MultiSelectListBox<V>> columnsWithSavedScrollPosition = new HashSet<>();

    protected boolean reorderable = false;

    //If a listBox and a grid change items at the same time the scrollTop position of the listBox becomes incorrect.
    // In this case the component saves and restores the scrollTop position to maintain the correct value.
    protected boolean saveAndRestoreColumnsScrollTopPosition = true;

    private final AtomicReference<DataProvider<V, ?>> dataProvider;
    private Registration dataProviderListenerRegistration;

    public JmixTwinColumn() {
        super(null);

        this.dataProvider = new AtomicReference(DataProvider.ofItems(new Object[0]));

        initComponent();
    }

    /**
     * Setting <code>true</code> keeps original items order in both columns.
     * Setting <code>false</code> organize items in order of their selection or deselection.
     *
     * @param reorderable keep original order or not
     */
    public void setReorderable(Boolean reorderable) {
        this.reorderable = reorderable;

        if (itemsData != null) {
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
     * Component provides the ability to select or deselect all items. This feature is disabled by default.
     * Setting <code>true</code> shows two buttons to select or deselect all items.
     */
    public void setSelectAllButtonsVisible(Boolean selectAllButtonsVisible) {
        if (selectAllButtonsVisible) {
            if (selectAllItems == null) {
                selectAllItems = createButton(
                        "jmix-twin-column-select-all-items-action",
                        new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT),
                        (ComponentEventListener<ClickEvent<Button>>) event ->
                                moveItems(items, selectedItems, true));
                deselectAllItems = createButton(
                        "jmix-twin-column-deselect-all-items-action",
                        new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT),
                        (ComponentEventListener<ClickEvent<Button>>) event ->
                                moveItems(selectedItems, items, true));
            }
            actionsPanel.addComponentAtIndex(actionsPanel.indexOf(selectItems) + 1, selectAllItems);
            actionsPanel.addComponentAtIndex(actionsPanel.indexOf(deselectItems) + 1, deselectAllItems);
        } else if (selectAllItems != null) {
            actionsPanel.remove(selectAllItems, deselectAllItems);
        }
    }

    /**
     * @return <code>true</code> if the buttons for selecting and deselection all items are visible,
     * <code>false</code> otherwise
     */
    public Boolean isSelectAllButtonsVisible() {
        return selectAllItems != null && actionsPanel.indexOf(selectAllItems) >= 0;
    }

    /**
     * Label text for the items column. It is placed under the items column
     */
    public void setItemsColumnLabel(String itemsColumnLabel) {
        this.itemsColumnLabel.setText(itemsColumnLabel);

        updateColumnLabelPadding(this.itemsColumnLabel);
    }

    /**
     * @return the items column label text
     */
    public String getItemsColumnLabel() {
        return itemsColumnLabel.getText();
    }

    /**
     * Label text for the selected items column. It is placed under the selected items column
     */
    public void setSelectedItemsColumnLabel(String selectedItemsColumnLabel) {
        this.selectedItemsColumnLabel.setText(selectedItemsColumnLabel);

        updateColumnLabelPadding(this.selectedItemsColumnLabel);
    }

    /**
     * @return the selected items column label text
     */
    public String getSelectedItemsColumnLabel() {
        return selectedItemsColumnLabel.getText();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        items.setReadOnly(readOnly);
        selectedItems.setReadOnly(readOnly);

        actionsPanel.getChildren()
                .filter(component -> component instanceof HasEnabled)
                .forEach(component -> ((HasEnabled) component).setEnabled(!readOnly));
    }

    @Override
    public void setMinWidth(String minWidth) {
        HasSize.super.setMinWidth(minWidth);

        if (ComponentUtils.isAutoSize(getMinWidth()) &&
                ComponentUtils.isAutoSize(getWidth()) &&
                ComponentUtils.isAutoSize(getMaxWidth())) {
            getElement().removeAttribute(HAS_WIDTH_ATTRIBUTE_NAME);
        } else {
            getElement().setAttribute(HAS_WIDTH_ATTRIBUTE_NAME, "");
        }
    }

    @Override
    public void setWidth(String width) {
        HasSize.super.setWidth(width);

        if (ComponentUtils.isAutoSize(getMinWidth()) && ComponentUtils.isAutoSize(getWidth()) && ComponentUtils.isAutoSize(getMaxWidth())) {
            getElement().removeAttribute(HAS_WIDTH_ATTRIBUTE_NAME);
        } else {
            getElement().setAttribute(HAS_WIDTH_ATTRIBUTE_NAME, "");
        }
    }

    @Override
    public void setMaxWidth(String maxWidth) {
        HasSize.super.setMaxWidth(maxWidth);

        if (ComponentUtils.isAutoSize(getMinWidth()) && ComponentUtils.isAutoSize(getWidth()) && ComponentUtils.isAutoSize(getMaxWidth())) {
            getElement().removeAttribute(HAS_WIDTH_ATTRIBUTE_NAME);
        } else {
            getElement().setAttribute(HAS_WIDTH_ATTRIBUTE_NAME, "");
        }
    }

    @Override
    public void setMinHeight(String minHeight) {
        HasSize.super.setMinHeight(minHeight);

        if (ComponentUtils.isAutoSize(getMinHeight()) && ComponentUtils.isAutoSize(getHeight()) && ComponentUtils.isAutoSize(getMaxHeight())) {
            getElement().removeAttribute(HAS_HEIGHT_ATTRIBUTE_NAME);
        } else {
            getElement().setAttribute(HAS_HEIGHT_ATTRIBUTE_NAME, "");
        }
    }

    @Override
    public void setHeight(String height) {
        HasSize.super.setHeight(height);

        if (ComponentUtils.isAutoSize(getMinHeight()) && ComponentUtils.isAutoSize(getHeight()) && ComponentUtils.isAutoSize(getMaxHeight())) {
            getElement().removeAttribute(HAS_HEIGHT_ATTRIBUTE_NAME);
        } else {
            getElement().setAttribute(HAS_HEIGHT_ATTRIBUTE_NAME, "");
        }
    }

    @Override
    public void setMaxHeight(String maxHeight) {
        HasSize.super.setMaxHeight(maxHeight);

        if (ComponentUtils.isAutoSize(getMinHeight()) && ComponentUtils.isAutoSize(getHeight()) && ComponentUtils.isAutoSize(getMaxHeight())) {
            getElement().removeAttribute(HAS_HEIGHT_ATTRIBUTE_NAME);
        } else {
            getElement().setAttribute(HAS_HEIGHT_ATTRIBUTE_NAME, "");
        }
    }

    public DataProvider<V, ?> getDataProvider() {
        return this.dataProvider.get();
    }

    @Override
    public TwinColumnListDataView<V> setItems(ListDataProvider<V> dataProvider) {
        setDataProvider(dataProvider);
        recreateItems(dataProvider.getItems());

        return getListDataView();
    }

    @Override
    public TwinColumnDataView<V> setItems(DataProvider<V, Void> dataProvider) {
        setDataProvider(dataProvider);
        return getGenericDataView();
    }

    @Override
    public TwinColumnDataView<V> setItems(InMemoryDataProvider<V> inMemoryDataProvider) {
        DataProvider<V, Void> convertedDataProvider = new DataProviderWrapper<>(inMemoryDataProvider) {
            protected SerializablePredicate<V> getFilter(Query<V, Void> query) {
                return Optional.ofNullable(inMemoryDataProvider.getFilter()).orElse((item) -> true);
            }
        };
        return setItems(convertedDataProvider);
    }

    @Override
    public TwinColumnDataView<V> getGenericDataView() {
        return new TwinColumnDataView<>(this::getDataProvider, this);
    }

    @Override
    public TwinColumnListDataView<V> getListDataView() {
        return new TwinColumnListDataView<>(this::getDataProvider, this, (filter, sorting) -> {
        });
    }

    public void setItemLabelGenerator(ItemLabelGenerator<V> itemLabelGenerator) {
        Objects.requireNonNull(itemLabelGenerator, "The item label generator can not be null");

        items.setItemLabelGenerator(itemLabelGenerator);
        selectedItems.setItemLabelGenerator(itemLabelGenerator);
    }

    protected void setDataProvider(DataProvider<V, ?> dataProvider) {
        this.dataProvider.set(dataProvider);
        recreateItems(List.of());
        if (this.dataProviderListenerRegistration != null) {
            this.dataProviderListenerRegistration.remove();
        }

        this.dataProviderListenerRegistration = dataProvider.addDataProviderListener(this::onDataChange);
    }

    @Override
    protected void setPresentationValue(Collection<V> newPresentationValue) {
        updateValueInternal(newPresentationValue);
    }

    protected void initComponent() {
        createContent();
    }

    protected void createContent() {
        createColumnLabels();
        createColumns();
        createButtonsPanel();
    }

    protected void createColumnLabels() {
        itemsColumnLabel = createColumnLabel();
        selectedItemsColumnLabel = createColumnLabel();

        itemsColumnLabel.addClassName("jmix-twin-column-items-column-label");
        selectedItemsColumnLabel.addClassName("jmix-twin-column-selected-items-column-label");

        SlotUtils.addToSlot(this, "items-label", itemsColumnLabel);
        SlotUtils.addToSlot(this, "selected-items-label", selectedItemsColumnLabel);
    }

    protected void createColumns() {
        items = createColumn();
        selectedItems = createColumn();

        items.setItems(new LinkedList<>());
        selectedItems.setItems(new LinkedList<>());

        SlotUtils.addToSlot(this, "items", items);
        SlotUtils.addToSlot(this, "selected-items", selectedItems);

        items.addClassName("jmix-twin-column-items-column");
        selectedItems.addClassName("jmix-twin-column-selected-items-column");

        items.setItemLabelGenerator(this::applyColumnItemLabelFormat);
        selectedItems.setItemLabelGenerator(this::applyColumnItemLabelFormat);
    }

    protected void createButtonsPanel() {
        actionsPanel = new VerticalLayout();
        actionsPanel.setAlignItems(FlexComponent.Alignment.BASELINE);
        actionsPanel.setWidth("AUTO");
        actionsPanel.setSpacing(false);

        selectItems = createButton(
                "jmix-twin-column-select-items-action",
                new Icon(VaadinIcon.ANGLE_RIGHT),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(items, selectedItems, false));

        deselectItems = createButton(
                "jmix-twin-column-deselect-items-action",
                new Icon(VaadinIcon.ANGLE_LEFT),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(selectedItems, items, false));

        actionsPanel.addClassName("jmix-twin-column-actions-panel");
        actionsPanel.add(selectItems, deselectItems);

        SlotUtils.addToSlot(this, "actions", actionsPanel);
    }

    protected NativeLabel createColumnLabel() {
        return new NativeLabel();
    }

    protected MultiSelectListBox<V> createColumn() {
        return new MultiSelectListBox<>();
    }

    protected Button createButton(String className, Icon icon,
                                  ComponentEventListener<ClickEvent<Button>> clickListener) {
        Button button = new Button();
        button.setClassName(className);
        button.setIcon(icon);
        button.addClickListener(clickListener);
        return button;
    }

    protected void onDataChange(DataChangeEvent<V> event) {
        List<V> newItems = event.getSource()
                .fetch(DataViewUtils.getQuery(JmixTwinColumn.this))
                .toList();
        recreateItems(newItems);
    }

    protected void columnItemDoubleClicked(MultiSelectListBox<V> from,
                                           MultiSelectListBox<V> to,
                                           Integer itemIndex) {
        moveItems(from, to, Collections.singletonList(from.getListDataView().getItem(itemIndex)));
    }

    protected void recreateItems(Collection<V> newItems) {
        if (itemsData == null) {
            itemsData = new LinkedList<>();
        }

        items.getListDataView().removeItems(itemsData);
        itemsData.clear();

        items.getListDataView().addItems(newItems);
        itemsData.addAll(newItems);

        updateColumnsReorderableComparator();
        updateColumnsListeners();
    }

    protected void updateValueInternal(Collection<V> value) {
        List<V> selectedItemsColumn = selectedItems.getListDataView().getItems().toList();
        List<V> itemsToAdd = value != null
                ? value.stream().filter(item -> !selectedItemsColumn.contains(item)).toList()
                : new LinkedList<>();
        List<V> itemsToRemove = selectedItems.getListDataView().getItems()
                .filter(item -> value == null || !value.contains(item))
                .toList();

        if (itemsToAdd.isEmpty() && itemsToRemove.isEmpty()) {
            return;
        }

        selectedItems.getListDataView().addItems(itemsToAdd);
        selectedItems.getListDataView().removeItems(itemsToRemove);

        items.getListDataView().addItems(itemsToRemove);
        items.getListDataView().removeItems(itemsToAdd);

        updateColumnsListeners();
    }

    protected String applyColumnItemLabelFormat(V value) {
        return value.toString();
    }

    protected void updateColumnLabelPadding(NativeLabel label) {
        label.setVisible(!Strings.isNullOrEmpty(label.getText()));
    }

    private void moveItems(MultiSelectListBox<V> from, MultiSelectListBox<V> to, boolean moveAllItems) {
        List<V> selectedItems = moveAllItems ?
                from.getListDataView().getItems().toList() :
                from.getSelectedItems().stream().toList();

        moveItems(from, to, selectedItems);
    }

    private void moveItems(MultiSelectListBox<V> from, MultiSelectListBox<V> to, List<V> selectedItems) {
        Runnable moveItems = () -> {
            from.select(Collections.emptySet());
            from.getListDataView().removeItems(selectedItems);

            to.getListDataView().addItems(selectedItems);
            to.select(selectedItems);

            setModelValue(this.selectedItems.getListDataView().getItems().collect(Collectors.toList()), false);

            updateColumnsListeners();

            columnsWithSavedScrollPosition.clear();

            if (saveAndRestoreColumnsScrollTopPosition) {
                restoreScrollPosition(items);
                restoreScrollPosition(this.selectedItems);
            }
        };

        if (saveAndRestoreColumnsScrollTopPosition) {
            Consumer<MultiSelectListBox<V>> moveItemsIfScrollPositionsSaved = column -> {
                columnsWithSavedScrollPosition.add(column);
                if (columnsWithSavedScrollPosition.size() == 2) {
                    moveItems.run();
                }
            };

            saveScrollPosition(items, moveItemsIfScrollPositionsSaved);
            saveScrollPosition(this.selectedItems, moveItemsIfScrollPositionsSaved);
        } else {
            moveItems.run();
        }
    }

    private void saveScrollPosition(MultiSelectListBox<V> column, Consumer<MultiSelectListBox<V>> consumer) {
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

    private void restoreScrollPosition(MultiSelectListBox<V> column) {
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
                    Integer.compare(itemsData.indexOf(value1), itemsData.indexOf(value2));
        }
        Set<V> selectedItems = items.getValue();
        items.getListDataView().setSortComparator(columnReorderableComparator);
        //after comparator setting - the value of the component stays the same, but items order is changed and
        // column selections show incorrect items, so need to change the value to update the component
        items.setValue(Collections.emptySet());
        items.setValue(selectedItems);

        selectedItems = this.selectedItems.getValue();
        this.selectedItems.getListDataView().setSortComparator(columnReorderableComparator);
        this.selectedItems.setValue(Collections.emptySet());
        this.selectedItems.setValue(selectedItems);
    }

    private void updateDoubleClickListeners() {
        itemsListeners.clear();
        selectedItemsListeners.clear();

        updateDoubleClickListeners(items, selectedItems, itemsListeners);
        updateDoubleClickListeners(selectedItems, items, selectedItemsListeners);
    }

    private void updateDoubleClickListeners(MultiSelectListBox<V> from,
                                            MultiSelectListBox<V> to,
                                            Map<Element, Integer> doubleClickListeners) {
        int itemIndex = 0;
        for (Component child : from.getChildren().toList()) {
            doubleClickListeners.put(child.getElement(), itemIndex++);

            child.getElement().addEventListener("dblclick", (DomEventListener) event ->
                    columnItemDoubleClicked(from, to, doubleClickListeners.get(event.getSource()))
            );
        }
    }
}
