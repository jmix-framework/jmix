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

package io.jmix.datatoolsui.action;

import io.jmix.core.CoreProperties;
import io.jmix.core.EntityImportExport;
import io.jmix.core.JmixEntity;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.datatoolsui.screen.entityinspector.EntityInspectorBrowser;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.ActionType;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentsHelper;
import io.jmix.ui.component.Table;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.screen.ScreenContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Collection;

import static io.jmix.ui.download.DownloadFormat.JSON;
import static io.jmix.ui.download.DownloadFormat.ZIP;

@SuppressWarnings({"rawtypes", "unchecked"})
@ActionType(ExportAction.ID)
public class ExportAction extends ItemTrackingAction {

    public static final String ID = "export";

    protected static final Logger log = LoggerFactory.getLogger(ExportAction.class);

    protected EntityImportExport entityImportExport;
    protected Downloader exportDisplay;
    protected Messages messages;
    protected UiProperties uiProperties;
    protected CoreProperties coreProperties;

    protected DownloadFormat format;
    protected MetaClass metaClass;
    protected Table table;

    public ExportAction() {
        super(ID);
    }

    public ExportAction(String id) {
        super(id);
    }

    @Autowired
    protected void setMessages(Messages messages) {
        this.messages = messages;
        this.caption = messages.getMessage(EntityInspectorBrowser.class, id);
    }

    @Autowired
    public void setEntityImportExport(EntityImportExport entityImportExport) {
        this.entityImportExport = entityImportExport;
    }

    @Autowired
    public void setExportDisplay(Downloader exportDisplay) {
        this.exportDisplay = exportDisplay;
    }

    @Autowired
    public void setUiProperties(UiProperties uiProperties) {
        this.uiProperties = uiProperties;
    }

    @Autowired
    public void setCoreProperties(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    public void setFormat(DownloadFormat format) {
        this.format = format;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    @Override
    public void actionPerform(Component component) {
        Collection<JmixEntity> selected = table.getSelected();
        if (selected.isEmpty()
                && table.getItems() != null) {
            selected = table.getItems().getItems();
        }

        try {
            int saveExportedByteArrayDataThresholdBytes = uiProperties.getSaveExportedByteArrayDataThresholdBytes();
            String tempDir = coreProperties.getTempDir();
            if (format == ZIP) {
                byte[] data = entityImportExport.exportEntitiesToZIP(selected);
                String resourceName = metaClass.getJavaClass().getSimpleName() + ".zip";
                exportDisplay.download(
                        new ByteArrayDataProvider(data, saveExportedByteArrayDataThresholdBytes, tempDir), resourceName, ZIP);
            } else if (format == JSON) {
                byte[] data = entityImportExport.exportEntitiesToJSON(selected)
                        .getBytes(StandardCharsets.UTF_8);
                String resourceName = metaClass.getJavaClass().getSimpleName() + ".json";
                exportDisplay.download(
                        new ByteArrayDataProvider(data, saveExportedByteArrayDataThresholdBytes, tempDir), resourceName, JSON);
            }
        } catch (Exception e) {
            ScreenContext screenContext = ComponentsHelper.getScreenContext(table);
            Notifications notifications = screenContext.getNotifications();
            notifications.create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(EntityInspectorBrowser.class, "exportFailed"))
                    .withDescription(e.getMessage())
                    .show();
            log.error("Entities export failed", e);
        }
    }
}
