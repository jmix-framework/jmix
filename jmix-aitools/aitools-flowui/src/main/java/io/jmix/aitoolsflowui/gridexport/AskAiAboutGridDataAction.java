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

package io.jmix.aitoolsflowui.gridexport;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import io.jmix.aitoolsflowui.gridexport.view.AiGridDataDialog;
import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowui.view.chat.AiChatView;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.common.event.Subscription;
import io.jmix.flowui.DialogWindows;
import io.jmix.flowui.accesscontext.UiShowViewContext;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.sys.ViewDescriptorUtils;
import io.jmix.flowui.view.View;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.action.ExportAction;
import io.jmix.gridexportflowui.action.ExportDialogContext;
import io.jmix.gridexportflowui.action.ExportDialogContextAware;
import io.jmix.gridexportflowui.exporter.AbstractDataGridExporter;
import io.jmix.gridexportflowui.exporter.ColumnsToExport;
import io.jmix.gridexportflowui.exporter.DataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import io.jmix.gridexportflowui.exporter.json.JsonExporter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Opens the {@link AiGridDataDialog "Ask AI about the data"} dialog, or the view that replaces it by id, for the rows
 * of the target grid, so that the user can ask the AI chat about them.
 * <p>
 * Used by itself on a grid, the action reads the rows with its own exporter and column settings, named and
 * prioritised as on {@link ExportAction}, and offers the selected rows and the current page. Listed in the dialog
 * actions of an {@link ExportAction}, it reads the rows as that export does and offers only the export's modes among
 * the selected rows and the current page. Its own settings are not used then.
 * <p>
 * The action is disabled when the AI chat is unavailable, the user may not open the chat or the dialog, no column may
 * be sent to the AI, no mode is allowed, the exporter cannot read rows, or there are no rows to send.
 */
@Experimental
@ActionType(AskAiAboutGridDataAction.ID)
public class AskAiAboutGridDataAction extends ListDataComponentAction<AskAiAboutGridDataAction, Object>
        implements ExportDialogContextAware {

    public static final String ID = "aitls_askAiAboutGridData";

    private static final Logger log = LoggerFactory.getLogger(AskAiAboutGridDataAction.class);
    private static final List<ExportMode> SUPPORTED_MODES = List.of(ExportMode.SELECTED_ROWS, ExportMode.CURRENT_PAGE);

    protected AiGridDataSupport gridDataSupport;
    protected AiChatService chatService;
    protected DialogWindows dialogWindows;
    protected JsonExporter exporter;
    protected boolean viewsPermitted = true;

    protected ColumnsToExport columnsToExport = ColumnsToExport.VISIBLE_COLUMNS;
    @Nullable
    protected Predicate<Grid.Column<Object>> columnsExportFilter;
    @Nullable
    protected List<String> columnKeysToExport;

    @Nullable
    protected ExportDialogContext exportDialogContext;
    @Nullable
    protected Subscription itemsChangeSubscription;

    public AskAiAboutGridDataAction() {
        this(ID);
    }

    public AskAiAboutGridDataAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        setText(messages.getMessage(AskAiAboutGridDataAction.class, "askAiAboutGridDataAction.text"));
    }

    @Autowired
    protected void setGridDataSupport(AiGridDataSupport gridDataSupport) {
        this.gridDataSupport = gridDataSupport;
    }

    @Autowired
    protected void setChatService(AiChatService chatService) {
        this.chatService = chatService;
    }

    @Autowired
    protected void setDialogWindows(DialogWindows dialogWindows) {
        this.dialogWindows = dialogWindows;
    }

    @Autowired
    protected void setAccessManager(AccessManager accessManager) {
        viewsPermitted = isViewPermitted(accessManager, AiChatView.class)
                && isViewPermitted(accessManager, AiGridDataDialog.class);
    }

    @Autowired
    protected void setGridExportProperties(GridExportProperties gridExportProperties) {
        this.columnsToExport = gridExportProperties.getDefaultColumnsToExport();
    }

    @Autowired
    protected void setExporterProvider(ObjectProvider<JsonExporter> exporterProvider) {
        // Own instance: the column value providers of this action must not leak into other exporters.
        this.exporter = exporterProvider.getObject();
    }

    /**
     * Passes the export the dialog is opened for: the action then reads the rows with the exporter, the column filter
     * and the modes of that export instead of its own settings.
     *
     * @param context the export the dialog is opened for
     */
    @NullMarked
    @Override
    public void setExportDialogContext(ExportDialogContext context) {
        this.exportDialogContext = context;
        refreshState();
    }

    @Override
    public void setTarget(@Nullable ListDataComponent<Object> target) {
        if (!Objects.equals(getTarget(), target)) {
            super.setTarget(target);
            attachItemsChangeListener(target);
        }
    }

    /**
     * Sets the {@link ColumnsToExport} that is used to filter the columns to read when the action is used by itself.
     * Has the lowest filtering priority.
     * <p>
     * The default value depends on {@link GridExportProperties#getDefaultColumnsToExport()}.
     *
     * @param columnsToExport {@link ColumnsToExport} that is used to filter the columns to read
     */
    public void setColumnsToExport(ColumnsToExport columnsToExport) {
        this.columnsToExport = columnsToExport;
        refreshState();
    }

    /**
     * @param columnsToExport {@link ColumnsToExport} that is used to filter the columns to read
     * @return this
     * @see #setColumnsToExport(ColumnsToExport)
     */
    public AskAiAboutGridDataAction withColumnsToExport(ColumnsToExport columnsToExport) {
        setColumnsToExport(columnsToExport);
        return this;
    }

    /**
     * Sets the {@link Predicate} that is used to filter the columns to read when the action is used by itself. Has
     * secondary filtering priority.
     *
     * @param columnsExportFilter column filter, or {@code null} to use {@link #setColumnsToExport(ColumnsToExport)}
     */
    public void setColumnsExportFilter(@Nullable Predicate<Grid.Column<Object>> columnsExportFilter) {
        this.columnsExportFilter = columnsExportFilter;
        refreshState();
    }

    /**
     * @param columnsExportFilter column filter, or {@code null} to use {@link #setColumnsToExport(ColumnsToExport)}
     * @return this
     * @see #setColumnsExportFilter(Predicate)
     */
    public AskAiAboutGridDataAction withColumnsExportFilter(
            @Nullable Predicate<Grid.Column<Object>> columnsExportFilter) {
        setColumnsExportFilter(columnsExportFilter);
        return this;
    }

    /**
     * Sets the keys of the columns to read when the action is used by itself. Has primary filtering priority.
     *
     * @param columnKeysToExport column keys, or {@code null} or an empty list to use the other column settings
     */
    public void setColumnKeysToExport(@Nullable List<String> columnKeysToExport) {
        this.columnKeysToExport = columnKeysToExport == null ? null : List.copyOf(columnKeysToExport);
        refreshState();
    }

    /**
     * @param columnKeysToExport column keys, or {@code null} or an empty list to use the other column settings
     * @return this
     * @see #setColumnKeysToExport(List)
     */
    public AskAiAboutGridDataAction withColumnKeysToExport(@Nullable List<String> columnKeysToExport) {
        setColumnKeysToExport(columnKeysToExport);
        return this;
    }

    /**
     * Adds a function to get the value of a column when the action is used by itself.
     *
     * @param columnId            column id
     * @param columnValueProvider column value provider function
     */
    public void addColumnValueProvider(String columnId,
                                       Function<DataGridExporter.ColumnValueContext, Object> columnValueProvider) {
        exporter.addColumnValueProvider(columnId, columnValueProvider);
    }

    /**
     * Removes a column value provider function by column id.
     *
     * @param columnId column id
     */
    public void removeColumnValueProvider(String columnId) {
        exporter.removeColumnValueProvider(columnId);
    }

    /**
     * @param columnId column id
     * @return column value provider function for the column id, or {@code null} if there is none
     */
    @Nullable
    public Function<DataGridExporter.ColumnValueContext, Object> getColumnValueProvider(String columnId) {
        return exporter.getColumnValueProvider(columnId);
    }

    @Override
    public void execute() {
        checkTarget();

        AiGridDataContext context = createGridDataContext(target);
        if (context == null) {
            throw new IllegalStateException(String.format("%s cannot read the rows: the exporter of the export " +
                    "is not an %s", getClass().getSimpleName(), AbstractDataGridExporter.class.getSimpleName()));
        }
        openDialog(context);
    }

    @Override
    protected boolean isPermitted() {
        return super.isPermitted() && viewsPermitted;
    }

    @Override
    protected boolean isApplicable() {
        ListDataComponent<Object> target = getTarget();
        if (!super.isApplicable() || target == null || !chatService.isAvailable()) {
            return false;
        }

        AiGridDataContext context = createGridDataContext(target);
        return context != null
                && gridDataSupport.hasAllowedColumns(context)
                && hasRows(context);
    }

    protected boolean hasRows(AiGridDataContext context) {
        for (ExportMode mode : context.getExportModes()) {
            if (gridDataSupport.countRows(context, mode) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns how the rows of the target are read: as the export the dialog is opened for reads them, or with the
     * settings of this action when it is used by itself.
     *
     * @param target the grid to read the rows from
     * @return the context, or {@code null} if the exporter of the export cannot read rows
     */
    @Nullable
    protected AiGridDataContext createGridDataContext(ListDataComponent<Object> target) {
        ExportDialogContext dialogContext = exportDialogContext;
        if (dialogContext == null) {
            return new AiGridDataContext(target, exporter, definePrimaryFilterPredicate(), SUPPORTED_MODES);
        }

        if (!(dialogContext.getExporter() instanceof AbstractDataGridExporter<?> dialogExporter)) {
            return null;
        }
        return new AiGridDataContext(target, dialogExporter, dialogContext.getColumnFilter(),
                getAllowedModes(dialogContext.getExportAction()));
    }

    protected List<ExportMode> getAllowedModes(ExportAction exportAction) {
        List<ExportMode> exportModes = exportAction.getAvailableExportModes();
        if (exportModes == null) {
            return List.of();
        }
        return SUPPORTED_MODES.stream()
                .filter(exportModes::contains)
                .toList();
    }

    protected Predicate<Grid.Column<Object>> definePrimaryFilterPredicate() {
        List<String> keys = columnKeysToExport;
        if (keys != null && !keys.isEmpty()) {
            return column -> keys.contains(column.getKey());
        }
        return Objects.requireNonNullElse(columnsExportFilter, columnsToExport.getFilterPredicate());
    }

    /**
     * Keeps the state up to date when the rows of the target change, as the action is disabled while there are none.
     *
     * @param target the new target, or {@code null} to stop listening
     */
    protected void attachItemsChangeListener(@Nullable ListDataComponent<Object> target) {
        if (itemsChangeSubscription != null) {
            itemsChangeSubscription.remove();
            itemsChangeSubscription = null;
        }

        if (target != null && target.getItems() instanceof ContainerDataUnit<?> unit) {
            itemsChangeSubscription = unit.getContainer().addCollectionChangeListener(event -> refreshState());
        }
    }

    protected void openDialog(AiGridDataContext context) {
        View<?> origin = UiComponentUtils.getView((Component) context.getTarget());
        // By id, not by class: an application may replace the dialog with a view of its own class.
        String dialogId = ViewDescriptorUtils.getInferredViewId(AiGridDataDialog.class);
        dialogWindows.view(origin, dialogId)
                .withViewConfigurer(view -> {
                    if (view instanceof AiGridDataView gridDataView) {
                        gridDataView.setGridDataContext(context);
                    } else {
                        log.warn("View {} does not implement {}, so it gets no grid to ask the AI about",
                                dialogId, AiGridDataView.class.getSimpleName());
                    }
                })
                .open();
    }

    protected boolean isViewPermitted(AccessManager accessManager, Class<? extends View<?>> viewClass) {
        UiShowViewContext showViewContext = new UiShowViewContext(ViewDescriptorUtils.getInferredViewId(viewClass));
        accessManager.applyRegisteredConstraints(showViewContext);
        return showViewContext.isPermitted();
    }
}
