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
package io.jmix.reportsui.screen.report.wizard;

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.EntityTree;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reports.entity.wizard.ReportTypeGenerate;
import io.jmix.reports.exception.TemplateGenerationException;
import io.jmix.reports.exception.ValidationException;
import io.jmix.reportsui.screen.report.wizard.step.*;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.DialogWindow;
import io.jmix.ui.component.Label;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@UiController("report_ReportWizardCreator")
@UiDescriptor("report-wizard.xml")
public class ReportWizardCreator extends Screen implements WizardScreen {

    @Autowired
    protected InstanceContainer<ReportData> reportDataDc;

    @Autowired
    protected CollectionContainer<ReportRegion> reportRegionsDc;

    @Autowired
    protected CollectionContainer<ReportGroup> groupsDc;

    @Autowired
    protected Button nextBtn;

    @Autowired
    protected Button backBtn;

    @Autowired
    protected Button saveBtn;

    @Autowired
    protected Label<String> descriptionLabel;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ReportsWizard reportWizard;

    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageBundle messageBundle;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected DetailsStepFragment detailsFragment;

    @Autowired
    protected RegionsStepFragment regionsStepFragment;

    @Autowired
    protected SaveStepFragment saveStepFragment;

    @Autowired
    protected QueryStepFragment queryStepFragment;

    @Autowired
    protected StepFragmentManager stepFragmentManager;

    @Autowired
    protected DataContext dataContext;

    @Subscribe
    protected void onInit(InitEvent event) {
        reportDataDc.setItem(dataContext.create(ReportData.class));

        stepFragmentManager.setWizardFragment(this);
        stepFragmentManager.setStepFragments(getBasicFragments());

        stepFragmentManager.showCurrentFragment();
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        setReportGroup();
    }

    @Subscribe(id = "reportDataDc", target = Target.DATA_CONTAINER)
    public void onReportDataDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<ReportData> event) {
        if (event.getProperty().equals("reportTypeGenerate")) {
            List<StepFragment> stepFragments = new ArrayList<>(getBasicFragments());
            if (Objects.equals(event.getValue(), ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY)) {
                stepFragments.add(2, queryStepFragment);
            }

            stepFragmentManager.setStepFragments(stepFragments);
        }
    }

    protected void setReportGroup() {
        if (!groupsDc.getItems().isEmpty()) {
            getItem().setGroup(groupsDc.getItems().iterator().next());
        }
    }

    protected List<StepFragment> getBasicFragments() {
        return Arrays.asList(detailsFragment, regionsStepFragment, saveStepFragment);
    }

    @Subscribe("nextBtn")
    public void onNextBtnClick(Button.ClickEvent event) {
        MetaClass metaClass = metadata.findClass(getItem().getEntityName());

        if (metaClass == null) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messageBundle.getMessage("fillEntityMsg"))
                    .show();
            return;
        }

        if (detailsFragment.isNeedUpdateEntityModel()) {
            EntityTree entityTree = reportWizard.buildEntityTree(metaClass);

            regionsStepFragment.setEntityTreeHasSimpleAttrs(entityTree.getEntityTreeStructureInfo().isEntityTreeHasSimpleAttrs());
            regionsStepFragment.setEntityTreeHasCollections(entityTree.getEntityTreeStructureInfo().isEntityTreeRootHasCollections());

            getItem().setEntityTreeRootNode(entityTree.getEntityTreeRootNode());
            detailsFragment.setNeedUpdateEntityModel(false);
        }
        stepFragmentManager.nextFragment();
        centerWindow();
    }

    @Subscribe("backBtn")
    public void onBackBtnClick(Button.ClickEvent event) {
        stepFragmentManager.prevFragment();
        centerWindow();
    }

    @Override
    public Button getForwardBtn() {
        return nextBtn;
    }

    @Override
    public Button getSaveBtn() {
        return saveBtn;
    }

    @Override
    public Button getBackwardBtn() {
        return backBtn;
    }

    @Override
    public void setCaption(String caption) {
        getWindow().setCaption(caption);
    }

    @Override
    public void setDescription(String description) {
        descriptionLabel.setValue(description);
    }

    @Subscribe("save")
    public void onSave(Action.ActionPerformedEvent event) {
        try {
            stepFragmentManager.validateCurrentFragment();
        } catch (ValidationException e) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage("validationFail.caption"))
                    .withDescription(e.getMessage())
                    .show();
            return;
        }
        if (reportDataDc.getItem().getReportRegions().isEmpty()) {
            dialogs.createOptionDialog()
                    .withCaption(messages.getMessage("dialogs.Confirmation"))
                    .withMessage(messageBundle.getMessage("confirmSaveWithoutRegions"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK).withHandler(handle ->
                                    convertToReportAndForceCloseWizard()
                            ),
                            new DialogAction(DialogAction.Type.NO)
                    ).show();
        } else {
            convertToReportAndForceCloseWizard();
        }
    }

    protected void centerWindow() {
        DialogWindow dialogWindow = (DialogWindow) getWindow();
        dialogWindow.center();
    }

    protected void convertToReportAndForceCloseWizard() {
        Report report = buildReport(false);
        if (report != null) {
            close(WINDOW_COMMIT_AND_CLOSE_ACTION);
        }
    }

    @Nullable
    public Report buildReport(boolean temporary) {
        ReportData reportData = reportDataDc.getItem();

        // be sure that reportData.name and reportData.outputFileFormat is not null before generation of template
        try {
            byte[] templateByteArray = reportWizard.generateTemplate(reportData, reportData.getTemplateFileType());
            reportData.setTemplateContent(templateByteArray);
        } catch (TemplateGenerationException e) {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage("templateGenerationException"))
                    .show();
            return null;
        }

        MetaClass entityMetaClass = metadata.getClass(reportData.getEntityName());
        String storeName = entityMetaClass.getStore().getName();

        if (!Stores.isMain(storeName)) {
            reportData.setDataStore(storeName);
        }

        Report report = reportWizard.toReport(reportData, temporary);
        reportData.setGeneratedReport(report);
        return report;
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        CloseAction closeAction = event.getCloseAction();
        boolean checkUnsavedChanges = closeAction instanceof ChangeTrackerCloseAction
                && ((ChangeTrackerCloseAction) closeAction).isCheckForUnsavedChanges();

        if (!event.closedWith(StandardOutcome.COMMIT) && checkUnsavedChanges
                && CollectionUtils.isNotEmpty(reportRegionsDc.getItems())) {
            dialogs.createOptionDialog()
                    .withCaption(messages.getMessage("dialogs.Confirmation"))
                    .withMessage(messages.getMessage(getClass(), "interruptConfirm"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK).withHandler(handle ->
                                    close(WINDOW_DISCARD_AND_CLOSE_ACTION)
                            ),
                            new DialogAction(DialogAction.Type.NO)
                    ).show();
            event.preventWindowClose();
        }
    }

    public ReportData getItem() {
        return reportDataDc.getItem();
    }
}