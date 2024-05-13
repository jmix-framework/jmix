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
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.listbox.dataview.ListBoxListDataView;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.shared.HasThemeVariant;
import com.vaadin.flow.data.binder.HasItemComponents;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableComparator;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsValidation;
import io.jmix.flowui.component.delegate.DataViewDelegate;
import io.jmix.flowui.component.listbox.JmixListBox;
import io.jmix.flowui.component.listbox.JmixMultiSelectListBox;
import io.jmix.flowui.component.validation.Validator;
import io.jmix.flowui.data.*;
import io.jmix.flowui.data.items.InMemoryDataProviderWrapper;
import io.jmix.flowui.exception.ComponentValidationException;
import io.jmix.flowui.exception.ValidationException;
import io.jmix.flowui.kit.component.HasSubParts;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.grid.JmixGrid;
import io.jmix.flowui.model.CollectionContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;

import javax.print.DocFlavor;
import java.util.*;
import java.util.stream.Stream;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

@Tag("jmix-twin-column")
public class TwinColumn<V> extends AbstractField<TwinColumn<V>, Collection<V>>
            implements HasSize, HasHelper, HasAriaLabel, HasLabel, HasRequired, HasTheme,
                       HasDataView<V, Void, TwinColumnDataView<V>>, HasListDataView<V, TwinColumnListDataView<V>>,
                       HasItemComponents<V>, HasSubParts, HasThemeVariant<TwinColumnVariant>,
                       SupportsItemsContainer<V>, SupportsValueSource<Collection<V>>, SupportsItemsEnum<V>,
                       SupportsDataProvider<V>, SupportsValidation<V>,
                       ApplicationContextAware, InitializingBean {

    private static final String LIST_BOX_DEFAULT_HEIGHT = "16em";
    private static final String COMPONENT_DEFAULT_WIDTH = "32em";
    private static final String LIST_BOX_MIN_WIDTH = "12em";
    private static final String LIST_BOX_MIN_HEIGHT = "14em";

    protected ApplicationContext applicationContext;
    protected UiComponents uiComponents;

    protected TwinColumnDelegate fieldDelegate;
    protected DataViewDelegate<TwinColumn<V>, V> dataViewDelegate;

    protected JmixMultiSelectListBox<V> selected;
    protected JmixMultiSelectListBox<V> options;

    protected VerticalLayout contentWrapper;
    protected VerticalLayout buttonsPanel;
    protected HorizontalLayout listBoxesLabelsLayout;

    protected ListBoxListDataView<V> optionsDataView;
    protected ListBoxListDataView<V> selectedDataView;

    protected Span componentLabel;
    protected Div selectedLabel;
    protected Div optionsLabel;
    protected Span helperLabel;
    protected Span errorLabel;

    protected JmixButton moveRight;
    protected JmixButton moveRightAll;
    protected JmixButton moveLeft;
    protected JmixButton moveLeftAll;
    protected JmixButton clearSelection;

    protected List<V> optionsData;
    protected Map<Element, Integer> optionsListeners = new HashMap<>();
    protected Map<Element, Integer> selectedListeners = new HashMap<>();
    protected List<Component> subParts;

    protected Boolean reorderable = false;
    protected Boolean allBtnEnabled = true;

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

        updateListBoxesReorderableComparator();
    }

    public Boolean isReorderable() {
        return reorderable;
    }

    public void setAllBtnEnabled(Boolean allBtnEnabled) {
        this.allBtnEnabled = allBtnEnabled;

        updateAllBtnEnabled();
    }

    public Boolean isAllBtnEnabled() {
        return allBtnEnabled;
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

        dataViewDelegate.getDataProvider().addDataProviderListener((DataProviderListener<V>) event -> {
            List<V> newOptions = event.getSource()
                    .fetch(DataViewUtils.getQuery(TwinColumn.this))
                    .toList();
            recreateOptions(newOptions);
        });
    }

    @Override
    public void setItems(Class<V> itemsEnum) {
        dataViewDelegate.setItems(itemsEnum);
    }

    @Override
    public TwinColumnDataView<V> setItems(DataProvider<V, Void> dataProvider) {
        bindDataProvider(dataProvider);
        recreateOptions(dataProvider.fetch(DataViewUtils.getQuery(this)).toList());
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
    public String getHelperText() {
        return helperLabel.getElement().getProperty("helperText");
    }

    @Override
    public Registration addValidator(Validator<? super V> validator) {
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

        errorLabel.setVisible(invalid);
        if (invalid) {
            componentLabel.addClassName("error-indicator");
        } else {
            componentLabel.removeClassName("error-indicator");
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

        fieldDelegate.updateInvalidState();
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

    public void setLeftColumnCaption(String leftColumnCaption) {
        optionsLabel.setText(leftColumnCaption);

        updateListBoxesLabelsLayoutVisibility();
    }

    public void setRightColumnCaption(String rightColumnCaption) {
        selectedLabel.setText(rightColumnCaption);

        updateListBoxesLabelsLayoutVisibility();
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

        moveRight.setEnabled(enabled);
        moveRightAll.setEnabled(enabled);
        moveLeft.setEnabled(enabled);
        moveLeftAll.setEnabled(enabled);

        fieldDelegate.updateInvalidState();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
    }

    @Override
    protected void setPresentationValue(Collection<V> newPresentationValue) {
        updateColumnComponents(newPresentationValue);
    }

    protected void initComponent() {
        createContent();

        fieldDelegate = createFieldDelegate();
        dataViewDelegate = createDataViewDelegate();

        createValueChangeListener();
    }

    protected void createHelper() {
        helperLabel = uiComponents.create(Span.class);
        helperLabel.setClassName("helper");

        contentWrapper.add(helperLabel);
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

    protected void createSubParts() {
        subParts = new LinkedList<>();

        subParts.add(componentLabel);
        subParts.add(selectedLabel);
        subParts.add(optionsLabel);
        subParts.add(options);
        subParts.add(selected);
        subParts.add(moveRight);
        subParts.add(moveRightAll);
        subParts.add(moveLeft);
        subParts.add(moveLeftAll);
        subParts.add(clearSelection);
    }

    protected JmixMultiSelectListBox<V> createListBox(String id) {
        JmixMultiSelectListBox<V> listBox = uiComponents.create(JmixMultiSelectListBox.class);
        listBox.setId(id);
        listBox.setMinWidth(LIST_BOX_MIN_WIDTH);
        listBox.setMinHeight(LIST_BOX_MIN_HEIGHT);
        listBox.setHeight("100%");
        listBox.setWidth("50%");
        return listBox;
    }

    private void createValueChangeListener() {
        addValueChangeListener((ValueChangeListener<ComponentValueChangeEvent<TwinColumn<V>, Collection<V>>>) event -> {
            String errorMessage = null;
            try {
                executeValidators();
                errorLabel.setVisible(false);
                componentLabel.removeClassName("error-indicator");
            } catch (ComponentValidationException e) {
                errorMessage = getErrorMessage();
                errorLabel.setVisible(true);
                componentLabel.addClassName("error-indicator");
            }
            errorLabel.setText(errorMessage);
        });
    }

    protected void createErrorLabel() {
        errorLabel = uiComponents.create(Span.class);
        errorLabel.setVisible(false);
        errorLabel.setClassName("error-label");

        contentWrapper.add(errorLabel);
    }

    private void createContent() {
        addClassName(LumoUtility.Overflow.AUTO);

        initContentWrapper();
        createComponentLabel();
        createListBoxLabels();
        createListBoxes();
        createButtonsPanel();
        createListBoxesLayout();
        createHelper();
        createErrorLabel();

        createSubParts();
    }

    protected JmixButton createButton(String id, String className, Icon icon,
                                      ComponentEventListener<ClickEvent<Button>> clickListener) {
        JmixButton button = uiComponents.create(JmixButton.class);

        button.setId(id);
        button.setClassName(className);
        button.setIcon(icon);
        button.addClickListener(clickListener);

        return button;
    }

    protected Div createListBoxLabel(String id) {
        Div label = uiComponents.create(Div.class);
        label.setId(id);
        label.addClassName(LumoUtility.Margin.Top.AUTO);
        label.setWidth("100%");
        label.setClassName("label");
        return label;
    }

    protected void createListBoxLabels() {
        selectedLabel = createListBoxLabel("selectedLabel");
        optionsLabel = createListBoxLabel("optionsLabel");

        listBoxesLabelsLayout = uiComponents.create(HorizontalLayout.class);
        contentWrapper.add(listBoxesLabelsLayout);

        listBoxesLabelsLayout.setPadding(false);
        listBoxesLabelsLayout.setWidth("100%");

        JmixButton stub = uiComponents.create(JmixButton.class);
        stub.addClassName("stub");
        stub.setIcon(new Icon(VaadinIcon.THUMBS_UP));
        stub.setHeight("1px");

        listBoxesLabelsLayout.add(optionsLabel);
        listBoxesLabelsLayout.add(stub);
        listBoxesLabelsLayout.add(selectedLabel);
    }

    protected void createComponentLabel() {
        componentLabel = uiComponents.create(Span.class);
        componentLabel.setId("label");
        componentLabel.setClassName("label");

        contentWrapper.add(componentLabel);
    }

    protected void initContentWrapper() {
        contentWrapper = uiComponents.create(VerticalLayout.class);
        add(contentWrapper);

        contentWrapper.setSpacing(false);

        if (getWidth() == null) {
            contentWrapper.setWidth(COMPONENT_DEFAULT_WIDTH);
        }
    }

    protected void bindDataProvider(DataProvider<V, ?> dataProvider) {
        // One of binding methods is called from a constructor so bean can be null
        if (dataViewDelegate != null) {
            dataViewDelegate.bind(dataProvider);
        }
    }

    protected void listBoxItemDoubleClicked(JmixMultiSelectListBox<V> from,
                                            JmixMultiSelectListBox<V> to,
                                            Integer itemIndex) {
        moveItems(from, to, Collections.singletonList(from.getListDataView().getItem(itemIndex)));
    }

    protected void moveItems(JmixMultiSelectListBox<V> from, JmixMultiSelectListBox<V> to, List<V> selectedItems) {
        from.getListDataView().removeItems(selectedItems);
        to.getListDataView().addItems(selectedItems);
        from.select(Collections.emptySet());
        to.select(selectedItems);

        setModelValue(from.getListDataView().getItems().toList(), true);

        updateDoubleClickListeners();
    }

    protected TwinColumnDelegate createFieldDelegate() {
        return applicationContext.getBean(TwinColumnDelegate.class, this);
    }

    protected DataViewDelegate<TwinColumn<V>, V> createDataViewDelegate() {
        return applicationContext.getBean(DataViewDelegate.class, this);
    }

    protected void createButtonsPanel() {
        buttonsPanel = uiComponents.create(VerticalLayout.class);
        buttonsPanel.setAlignItems(FlexComponent.Alignment.BASELINE);
        buttonsPanel.setClassName(LumoUtility.Padding.SMALL);
        buttonsPanel.setWidth("AUTO");
        buttonsPanel.setSpacing(false);

        moveRight = createButton(
                "moveRight",
                "button-first",
                new Icon(VaadinIcon.ANGLE_RIGHT),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(options, selected, false));
        moveRightAll = createButton(
                "moveRightAll",
                "button-middle",
                new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(options, selected, true));
        moveLeft = createButton(
                "moveLeft",
                "button-middle",
                new Icon(VaadinIcon.ANGLE_LEFT),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(selected, options, false));
        moveLeftAll = createButton(
                "moveLeftAll",
                "button-middle",
                new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT),
                (ComponentEventListener<ClickEvent<Button>>) event ->
                        moveItems(selected, options, true));
        clearSelection = createButton(
                "clearSelection",
                "button-last",
                new Icon(VaadinIcon.CLOSE_SMALL),
                (ComponentEventListener<ClickEvent<Button>>) event -> {
                    selected.deselectAll();
                    options.deselectAll();
                }
        );

        buttonsPanel.add(moveRight, moveRightAll, moveLeft, moveLeftAll, clearSelection);
    }

    protected void createListBoxes() {
        options = createListBox("options");
        selected = createListBox("selected");

        options.addSelectionListener((MultiSelectionListener<MultiSelectListBox<V>, V>) event ->
                updateListBoxSelectionStyles(options, optionsDataView));
        selected.addSelectionListener((MultiSelectionListener<MultiSelectListBox<V>, V>) event ->
                updateListBoxSelectionStyles(selected, selectedDataView));

        optionsDataView = options.setItems(new LinkedList<>());
        selectedDataView = selected.setItems(new LinkedList<>());

        if (isReorderable()) {
            updateListBoxesReorderableComparator();
        }

        updateListBoxesLabelsLayoutVisibility();
    }

    private void moveItems(JmixMultiSelectListBox<V> from, JmixMultiSelectListBox<V> to, boolean moveAllItems) {
        List<V> selectedItems = moveAllItems ?
                from.getListDataView().getItems().toList() :
                from.getSelectedItems().stream().toList();

        moveItems(from, to, selectedItems);
    }

    protected void updateListBoxesReorderableComparator() {
        SerializableComparator<V> listBoxReorderableComparator = (value1, value2) ->
                Integer.compare(optionsData.indexOf(value1), optionsData.indexOf(value2));
        optionsDataView.setSortComparator(isReorderable() ? listBoxReorderableComparator : null);
        selectedDataView.setSortComparator(isReorderable() ? listBoxReorderableComparator : null);
    }

    protected void updateAllBtnEnabled() {
        moveRightAll.setVisible(isAllBtnEnabled());
        moveLeftAll.setVisible(isAllBtnEnabled());
    }

    protected void updateListBoxesLabelsLayoutVisibility() {
        listBoxesLabelsLayout.setVisible(
                StringUtils.isNotEmpty(optionsLabel.getText()) ||
                        StringUtils.isNotEmpty(selectedLabel.getText()));
    }

    private void updateDoubleClickListeners() {
        optionsListeners.clear();
        selectedListeners.clear();

        updateDoubleClickListeners(options, selected, optionsListeners);
        updateDoubleClickListeners(selected, options, selectedListeners);
    }

    private void updateDoubleClickListeners(JmixMultiSelectListBox<V> from,
                                            JmixMultiSelectListBox<V> to,
                                            Map<Element, Integer> fromDoubleClickListeners) {
        int itemIndex = 0;
        for (Component child : from.getChildren().toList())  {
            fromDoubleClickListeners.put(child.getElement(), itemIndex++);

            child.getElement().addEventListener("dblclick", (DomEventListener) event ->
                    listBoxItemDoubleClicked(from, to, fromDoubleClickListeners.get(event.getSource()))
            );
        }
    }

    private void updateColumnComponents(Collection<V> newPresentationValue) {
        optionsDataView.removeItems(newPresentationValue);
        selectedDataView.addItems(newPresentationValue);
    }

    private void recreateOptions(Collection<V> newOptions) {
        if (optionsData == null) {
            optionsData = new LinkedList<>();
        }

        optionsDataView.removeItems(optionsData);
        optionsData.clear();

        optionsDataView.addItems(newOptions);
        optionsData.addAll(newOptions);

        updateListBoxSelectionStyles(options, optionsDataView);
        updateListBoxSelectionStyles(selected, selectedDataView);
        updateDoubleClickListeners();
    }

    private void updateListBoxSelectionStyles(JmixMultiSelectListBox<V> listBox, ListBoxListDataView<V> listBoxDataView) {
        List<Component> list = listBox.getChildren().toList();
        for (int i = 0; i < list.size(); i++) {
            list.get(i).getElement().removeProperty("position");

            if (!list.get(i).hasClassName("line")) {
                list.get(i).addClassName("line");
            }

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
