/*
 * Copyright 2023 Haulmont.
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

package io.jmix.gridexportflowui.action;

import com.vaadin.flow.component.grid.Grid;
import io.jmix.core.Messages;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.model.HasLoader;
import io.jmix.gridexportflowui.GridExportProperties;
import io.jmix.gridexportflowui.exporter.ColumnsToExport;
import io.jmix.gridexportflowui.exporter.DataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Base action for export table content with defined exporter.
 * <code>dataGridExporter</code> is required for this action
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.).
 */
@ActionType(ExportAction.ID)
public class ExportAction extends ListDataComponentAction<ExportAction, Object> implements ApplicationContextAware {

    public static final String ID = "grdexp_export";

    protected ApplicationContext applicationContext;

    protected Messages messages;
    protected MetadataTools metadataTools;
    protected Downloader downloader;
    protected Dialogs dialogs;

    protected DataGridExporter dataGridExporter;
    protected List<ExportMode> availableExportModes;

    protected ColumnsToExport columnsToExport;
    protected Predicate<Grid.Column<Object>> columnsExportFilter;
    protected List<String> columnKeysToExport;

    public ExportAction() {
        this(ID);
    }

    public ExportAction(String id) {
        super(id);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    @Autowired
    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
    }

    @Autowired
    public void setGridExportProperties(GridExportProperties gridExportProperties) {
        this.availableExportModes = gridExportProperties.getDefaultExportModes().stream()
                .map(ExportMode::valueOf)
                .toList();
        this.columnsToExport = ColumnsToExport.valueOf(gridExportProperties.getDefaultColumnsToExport());
    }

    /**
     * Sets the {@link ColumnsToExport} that is used to filter columns to export. This is a simple, predefined
     * alternative to {@link #setColumnsExportFilter(Predicate)}. Has the lowest filtering priority.
     * <p>
     * The default value depends on {@link GridExportProperties#getDefaultColumnsToExport()}.
     *
     * @param columnsToExport {@link ColumnsToExport} that is used to filter columns to export
     */
    public void setColumnsToExport(ColumnsToExport columnsToExport) {
        this.columnsToExport = columnsToExport;
    }

    /**
     * @return this
     * @see #setColumnsToExport(ColumnsToExport)
     */
    public ExportAction withColumnsToExport(ColumnsToExport columnsToExport) {
        setColumnsToExport(columnsToExport);
        return this;
    }

    /**
     * Sets the {@link Predicate} that is used to filter columns to export. This is a flexible alternative to
     * {@link #setColumnsToExport(ColumnsToExport)}. Has secondary filtering priority.
     *
     * @param columnsExportFilter column export predicate to set
     */
    public void setColumnsExportFilter(Predicate<Grid.Column<Object>> columnsExportFilter) {
        this.columnsExportFilter = columnsExportFilter;
    }

    /**
     * @return this
     * @see #setColumnsExportFilter(Predicate)
     */
    public ExportAction withColumnsExportFilter(Predicate<Grid.Column<Object>> columnsExportFilter) {
        setColumnsExportFilter(columnsExportFilter);
        return this;
    }

    /**
     * Sets a list of column keys to use for export. Has primary filtering priority.
     *
     * @param columnKeysToExport list of column keys
     */
    public void setColumnKeysToExport(List<String> columnKeysToExport) {
        this.columnKeysToExport = columnKeysToExport;
    }

    /**
     * @return this
     * @see #setColumnKeysToExport(List)
     */
    public ExportAction withColumnKeysToExport(List<String> columnKeysToExport) {
        setColumnKeysToExport(columnKeysToExport);
        return this;
    }

    /**
     * Sets the export modes that will be available in the export option dialog.
     *
     * @param availableExportModes export modes to set
     */
    public void setAvailableExportModes(List<ExportMode> availableExportModes) {
        this.availableExportModes = availableExportModes;
    }

    /**
     * @return this
     * @see #setAvailableExportModes(List)
     */
    public ExportAction withAvailableExportModes(List<ExportMode> availableExportModes) {
        setAvailableExportModes(availableExportModes);
        return this;
    }

    /**
     * Sets the dataGrid exporter instance
     */
    public void setDataGridExporter(DataGridExporter dataGridExporter) {
        this.dataGridExporter = dataGridExporter;

        this.text = dataGridExporter.getLabel() == null
                ? this.text
                : dataGridExporter.getLabel();
    }

    @SuppressWarnings("unchecked")
    public <T> T withExporter(Class<T> exporterClass) {
        setDataGridExporter((DataGridExporter) applicationContext.getBean(exporterClass));
        return (T) dataGridExporter;
    }

    /**
     * Sets a file name.
     *
     * @param fileName a file name
     */
    public void setFileName(String fileName) {
        dataGridExporter.setFileName(fileName);
    }

    /**
     * Sets a file name.
     *
     * @param fileName a file name
     * @return builder
     */
    @SuppressWarnings("unchecked")
    public <T> T withFileName(String fileName) {
        setFileName(fileName);
        return (T) dataGridExporter;
    }

    /**
     * Adds a function to get value from the column.
     *
     * @param columnId            column id
     * @param columnValueProvider column value provider function
     */
    public void addColumnValueProvider(String columnId,
                                       Function<DataGridExporter.ColumnValueContext, Object> columnValueProvider) {
        if (dataGridExporter != null) {
            dataGridExporter.addColumnValueProvider(columnId, columnValueProvider);
        }
    }

    /**
     * Removes an column value provider function by column id.
     *
     * @param columnId column id
     */
    public void removeColumnValueProvider(String columnId) {
        if (dataGridExporter != null) {
            dataGridExporter.removeColumnValueProvider(columnId);
        }
    }

    /**
     * @param columnId column id
     * @return column value provider function for the column id
     */
    @Nullable
    public Function<DataGridExporter.ColumnValueContext, Object> getColumnValueProvider(String columnId) {
        return dataGridExporter != null
                ? dataGridExporter.getColumnValueProvider(columnId)
                : null;
    }

    @Override
    public void execute() {
        Preconditions.checkNotNullArgument(dataGridExporter,
                Grid.class.getSimpleName() + " exporter is not defined");

        List<Action> actions = new ArrayList<>();

        Predicate<Grid.Column<Object>> primaryFilterPredicate = definePrimaryFilterPredicate();

        for (ExportMode exportMode : availableExportModes) {
            switch (exportMode) {
                case ALL_ROWS -> {
                    if (isExportAllAvailable(target)) {
                        actions.add(createExportAllAction(primaryFilterPredicate));
                    }
                }
                case CURRENT_PAGE -> actions.add(createCurrentPageAction(primaryFilterPredicate));
                case SELECTED_ROWS -> {
                    if (!target.getSelectedItems().isEmpty()) {
                        actions.add(createExportSelectedAction(primaryFilterPredicate));
                    }
                }
            }
        }

        actions.add(new DialogAction(DialogAction.Type.CANCEL));
        actions.get(0).setVariant(ActionVariant.PRIMARY);

        dialogs.createOptionDialog()
                .withHeader(getMessage("exportConfirmationDialog.header"))
                .withText(getMessage("exportConfirmationDialog.message"))
                .withActions(actions.toArray(new Action[0]))
                .withMinWidth("32em")
                .withMaxWidth("50em")
                .open();
    }

    protected Predicate<Grid.Column<Object>> definePrimaryFilterPredicate() {
        if (!CollectionUtils.isEmpty(columnKeysToExport)) {
            return column -> columnKeysToExport.contains(column.getKey());
        } else {
            return Objects.requireNonNullElse(columnsExportFilter,
                    columnsToExport.getFilterPredicate());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void doExport(ExportMode exportMode, Predicate<Grid.Column<Object>> primaryFilterPredicate) {
        if (getTarget() instanceof Grid grid) {
            dataGridExporter.exportDataGrid(downloader, grid, exportMode, primaryFilterPredicate);
        } else {
            throw new UnsupportedOperationException("Unsupported component for export");
        }
    }

    protected String getMessage(String id) {
        return messages.getMessage(getClass(), id);
    }

    protected Action createExportSelectedAction(Predicate<Grid.Column<Object>> primaryFilterPredicate) {
        return new SecuredBaseAction("ExportMode.SELECTED_ROWS")
                .withText(messages.getMessage(ExportMode.SELECTED_ROWS))
                .withHandler(event -> doExport(ExportMode.SELECTED_ROWS, primaryFilterPredicate));
    }

    protected Action createExportAllAction(Predicate<Grid.Column<Object>> primaryFilterPredicate) {
        return new SecuredBaseAction("ExportMode.CURRENT_PAGE")
                .withText(messages.getMessage(ExportMode.ALL_ROWS))
                .withHandler(event -> doExport(ExportMode.ALL_ROWS, primaryFilterPredicate));
    }

    protected Action createCurrentPageAction(Predicate<Grid.Column<Object>> primaryFilterPredicate) {
        return new SecuredBaseAction("ExportMode.CURRENT_PAGE")
                .withText(messages.getMessage(ExportMode.CURRENT_PAGE))
                .withHandler(event -> doExport(ExportMode.CURRENT_PAGE, primaryFilterPredicate));
    }

    protected boolean isExportAllAvailable(ListDataComponent<?> target) {
        return target.getItems() instanceof ContainerDataUnit<?> containerItems
                && metadataTools.getPrimaryKeyProperty(containerItems.getEntityMetaClass()) != null
                && containerItems.getContainer() instanceof HasLoader containerWithLoader
                && containerWithLoader.getLoader() != null;
    }
}
