/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsui.screen.report.edit.tabs;

import io.jmix.core.CoreProperties;
import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.Sort;
import io.jmix.reports.entity.*;
import io.jmix.reportsui.screen.definition.edit.BandDefinitionEditor;
import io.jmix.reportsui.screen.template.edit.TemplateEditor;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.Screens;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.app.file.FileUploadDialog;
import io.jmix.ui.component.*;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.*;
import io.jmix.ui.screen.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@UiController("report_ReportEditGeneral.fragment")
@UiDescriptor("report-edit-general-fragment.xml")
public class ReportEditGeneralFragment extends ScreenFragment {

    @Autowired
    protected Tree<BandDefinition> bandsTree;

    @Autowired
    protected InstanceLoader<Report> reportDl;

    @Autowired
    protected InstanceContainer<Report> reportDc;

    @Autowired
    protected CollectionContainer<BandDefinition> bandsDc;

    @Autowired
    protected CollectionContainer<BandDefinition> availableParentBandsDc;

    @Autowired
    protected CollectionPropertyContainer<ReportTemplate> templatesDc;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SecureOperations secureOperations;

    @Autowired
    protected PolicyStore policyStore;

    @Autowired
    protected CollectionPropertyContainer<DataSet> dataSetsDc;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Downloader downloader;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected Screens screens;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected BandDefinitionEditor bandEditor;

    @Autowired
    protected FileUploadField invisibleFileUpload;

    @Autowired
    private HBoxLayout reportFields;

    @Autowired
    protected Button up;

    @Autowired
    protected Button down;

    @Autowired
    protected EntityComboBox<ReportTemplate> defaultTemplateField;

    @Autowired
    protected DataContext dataContext;

    @Subscribe("invisibleFileUpload")
    protected void onInvisibleFileUploadFileUploadSucceed(SingleFileUploadField.FileUploadSucceedEvent event) {
        final ReportTemplate defaultTemplate = reportDc.getItem().getDefaultTemplate();
        if (defaultTemplate != null) {
            if (!isTemplateWithoutFile(defaultTemplate)) {
                defaultTemplate.setContent(invisibleFileUpload.getValue());
                defaultTemplate.setName(event.getFileName());
                templatesDc.replaceItem(defaultTemplate);
            } else {
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withCaption(messageBundle.getMessage("notification.fileIsNotAllowedForSpecificTypes"))
                        .show();
            }
        } else {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messageBundle.getMessage("notification.defaultTemplateIsEmpty"))
                    .show();
        }
    }

    @Subscribe(id = "bandsDc", target = Target.DATA_CONTAINER)
    protected void onBandsDcItemChange(InstanceContainer.ItemChangeEvent<BandDefinition> event) {
        bandEditor.setEnabled(event.getItem() != null);
        availableParentBandsDc.getMutableItems().clear();
        if (event.getItem() != null) {
            for (BandDefinition bandDefinition : bandsDc.getItems()) {
                if (!isChildOrEqual(event.getItem(), bandDefinition) ||
                        Objects.equals(event.getItem().getParentBandDefinition(), bandDefinition)) {
                    availableParentBandsDc.getMutableItems().add(bandDefinition);
                }
            }
        }
    }

    @Subscribe(id = "bandsDc", target = Target.DATA_CONTAINER)
    protected void onBandsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<BandDefinition> event) {
        if ("parentBandDefinition".equals(event.getProperty())) {
            BandDefinition previousParent = (BandDefinition) event.getPrevValue();
            BandDefinition parent = (BandDefinition) event.getValue();

            if (event.getValue() == event.getItem()) {
                event.getItem().setParentBandDefinition(previousParent);
            } else {
                previousParent.getChildrenBandDefinitions().remove(event.getItem());
                parent.getChildrenBandDefinitions().add(event.getItem());
                bandsDc.replaceItem(previousParent);
                bandsDc.replaceItem(parent);
            }

            if (event.getPrevValue() != null) {
                orderBandDefinitions(previousParent);
            }

            if (event.getValue() != null) {
                orderBandDefinitions(parent);
            }
        }
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        defaultTemplateField.setEditable(isUpdatePermitted());
    }

    @Subscribe("defaultTemplateField.create")
    protected void onDefaultTemplateFieldCreate(Action.ActionPerformedEvent event) {
        Report report = reportDc.getItem();
        ReportTemplate template = metadata.create(ReportTemplate.class);
        template.setReport(report);

        TemplateEditor editor = screenBuilders.editor(defaultTemplateField)
                .withScreenClass(TemplateEditor.class)
                .withContainer(templatesDc)
                .withOpenMode(OpenMode.DIALOG)
                .editEntity(template)
                .build();

        editor.addAfterCloseListener(e -> {
            StandardCloseAction standardCloseAction = (StandardCloseAction) e.getCloseAction();
            if (Window.COMMIT_ACTION_ID.equals(standardCloseAction.getActionId())) {
                ReportTemplate item = editor.getEditedEntity();
                templatesDc.getMutableItems().add(item);
                report.setDefaultTemplate(item);
            }
            defaultTemplateField.focus();
        });
        editor.show();
    }

    @Install(to = "defaultTemplateField.create", subject = "enabledRule")
    protected boolean defaultTemplateFieldCreateEnabledRule() {
        return isUpdatePermitted();
    }

    @Install(to = "defaultTemplateField.edit", subject = "enabledRule")
    protected boolean defaultTemplateFieldEditEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("defaultTemplateField.edit")
    protected void onDefaultTemplateFieldEdit(Action.ActionPerformedEvent event) {
        Report report = reportDc.getItem();
        ReportTemplate defaultTemplate = report.getDefaultTemplate();
        if (defaultTemplate != null) {
            TemplateEditor editor = screenBuilders.editor(defaultTemplateField)
                    .withScreenClass(TemplateEditor.class)
                    .withOpenMode(OpenMode.DIALOG)
                    .withContainer(templatesDc)
                    .editEntity(defaultTemplate)
                    .build();

            editor.addAfterCloseListener(e -> {
                StandardCloseAction standardCloseAction = (StandardCloseAction) e.getCloseAction();
                if (Window.COMMIT_ACTION_ID.equals(standardCloseAction.getActionId())) {
                    ReportTemplate item = (ReportTemplate) editor.getEditedEntity();
                    report.setDefaultTemplate(item);
                }
                defaultTemplateField.focus();
            });
            editor.show();
        } else {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messageBundle.getMessage("notification.defaultTemplateIsEmpty"))
                    .show();
        }
    }

    @Subscribe("defaultTemplateField.upload")
    protected void onDefaultTemplateUpload(Action.ActionPerformedEvent event) {
        final ReportTemplate defaultTemplate = reportDc.getItem().getDefaultTemplate();
        if (defaultTemplate != null) {
            if (!isTemplateWithoutFile(defaultTemplate)) {
                FileUploadDialog fileUploadDialog = screenBuilders.screen(getFragment().getFrameOwner())
                        .withScreenClass(FileUploadDialog.class)
                        .withOpenMode(OpenMode.DIALOG)
                        .build();

                fileUploadDialog.addAfterCloseListener(closeEvent -> {
                    StandardCloseAction standardCloseAction = (StandardCloseAction) closeEvent.getCloseAction();
                    if (Window.COMMIT_ACTION_ID.equals(standardCloseAction.getActionId())) {
                        try {
                            InputStream content = fileUploadDialog.getFileContent();
                            if (content != null) {
                                defaultTemplate.setContent(IOUtils.toByteArray(content));
                                defaultTemplate.setName(fileUploadDialog.getFileName());
                                templatesDc.replaceItem(defaultTemplate);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(String.format(
                                    "An error occurred while uploading file for template [%s]",
                                    defaultTemplate.getCode()));
                        }
                    }
                    defaultTemplateField.focus();
                });
                fileUploadDialog.show();

            } else {
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withCaption(messageBundle.getMessage("notification.fileIsNotAllowedForSpecificTypes"))
                        .show();
            }
        } else {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messageBundle.getMessage("notification.defaultTemplateIsEmpty"))
                    .show();
        }
    }

    @Install(to = "defaultTemplateField.upload", subject = "enabledRule")
    protected boolean defaultTemplateFieldUploadEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("defaultTemplateField.download")
    protected void onDefaultTemplateDownload(Action.ActionPerformedEvent event) {
        ReportTemplate defaultTemplate = reportDc.getItem().getDefaultTemplate();
        if (defaultTemplate != null) {
            if (defaultTemplate.isCustom()) {
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withCaption(messageBundle.getMessage("unableToSaveTemplateWhichDefinedWithClass"))
                        .show();
            } else if (isTemplateWithoutFile(defaultTemplate)) {
                notifications.create(Notifications.NotificationType.HUMANIZED)
                        .withCaption(messageBundle.getMessage("notification.fileIsNotAllowedForSpecificTypes"))
                        .show();
            } else {
                byte[] reportTemplate = defaultTemplate.getContent();
                downloader.download(new ByteArrayDataProvider(reportTemplate, uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()),
                        defaultTemplate.getName(), DownloadFormat.getByExtension(defaultTemplate.getExt()));
            }
        } else {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messageBundle.getMessage("notification.defaultTemplateIsEmpty"))
                    .show();
        }

        defaultTemplateField.focus();
    }


    @Install(to = "bandsTree.upAction", subject = "enabledRule")
    protected boolean bandsTreeUpActionEnabledRule() {
        return isUpButtonEnabled();
    }

    @Install(to = "bandsTree.downAction", subject = "enabledRule")
    protected boolean bandsTreeDownActionEnabledRule() {
        return isDownButtonEnabled();
    }

    public void sortBandDefinitionsByPosition() {
        bandsDc.getSorter().sort(Sort.by(Sort.Direction.ASC, "position"));
    }

    @Subscribe("bandsTree.create")
    protected void onBandsTreeCreate(Action.ActionPerformedEvent event) {
        BandDefinition parentDefinition = bandsDc.getItemOrNull();
        Report report = reportDc.getItem();
        // Use root band as parent if no items selected
        if (parentDefinition == null) {
            parentDefinition = report.getRootBandDefinition();
        }
        if (parentDefinition.getChildrenBandDefinitions() == null) {
            parentDefinition.setChildrenBandDefinitions(new ArrayList<>());
        }


        orderBandDefinitions(parentDefinition);

        BandDefinition newBandDefinition = dataContext.create(BandDefinition.class);
        newBandDefinition.setName("newBand" + (parentDefinition.getChildrenBandDefinitions().size() + 1));
        newBandDefinition.setOrientation(Orientation.HORIZONTAL);
        newBandDefinition.setParentBandDefinition(parentDefinition);
        if (parentDefinition.getChildrenBandDefinitions() != null) {
            newBandDefinition.setPosition(parentDefinition.getChildrenBandDefinitions().size());
        } else {
            newBandDefinition.setPosition(0);
        }
        newBandDefinition.setReport(report);
        parentDefinition.getChildrenBandDefinitions().add(newBandDefinition);

        bandsDc.getMutableItems().add(newBandDefinition);

        bandsTree.expandTree();
        bandsTree.setSelected(newBandDefinition);//let's try and see if it increases usability

        bandsTree.focus();
    }

    @Install(to = "bandsTree.create", subject = "enabledRule")
    protected boolean bandsTreeCreateEnabledRule() {
        return isUpdatePermitted();
    }

    @Subscribe("bandsTree.remove")
    protected void onBandsTreeRemove(Action.ActionPerformedEvent event) {
        Set<BandDefinition> selected = bandsTree.getSelected();
        removeChildrenCascade(selected);
        for (Object object : selected) {
            BandDefinition definition = (BandDefinition) object;
            if (definition.getParentBandDefinition() != null) {
                orderBandDefinitions(((BandDefinition) object).getParentBandDefinition());
            }
        }
        bandsTree.focus();
    }

    @Install(to = "bandsTree.remove", subject = "enabledRule")
    protected boolean bandsTreeRemoveEnabledRule() {
        Object selectedItem = bandsTree.getSingleSelected();
        if (selectedItem != null) {
            return !Objects.equals(reportDc.getItem().getRootBandDefinition(), selectedItem);
        }

        return false;
    }

    private void removeChildrenCascade(Collection selected) {
        for (Object o : selected) {
            BandDefinition definition = (BandDefinition) o;
            BandDefinition parentDefinition = definition.getParentBandDefinition();
            if (parentDefinition != null) {
                definition.getParentBandDefinition().getChildrenBandDefinitions().remove(definition);
            }

            if (definition.getChildrenBandDefinitions() != null) {
                removeChildrenCascade(new ArrayList<>(definition.getChildrenBandDefinitions()));
            }

            if (definition.getDataSets() != null) {
                bandsDc.setItem(definition);
                for (DataSet dataSet : new ArrayList<>(definition.getDataSets())) {
                    if (entityStates.isNew(dataSet)) {
                        dataSetsDc.getMutableItems().remove(dataSet);
                    }
                }
            }
            bandsDc.getMutableItems().remove(definition);
        }
    }

    protected void orderBandDefinitions(BandDefinition parent) {
        if (parent.getChildrenBandDefinitions() != null) {
            List<BandDefinition> childrenBandDefinitions = parent.getChildrenBandDefinitions();
            for (int i = 0, childrenBandDefinitionsSize = childrenBandDefinitions.size(); i < childrenBandDefinitionsSize; i++) {
                BandDefinition bandDefinition = childrenBandDefinitions.get(i);
                bandDefinition.setPosition(i);
            }
            sortBandDefinitionsByPosition();
        }
    }

    @Subscribe("up")
    protected void onUpClick(Button.ClickEvent event) {
        BandDefinition definition = bandsTree.getSingleSelected();
        if (definition != null && definition.getParentBandDefinition() != null) {
            BandDefinition parentDefinition = definition.getParentBandDefinition();
            List<BandDefinition> definitionsList = parentDefinition.getChildrenBandDefinitions();
            int index = definitionsList.indexOf(definition);
            if (index > 0) {
                BandDefinition previousDefinition = definitionsList.get(index - 1);
                definition.setPosition(definition.getPosition() - 1);
                previousDefinition.setPosition(previousDefinition.getPosition() + 1);

                definitionsList.set(index, previousDefinition);
                definitionsList.set(index - 1, definition);

                sortBandDefinitionsByPosition();
            }
        }
    }

    protected boolean isUpButtonEnabled() {
        if (bandsTree != null) {
            BandDefinition selectedItem = bandsTree.getSingleSelected();
            return selectedItem != null && selectedItem.getPosition() > 0 && isUpdatePermitted();
        }
        return false;
    }

    @Subscribe("down")
    protected void onDownClick(Button.ClickEvent event) {
        BandDefinition definition = bandsTree.getSingleSelected();
        if (definition != null && definition.getParentBandDefinition() != null) {
            BandDefinition parentDefinition = definition.getParentBandDefinition();
            List<BandDefinition> definitionsList = parentDefinition.getChildrenBandDefinitions();
            int index = definitionsList.indexOf(definition);
            if (index < definitionsList.size() - 1) {
                BandDefinition nextDefinition = definitionsList.get(index + 1);
                definition.setPosition(definition.getPosition() + 1);
                nextDefinition.setPosition(nextDefinition.getPosition() - 1);

                definitionsList.set(index, nextDefinition);
                definitionsList.set(index + 1, definition);

                sortBandDefinitionsByPosition();
            }
        }
    }


    protected boolean isDownButtonEnabled() {
        if (bandsTree != null) {
            BandDefinition bandDefinition = bandsTree.getSingleSelected();
            if (bandDefinition != null) {
                BandDefinition parent = bandDefinition.getParentBandDefinition();
                return parent != null &&
                        parent.getChildrenBandDefinitions() != null &&
                        bandDefinition.getPosition() < parent.getChildrenBandDefinitions().size() - 1
                        && isUpdatePermitted();
            }
        }
        return false;
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }

    protected boolean isTemplateWithoutFile(ReportTemplate template) {
        return template.getOutputType() == JmixReportOutputType.chart ||
                template.getOutputType() == JmixReportOutputType.table ||
                template.getOutputType() == JmixReportOutputType.pivot;
    }

    protected boolean isChildOrEqual(BandDefinition definition, BandDefinition child) {
        if (definition.equals(child)) {
            return true;
        } else if (child != null) {
            return isChildOrEqual(definition, child.getParentBandDefinition());
        } else {
            return false;
        }
    }

    public void setupDropZoneForTemplate() {
        final ReportTemplate defaultTemplate = reportDc.getItem().getDefaultTemplate();
        if (defaultTemplate != null) {
            invisibleFileUpload.setDropZone(new UploadField.DropZone(reportFields));
        } else {
            invisibleFileUpload.setDropZone(null);
        }
    }
}
