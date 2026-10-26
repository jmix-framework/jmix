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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.grid.Grid.NestedNullBehavior;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.grid.contextmenu.GridSubMenu;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.*;
import io.jmix.core.accesscontext.InMemoryCrudEntityContext;
import io.jmix.core.common.event.Subscription;
import io.jmix.core.impl.FetchPlanRepositoryImpl;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.core.metamodel.model.MetadataObject;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.component.AggregationInfo;
import io.jmix.flowui.component.HasLengthLimited;
import io.jmix.flowui.component.HasRequired;
import io.jmix.flowui.component.SupportsMetaClass;
import io.jmix.flowui.component.SupportsStatusChangeHandler;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.grid.DataGridColumn;
import io.jmix.flowui.component.grid.EnhancedDataGrid;
import io.jmix.flowui.component.grid.GridContextMenuItemComponent;
import io.jmix.flowui.component.grid.editor.DataGridEditor;
import io.jmix.flowui.component.grid.editor.EditComponentGenerationContext;
import io.jmix.flowui.data.SupportsValueSource;
import io.jmix.flowui.data.aggregation.AggregationStrategy;
import io.jmix.flowui.data.provider.EmptyValueProvider;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.component.HasActions;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.kit.component.grid.JmixGridContextMenu;
import io.jmix.flowui.kit.xml.layout.support.LoaderUtils;
import io.jmix.flowui.model.*;
import io.jmix.flowui.model.impl.DataLoadersHelper;
import io.jmix.flowui.view.ReadOnlyTracker;
import io.jmix.flowui.view.ReadOnlyTracker.ReadOnlyChangeEvent;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.inittask.AssignActionInitTask;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.component.datagrid.RendererProvider;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import io.jmix.flowui.xml.layout.support.IconLoaderSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.datatype.DatatypeElementFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractGridLoader<T extends Grid & EnhancedDataGrid & HasActions>
        extends AbstractComponentLoader<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractGridLoader.class);

    public static final String COLUMN_ELEMENT_NAME = "column";
    public static final String EDITOR_ACTIONS_COLUMN_ELEMENT_NAME = "editorActionsColumn";
    public static final String EDITOR_COMPONENT_ELEMENT_NAME = "editorComponent";

    protected ActionLoaderSupport actionLoaderSupport;
    protected MetadataTools metaDataTools;
    protected Messages messages;
    protected Subscription masterDataLoaderPostLoadListener; // used for CollectionPropertyContainer
    protected FetchPlanRepositoryImpl fetchPlanRepository;
    protected ClassManager classManager;
    protected AccessManager accessManager;
    protected IconLoaderSupport iconLoaderSupport;

    protected List<DataGridColumn<?>> pendingToFilterableColumns = new ArrayList<>();
    protected Map<Element, ComponentLoader<?>> rowCellComponentLoaders = new HashMap<>();

    @Override
    public void initComponent() {
        super.initComponent();

        createRowCellComponents("headerRows", "headerRow");
        createRowCellComponents("footerRows", "footerRow");
    }

    @Override
    public void loadComponent() {
        loadInteger(element, "pageSize", resultComponent::setPageSize);
        loadBoolean(element, "rowsDraggable", resultComponent::setRowsDraggable);
        loadResourceString(element, "emptyStateText", context.getMessageGroup(),
                resultComponent::setEmptyStateText);
        loadResourceString(element, "ariaLabel", context.getMessageGroup(), resultComponent::setAriaLabel);
        loadBoolean(element, "allRowsVisible", resultComponent::setAllRowsVisible);
        loadEnum(element, GridDropMode.class, "dropMode", resultComponent::setDropMode);
        loadBoolean(element, "detailsVisibleOnClick", resultComponent::setDetailsVisibleOnClick);
        loadEnum(element, SelectionMode.class, "selectionMode", this::setSelectionMode);
        loadBoolean(element, "columnReorderingAllowed", resultComponent::setColumnReorderingAllowed);
        loadEnum(element, NestedNullBehavior.class, "nestedNullBehavior", resultComponent::setNestedNullBehavior);
        loadBoolean(element, "editorBuffered", editorBuffered ->
                resultComponent.getEditor().setBuffered(editorBuffered));
        loadEnum(element, EnhancedDataGrid.AggregationPosition.class, "aggregationPosition",
                resultComponent::setAggregationPosition);
        loadEnum(element, ColumnRendering.class, "columnRendering", resultComponent::setColumnRendering);

        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadThemeNames(resultComponent, element);
        componentLoader().loadFocusableAttributes(resultComponent, element);
        componentLoader().loadClassNames(resultComponent, element);
        componentLoader().loadSizeAttributes(resultComponent, element);

        loadI18n();
        loadData();
        loadMultiSort();

        loadContextMenu();
        loadEmptyStateComponent();
        loadActions();
    }

    protected void loadI18n() {
        Element i18nElement = element.element("i18n");
        if (i18nElement == null) {
            return;
        }

        GridI18n i18n = Objects.requireNonNullElseGet(resultComponent.getI18n(), GridI18n::new);
        loadResourceString(i18nElement, "selectAll", context.getMessageGroup(), i18n::setSelectAll);
        loadResourceString(i18nElement, "selectAllUnavailable", context.getMessageGroup(),
                i18n::setSelectAllUnavailable);
        loadResourceString(i18nElement, "selectRow", context.getMessageGroup(), i18n::setSelectRow);
        loadResourceString(i18nElement, "sorter", context.getMessageGroup(), i18n::setSorter);
        resultComponent.setI18n(i18n);
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

            loadColumns(resultComponent, columns, holder.getMetaClass(), fetchPlan);
        }

        setupDataProvider(holder);

        // filters must be initialized after the data provider
        // check for parent was implemented for the case
        // when a column was deleted due to security constraints
        pendingToFilterableColumns.forEach(column ->
                column.getParent()
                        .ifPresent(__ -> column.setFilterable(true))
        );
        pendingToFilterableColumns.clear();

        // header and footer rows join columns into groups, so they are built after the filters too
        loadHeaderRows();
        loadFooterRows();

        // aggregation must be initialized after filters
        // since Vaadin Framework groups columns when appending a header row, it is necessary to
        // load the aggregation after the filters
        // otherwise, when installing a filter, it will be impossible to get actual column from the public API
        loadBoolean(element, "aggregatable", resultComponent::setAggregatable);
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

    protected void createRowCellComponents(String rowsElementName, String rowElementName) {
        Element rowsElement = element.element(rowsElementName);
        if (rowsElement == null) {
            return;
        }

        for (Element rowElement : rowsElement.elements(rowElementName)) {
            for (Element cellElement : rowElement.elements("cell")) {
                List<Element> componentElements = cellElement.elements();
                if (componentElements.isEmpty()) {
                    continue;
                }

                String cellColumns = getCellColumnsValue(rowElement, cellElement);
                if (componentElements.size() > 1) {
                    throw new GuiDevelopmentException("'%s' cell '%s' must contain at most one component element"
                            .formatted(rowElementName, cellColumns), context);
                }

                if (loadString(cellElement, "text").isPresent()) {
                    throw new GuiDevelopmentException(
                            "'%s' cell '%s' cannot have both the 'text' attribute and a component"
                                    .formatted(rowElementName, cellColumns), context);
                }

                ComponentLoader<?> componentLoader =
                        getLayoutLoader().createComponentLoader(componentElements.get(0));
                componentLoader.initComponent();
                rowCellComponentLoaders.put(cellElement, componentLoader);
            }
        }
    }

    protected void loadHeaderRows() {
        Element headerRowsElement = element.element("headerRows");
        if (headerRowsElement == null) {
            return;
        }

        if (resultComponent.getHeaderRows().isEmpty()) {
            // The first created header row becomes the default one that holds the column headers
            // and cannot have joined cells. Without content, it is hidden on the client side.
            resultComponent.appendHeaderRow().getCells()
                    .forEach(cell -> cell.setText(null));
        }

        // Cells can be joined only in the top-most header row, so the rows are prepended bottom-up
        for (Element rowElement : Lists.reverse(headerRowsElement.elements("headerRow"))) {
            HeaderRow headerRow = resultComponent.prependHeaderRow();
            validateRowCells(rowElement);

            for (Element cellElement : rowElement.elements("cell")) {
                List<Grid.Column<?>> columns = getCellColumns(rowElement, cellElement);
                if (columns.isEmpty()) {
                    continue;
                }

                // getCell is declared in the package-private AbstractRow, so it is not used as a method reference
                HeaderRow.HeaderCell cell = joinCells(rowElement, cellElement, columns,
                        column -> headerRow.getCell(column), headerRow::join);
                loadResourceString(cellElement, "text", context.getMessageGroup(), cell::setText);
                loadString(cellElement, "partName", cell::setPartName);
                loadCellComponent(cellElement, cell::setComponent);
            }
        }
    }

    protected void loadFooterRows() {
        Element footerRowsElement = element.element("footerRows");
        if (footerRowsElement == null) {
            return;
        }

        if (resultComponent.getFooterRows().isEmpty()) {
            // The top-most footer row holds the column footers and cannot have joined cells,
            // so it is created first. Without content, it is hidden on the client side.
            resultComponent.appendFooterRow().getCells()
                    .forEach(cell -> cell.setText(null));
        }

        // Cells can be joined only in the bottom-most footer row, so the rows are appended top-down
        for (Element rowElement : footerRowsElement.elements("footerRow")) {
            FooterRow footerRow = resultComponent.appendFooterRow();
            validateRowCells(rowElement);

            for (Element cellElement : rowElement.elements("cell")) {
                List<Grid.Column<?>> columns = getCellColumns(rowElement, cellElement);
                if (columns.isEmpty()) {
                    continue;
                }

                FooterRow.FooterCell cell = joinCells(rowElement, cellElement, columns,
                        column -> footerRow.getCell(column), footerRow::join);
                loadResourceString(cellElement, "text", context.getMessageGroup(), cell::setText);
                loadString(cellElement, "partName", cell::setPartName);
                loadCellComponent(cellElement, cell::setComponent);
            }
        }
    }

    protected void validateRowCells(Element rowElement) {
        Set<String> rowColumnKeys = new HashSet<>();
        for (Element cellElement : rowElement.elements("cell")) {
            Set<String> cellColumnKeys = new LinkedHashSet<>(
                    LoaderUtils.split(getCellColumnsValue(rowElement, cellElement)));

            for (String key : cellColumnKeys) {
                if (!rowColumnKeys.add(key)) {
                    throw new GuiDevelopmentException("Column '%s' is listed in more than one cell of a '%s'"
                            .formatted(key, rowElement.getName()), context);
                }
            }
        }
    }

    protected String getCellColumnsValue(Element rowElement, Element cellElement) {
        return loadString(cellElement, "columns")
                .orElseThrow(() -> new GuiDevelopmentException("'%s' cell must define the 'columns' attribute"
                        .formatted(rowElement.getName()), context));
    }

    protected List<Grid.Column<?>> getCellColumns(Element rowElement, Element cellElement) {
        String cellColumns = getCellColumnsValue(rowElement, cellElement);

        List<Grid.Column<?>> columns = new ArrayList<>();
        for (String key : new LinkedHashSet<>(LoaderUtils.split(cellColumns))) {
            Grid.Column<?> column = resultComponent.getColumnByKey(key);
            if (column == null) {
                throw new GuiDevelopmentException("'%s' cell '%s' refers to unknown column '%s'"
                        .formatted(rowElement.getName(), cellColumns, key), context);
            }

            // A column hidden by security is still known to the grid but is detached from it
            if (column.getParent().isPresent()) {
                columns.add(column);
            }
        }

        if (columns.isEmpty()) {
            log.debug("'{}' cell '{}' of {} is skipped: all its columns are hidden by security",
                    rowElement.getName(), cellColumns, resultComponent.getId().orElse(null));
        }

        return columns;
    }

    @SuppressWarnings("unchecked")
    protected <C> C joinCells(Element rowElement, Element cellElement, List<Grid.Column<?>> columns,
                              Function<Grid.Column<?>, C> cellProvider, Function<List<C>, C> joiner) {
        List<C> cells = columns.stream()
                .map(cellProvider)
                .distinct()
                .toList();

        // A column joined in another row resolves to the cell of its whole group,
        // so the cells may cover more columns than the declared ones
        List<Grid.Column<?>> gridColumns = resultComponent.getColumns();
        String coveredColumns = gridColumns.stream()
                .filter(column -> !columns.contains(column)
                        && cells.contains(cellProvider.apply(column)))
                .map(column -> "'" + column.getKey() + "'")
                .collect(Collectors.joining(", "));

        String cellColumns = getCellColumnsValue(rowElement, cellElement);
        if (!coveredColumns.isEmpty()) {
            throw new GuiDevelopmentException(
                    "'%s' cell '%s' would also cover columns %s joined with its columns in another row"
                            .formatted(rowElement.getName(), cellColumns, coveredColumns), context);
        }

        if (cells.size() == 1) {
            return cells.get(0);
        }

        try {
            return joiner.apply(cells);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            throw new GuiDevelopmentException("'%s' cell '%s' cannot join its columns: %s"
                    .formatted(rowElement.getName(), cellColumns, e.getMessage()), context);
        }
    }

    protected void loadCellComponent(Element cellElement, Consumer<Component> setter) {
        ComponentLoader<?> componentLoader = rowCellComponentLoaders.get(cellElement);
        if (componentLoader != null) {
            setter.accept(componentLoader.getResultComponent());
            componentLoader.loadComponent();
        }
    }

    protected void loadColumns(T resultComponent, Element columnsElement, MetaClass metaClass, FetchPlan fetchPlan) {
        Boolean includeAll = loadBoolean(columnsElement, "includeAll").orElse(false);
        boolean sortable = loadBoolean(columnsElement, "sortable")
                .orElse(true);
        boolean resizable = loadBoolean(columnsElement, "resizable")
                .orElse(false);
        boolean filterable = loadBoolean(columnsElement, "filterable")
                .orElse(false);

        loadString(columnsElement, "headerFilterApplyShortcut",
                resultComponent::setHeaderFilterApplyShortcut);

        if (columnsElement.elements(EDITOR_ACTIONS_COLUMN_ELEMENT_NAME).size() > 1) {
            throw new GuiDevelopmentException("DataGrid can contain only one editorActionsColumn",
                    context, "Component ID", resultComponent.getId());
        }

        ColumnDefaultValues columnDefaultValues = new ColumnDefaultValues(sortable, resizable, filterable);
        if (includeAll) {
            loadColumnsByInclude(resultComponent, columnsElement, metaClass, fetchPlan, columnDefaultValues);
            // In case of includeAll, EditorActionsColumn will be place at the end
            loadEditorActionsColumns(resultComponent, columnsElement, metaClass);
        } else {
            List<Element> columnElements = columnsElement.elements();
            for (Element columnElement : columnElements) {
                loadColumnsElementChild(resultComponent, columnElement, metaClass, columnDefaultValues);
            }
        }
    }

    protected void loadColumnsElementChild(T resultComponent, Element columnElement, MetaClass metaClass,
                                           ColumnDefaultValues columnDefaultValues) {
        switch (columnElement.getName()) {
            case COLUMN_ELEMENT_NAME:
                loadColumn(resultComponent, columnElement, metaClass, columnDefaultValues);
                break;
            case EDITOR_ACTIONS_COLUMN_ELEMENT_NAME:
                loadEditorActionsColumn(resultComponent, columnElement, metaClass);
                break;
            default:
                throw new GuiDevelopmentException("Unknown columns' child element: " + columnElement.getName(),
                        context, "Component ID", resultComponent.getId());
        }
    }

    protected void loadEditorActionsColumns(T resultComponent, Element columnsElement, MetaClass metaClass) {
        List<Element> editorActionsColumns = columnsElement.elements(EDITOR_ACTIONS_COLUMN_ELEMENT_NAME);
        if (CollectionUtils.isEmpty(editorActionsColumns)) {
            return;
        }

        for (Element columnElement : editorActionsColumns) {
            loadEditorActionsColumn(resultComponent, columnElement, metaClass);
        }
    }

    protected void loadEditorActionsColumn(T resultComponent, Element columnElement, MetaClass metaClass) {
        if (columnElement.elements().isEmpty()) {
            throw new GuiDevelopmentException("'editorActionsColumn' cannot be empty",
                    context, "Component ID", resultComponent.getId());
        }

        Editor<?> editor = resultComponent.getEditor();
        Grid.Column<?> editColumn = createEditColumn(resultComponent, columnElement, editor, metaClass);

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

        //If the key is null then NPE will rise when the settings are applied
        loadString(columnElement, "key").ifPresentOrElse(
                editColumn::setKey,
                () -> editColumn.setKey(EDITOR_ACTIONS_COLUMN_ELEMENT_NAME));
        loadString(columnElement, "width", editColumn::setWidth);
        loadBoolean(columnElement, "autoWidth", editColumn::setAutoWidth);
        loadBoolean(columnElement, "resizable", editColumn::setResizable);
        loadInteger(columnElement, "flexGrow", editColumn::setFlexGrow);
        loadResourceString(columnElement, "header", context.getMessageGroup(), editColumn::setHeader);
        loadResourceString(columnElement, "footer", context.getMessageGroup(), editColumn::setFooter);
        loadBoolean(columnElement, "visible", editColumn::setVisible);
    }

    protected void configureParentViewReadOnlyChangeListener(Button editButton) {
        View<?> view = UiComponentUtils.findView(resultComponent);
        if (view == null) {

            log.warn("Unable to find view for Grid '{}' after attaching", resultComponent.getId().orElse(null));
        } else if (!(view instanceof ReadOnlyTracker readOnlyTracker)) {

            log.info("Adding the {} listener will be skipped for Grid {} because the view {} is not an instance of {}",
                    ReadOnlyChangeEvent.class.getSimpleName(), resultComponent.getId().orElse(null),
                    view.getId(), StandardDetailView.class.getSimpleName());
        } else {
            // read-only state can be configured before this moment e.g., via URL query parameters
            editButton.setEnabled(!readOnlyTracker.isReadOnly());

            Registration registration = readOnlyTracker.addReadOnlyStateChangeListener(
                    readOnlyChangeEvent -> editButton.setEnabled(!readOnlyChangeEvent.isReadOnly()));
            editButton.addDetachListener(__ -> registration.remove());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Grid.Column<?> createEditColumn(T resultComponent, Element columnElement, Editor editor,
                                              MetaClass metaClass) {
        return resultComponent.addComponentColumn(item -> {
            UiEntityContext entityContext = new UiEntityContext(metaClass);
            getAccessManager().applyRegisteredConstraints(entityContext);

            InMemoryCrudEntityContext inMemoryContext = new InMemoryCrudEntityContext(metaClass, applicationContext);
            getAccessManager().applyRegisteredConstraints(inMemoryContext);

            if (entityContext.isEditPermitted()
                    && (inMemoryContext.updatePredicate() == null
                    || item != null && inMemoryContext.isUpdatePermitted(item))) {
                Button editButton = loadEditorButton(columnElement, "editButton");

                if (editButton != null) {
                    configureParentViewReadOnlyChangeListener(editButton);

                    editButton.addClickListener(__ -> {
                        if (editor.isOpen()) {
                            editor.cancel();
                        }
                        editor.editItem(item);
                    });
                    return editButton;
                }
            }

            // Vaadin throws NPE if null is returned
            return new Span();
        });
    }

    @Nullable
    protected Button loadEditorButton(Element columnElement, String buttonElementName) {
        Element buttonElement = columnElement.element(buttonElementName);
        if (buttonElement != null) {
            JmixButton button = factory.create(JmixButton.class);

            loadResourceString(buttonElement, "text", context.getMessageGroup(), button::setText);
            iconLoaderSupport().loadIcon(buttonElement, button::setIcon);
            componentLoader().loadTitle(button, buttonElement, context);
            componentLoader().loadClassNames(button, buttonElement);
            componentLoader().loadThemeNames(button, buttonElement);
            componentLoader().loadShortcutCombination(button, buttonElement);
            loadBoolean(buttonElement, "iconAfterText", button::setIconAfterText);

            return button;
        }

        return null;
    }

    protected void loadColumnsByInclude(T component, Element columnsElement, MetaClass metaClass,
                                        FetchPlan fetchPlan, ColumnDefaultValues columnDefaultValues) {
        Collection<String> appliedProperties = getAppliedProperties(columnsElement, fetchPlan, metaClass);

        List<Element> columnElements = columnsElement.elements(COLUMN_ELEMENT_NAME);
        Set<Element> overriddenColumns = new HashSet<>();

        DocumentFactory documentFactory = DatatypeElementFactory.getInstance();

        for (String property : appliedProperties) {
            Element column = getOverriddenColumn(columnElements, property);
            if (column == null) {
                column = documentFactory.createElement(COLUMN_ELEMENT_NAME);
                column.add(documentFactory.createAttribute(column, "property", property));
            } else {
                overriddenColumns.add(column);
            }

            loadColumn(component, column, metaClass, columnDefaultValues);
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
                    loadColumn(component, column, metaClass, columnDefaultValues);
                }
            }
        }
    }

    protected void loadColumn(T component, Element element, MetaClass metaClass,
                              ColumnDefaultValues columnDefaultValues) {
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

        DataGridColumn<?> column = metaPropertyPath != null
                ? addColumn(key, metaPropertyPath)
                : addEmptyColumn(key);

        loadString(element, "width", column::setWidth);
        loadResourceString(element, "header", context.getMessageGroup(), column::setHeader);
        loadResourceString(element, "footer", context.getMessageGroup(), column::setFooter);
        loadBoolean(element, "frozen", column::setFrozen);
        loadBoolean(element, "frozenToEnd", column::setFrozenToEnd);
        loadInteger(element, "flexGrow", column::setFlexGrow);
        loadBoolean(element, "autoWidth", column::setAutoWidth);
        loadBoolean(element, "visible", column::setVisible);
        loadEnum(element, ColumnTextAlign.class, "textAlign", column::setTextAlign);
        loadColumnSortable(element, columnDefaultValues.sortable(), column, metaPropertyPath);
        loadColumnResizable(element, column, columnDefaultValues.resizable());
        loadColumnFilterable(element, column, columnDefaultValues.filterable());
        loadColumnEditable(element, column, property);
        loadAggregationInfo(element, column);

        loadRenderer(element, metaClass, metaPropertyPath)
                .ifPresent(column::setRenderer);
    }

    @SuppressWarnings("unchecked")
    protected DataGridColumn<?> addEmptyColumn(String key) {
        return (DataGridColumn<?>) resultComponent.addColumn(new EmptyValueProvider<>())
                .setKey(key);
    }

    @SuppressWarnings("rawtypes")
    protected Optional<? extends Renderer> loadRenderer(Element columnElement,
                                                        MetaClass metaClass,
                                                        @Nullable MetaPropertyPath metaPropertyPath) {
        if (columnElement.elements().isEmpty()) {
            return Optional.empty();
        }

        Optional<? extends Renderer> fragmentRenderer = componentLoader().loadFragmentRenderer(columnElement);
        if (fragmentRenderer.isPresent()) {
            return fragmentRenderer;

        }
        Map<String, RendererProvider> providers = applicationContext.getBeansOfType(RendererProvider.class);

        for (RendererProvider<?> provider : providers.values()) {
            for (Element element : columnElement.elements()) {
                RendererProvider.RendererCreationContext rendererCreationContext = getRendererCreationContext(metaClass, metaPropertyPath, element);
                if (provider.supports(rendererCreationContext)) {
                    return Optional.of(provider.createRenderer(rendererCreationContext));
                }
            }
        }

        return Optional.empty();
    }

    protected RendererProvider.RendererCreationContext getRendererCreationContext(MetaClass metaClass,
                                                                                  @Nullable MetaPropertyPath metaPropertyPath,
                                                                                  Element element) {
        RendererProvider.RendererCreationContext rendererCreationContext;
        if (metaPropertyPath != null) {
            rendererCreationContext = new RendererProvider.MetaPropertyPathRendererCreationContext(
                    element, resultComponent, metaPropertyPath, context
            );
        } else {
            rendererCreationContext = new RendererProvider.MetaClassRendererCreationContext(
                    element, resultComponent, metaClass, context
            );
        }

        return rendererCreationContext;
    }

    protected void loadColumnSortable(Element element, boolean sortableColumns, DataGridColumn<?> column,
                                      @Nullable MetaPropertyPath metaPropertyPath) {
        if (metaPropertyPath == null || isTransientProperty(metaPropertyPath)) {
            Boolean columnSortable = loadBoolean(element, "sortable").orElse(null);
            column.setSortable(columnSortable != null ? columnSortable : false);
        } else if (metaDataTools.isElementCollection(metaPropertyPath.getMetaProperty())) {
            column.setSortable(false);
        } else {
            loadColumnSortable(element, column, sortableColumns);
        }
    }

    protected void loadColumnSortable(Element element, DataGridColumn<?> column, boolean sortableColumns) {
        loadBoolean(element, "sortable")
                .ifPresentOrElse(column::setSortable, () -> column.setSortable(sortableColumns));
    }

    protected void loadColumnResizable(Element element, DataGridColumn<?> column, boolean resizableColumns) {
        loadBoolean(element, "resizable")
                .ifPresentOrElse(column::setResizable, () -> column.setResizable(resizableColumns));
    }

    protected void loadColumnFilterable(Element element, DataGridColumn<?> column, boolean defaultFilterable) {
        loadBoolean(element, "filterable")
                .ifPresentOrElse(filterable -> {
                    if (filterable) {
                        pendingToFilterableColumns.add(column);
                    }
                }, () -> {
                    if (defaultFilterable) {
                        //noinspection unchecked
                        if (resultComponent.getColumnMetaPropertyPath(column) != null) {
                            pendingToFilterableColumns.add(column);
                        } else {
                            log.info("Unable to set column '{}' filterable because column meta property path is null",
                                    column.getKey());
                        }
                    }
                });
    }

    protected void loadColumnEditable(Element element, DataGridColumn<?> column, @Nullable String property) {
        Element editorComponentElement = element.element(EDITOR_COMPONENT_ELEMENT_NAME);
        if (editorComponentElement != null) {
            loadColumnEditorComponent(element, editorComponentElement, column, property);
            return;
        }

        loadBoolean(element, "editable", editable -> {
            if (Boolean.TRUE.equals(editable)) {
                if (property == null) {
                    throw new GuiDevelopmentException(
                            "'property' attribute is required for editable column with '%s' key"
                                    .formatted(column.getKey()), context);
                }

                setDefaultEditComponent(column, property);
            }
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void loadColumnEditorComponent(Element columnElement, Element editorComponentElement,
                                             DataGridColumn<?> column, @Nullable String property) {
        if (!loadBoolean(columnElement, "editable").orElse(true)) {
            throw new GuiDevelopmentException(
                    "'editable=\"false\"' cannot be combined with 'editorComponent' in the '%s' column"
                            .formatted(column.getKey()), context);
        }

        if (property == null) {
            throw new GuiDevelopmentException(
                    "'property' attribute is required for the '%s' column with 'editorComponent'"
                            .formatted(column.getKey()), context);
        }

        List<Element> componentElements = editorComponentElement.elements();
        if (componentElements.size() != 1) {
            throw new GuiDevelopmentException(
                    "'editorComponent' of the '%s' column must contain exactly one component element"
                            .formatted(column.getKey()), context);
        }

        Element componentElement = componentElements.get(0);
        for (String attribute : List.of("property", "dataContainer")) {
            if (loadString(componentElement, attribute, false).isPresent()) {
                throw new GuiDevelopmentException(("Component in 'editorComponent' of the '%s' column " +
                        "cannot define '%s': it is bound to the column property")
                        .formatted(column.getKey(), attribute), context);
            }
        }

        // The component is created once to fail on view load rather than on the first edit.
        ComponentLoader<?> typeCheckLoader = getLayoutLoader().createComponentLoader(componentElement);
        typeCheckLoader.initComponent();
        Component typeCheckComponent = typeCheckLoader.getResultComponent();
        if (!(typeCheckComponent instanceof SupportsValueSource)) {
            throw new GuiDevelopmentException(
                    "Component in 'editorComponent' of the '%s' column must implement %s, but %s does not"
                            .formatted(column.getKey(), SupportsValueSource.class.getSimpleName(),
                                    typeCheckComponent.getClass().getSimpleName()), context);
        }

        MetaPropertyPath metaPropertyPath = resultComponent.getColumnMetaPropertyPath(column);

        Editor<?> editor = resultComponent.getEditor();
        if (editor instanceof DataGridEditor) {
            ((DataGridEditor) editor).setColumnEditorComponent(column, property,
                    (Function<EditComponentGenerationContext<?>, Component>) generationContext ->
                            createColumnEditorComponent(componentElement, property, metaPropertyPath,
                                    generationContext));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected Component createColumnEditorComponent(Element componentElement, String property,
                                                    @Nullable MetaPropertyPath metaPropertyPath,
                                                    EditComponentGenerationContext<?> generationContext) {
        ComponentLoader<?> componentLoader = getLayoutLoader().createComponentLoader(componentElement);
        componentLoader.initComponent();

        Component component = componentLoader.getResultComponent();
        if (component instanceof SupportsMetaClass supportsMetaClass
                && metaPropertyPath != null
                && metaPropertyPath.getRange().isClass()) {
            supportsMetaClass.setMetaClass(metaPropertyPath.getRange().asClass());
        }

        // Items must be set before binding, otherwise resetting them writes null into the edited item.
        componentLoader.loadComponent();

        ((SupportsValueSource) component).setValueSource(
                generationContext.getValueSourceProvider().getValueSource(property));

        // Binding overrides these attributes, so XML values are applied again.
        if (component instanceof HasValueAndElement<?, ?> hasValueAndElement) {
            componentLoader().loadValueAndElementAttributes(hasValueAndElement, componentElement);
        }
        if (component instanceof HasRequired hasRequired) {
            componentLoader().loadRequired(hasRequired, componentElement, context);
        }
        if (component instanceof HasLengthLimited hasLengthLimited) {
            loadInteger(componentElement, "maxLength", hasLengthLimited::setMaxLength);
            loadInteger(componentElement, "minLength", hasLengthLimited::setMinLength);
        }

        if (component instanceof SupportsStatusChangeHandler supportsStatusChangeHandler) {
            supportsStatusChangeHandler.setStatusChangeHandler(generationContext.getStatusHandler());
        }
        if (component instanceof HasSize hasSize && loadString(componentElement, "width").isEmpty()) {
            hasSize.setWidthFull();
        }

        // Nested fragments register their initialization as autowire tasks of the root context.
        ComponentLoader.Context rootContext = context;
        while (rootContext.getParentContext() != null) {
            rootContext = rootContext.getParentContext();
        }
        if (rootContext instanceof ComponentLoader.ComponentContext componentContext) {
            componentContext.executeAutowireTasks();
        }
        context.executeInitTasks();

        return component;
    }

    protected void setSelectionMode(SelectionMode selectionMode) {
        GridSelectionModel<?> gridSelectionModel = resultComponent.setSelectionMode(selectionMode);

        if (gridSelectionModel instanceof GridMultiSelectionModel<?> multiSelectionModel) {
            multiSelectionModel.setSelectionColumnFrozen(true);
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadAggregationInfo(Element columnElement, DataGridColumn<?> column) {
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
    protected void setDefaultEditComponent(DataGridColumn<?> column, String property) {
        Editor<?> editor = resultComponent.getEditor();
        if (editor instanceof DataGridEditor) {
            ((DataGridEditor) editor).initColumnDefaultEditorComponent(column, property);
        }
    }

    protected DataGridColumn<?> addColumn(String key, MetaPropertyPath metaPropertyPath) {
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

        InstanceContainer<?> container = context.getDataHolder().getContainer(containerId);

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
     * Contains information about resizable, sortable, filterable column default values for the column.
     */
    protected record ColumnDefaultValues(boolean sortable, boolean resizable, boolean filterable) {
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

    protected Messages getMessages() {
        if (messages == null) {
            messages = applicationContext.getBean(Messages.class);
        }
        return messages;
    }

    protected ClassManager getClassManager() {
        if (classManager == null) {
            classManager = applicationContext.getBean(ClassManager.class);
        }
        return classManager;
    }

    protected AccessManager getAccessManager() {
        if (accessManager == null) {
            accessManager = applicationContext.getBean(AccessManager.class);
        }
        return accessManager;
    }

    protected IconLoaderSupport iconLoaderSupport() {
        if (iconLoaderSupport == null) {
            iconLoaderSupport = applicationContext.getBean(IconLoaderSupport.class, context);
        }

        return iconLoaderSupport;
    }

    @Nullable
    protected String loadMessage(@Nullable String message) {
        if (Strings.isNullOrEmpty(message)) {
            return message;
        }

        return getMessages().getMessage(message);
    }

    protected abstract void setupDataProvider(GridDataHolder holder);

    protected void loadContextMenu() {
        Element contextMenuElement = element.element("contextMenu");
        if (contextMenuElement == null) {
            return;
        }
        JmixGridContextMenu<?> contextMenu = resultComponent.getContextMenu();

        loadId(contextMenu, contextMenuElement);
        loadVisible(contextMenu, contextMenuElement);

        componentLoader().loadCss(contextMenu, contextMenuElement);
        componentLoader().loadClassNames(contextMenu, contextMenuElement);

        for (Element childItemElement : contextMenuElement.elements()) {
            addContextMenuItem(contextMenu::addItem, contextMenu::addComponent, childItemElement);
        }
    }

    protected void loadEmptyStateComponent() {
        Element emptyStateComponentElement = element.element("emptyStateComponent");
        if (emptyStateComponentElement == null) {
            return;
        }

        if (emptyStateComponentElement.elements().size() != 1) {
            String message = "%s with '%s' ID must contain a single nested element for the 'emptyStateComponent'"
                    .formatted(resultComponent.getClass().getSimpleName(), resultComponent.getId().orElse(null));

            throw new GuiDevelopmentException(message, context);
        }

        Element subElement = emptyStateComponentElement.elements().get(0);
        ComponentLoader<?> componentLoader = getLayoutLoader().createComponentLoader(subElement);

        componentLoader.initComponent();
        componentLoader.loadComponent();

        Component emptyStateComponent = componentLoader.getResultComponent();
        resultComponent.setEmptyStateComponent(emptyStateComponent);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void addContextMenuItem(Function<Component, GridMenuItem<?>> menuItemGenerator,
                                      Consumer<Component> separatorConsumer,
                                      Element childElement) {
        switch (childElement.getName()) {
            case "item":
                GridContextMenuItemComponent<?> component = new GridContextMenuItemComponent<>();
                component.setApplicationContext(applicationContext);
                GridMenuItem menuItem = menuItemGenerator.apply(component);
                component.setMenuItem(menuItem);
                loadContextMenuItem(component, menuItem, childElement);
                break;
            case "separator":
                separatorConsumer.accept(new Hr());
                break;
            case "icon":
                // A valid icon element. Loaded along with other attributes.
                break;
            default:
                throw new GuiDevelopmentException("Unknown context menu child element: " + childElement.getName(),
                        context, "Component ID", resultComponent.getId());
        }
    }

    protected void loadContextMenuItem(GridContextMenuItemComponent<?> component,
                                       GridMenuItem<?> menuItem,
                                       Element itemElement) {
        loadId(menuItem, itemElement);
        loadVisible(menuItem, itemElement);

        componentLoader().loadCss(menuItem, itemElement);
        componentLoader().loadEnabled(menuItem, itemElement);
        componentLoader().loadClassNames(menuItem, itemElement);

        componentLoader().loadText(component, itemElement);
        componentLoader().loadWhiteSpace(component, itemElement);
        iconLoaderSupport().loadIcon(itemElement, component::setPrefixComponent);

        loadContextMenuItemAction(component, itemElement);

        GridSubMenu<?> subMenu = menuItem.getSubMenu();
        for (Element contextMenuChildItemElement : itemElement.elements()) {
            addContextMenuItem(subMenu::addItem, subMenu::addComponent, contextMenuChildItemElement);
        }
    }

    protected void loadContextMenuItemAction(GridContextMenuItemComponent<?> component, Element element) {
        loadString(element, "action")
                .ifPresent(actionId -> getContext().addInitTask(
                        new AssignActionInitTask<>(component, actionId)));
    }

    protected boolean isTransientProperty(@Nullable MetaPropertyPath metaPropertyPath) {
        if (metaPropertyPath == null) {
            return false;
        }
        MetaProperty metaProperty = metaPropertyPath.getMetaProperty();
        if (Boolean.TRUE.equals(metaProperty.getAnnotations().get(MetadataTools.SORTABLE_IN_STORE_ANN_NAME))) {
            return false;
        }
        MetaClass metaClass = getMetaDataTools().getPropertyEnclosingMetaClass(metaPropertyPath);
        return getMetaDataTools().isJpaEntity(metaClass)
                && !getMetaDataTools().isJpa(metaProperty);
    }

    protected void loadActions() {
        getActionLoaderSupport().loadActions(resultComponent, element);
    }
}
