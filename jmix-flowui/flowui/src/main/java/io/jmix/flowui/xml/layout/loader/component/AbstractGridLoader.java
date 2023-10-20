/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.xml.layout.loader.component;

import com.google.common.base.Splitter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnRendering;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.jmix.core.*;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.impl.FetchPlanRepositoryImpl;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.core.querycondition.PropertyConditionUtils;
import io.jmix.flowui.app.datagrid.HeaderPropertyFilterLayout;
import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.grid.editor.DataGridEditor;
import io.jmix.flowui.component.propertyfilter.PropertyFilter;
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport;
import io.jmix.flowui.data.aggregation.AggregationStrategy;
import io.jmix.flowui.data.provider.EmptyValueProvider;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.*;
import io.jmix.flowui.model.impl.DataLoadersHelper;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.component.datagrid.RendererProvider;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.datatype.DatatypeElementFactory;
import org.springframework.lang.Nullable;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractGridLoader<T extends Grid & EnhancedDataGrid & HasActions>
        extends AbstractComponentLoader<T> {

    public static final String COLUMN_ELEMENT_NAME = "column";
    public static final String EDITOR_ACTIONS_COLUMN_ELEMENT_NAME = "editorActionsColumn";
    public static final String COLUMN_FILTER_POPUP_CLASSNAME = "column-filter-popup";
    public static final String COLUMN_FILTER_DIALOG_CLASSNAME = "column-filter-dialog";
    public static final String COLUMN_FILTER_FOOTER_SMALL_CLASSNAME = "column-filter-footer-small";
    public static final String ATTRIBUTE_JMIX_ROLE_NAME = "jmix-role";
    public static final String COLUMN_FILTER_BUTTON_ROLE = "column-filter-button";
    public static final String COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME = "activated";

    protected ActionLoaderSupport actionLoaderSupport;
    protected MetadataTools metaDataTools;
    protected Subscription masterDataLoaderPostLoadListener; // used for CollectionPropertyContainer
    protected FetchPlanRepositoryImpl fetchPlanRepository;
    protected ClassManager classManager;
    protected Messages messages;
    protected PropertyFilterSupport propertyFilterSupport;

    @Override
    public void loadComponent() {
        loadInteger(element, "pageSize", resultComponent::setPageSize);
        loadBoolean(element, "rowsDraggable", resultComponent::setRowsDraggable);
        loadBoolean(element, "allRowsVisible", resultComponent::setAllRowsVisible);
        loadEnum(element, GridDropMode.class, "dropMode", resultComponent::setDropMode);
        loadBoolean(element, "detailsVisibleOnClick", resultComponent::setDetailsVisibleOnClick);
        loadEnum(element, SelectionMode.class, "selectionMode", resultComponent::setSelectionMode);
        loadBoolean(element, "columnReorderingAllowed", resultComponent::setColumnReorderingAllowed);
        loadEnum(element, NestedNullBehavior.class, "nestedNullBehavior", resultComponent::setNestedNullBehavior);
        loadBoolean(element, "editorBuffered", editorBuffered ->
                resultComponent.getEditor().setBuffered(editorBuffered));
        loadBoolean(element, "aggregatable", resultComponent::setAggregatable);
        loadEnum(element, EnhancedDataGrid.AggregationPosition.class, "aggregationPosition",
                resultComponent::setAggregationPosition);
        loadEnum(element, ColumnRendering.class, "columnRendering", resultComponent::setColumnRendering);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadTabIndex(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadData();
        loadMultiSort();

        getActionLoaderSupport().loadActions(resultComponent, element);
    }

    protected void loadData() {
        GridDataHolder holder = initDataGridDataHolder();

        if (!holder.isContainerLoaded()
                && holder.getMetaClass() == null) {
            String message = String.format(
                    "%s doesn't have data binding. Set either dataContainer and property or metaClass attribute.",
                    resultComponent.getClass().getSimpleName()
            );

            throw new GuiDevelopmentException(message, context, "Component ID", element.attributeValue("id"));
        }
        Element columns = element.element("columns");

        if (columns != null) {
            FetchPlan fetchPlan = holder.getFetchPlan();
            assert holder.getMetaClass() != null || fetchPlan != null;

            if (fetchPlan == null) {
                fetchPlan = getFetchPlanRepository().getFetchPlan(holder.getMetaClass(), FetchPlan.LOCAL);
            }

            loadColumns(resultComponent, columns, holder, fetchPlan);
        }

        setupDataProvider(holder);
    }

    protected void loadMultiSort() {
        boolean multiSort = loadBoolean(element, "multiSort")
                .orElse(false);
        Grid.MultiSortPriority multiSortPriority = loadEnum(element, Grid.MultiSortPriority.class, "multiSortPriority")
                .orElse(Grid.MultiSortPriority.PREPEND);
        boolean multiSortOnShiftClickOnly = loadBoolean(element, "multiSortOnShiftClickOnly")
                .orElse(false);

        resultComponent.setMultiSort(multiSort, multiSortPriority, multiSortOnShiftClickOnly);
    }

    protected void loadColumns(T resultComponent, Element columnsElement, GridDataHolder holder, FetchPlan fetchPlan) {
        Boolean includeAll = loadBoolean(columnsElement, "includeAll").orElse(false);
        boolean sortable = loadBoolean(columnsElement, "sortable")
                .orElse(true);
        boolean resizable = loadBoolean(columnsElement, "resizable")
                .orElse(false);

        if (includeAll) {
            loadColumnsByInclude(resultComponent, columnsElement, holder, fetchPlan, sortable, resizable);
            // In case of includeAll, EditorActionsColumn will be place at the end
            loadEditorActionsColumns(resultComponent, columnsElement);
        } else {
            List<Element> columnElements = columnsElement.elements();
            for (Element columnElement : columnElements) {
                loadColumnsElementChild(resultComponent, columnElement, holder, sortable, resizable);
            }
        }
    }

    protected void loadColumnsElementChild(T resultComponent, Element columnElement, GridDataHolder holder,
                                           boolean sortableColumns, boolean resizableColumns) {
        switch (columnElement.getName()) {
            case COLUMN_ELEMENT_NAME:
                loadColumn(resultComponent, columnElement, holder.dataLoader,
                        Objects.requireNonNull(holder.getMetaClass()), sortableColumns, resizableColumns);
                break;
            case EDITOR_ACTIONS_COLUMN_ELEMENT_NAME:
                loadEditorActionsColumn(resultComponent, columnElement);
                break;
            default:
                throw new GuiDevelopmentException("Unknown columns' child element: " + columnElement.getName(),
                        context, "Component ID", resultComponent.getId());
        }
    }

    protected void loadEditorActionsColumns(T resultComponent, Element columnsElement) {
        List<Element> editorActionsColumns = columnsElement.elements(EDITOR_ACTIONS_COLUMN_ELEMENT_NAME);
        if (CollectionUtils.isEmpty(editorActionsColumns)) {
            return;
        }

        for (Element columnElement : editorActionsColumns) {
            loadEditorActionsColumn(resultComponent, columnElement);
        }
    }

    protected void loadEditorActionsColumn(T resultComponent, Element columnElement) {
        if (columnElement.elements().size() == 0) {
            throw new GuiDevelopmentException("'editorActionsColumn' cannot be empty",
                    context, "Component ID", resultComponent.getId());
        }

        Editor editor = resultComponent.getEditor();
        Grid.Column<?> editColumn = createEditColumn(resultComponent, columnElement, editor);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setPadding(false);

        Button saveButton = loadEditorButton(columnElement, "saveButton");
        if (saveButton != null) {
            saveButton.addClickListener(__ -> editor.save());
            actions.add(saveButton);
        }

        Button cancelButton = loadEditorButton(columnElement, "cancelButton");
        if (cancelButton != null) {
            cancelButton.addClickListener(__ -> editor.cancel());
            actions.add(cancelButton);
        }

        Button closeButton = loadEditorButton(columnElement, "closeButton");
        if (closeButton != null) {
            closeButton.addClickListener(__ -> editor.closeEditor());
            actions.add(closeButton);
        }

        editColumn.setEditorComponent(actions);

        loadString(columnElement, "key", editColumn::setKey);
        loadString(columnElement, "width", editColumn::setWidth);
        loadBoolean(columnElement, "autoWidth", editColumn::setAutoWidth);
        loadBoolean(columnElement, "resizable", editColumn::setResizable);
        loadInteger(columnElement, "flexGrow", editColumn::setFlexGrow);
        loadResourceString(columnElement, "header", context.getMessageGroup(), editColumn::setHeader);
        loadResourceString(columnElement, "footer", context.getMessageGroup(), editColumn::setFooter);
        loadBoolean(columnElement, "visible", editColumn::setVisible);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Column<?> createEditColumn(T resultComponent, Element columnElement, Editor editor) {
        return resultComponent.addComponentColumn(item -> {
            Button editButton = loadEditorButton(columnElement, "editButton");
            if (editButton != null) {
                editButton.addClickListener(__ -> {
                    if (editor.isOpen()) {
                        editor.cancel();
                    }
                    editor.editItem(item);
                });
                return editButton;
            } else {
                // Vaadin throws NPE if null is returned
                return new Span();
            }
        });
    }

    @Nullable
    protected Button loadEditorButton(Element columnElement, String buttonElementName) {
        Element buttonElement = columnElement.element(buttonElementName);
        if (buttonElement != null) {
            JmixButton button = factory.create(JmixButton.class);

            componentLoader().loadText(button, buttonElement);
            componentLoader().loadIcon(buttonElement, button::setIcon);
            componentLoader().loadTitle(button, buttonElement, context);
            componentLoader().loadClassNames(button, buttonElement);
            componentLoader().loadThemeNames(button, buttonElement);
            loadBoolean(buttonElement, "iconAfterText", button::setIconAfterText);

            return button;
        }

        return null;
    }

    protected void loadColumnsByInclude(T component, Element columnsElement, GridDataHolder holder,
                                        FetchPlan fetchPlan, boolean sortableColumns, boolean resizableColumns) {
        MetaClass metaClass = Objects.requireNonNull(holder.getMetaClass());
        Collection<String> appliedProperties = getAppliedProperties(columnsElement, fetchPlan, metaClass);

        List<Element> columnElements = columnsElement.elements(COLUMN_ELEMENT_NAME);
        Set<Element> overriddenColumns = new HashSet<>();

        DocumentFactory documentFactory = DatatypeElementFactory.getInstance();
        DataLoader dataLoader = holder.getDataLoader();

        for (String property : appliedProperties) {
            Element column = getOverriddenColumn(columnElements, property);
            if (column == null) {
                column = documentFactory.createElement(COLUMN_ELEMENT_NAME);
                column.add(documentFactory.createAttribute(column, "property", property));
            } else {
                overriddenColumns.add(column);
            }

            loadColumn(component, column, dataLoader, metaClass, sortableColumns, resizableColumns);
        }

        // load remains columns
        List<Element> remainedColumns = columnsElement.elements(COLUMN_ELEMENT_NAME);
        for (Element column : remainedColumns) {
            if (overriddenColumns.contains(column)) {
                continue;
            }

            // check property and add
            String propertyId = loadString(column, "property").orElse(null);
            if (propertyId != null) {
                MetaPropertyPath propertyPath = metaClass.getPropertyPath(propertyId);
                if (propertyPath == null || getMetaDataTools().fetchPlanContainsProperty(fetchPlan, propertyPath)) {
                    loadColumn(component, column, dataLoader, metaClass, sortableColumns, resizableColumns);
                }
            }
        }
    }

    protected void loadColumn(T component, Element element, @Nullable DataLoader dataLoader, MetaClass metaClass,
                              boolean sortableColumns, boolean resizableColumns) {
        String property = loadString(element, "property")
                .orElse(null);

        MetaPropertyPath metaPropertyPath = property != null
                ? getMetaDataTools().resolveMetaPropertyPathOrNull(metaClass, property)
                : null;

        String key = loadString(element, "key")
                .orElseGet(() -> {
                    // We check 'metaPropertyPath' but returns 'property', because we need
                    // a string that matches the meta property, i.e. if 'metaPropertyPath'
                    // is found for `property` then this `property` can be used as a key.
                    if (metaPropertyPath != null) {
                        return property;
                    } else {
                        throw new GuiDevelopmentException("Either key or property must be defined for a column",
                                context, "Component ID", component.getId());
                    }
                });

        Column<?> column = metaPropertyPath != null
                ? addColumn(key, metaPropertyPath)
                : addEmptyColumn(key);

        loadString(element, "width", column::setWidth);
        loadResourceString(element, "header", context.getMessageGroup(), column::setHeader);
        loadResourceString(element, "footer", context.getMessageGroup(), column::setFooter);
        loadBoolean(element, "frozen", column::setFrozen);
        loadInteger(element, "flexGrow", column::setFlexGrow);
        loadBoolean(element, "autoWidth", column::setAutoWidth);
        loadBoolean(element, "visible", column::setVisible);
        loadEnum(element, ColumnTextAlign.class, "textAlign", column::setTextAlign);

        loadColumnSortable(element, column, sortableColumns);
        loadColumnResizable(element, column, resizableColumns);
        loadColumnFilterable(element, column, dataLoader, metaClass, property);
        loadColumnEditable(element, column, property);
        loadAggregationInfo(element, column);

        loadRenderer(element, metaPropertyPath)
                .ifPresent(column::setRenderer);
    }

    @SuppressWarnings("unchecked")
    protected Column<?> addEmptyColumn(String key) {
        return resultComponent.addColumn(new EmptyValueProvider<>())
                .setKey(key);
    }

    @SuppressWarnings("rawtypes")
    protected Optional<Renderer> loadRenderer(Element columnElement, @Nullable MetaPropertyPath metaPropertyPath) {
        if (columnElement.elements().isEmpty()
                || metaPropertyPath == null) {
            return Optional.empty();
        }

        Map<String, RendererProvider> providers = applicationContext.getBeansOfType(RendererProvider.class);
        for (RendererProvider<?> provider : providers.values()) {
            for (Element element : columnElement.elements()) {
                if (provider.supports(element.getName())) {
                    return Optional.of(provider.createRenderer(element, metaPropertyPath, context));
                }
            }
        }

        return Optional.empty();
    }

    protected void loadColumnSortable(Element element, Column<?> column, boolean sortableColumns) {
        loadBoolean(element, "sortable")
                .ifPresentOrElse(column::setSortable, () -> column.setSortable(sortableColumns));
    }

    protected void loadColumnResizable(Element element, Column<?> column, boolean resizableColumns) {
        loadBoolean(element, "resizable")
                .ifPresentOrElse(column::setResizable, () -> column.setResizable(resizableColumns));
    }

    protected void loadColumnFilterable(Element element, Column<?> column, @Nullable DataLoader dataLoader,
                                        MetaClass metaClass, String property) {
        loadBoolean(element, "filterable")
                .ifPresent(filterable -> {
                    if (!filterable) {
                        return;
                    }

                    if (dataLoader == null) {
                        throw new GuiDevelopmentException(resultComponent.getClass().getSimpleName() +
                                " with a filterable column must have a DataLoader",
                                context, "Component ID", resultComponent.getId());
                    }

                    setFilterComponent(column, dataLoader, metaClass, property);
                });
    }

    protected void setFilterComponent(Column<?> column, DataLoader dataLoader,
                                      MetaClass metaClass, String property) {
        PropertyFilter<?> propertyFilter = createPropertyFilter(dataLoader, metaClass, property);
        Component filterButton = createFilterButton(propertyFilter);

        Component headerComponent = createHeaderComponent(column.getHeaderText(), filterButton);
        column.setHeader(headerComponent);
    }

    protected PropertyFilter<?> createPropertyFilter(DataLoader dataLoader, MetaClass metaClass, String property) {
        PropertyFilter<?> propertyFilter = factory.create(PropertyFilter.class);

        propertyFilter.setDataLoader(dataLoader);
        propertyFilter.setProperty(property);
        propertyFilter.setAutoApply(false);

        propertyFilter.setOperation(getPropertyFilterSupport().getDefaultOperation(metaClass, property));
        propertyFilter.setOperationEditable(true);
        propertyFilter.setParameterName(PropertyConditionUtils.generateParameterName(property));
        propertyFilter.setWidthFull();

        return propertyFilter;
    }

    protected Component createFilterButton(PropertyFilter<?> propertyFilter) {
        JmixButton filterButton = factory.create(JmixButton.class);
        filterButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_ICON);
        filterButton.setIcon(VaadinIcon.FILTER.create());
        filterButton.setClassName(LumoUtility.TextColor.TERTIARY);
        filterButton.getElement().setAttribute(ATTRIBUTE_JMIX_ROLE_NAME, COLUMN_FILTER_BUTTON_ROLE);

        // Workaround (waiting for overlay component),
        // when device is small - standard dialog is used
        Dialog overlay = createOverlay(propertyFilter, filterButton);

        filterButton.addClickListener(__ -> {
            overlay.open();

            if (!isSmallDevice()) {
                overlay.getElement().executeJs(getOverlayPositionExpression(), overlay, filterButton);
            }
        });

        return filterButton;
    }

    @SuppressWarnings({"rawtypes"})
    protected Dialog createOverlay(PropertyFilter propertyFilter, JmixButton filterButton) {
        JmixButton clearButton = createClearFilterButton(propertyFilter);

        HeaderPropertyFilterLayout headerPropertyFilterLayout = factory.create(HeaderPropertyFilterLayout.class);
        headerPropertyFilterLayout.getContent().add(propertyFilter, clearButton);

        Dialog dialog = new Dialog(headerPropertyFilterLayout);
        dialog.addClassName(COLUMN_FILTER_DIALOG_CLASSNAME);

        if (!isSmallDevice()) {
            dialog.addClassName(COLUMN_FILTER_POPUP_CLASSNAME);
        } else {
            dialog.addClassName(COLUMN_FILTER_FOOTER_SMALL_CLASSNAME);
        }

        AtomicReference appliedValue = new AtomicReference<>();

        dialog.getFooter().add(
                createApplyButton(propertyFilter, dialog, appliedValue),
                createCancelButton(propertyFilter, dialog, appliedValue)
        );

        dialog.addOpenedChangeListener(event -> onDialogOpen(event, propertyFilter, filterButton));

        dialog.addDialogCloseActionListener(__ -> doCancel(propertyFilter, dialog, appliedValue));

        return dialog;
    }

    @SuppressWarnings("rawtypes")
    protected void onDialogOpen(Dialog.OpenedChangeEvent event, PropertyFilter propertyFilter,
                                JmixButton filterButton) {
        if (event.isOpened()) {
            propertyFilter.focus();
        } else {
            filterButton.getElement().setAttribute(COLUMN_FILTER_BUTTON_ACTIVATED_ATTRIBUTE_NAME,
                    propertyFilter.getValue() != null);
        }
    }

    @SuppressWarnings("rawtypes")
    protected JmixButton createClearFilterButton(PropertyFilter propertyFilter) {
        JmixButton clearButton = factory.create(JmixButton.class);

        clearButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        clearButton.setIcon(VaadinIcon.ERASER.create());
        clearButton.addClickListener(__ -> propertyFilter.clear());

        return clearButton;
    }

    @SuppressWarnings({"rawtypes"})
    protected JmixButton createApplyButton(PropertyFilter propertyFilter, Dialog dialog, AtomicReference appliedValue) {
        JmixButton applyButton = factory.create(JmixButton.class);
        applyButton.setIcon(VaadinIcon.CHECK.create());
        applyButton.setText(getMessages().getMessage("columnFilter.apply.text"));

        applyButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyButton.addClickListener(__ -> doApply(propertyFilter, dialog, appliedValue));

        if (isSmallDevice()) {
            applyButton.getStyle().set("flex-grow", "1");
        }

        return applyButton;
    }

    @SuppressWarnings("rawtypes")
    protected JmixButton createCancelButton(PropertyFilter propertyFilter, Dialog dialog, AtomicReference appliedValue) {
        JmixButton cancelButton = factory.create(JmixButton.class);
        cancelButton.setIcon(VaadinIcon.BAN.create());
        cancelButton.setText(getMessages().getMessage("columnFilter.cancel.text"));

        cancelButton.addClickListener(__ -> doCancel(propertyFilter, dialog, appliedValue));

        if (isSmallDevice()) {
            cancelButton.getStyle().set("flex-grow", "1");
        }

        return cancelButton;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void doApply(PropertyFilter propertyFilter, Dialog dialog, AtomicReference appliedValue) {
        propertyFilter.getDataLoader().load();
        appliedValue.set(propertyFilter.getValue());

        dialog.close();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void doCancel(PropertyFilter propertyFilter, Dialog dialog, AtomicReference appliedValue) {
        propertyFilter.setValue(appliedValue.get());

        dialog.close();
    }

    protected Component createHeaderComponent(String headerText, Component filterButton) {
        HorizontalLayout layout = factory.create(HorizontalLayout.class);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.setClassName(LumoUtility.Gap.XSMALL);

        layout.add(new Span(headerText), filterButton);
        return layout;
    }

    protected void loadColumnEditable(Element element, Column<?> column, String property) {
        loadBoolean(element, "editable", editable -> {
            if (Boolean.TRUE.equals(editable)) {
                setDefaultEditComponent(column, property);
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected void loadAggregationInfo(Element columnElement, Column<?> column) {
        Element aggregationElement = columnElement.element("aggregation");

        if (aggregationElement != null) {
            AggregationInfo aggregation = new AggregationInfo();

            aggregation.setPropertyPath(resultComponent.getColumnMetaPropertyPath(column));

            loadEnum(aggregationElement, AggregationInfo.Type.class, "type", aggregation::setType);
            loadResourceString(aggregationElement, "cellTitle", context.getMessageGroup(), aggregation::setCellTitle);
            loadStrategyClassFqn(aggregation, aggregationElement);
            componentLoader().loadFormatter(aggregation, aggregationElement);

            resultComponent.addAggregation(column, aggregation);

            if (aggregation.getType() == null && aggregation.getStrategy() == null) {
                throw new GuiDevelopmentException("Incorrect aggregation - type or strategyClass is required", context);
            }
        }
    }

    protected void loadStrategyClassFqn(AggregationInfo aggregation, Element element) {
        loadString(element, "strategyClass")
                .ifPresent(strategyClass -> {
                    Class<?> aggregationClass = getClassManager().findClass(strategyClass);

                    if (aggregationClass == null) {
                        String message = String.format("Aggregation class %s is not found", strategyClass);
                        throw new GuiDevelopmentException(message, context);
                    }

                    try {
                        Constructor<?> constructor = aggregationClass.getDeclaredConstructor();
                        AggregationStrategy<?, ?> customStrategy =
                                (AggregationStrategy<?, ?>) constructor.newInstance();
                        applicationContext.getAutowireCapableBeanFactory().autowireBean(customStrategy);
                        aggregation.setStrategy(customStrategy);
                    } catch (Exception e) {
                        throw new RuntimeException("Unable to instantiate strategy for aggregation", e);
                    }
                });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void setDefaultEditComponent(Column<?> column, String property) {
        Editor<?> editor = resultComponent.getEditor();
        if (editor instanceof DataGridEditor) {
            ((DataGridEditor) editor).initColumnDefaultEditorComponent(column, property);
        }
    }

    protected Column<?> addColumn(String key, MetaPropertyPath metaPropertyPath) {
        return resultComponent.addColumn(key, metaPropertyPath);
    }

    protected Collection<String> getAppliedProperties(Element columnsElement,
                                                      @Nullable FetchPlan fetchPlan, MetaClass metaClass) {
        String exclude = loadString(columnsElement, "exclude").orElse(null);
        List<String> excludes = exclude == null ? Collections.emptyList() :
                Splitter.on(",").omitEmptyStrings().trimResults().splitToList(exclude);

        Stream<String> properties;
        if (getMetaDataTools().isJpaEntity(metaClass) && fetchPlan != null) {
            properties = fetchPlan.getProperties().stream().map(FetchPlanProperty::getName);
        } else {
            properties = metaClass.getProperties().stream().map(MetadataObject::getName);
        }

        return properties.filter(s -> !excludes.contains(s)).collect(Collectors.toList());
    }

    @Nullable
    protected Element getOverriddenColumn(List<Element> columns, String property) {
        for (Element element : columns) {
            String propertyAttr = element.attributeValue("property");
            if (StringUtils.isNotEmpty(propertyAttr) && propertyAttr.equals(property)) {
                return element;
            }
        }
        return null;
    }

    protected void loadMetaClass(Element element, Consumer<MetaClass> setter) {
        loadString(element, "metaClass", metaClassStr ->
                setter.accept(applicationContext.getBean(Metadata.class).getClass(metaClassStr)));
    }

    protected void initMasterDataLoaderListener(CollectionPropertyContainer<?> collectionContainer) {
        DataLoader masterDataLoader = DataLoadersHelper.getMasterDataLoader(collectionContainer);

        masterDataLoaderPostLoadListener = masterDataLoader instanceof InstanceLoader
                ? ((InstanceLoader<?>) masterDataLoader).addPostLoadListener(this::onMasterDataLoaderPostLoad)
                : masterDataLoader instanceof CollectionLoader
                ? ((CollectionLoader<?>) masterDataLoader).addPostLoadListener(this::onMasterDataLoaderPostLoad)
                : null;
    }

    protected GridDataHolder initDataGridDataHolder() {
        GridDataHolder holder = new GridDataHolder();

        String containerId = loadString(element, "dataContainer").orElse(null);
        if (containerId == null) {
            loadMetaClass(element, holder::setMetaClass);
            return holder;
        }

        InstanceContainer<?> container = getComponentContext().getViewData().getContainer(containerId);

        CollectionContainer<?> collectionContainer;
        if (container instanceof CollectionContainer) {
            collectionContainer = (CollectionContainer<?>) container;
        } else {
            throw new GuiDevelopmentException("Not a CollectionContainer: " + containerId, context);
        }

        if (collectionContainer instanceof CollectionPropertyContainer) {
            initMasterDataLoaderListener((CollectionPropertyContainer<?>) collectionContainer);
        }

        if (collectionContainer instanceof HasLoader) {
            //noinspection ConstantConditions
            holder.setDataLoader(((HasLoader) collectionContainer).getLoader());
        }

        holder.setMetaClass(collectionContainer.getEntityMetaClass());
        holder.setContainer(collectionContainer);
        //noinspection ConstantConditions
        holder.setFetchPlan(collectionContainer.getFetchPlan());

        return holder;
    }

    /**
     * Contains information about metaClass, data container, loader, fetch plan.
     */
    protected static class GridDataHolder {


        protected MetaClass metaClass;

        protected CollectionContainer<?> container;
        protected DataLoader dataLoader;
        protected FetchPlan fetchPlan;

        public GridDataHolder() {
        }

        @Nullable
        public MetaClass getMetaClass() {
            return metaClass;
        }

        public void setMetaClass(MetaClass metaClass) {
            this.metaClass = metaClass;
        }

        @Nullable
        public CollectionContainer<?> getContainer() {
            return container;
        }

        public void setContainer(CollectionContainer<?> container) {
            this.container = container;
        }

        @Nullable
        public DataLoader getDataLoader() {
            return dataLoader;
        }

        public void setDataLoader(DataLoader dataLoader) {
            this.dataLoader = dataLoader;
        }

        @Nullable
        public FetchPlan getFetchPlan() {
            return fetchPlan;
        }

        public void setFetchPlan(FetchPlan fetchPlan) {
            this.fetchPlan = fetchPlan;
        }

        public boolean isContainerLoaded() {
            return container != null;
        }
    }

    protected void onMasterDataLoaderPostLoad(Object o) {
        if (masterDataLoaderPostLoadListener != null) {
            masterDataLoaderPostLoadListener.remove();
        }
    }

    protected FetchPlanRepositoryImpl getFetchPlanRepository() {
        if (fetchPlanRepository == null) {
            fetchPlanRepository = applicationContext.getBean(FetchPlanRepositoryImpl.class, context);
        }
        return fetchPlanRepository;
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }

    protected MetadataTools getMetaDataTools() {
        if (metaDataTools == null) {
            metaDataTools = applicationContext.getBean(MetadataTools.class, context);
        }
        return metaDataTools;
    }

    protected ClassManager getClassManager() {
        if (classManager == null) {
            classManager = applicationContext.getBean(ClassManager.class);
        }
        return classManager;
    }

    protected PropertyFilterSupport getPropertyFilterSupport() {
        if (propertyFilterSupport == null) {
            propertyFilterSupport = applicationContext.getBean(PropertyFilterSupport.class, context);
        }
        return propertyFilterSupport;
    }

    protected Messages getMessages() {
        if (messages == null) {
            messages = applicationContext.getBean(Messages.class);
        }

        return messages;
    }

    protected String getOverlayPositionExpression() {
        return "$0.$.overlay.$.overlay.style['top'] = $1.getBoundingClientRect().top + 'px';" +
                "const sum = $1.getBoundingClientRect().left + $1.getBoundingClientRect().width " +
                "+ $0.$.overlay.$.overlay.getBoundingClientRect().width;" +
                "if (sum < window.innerWidth) { " +
                "$0.$.overlay.$.overlay.style['left'] = $1.getBoundingClientRect().left + 'px'; " +
                "} else { " +
                "$0.$.overlay.$.overlay.style['right'] = window.innerWidth - $1.getBoundingClientRect().left " +
                "- $1.getBoundingClientRect().width + 'px';" +
                "}";
    }

    protected boolean isSmallDevice() {
        // magic number from vaadin-app-layout.js
        // '--vaadin-app-layout-touch-optimized' style property
        return UI.getCurrent().getInternals().getExtendedClientDetails().getScreenWidth() < 801;
    }

    protected abstract void setupDataProvider(GridDataHolder holder);
}
