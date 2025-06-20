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

package io.jmix.messagetemplatesflowui.view.messagetemplate;

import com.google.common.io.Files;
import com.vaadin.flow.router.Route;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.accesscontext.UiEntityContext;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.upload.FileUploadField;
import io.jmix.flowui.download.ByteArrayDownloadDataProvider;
import io.jmix.flowui.download.DownloadFormat;
import io.jmix.flowui.download.Downloader;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.kit.component.upload.event.FileUploadSucceededEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import io.jmix.messagetemplates.entity.MessageTemplate;
import io.jmix.messagetemplates.entity.MessageTemplateParameter;
import io.jmix.messagetemplates.entity.TemplateType;
import io.jmix.messagetemplatesflowui.accesscontext.UiImportExportMessageTemplateContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;
import static io.jmix.flowui.download.DownloadFormat.JSON;
import static io.jmix.flowui.download.DownloadFormat.ZIP;

@Route(value = "msgtmp/messagetemplate", layout = DefaultMainViewParent.class)
@ViewController("msgtmp_MessageTemplate.list")
@ViewDescriptor("message-template-list-view.xml")
@LookupComponent("messageTemplatesDataGrid")
@DialogMode(width = "64em")
public class MessageTemplateListView extends StandardListView<MessageTemplate> {

    private static final Logger log = LoggerFactory.getLogger(MessageTemplateListView.class);

    protected static final int MAX_CODE_LENGTH = 255;

    @ViewComponent
    protected MessageBundle messageBundle;

    @ViewComponent
    protected DataGrid<MessageTemplate> messageTemplatesDataGrid;
    @ViewComponent
    protected CollectionContainer<MessageTemplate> messageTemplatesDc;
    @ViewComponent
    protected CollectionLoader<MessageTemplate> messageTemplatesDl;

    @ViewComponent
    protected FileUploadField importField;
    @ViewComponent
    protected DropdownButton exportButton;

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected EntityImportExport entityImportExport;
    @Autowired
    protected EntityImportPlans entityImportPlans;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected Notifications notifications;

    protected boolean isCreatePermitted;
    protected boolean isImportExportPermitted;

    @Subscribe
    public void onInit(InitEvent event) {
        applySecurityConstraints();
    }

    protected void applySecurityConstraints() {
        MetaClass metaClass = messageTemplatesDc.getEntityMetaClass();

        UiEntityContext uiEntityContext = new UiEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(uiEntityContext);

        isCreatePermitted = uiEntityContext.isCreatePermitted();

        UiImportExportMessageTemplateContext uiImportExportContext = new UiImportExportMessageTemplateContext();
        accessManager.applyRegisteredConstraints(uiImportExportContext);

        isImportExportPermitted = uiImportExportContext.isPermitted();

        importField.setEnabled(isImportExportPermitted && isCreatePermitted);
        exportButton.setEnabled(isImportExportPermitted);
    }

    public void setTemplateType(String templateType) {
        messageTemplatesDl.setParameter("templateType", TemplateType.valueOf(templateType));
    }

    @Install(to = "messageTemplatesDataGrid.copy", subject = "enabledRule")
    public boolean messageTemplatesDataGridCopyEnabledRule() {
        return isCreatePermitted;
    }

    @Subscribe("messageTemplatesDataGrid.copy")
    public void onMessageTemplatesDataGridCopy(ActionPerformedEvent event) {
        MessageTemplate selectedItem = messageTemplatesDataGrid.getSingleSelectedItem();

        if (selectedItem != null) {
            copyTemplate(selectedItem);
            messageTemplatesDl.load();
        }
    }

    @Subscribe("importField")
    public void onImportFileUploadSucceeded(FileUploadSucceededEvent<FileUploadField> event) {
        byte[] fileBytes = event.getSource().getValue();

        Preconditions.checkNotNullArgument(fileBytes);

        Collection<Object> importedMessageTemplates = null;
        String fileName = event.getFileName();

        try {
            EntityImportPlan messageTemplatesImportPlan = createMessageTemplatesImportPlan();

            if (JSON.getFileExt().equals(Files.getFileExtension(fileName))) {
                String jsonContent = new String(fileBytes, StandardCharsets.UTF_8);
                importedMessageTemplates =
                        entityImportExport.importEntitiesFromJson(jsonContent, messageTemplatesImportPlan);
            } else {
                importedMessageTemplates =
                        entityImportExport.importEntitiesFromZIP(fileBytes, messageTemplatesImportPlan);
            }
        } catch (Exception e) {
            String importFailedHeader = messageBundle.getMessage("importFailedHeader");
            String importFailedMessage = messageBundle.formatMessage("importFailedMessage",
                    fileName, nullToEmpty(e.getMessage()));

            importFailedMessage = createMultiRowText(importFailedMessage,
                    messageBundle.getMessage("importFailedMessage").length()
                            + fileName.length() + 20);

            notifications.create(importFailedHeader, importFailedMessage)
                    .withType(Notifications.Type.ERROR)
                    .show();

            log.error("MessageTemplates import failed", e);
        }

        if (importedMessageTemplates == null) {
            return;
        }

        String importSuccessfulHeader =
                messageBundle.formatMessage("importSuccessfulHeader", importedMessageTemplates.size());

        notifications.create(importSuccessfulHeader)
                .withType(Notifications.Type.SUCCESS)
                .show();

        messageTemplatesDl.load();
    }

    protected EntityImportPlan createMessageTemplatesImportPlan() {
        return entityImportPlans.builder(MessageTemplate.class)
                .addLocalProperties()
                .addOneToManyProperty(
                        "parameters",
                        entityImportPlans.builder(MessageTemplateParameter.class)
                                .addLocalProperties()
                                .build(),
                        CollectionImportPolicy.KEEP_ABSENT_ITEMS
                )
                .build();
    }

    @Install(to = "messageTemplatesDataGrid.exportJson", subject = "enabledRule")
    public boolean messageTemplatesDataGridExportJsonEnabledRule() {
        return isImportExportPermitted;
    }

    @Install(to = "messageTemplatesDataGrid.exportZip", subject = "enabledRule")
    public boolean messageTemplatesDataGridExportZipEnabledRule() {
        return isImportExportPermitted;
    }

    @Subscribe("messageTemplatesDataGrid.exportJson")
    public void onMessageTemplatesDataGridExportJson(ActionPerformedEvent event) {
        doExport(JSON);
    }

    @Subscribe("messageTemplatesDataGrid.exportZip")
    public void onMessageTemplatesDataGridExportZip(ActionPerformedEvent event) {
        doExport(ZIP);
    }

    protected void doExport(DownloadFormat format) {
        Collection<MessageTemplate> selectedItems = messageTemplatesDataGrid.getSelectedItems();

        // export all if no selection
        if (selectedItems.isEmpty()) {
            selectedItems = messageTemplatesDc.getItems();
        }

        int saveExportedByteArrayDataThresholdBytes = uiProperties.getSaveExportedByteArrayDataThresholdBytes();
        String tempDir = coreProperties.getTempDir();

        byte[] bytes = null;

        try {
            Collection<MessageTemplate> reloadedMessageTemplates = reloadTemplatesWithParameters(selectedItems);

            if (JSON.equals(format)) {
                bytes = entityImportExport.exportEntitiesToJSON(reloadedMessageTemplates)
                        .getBytes(StandardCharsets.UTF_8);
            } else {
                bytes = entityImportExport.exportEntitiesToZIP(reloadedMessageTemplates);
            }
        } catch (Exception e) {
            String exportFailedHeader = messageBundle.formatMessage("exportFailedHeader", e.getMessage());

            exportFailedHeader = createMultiRowText(exportFailedHeader,
                    messageBundle.getMessage("exportFailedHeader").length() + 20
            );

            notifications.create(exportFailedHeader)
                    .withType(Notifications.Type.ERROR)
                    .show();

            log.error("MessageTemplates export failed", e);
        }

        if (bytes == null) {
            return;
        }

        ByteArrayDownloadDataProvider dataProvider =
                new ByteArrayDownloadDataProvider(bytes, saveExportedByteArrayDataThresholdBytes, tempDir);
        String resourceName = MessageTemplate.class.getSimpleName() + "." + format.getFileExt();

        downloader.download(dataProvider, resourceName, format);
    }

    protected void copyTemplate(MessageTemplate messageTemplate) {
        messageTemplate = reloadTemplateWithParameters(messageTemplate);

        MessageTemplate copiedTemplate = metadataTools.deepCopy(messageTemplate);
        copiedTemplate.setId(UuidProvider.createUuid());
        copiedTemplate.setCode(generateTemplateCode(copiedTemplate.getCode()));

        SaveContext saveContext = new SaveContext();
        saveContext.setDiscardSaved(true);
        saveContext.saving(copiedTemplate);

        if (copiedTemplate.getParameters() != null) {
            updateParameters(copiedTemplate);
            saveContext.saving(copiedTemplate.getParameters());
        }

        dataManager.save(saveContext);
    }

    protected void updateParameters(MessageTemplate copiedTemplate) {
        for (MessageTemplateParameter parameter : copiedTemplate.getParameters()) {
            parameter.setId(UuidProvider.createUuid());
            parameter.setTemplate(copiedTemplate);
        }
    }

    protected Collection<MessageTemplate> reloadTemplatesWithParameters(Collection<MessageTemplate> messageTemplates) {
        List<Object> ids = messageTemplates.stream()
                .map(EntityValues::getId)
                .toList();

        return dataManager.load(MessageTemplate.class)
                .ids(ids)
                .fetchPlan(this::fetchPlanBuilder)
                .list();
    }

    protected MessageTemplate reloadTemplateWithParameters(MessageTemplate template) {
        return dataManager.load(Id.of(template))
                .fetchPlan(this::fetchPlanBuilder)
                .one();
    }

    protected void fetchPlanBuilder(FetchPlanBuilder fetchPlanBuilder) {
        fetchPlanBuilder.addFetchPlan(FetchPlan.BASE)
                .add("parameters", FetchPlan.BASE)
                .build();
    }

    protected String generateTemplateCode(String existedCode) {
        return generateTemplateCode(existedCode, 0);
    }

    protected String generateTemplateCode(String existedCode, int iteration) {
        if (iteration == 1) {
            ++iteration; // like in file system: duplication of file 'a.txt' is a 'a (2).txt', NOT 'a (1).txt'
        }

        String templateCode = StringUtils.stripEnd(existedCode, null);
        if (iteration > 0) {
            String suffix = "-%s".formatted(iteration);
            String newTemplateCode = templateCode;

            while (newTemplateCode.length() + suffix.length() > MAX_CODE_LENGTH) {
                newTemplateCode = StringUtils.chop(newTemplateCode);
            }

            templateCode = newTemplateCode + suffix;
        }

        if (isTemplateCodeExist(templateCode)) {
            return generateTemplateCode(existedCode, ++iteration);
        }

        return templateCode;
    }

    protected boolean isTemplateCodeExist(String templateCode) {
        LoadContext<MessageTemplate> loadContext
                = new LoadContext<>(messageTemplatesDc.getEntityMetaClass());

        loadContext.setQueryString("select t from msgtmp_MessageTemplate t where t.code = :templateCode")
                .setParameter("templateCode", templateCode);

        return dataManager.getCount(loadContext) > 0;
    }

    protected String createMultiRowText(String text, int rowLength) {
        String[] parts = text.split(" ");
        StringBuilder sb = new StringBuilder();
        int currentRowLength = 0;

        for (int i = 0; i < parts.length; i++) {
            if (currentRowLength + parts[i].length() > rowLength) {
                sb.append("\n");
                currentRowLength = 0;
            }
            sb.append(parts[i]);
            currentRowLength += parts[i].length();
            if (i != parts.length - 1) {
                sb.append(" ");
                currentRowLength++;
            }
        }
        return sb.toString();
    }
}
