/*
 * Copyright 2020 Haulmont.
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

package io.jmix.uiexport.action;

import io.jmix.core.JmixEntity;
import io.jmix.core.Messages;
import io.jmix.ui.Dialogs;
import io.jmix.ui.action.*;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.DataGrid;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.data.meta.ContainerDataUnit;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.meta.StudioAction;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.uiexport.exporter.ExportMode;
import io.jmix.uiexport.exporter.TableExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;

/**
 * Base action for export table content with defined exporter.
 * <code>tableExporter</code> is required for this action
 * <p>
 * Should be defined for a list component ({@code Table}, {@code DataGrid}, etc.) in a screen XML descriptor.
 */
@SuppressWarnings("rawtypes")
@StudioAction(category = "List Actions", description = "Export selected entities")
@ActionType(ExportAction.ID)
public class ExportAction extends ListAction implements ApplicationContextAware {

    public static final String ID = "export";

    protected ApplicationContext applicationContext;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Downloader downloader;

    protected TableExporter tableExporter;

    public ExportAction(String id) {
        this(id, null);
    }

    public ExportAction() {
        this(ID);
    }

    public ExportAction(String id, @Nullable String shortcut) {
        super(id, shortcut);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets the table exporter instance
     */
    public void setTableExporter(TableExporter tableExporter) {
        this.tableExporter = tableExporter;
        this.caption = tableExporter.getCaption() != null ? tableExporter.getCaption() : this.caption;
    }

    /**
     * Autowire table exporter instance by exporter class
     */
    public <T> T withExporter(Class<T> exporterClass) {
        setTableExporter((TableExporter) applicationContext.getBean(exporterClass));
        return (T) tableExporter;
    }

    @Override
    public void actionPerform(Component component) {
        // if standard behaviour
        if (!hasSubscriptions(ActionPerformedEvent.class)) {
            execute();
        } else {
            super.actionPerform(component);
        }
    }

    protected void execute() {
        if (tableExporter == null) {
            throw new IllegalStateException("Table exporter is not defined");
        }
        if (needExportAll()) {
            doExport(ExportMode.ALL);

        } else {
            AbstractAction exportSelectedAction = new AbstractAction("actions.export.SELECTED_ROWS", Status.PRIMARY) {
                @Override
                public void actionPerform(Component component) {
                    doExport(ExportMode.SELECTED);
                }
            };
            exportSelectedAction.setCaption(getMessage(exportSelectedAction.getId()));

            AbstractAction exportAllAction = new AbstractAction("actions.export.ALL_ROWS") {
                @Override
                public void actionPerform(Component component) {
                    doExport(ExportMode.ALL);
                }
            };
            exportAllAction.setCaption(getMessage(exportAllAction.getId()));

            Action[] actions = new Action[]{
                    exportSelectedAction,
                    exportAllAction,
                    new DialogAction(DialogAction.Type.CANCEL)
            };

            Dialogs dialogs = ComponentsHelper.getScreenContext(target).getDialogs();

            dialogs.createOptionDialog()
                    .withCaption(getMessage("actions.exportSelectedTitle"))
                    .withMessage(getMessage("actions.exportSelectedCaption"))
                    .withActions(actions)
                    .show();
        }
    }

    protected void doExport(ExportMode exportMode) {
        if (getTarget() instanceof Table) {
            tableExporter.exportTable(downloader, (Table<JmixEntity>) getTarget(), exportMode);
        } else if (getTarget() instanceof DataGrid) {
            tableExporter.exportDataGrid(downloader, (DataGrid<JmixEntity>) getTarget(), exportMode);
        } else {
            throw new UnsupportedOperationException("Unsupported component for export");
        }
    }

    protected String getMessage(String id) {
        return messages.getMessage(id);
    }

    protected boolean needExportAll() {
        if (target.getSelected().isEmpty()
                || !(target.getItems() instanceof ContainerDataUnit)) {
            return true;
        }
        CollectionContainer container = ((ContainerDataUnit) target.getItems()).getContainer();
        return container != null && container.getItems().size() <= 1;
    }
}
