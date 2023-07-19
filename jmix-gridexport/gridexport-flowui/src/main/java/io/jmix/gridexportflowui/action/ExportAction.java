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
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.action.ActionType;
import io.jmix.flowui.action.DialogAction;
import io.jmix.flowui.action.SecuredBaseAction;
import io.jmix.flowui.action.list.ListDataComponentAction;
import io.jmix.flowui.component.ListDataComponent;
import io.jmix.flowui.data.ContainerDataUnit;
import io.jmix.flowui.data.DataUnit;
import io.jmix.flowui.data.grid.ContainerDataGridItems;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.action.ActionVariant;
import io.jmix.flowui.model.HasLoader;
import io.jmix.gridexportflowui.exporter.DataGridExporter;
import io.jmix.gridexportflowui.exporter.ExportMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Base action for export table content with defined exporter.
 * <code>dataGridExporter</code> is required for this action
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.).
 */
@ActionType(ExportAction.ID)
public class ExportAction extends ListDataComponentAction<ExportAction, Object> implements ApplicationContextAware {

    public static final String ID = "export";

    protected ApplicationContext applicationContext;

    protected Messages messages;
    protected Downloader downloader;
    protected Dialogs dialogs;

    protected DataGridExporter dataGridExporter;

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
    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    @Autowired
    public void setDialogs(Dialogs dialogs) {
        this.dialogs = dialogs;
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

        Action exportAllAction = createExportAllAction();
        Action exportSelectedAction = createExportSelectedAction();
        Action exportCurrentPageAction = createCurrentPageAction();

        List<Action> actions = new ArrayList<>();

        if (isExportAllEnabled() && isDataLoaderExist(target)) {
            actions.add(exportAllAction);
        }

        actions.add(exportCurrentPageAction);

        if (!target.getSelectedItems().isEmpty()) {
            actions.add(exportSelectedAction);
        }

        actions.add(new DialogAction(DialogAction.Type.CANCEL));

        if (actions.contains(exportAllAction)) {
            exportAllAction.setVariant(ActionVariant.PRIMARY);
        } else {
            exportCurrentPageAction.setVariant(ActionVariant.PRIMARY);
        }

        dialogs.createOptionDialog()
                .withHeader(getMessage("exportConfirmationDialog.header"))
                .withText(getMessage("exportConfirmationDialog.message"))
                .withActions(actions.toArray(new Action[0]))
                .withWidth("32em")
                .open();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void doExport(ExportMode exportMode) {
        if (getTarget() instanceof Grid) {
            dataGridExporter.exportDataGrid(downloader, (Grid) getTarget(), exportMode);
        } else {
            throw new UnsupportedOperationException("Unsupported component for export");
        }
    }

    protected String getMessage(String id) {
        return messages.getMessage(getClass(), id);
    }

    protected boolean isExportAllEnabled() {
        return false;
    }

    protected Action createExportSelectedAction() {
        return new SecuredBaseAction("ExportMode.SELECTED_ROWS")
                .withText(messages.getMessage(ExportMode.SELECTED_ROWS))
                .withHandler(event -> doExport(ExportMode.SELECTED_ROWS));
    }

    protected Action createExportAllAction() {
        return new SecuredBaseAction("ExportMode.CURRENT_PAGE")
                .withText(messages.getMessage(ExportMode.ALL_ROWS))
                .withHandler(event -> doExport(ExportMode.ALL_ROWS));
    }

    protected Action createCurrentPageAction() {
        return new SecuredBaseAction("ExportMode.CURRENT_PAGE")
                .withText(messages.getMessage(ExportMode.CURRENT_PAGE))
                .withHandler(event -> doExport(ExportMode.CURRENT_PAGE));
    }

    protected boolean isDataLoaderExist(ListDataComponent<?> target) {
        DataUnit items = target.getItems();

        return items instanceof ContainerDataUnit<?>
                && ((ContainerDataUnit<?>) items).getContainer() instanceof HasLoader
                && ((HasLoader) ((ContainerDataGridItems<?>) items).getContainer()).getLoader() != null;
    }
}
