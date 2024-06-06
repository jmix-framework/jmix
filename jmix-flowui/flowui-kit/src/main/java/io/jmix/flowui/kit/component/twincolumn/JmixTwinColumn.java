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
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Tag("jmix-twin-column")
@JsModule("./src/twin-column/jmix-twin-column.js")
public class JmixTwinColumn<V> extends AbstractField<JmixTwinColumn<V>, Collection<V>>
        implements HasSize, HasHelper, HasAriaLabel, HasLabel, HasThemeVariant<TwinColumnVariant> {

    private static final String JS_SCROLL_TOP_VARIABLE = "this._scrollerElement.scrollTop";

    protected NativeLabel optionsColumnLabel;
    protected NativeLabel selectedItemsColumnLabel;

    protected JmixMultiSelectListBox<V> options;
    protected MultiSelectListBox<V> selected;

    protected VerticalLayout actionsPanel;

    protected Button selectItems;
    protected Button selectAllItems;
    protected Button deselectItems;
    protected Button deselectAllItems;

    protected List<V> optionsData;
    protected Map<Element, Integer> optionsListeners = new HashMap<>();
    protected Map<Element, Integer> selectedListeners = new HashMap<>();
    protected Map<MultiSelectListBox<V>, Integer> columnToScrollValue = new HashMap<>();
    protected Set<MultiSelectListBox<V>> columnsWithSavedScrollPosition = new HashSet<>();

    protected boolean reorderable = false;

    //change in tests using reflection
    private final boolean saveAndRestoreColumnsScrollTopPosition = true;

    public JmixTwinColumn() {
        super(null);

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
     * Component provides the ability to select or deselect all items. This feature is disabled by default.
     * Setting <code>true</code> shows two buttons to select or deselect all items.
     */
    public void setSelectAllButtonsVisible(Boolean selectAllButtonsVisible) {
        if (selectAllButtonsVisible) {
            if (selectAllItems == null) {
                selectAllItems  = createButton(
                        "jmix-twin-column-select-all-items-action",
                        new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT),
                        (ComponentEventListener<ClickEvent<Button>>) event ->
                                moveItems(options, selected, true));
                deselectAllItems = createButton(
                        "jmix-twin-column-deselect-all-items-action",
                        new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT),
                        (ComponentEventListener<ClickEvent<Button>>) event ->
                                moveItems(selected, options, true));
            }
            actionsPanel.addComponentAtIndex(actionsPanel.indexOf(selectItems) + 1, selectAllItems);
            actionsPanel.addComponentAtIndex(actionsPanel.indexOf(deselectItems) + 1, deselectAllItems);
        } else if (selectAllItems != null) {
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

        options.setReadOnly(readOnly);
        selected.setReadOnly(readOnly);

        actionsPanel.getChildren()
                .filter(component -> component instanceof HasEnabled)
                .forEach(component -> ((HasEnabled) component).setEnabled(!readOnly));
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

        options.setItemLabelGenerator(this::applyColumnItemLabelFormat);
        selected.setItemLabelGenerator(this::applyColumnItemLabelFormat);
    }

    protected void createButtonsPanel() {
        actionsPanel = new VerticalLayout();
        actionsPanel.setAlignItems(FlexComponent.Alignment.BASELINE);
        actionsPanel.setClassName(LumoUtility.Padding.SMALL);
        actionsPanel.setWidth("AUTO");
        actionsPanel.setSpacing(false);

        selectItems = createButton(
                "jmix-twin-column-select-items-action",
                new Icon(VaadinIcon.ANGLE_RIGHT),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(options, selected, false));

        deselectItems = createButton(
                "jmix-twin-column-deselect-items-action",
                new Icon(VaadinIcon.ANGLE_LEFT),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(selected, options, false));

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

    protected void columnItemDoubleClicked(MultiSelectListBox<V> from,
                                           MultiSelectListBox<V> to,
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

    protected void updateValueInternal(Collection<V> value) {
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

    protected String applyColumnItemLabelFormat(V value) {
        return value.toString();
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

            setModelValue(selected.getListDataView().getItems().collect(Collectors.toList()),false);

            updateColumnsListeners();

            columnsWithSavedScrollPosition.clear();

            if (saveAndRestoreColumnsScrollTopPosition) {
                restoreScrollPosition(options);
                restoreScrollPosition(selected);
            }
        };

        if (saveAndRestoreColumnsScrollTopPosition) {
            Consumer<MultiSelectListBox<V>> moveItemsIfScrollPositionsSaved = column -> {
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

    private void updateDoubleClickListeners() {
        optionsListeners.clear();
        selectedListeners.clear();

        updateDoubleClickListeners(options, selected, optionsListeners);
        updateDoubleClickListeners(selected, options, selectedListeners);
    }

    private void updateDoubleClickListeners(MultiSelectListBox<V> from,
                                            MultiSelectListBox<V> to,
                                            Map<Element, Integer> doubleClickListeners) {
        int itemIndex = 0;
        for (Component child : from.getChildren().toList())  {
            doubleClickListeners.put(child.getElement(), itemIndex++);

            child.getElement().addEventListener("dblclick", (DomEventListener) event ->
                    columnItemDoubleClicked(from, to, doubleClickListeners.get(event.getSource()))
            );
        }
    }
}
