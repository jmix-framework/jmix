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

import io.jmix.core.MessageTools;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.wizard.*;
import io.jmix.reportsui.screen.ReportGuiManager;
import io.jmix.reportsui.screen.report.wizard.ReportWizardCreator;
import io.jmix.reportsui.screen.report.wizard.region.EntityTreeLookup;
import io.jmix.reportsui.screen.report.wizard.region.RegionEditor;
import io.jmix.ui.*;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UiController("report_Region.fragment")
@UiDescriptor("regions-step-fragment.xml")
public class RegionsStepFragment extends StepFragment {

    @Autowired
    protected PopupButton addRegionPopupBtn;

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
    protected MessageTools messageTools;

    @Autowired
    protected ReportGuiManager reportGuiManager;

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

    protected ReportTypeGenerate getReportTypeGenerate() {
        return reportDataDc.getItem().getReportTypeGenerate();
    }

    @Subscribe
    public void onInit(InitEvent event) {
        initMoveAction();
        initRegionsTable();
    }

    public void initRegionsTable() {
        regionsTable.addGeneratedColumn("regionsGeneratedColumn", new ReportRegionTableColumnGenerator());
    }

    public void initMoveAction() {
//        moveDownBtn.getAction().setDirection(ItemOrderableAction.Direction.UP);
//        down.setDirection(ItemOrderableAction.Direction.DOWN);
    }

    @Override
    public String getCaption() {
        return messages.getMessage(getClass(), "reportRegions");
    }

    @Override
    public String getDescription() {
        return messages.getMessage(getClass(), "addPropertiesAndTableAreas");
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
        lookupParams.put("persistentOnly", ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY == getReportTypeGenerate());

        EntityTreeLookup entityTreeLookup = (EntityTreeLookup) screenBuilders.lookup(EntityTreeNode.class, getFragment().getFrameOwner())
                .withScreenId("report_ReportEntityTree.lookup")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(lookupParams))
                .withSelectHandler(items -> {
                    if (items.size() == 1) {
                        EntityTreeNode regionPropertiesRootNode = IterableUtils.get(items, 0);

                        Map<String, Object> editorParams = new HashMap<>();
                        editorParams.put("scalarOnly", Boolean.TRUE);
                        editorParams.put("persistentOnly", ReportTypeGenerate.LIST_OF_ENTITIES_WITH_QUERY == getReportTypeGenerate());
                        editorParams.put("rootEntity", regionPropertiesRootNode);
                        item.setRegionPropertiesRootNode(regionPropertiesRootNode);

                        RegionEditor regionEditor = screenBuilders.editor(ReportRegion.class, getFragment().getFrameOwner())
                                .withScreenClass(RegionEditor.class)
                                .editEntity(item)
                                .withOpenMode(OpenMode.DIALOG)
                                .withContainer(reportRegionsDc)
                                .withOptions(new MapScreenOptions(editorParams))
                                .build();

                        regionEditor.show();
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

        RegionEditor regionEditor = screenBuilders.editor(ReportRegion.class, getFragment().getFrameOwner())
                .withScreenClass(RegionEditor.class)
                .editEntity(item)
                .withOpenMode(OpenMode.DIALOG)
                .withContainer(reportRegionsDc)
                .withOptions(new MapScreenOptions(editorParams))
                .build();

//            regionEditor.setTabulated(item.getIsTabulatedRegion());

        regionEditor.show();
    }

    @Subscribe("runBtn")
    public void onRunBtnClick(Button.ClickEvent event) {
        if (reportDataDc.getItem().getReportRegions().isEmpty()) {
            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage(getClass(),"addRegionsWarn"))
                    .show();
            return;
        }
        ReportWizardCreator reportWizardCreator = (ReportWizardCreator) getFragment().getFrameOwner().getHostController();
        lastGeneratedTmpReport = reportWizardCreator.buildReport(true);

        if (lastGeneratedTmpReport != null) {
            reportGuiManager.runReport(lastGeneratedTmpReport, getFragment().getFrameOwner());
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

    protected class ReportRegionTableColumnGenerator implements Table.ColumnGenerator<ReportRegion> {
        protected static final String WIDTH_PERCENT_100 = "100%";
        protected static final int MAX_ATTRS_BTN_CAPTION_WIDTH = 95;
        protected static final String BOLD_LABEL_STYLE = "semi-bold-label";

        private ReportRegion currentReportRegionGeneratedColumn;

        @Override
        public Component generateCell(ReportRegion entity) {
            currentReportRegionGeneratedColumn = entity;
            BoxLayout mainLayout = uiComponents.create(VBoxLayout.class);
            mainLayout.setWidth(WIDTH_PERCENT_100);
            mainLayout.add(createFirstTwoRowsLayout());
            mainLayout.add(createThirdRowAttrsLayout());
            return mainLayout;
        }

        private BoxLayout createFirstTwoRowsLayout() {
            BoxLayout firstTwoRowsLayout = uiComponents.create(HBoxLayout.class);
            BoxLayout expandedAttrsLayout = createExpandedAttrsLayout();
            firstTwoRowsLayout.setWidth(WIDTH_PERCENT_100);
            firstTwoRowsLayout.add(expandedAttrsLayout);
            firstTwoRowsLayout.add(createBtnsLayout());
            firstTwoRowsLayout.expand(expandedAttrsLayout);
            return firstTwoRowsLayout;
        }

        private BoxLayout createExpandedAttrsLayout() {
            BoxLayout expandedAttrsLayout = uiComponents.create(HBoxLayout.class);
            expandedAttrsLayout.setWidth(WIDTH_PERCENT_100);
            expandedAttrsLayout.add(createFirstRowAttrsLayout());
            expandedAttrsLayout.add(createSecondRowAttrsLayout());
            return expandedAttrsLayout;
        }

        private BoxLayout createFirstRowAttrsLayout() {
            BoxLayout firstRowAttrsLayout = uiComponents.create(HBoxLayout.class);
            firstRowAttrsLayout.setSpacing(true);
            Label regionLbl = uiComponents.create(Label.class);
            regionLbl.setStyleName(BOLD_LABEL_STYLE);
            regionLbl.setValue(messages.getMessage("region"));
            Label regionValueLbl = uiComponents.create(Label.class);
            regionValueLbl.setValue(currentReportRegionGeneratedColumn.getName());
            regionValueLbl.setWidth(WIDTH_PERCENT_100);
            firstRowAttrsLayout.add(regionLbl);
            firstRowAttrsLayout.add(regionValueLbl);
            return firstRowAttrsLayout;
        }

        private BoxLayout createSecondRowAttrsLayout() {
            BoxLayout secondRowAttrsLayout = uiComponents.create(HBoxLayout.class);
            secondRowAttrsLayout.setSpacing(true);
            Label entityLbl = uiComponents.create(Label.class);
            entityLbl.setStyleName(BOLD_LABEL_STYLE);
            entityLbl.setValue(messages.getMessage("entity"));
            Label entityValueLbl = uiComponents.create(Label.class);

            entityValueLbl.setValue(currentReportRegionGeneratedColumn.getNameForBand());
            entityValueLbl.setWidth(WIDTH_PERCENT_100);
            secondRowAttrsLayout.add(entityLbl);
            secondRowAttrsLayout.add(entityValueLbl);
            return secondRowAttrsLayout;
        }

        private BoxLayout createBtnsLayout() {
            BoxLayout btnsLayout = uiComponents.create(HBoxLayout.class);
            btnsLayout.setSpacing(true);
            btnsLayout.setStyleName("on-hover-visible-layout");
            return btnsLayout;
        }

        private BoxLayout createThirdRowAttrsLayout() {
            BoxLayout thirdRowAttrsLayout = uiComponents.create(HBoxLayout.class);
            thirdRowAttrsLayout.setSpacing(true);
            Label entityLbl = uiComponents.create(Label.class);
            entityLbl.setStyleName(BOLD_LABEL_STYLE);
            entityLbl.setValue(messages.getMessage("attributes"));
            Button editBtn = uiComponents.create(Button.class);
            editBtn.setCaption(generateAttrsBtnCaption());
            editBtn.setStyleName("link");
            editBtn.setWidth(WIDTH_PERCENT_100);
            thirdRowAttrsLayout.add(entityLbl);
            thirdRowAttrsLayout.add(editBtn);
            return thirdRowAttrsLayout;
        }

        private String generateAttrsBtnCaption() {
            return StringUtils.abbreviate(StringUtils.join(
                    CollectionUtils.collect(currentReportRegionGeneratedColumn.getRegionProperties(),
                            RegionProperty::getHierarchicalLocalizedNameExceptRoot), ", "
                    ), MAX_ATTRS_BTN_CAPTION_WIDTH
            );
        }
    }

    @Override
    public List<String> validateFragment() {
        List<String> validationMessages = super.validateFragment();
        if (reportRegionsDc.getItems().isEmpty()) {
            validationMessages.add(messages.getMessage(getClass(), "addRegionsWarn"));
        }
        return validationMessages;
    }
}