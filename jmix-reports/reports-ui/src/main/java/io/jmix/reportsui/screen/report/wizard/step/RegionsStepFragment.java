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

package io.jmix.reportsui.screen.report.wizard.step;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.wizard.*;
import io.jmix.reportsui.runner.FluentUiReportRunner;
import io.jmix.reportsui.runner.ParametersDialogShowMode;
import io.jmix.reportsui.runner.UiReportRunner;
import io.jmix.reportsui.screen.ReportsClientProperties;
import io.jmix.reportsui.screen.report.wizard.ReportWizardCreator;
import io.jmix.reportsui.screen.report.wizard.region.EntityTreeLookup;
import io.jmix.reportsui.screen.report.wizard.region.RegionEditor;
import io.jmix.ui.*;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UiController("report_RegionStep.fragment")
@UiDescriptor("regions-step-fragment.xml")
public class RegionsStepFragment extends StepFragment {
    protected static final int MAX_ATTRS_BTN_CAPTION_WIDTH = 135;

    @Autowired
    protected PopupButton addRegionPopupBtn;

    @Autowired
    protected Button addTabulatedRegionBtn;

    @Autowired
    protected Button addSimpleRegionBtn;

    @Autowired
    protected Button addRegionDisabledBtn;

    @Autowired
    protected Button removeBtn;

    @Autowired
    protected Table<ReportRegion> regionsTable;

    @Autowired
    protected BoxLayout buttonsBox;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected UiReportRunner uiReportRunner;

    @Autowired
    protected ReportsClientProperties reportsClientProperties;

    @Autowired
    private InstanceContainer<ReportData> reportDataDc;

    @Autowired
    private CollectionPropertyContainer<ReportRegion> reportRegionsDc;

    @Autowired
    protected Button runBtn;

    protected Report lastGeneratedTmpReport;

    protected boolean entityTreeHasSimpleAttrs;
    protected boolean entityTreeHasCollections;

    public void setEntityTreeHasCollections(boolean entityTreeHasCollections) {
        this.entityTreeHasCollections = entityTreeHasCollections;
    }

    public void setEntityTreeHasSimpleAttrs(boolean entityTreeHasSimpleAttrs) {
        this.entityTreeHasSimpleAttrs = entityTreeHasSimpleAttrs;
    }

    @Nullable
    protected ReportTypeGenerate getReportTypeGenerate() {
        return reportDataDc.getItem().getReportTypeGenerate();
    }

    @Install(to = "regionsTable.down", subject = "enabledRule")
    private boolean regionsTableDownEnabledRule() {
        ReportRegion item = regionsTable.getSingleSelected();
        if (item == null) {
            return false;
        }
        return item.getOrderNum() < reportRegionsDc.getItems().size();
    }

    @Install(to = "regionsTable.up", subject = "enabledRule")
    private boolean regionsTableUpEnabledRule() {
        ReportRegion item = regionsTable.getSingleSelected();
        if (item == null) {
            return false;
        }
        return item.getOrderNum() > 1;
    }

    @Override
    public void beforeShow() {
        updateButtons();
        showAddRegion();
    }

    @Override
    public void afterShow() {
        runBtn.setVisible(getReportTypeGenerate() != ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY);
    }

    @Override
    public String getCaption() {
        return messageBundle.getMessage("reportRegions");
    }

    @Override
    public String getDescription() {
        if (getReportTypeGenerate() != null && getReportTypeGenerate().isList()) {
            MetaClass entityMetaClass = metadata.getClass(reportDataDc.getItem().getEntityName());
            return messageBundle.formatMessage("regionTabulatedMessage", messageTools.getEntityCaption(entityMetaClass));
        } else {
            return messageBundle.getMessage("addPropertiesAndTableAreas");
        }
    }

    @Install(to = "regionsTable.attributes", subject = "columnGenerator")
    protected Component regionsTableAttributesColumnGenerator(ReportRegion reportRegion) {
        String attributes = StringUtils.abbreviate(StringUtils.join(
                CollectionUtils.collect(reportRegion.getRegionProperties(),
                        RegionProperty::getHierarchicalLocalizedNameExceptRoot), ", "),
                MAX_ATTRS_BTN_CAPTION_WIDTH);
        LinkButton linkButton = uiComponents.create(LinkButton.class);
        linkButton.setHeight("40px");
        linkButton.setCaption(attributes);
        linkButton.setWidthFull();
        linkButton.addClickListener(event -> editRegion());
        return linkButton;
    }

    protected void editRegion() {
        ReportRegion selectedRegion = regionsTable.getSingleSelected();
        if (selectedRegion != null) {
            Map<String, Object> editorParams = new HashMap<>();
            editorParams.put("rootEntity", selectedRegion.getRegionPropertiesRootNode());
            editorParams.put("scalarOnly", Boolean.TRUE);
            editorParams.put("persistentOnly", ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY == getReportTypeGenerate());
            showRegionEditor(selectedRegion, editorParams);
        }
    }

    @Install(to = "regionsTable.name", subject = "columnGenerator")
    protected Component regionsTableNameColumnGenerator(ReportRegion reportRegion) {
        String messageKey = reportRegion.isTabulatedRegion() ? "ReportRegion.tabulatedName" : "ReportRegion.simpleName";
        return new Table.PlainTextCell(messageBundle.formatMessage(messageKey, reportRegion.getOrderNum()));
    }

    @Install(to = "regionsTable.entity", subject = "columnGenerator")
    protected Component regionsTableEntityColumnGenerator(ReportRegion reportRegion) {
        MetaClass metaClass = metadata.getClass(reportRegion.getRegionPropertiesRootNode().getMetaClassName());
        return new Table.PlainTextCell(messageTools.getEntityCaption(metaClass));
    }

    @Subscribe("addRegionPopupBtn.addTabulatedRegion")
    public void onAddRegionPopupBtnAddTabulatedRegion(Action.ActionPerformedEvent event) {
        openTabulatedRegionEditor(createReportRegion(true));
    }

    @Subscribe("addRegionPopupBtn.addSimpleRegion")
    public void onAddRegionPopupBtnAddSimpleRegion(Action.ActionPerformedEvent event) {
        openRegionEditor(createReportRegion(false));
    }

    protected ReportRegion createReportRegion(boolean tabulated) {
        ReportRegion reportRegion = metadata.create(ReportRegion.class);
        reportRegion.setReportData(reportDataDc.getItem());
        reportRegion.setIsTabulatedRegion(tabulated);
        reportRegion.setOrderNum((long) reportDataDc.getItem().getReportRegions().size() + 1L);
        return reportRegion;
    }

    protected void showAddRegion() {
        if (reportRegionsDc.getItems().isEmpty()) {
            if (getReportTypeGenerate() != null && getReportTypeGenerate().isList()) {
                if (entityTreeHasSimpleAttrs) {
                    openTabulatedRegionEditor(createReportRegion(true));
                }
            } else {
                if (entityTreeHasSimpleAttrs) {
                    openRegionEditor(createReportRegion(false));
                } else if (entityTreeHasCollections) {
                    openTabulatedRegionEditor(createReportRegion(true));
                }
            }
        }
    }

    protected void openTabulatedRegionEditor(final ReportRegion item) {
        if (ReportTypeGenerate.SINGLE_ENTITY == getReportTypeGenerate()) {
            openRegionEditorOnlyWithNestedCollections(item);
        } else {
            openRegionEditor(item);
        }
    }

    protected void openRegionEditorOnlyWithNestedCollections(final ReportRegion item) {//show lookup for choosing parent collection for tabulated region
        final Map<String, Object> lookupParams = new HashMap<>();
        lookupParams.put("rootEntity", reportDataDc.getItem().getEntityTreeRootNode());
        lookupParams.put("collectionsOnly", Boolean.TRUE);

        EntityTreeLookup entityTreeLookup = (EntityTreeLookup) screenBuilders.lookup(EntityTreeNode.class, getFragment().getFrameOwner())
                .withScreenId("report_ReportEntityTree.lookup")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(lookupParams))
                .withSelectHandler(items -> {
                    if (items.size() == 1) {
                        EntityTreeNode regionPropertiesRootNode = IterableUtils.get(items, 0);

                        Map<String, Object> editorParams = new HashMap<>();
                        editorParams.put("scalarOnly", Boolean.TRUE);
                        editorParams.put("rootEntity", regionPropertiesRootNode);
                        item.setRegionPropertiesRootNode(regionPropertiesRootNode);

                        showRegionEditor(item, editorParams);
                    }
                })
                .build();

        entityTreeLookup.show();
    }

    protected void openRegionEditor(ReportRegion item) {
        item.setRegionPropertiesRootNode(reportDataDc.getItem().getEntityTreeRootNode());

        Map<String, Object> editorParams = new HashMap<>();
        editorParams.put("rootEntity", reportDataDc.getItem().getEntityTreeRootNode());
        editorParams.put("scalarOnly", Boolean.TRUE);
        editorParams.put("persistentOnly", ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY == getReportTypeGenerate());

        showRegionEditor(item, editorParams);
    }

    protected void showRegionEditor(ReportRegion item, Map<String, Object> editorParams) {
        RegionEditor regionEditor = screenBuilders.editor(ReportRegion.class, getFragment().getFrameOwner())
                .withScreenClass(RegionEditor.class)
                .editEntity(item)
                .withOpenMode(OpenMode.DIALOG)
                .withContainer(reportRegionsDc)
                .withOptions(new MapScreenOptions(editorParams))
                .build();

        regionEditor.show();
    }

    @Subscribe("runBtn")
    public void onRunBtnClick(Button.ClickEvent event) {
        if (reportDataDc.getItem().getReportRegions().isEmpty()) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messageBundle.getMessage("addRegionsWarn"))
                    .show();
            return;
        }
        ReportWizardCreator reportWizardCreator = (ReportWizardCreator) getFragment().getFrameOwner().getHostController();
        lastGeneratedTmpReport = reportWizardCreator.buildReport(true);

        if (lastGeneratedTmpReport != null) {
            FluentUiReportRunner fluentRunner = uiReportRunner.byReportEntity(lastGeneratedTmpReport)
                    .withParametersDialogShowMode(ParametersDialogShowMode.IF_REQUIRED);
            if (reportsClientProperties.getUseBackgroundReportProcessing()) {
                fluentRunner.inBackground(getFragment().getFrameOwner());
            }
            fluentRunner.runAndShow();
        }
    }

    @Install(to = "regionsTable.remove", subject = "afterActionPerformedHandler")
    protected void regionsTableRemoveAfterActionPerformedHandler(RemoveOperation.AfterActionPerformedEvent<ReportRegion> afterActionPerformedEvent) {
        normalizeRegionPropertiesOrderNum();
    }

    protected void normalizeRegionPropertiesOrderNum() {
        long normalizedIdx = 0;
        List<ReportRegion> allItems = new ArrayList<>(reportRegionsDc.getItems());
        for (ReportRegion item : allItems) {
            item.setOrderNum(++normalizedIdx); //first must to be 1
        }
    }

    @Subscribe(id = "reportRegionsDc", target = Target.DATA_CONTAINER)
    public void onReportRegionsDcCollectionChange(CollectionContainer.CollectionChangeEvent<ReportRegion> event) {
        if (event.getChangeType() == CollectionChangeType.ADD_ITEMS || event.getChangeType() == CollectionChangeType.REMOVE_ITEMS) {
            updateButtons();
        }
    }

    protected void updateButtons() {
        ReportData item = reportDataDc.getItem();
        buttonsBox.removeAll();
        if (item.getReportTypeGenerate().isList()) {
            addTabulatedRegionBtn.setEnabled(entityTreeHasSimpleAttrs && item.getReportRegions().isEmpty());
            buttonsBox.add(addTabulatedRegionBtn);
        } else {
            if (entityTreeHasSimpleAttrs && entityTreeHasCollections) {
                buttonsBox.add(addRegionPopupBtn);
            } else if (entityTreeHasSimpleAttrs) {
                buttonsBox.add(addSimpleRegionBtn);
            } else if (entityTreeHasCollections) {
                buttonsBox.add(addTabulatedRegionBtn);
            } else {
                buttonsBox.add(addRegionDisabledBtn);
            }
        }
    }

    @Override
    public List<String> validateFragment() {
        List<String> validationMessages = super.validateFragment();
        if (reportRegionsDc.getItems().isEmpty()) {
            validationMessages.add(messageBundle.getMessage("addRegionsWarn"));
        }
        return validationMessages;
    }
}