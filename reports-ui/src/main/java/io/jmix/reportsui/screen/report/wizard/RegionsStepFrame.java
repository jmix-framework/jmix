/*
 * Copyright (c) 2008-2019 Haulmont.
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

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.wizard.EntityTreeNode;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData.ReportType;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reportsui.action.list.OrderableItemMoveAction;
import io.jmix.reportsui.action.list.OrderableItemMoveAction.Direction;
import io.jmix.reportsui.screen.report.wizard.region.EntityTreeLookup;
import io.jmix.reportsui.screen.report.wizard.region.RegionEditor;
import io.jmix.reportsui.screen.report.wizard.step.StepFrame;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.MapScreenOptions;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.StandardCloseAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class RegionsStepFrame extends StepFrame {
    protected static final String ADD_TABULATED_REGION_ACTION_ID = "tabulatedRegion";
    protected static final String ADD_SIMPLE_REGION_ACTION_ID = "simpleRegion";

    protected AddSimpleRegionAction addSimpleRegionAction;
    protected AddTabulatedRegionAction addTabulatedRegionAction;
    protected EditRegionAction editRegionAction;
    protected RemoveRegionAction removeRegionAction;

    public RegionsStepFrame(ReportWizardCreator wizard) {
        super(wizard, wizard.getMessage("reportRegions"), "regionsStep");
        initFrameHandler = new InitRegionsStepFrameHandler();

        beforeShowFrameHandler = new BeforeShowRegionsStepFrameHandler();

        beforeHideFrameHandler = new BeforeHideRegionsStepFrameHandler();
    }

    protected abstract class AddRegionAction extends AbstractAction {

        protected AddRegionAction(String id) {
            super(id);
        }

        protected ReportRegion createReportRegion(boolean tabulated) {
            ReportRegion reportRegion = wizard.metadata.create(ReportRegion.class);
            reportRegion.setReportData(wizard.getItem());
            reportRegion.setIsTabulatedRegion(tabulated);
            reportRegion.setOrderNum((long) wizard.getItem().getReportRegions().size() + 1L);
            return reportRegion;
        }

        protected void openTabulatedRegionEditor(final ReportRegion item) {
            if (ReportType.SINGLE_ENTITY == wizard.reportTypeRadioButtonGroup.getValue()) {
                openRegionEditorOnlyWithNestedCollections(item);

            } else {
                openRegionEditor(item);
            }
        }

        private void openRegionEditorOnlyWithNestedCollections(final ReportRegion item) {//show lookup for choosing parent collection for tabulated region
            final Map<String, Object> lookupParams = new HashMap<>();
            lookupParams.put("rootEntity", wizard.getItem().getEntityTreeRootNode());
            lookupParams.put("collectionsOnly", Boolean.TRUE);
            lookupParams.put("persistentOnly", ReportType.LIST_OF_ENTITIES_WITH_QUERY == wizard.reportTypeRadioButtonGroup.getValue());

            EntityTreeLookup entityTreeLookup = (EntityTreeLookup) wizard.screenBuilders.lookup(EntityTreeNode.class, wizard)
                    .withScreenId("report_ReportEntityTree.lookup")
                    .withOpenMode(OpenMode.DIALOG)
                    .withOptions(new MapScreenOptions(lookupParams))
                    .withSelectHandler(items -> {
                        if (items.size() == 1) {
                            EntityTreeNode regionPropertiesRootNode = IterableUtils.get(items, 0);

                            Map<String, Object> editorParams = new HashMap<>();
                            editorParams.put("scalarOnly", Boolean.TRUE);
                            editorParams.put("persistentOnly", ReportType.LIST_OF_ENTITIES_WITH_QUERY == wizard.reportTypeRadioButtonGroup.getValue());
                            editorParams.put("rootEntity", regionPropertiesRootNode);
                            item.setRegionPropertiesRootNode(regionPropertiesRootNode);

                            RegionEditor regionEditor = wizard.screenBuilders.editor(ReportRegion.class, wizard)
                                    .withScreenClass(RegionEditor.class)
                                    .editEntity(item)
                                    .withOpenMode(OpenMode.DIALOG)
                                    .withContainer(wizard.reportRegionsDc)
                                    .withOptions(new MapScreenOptions(editorParams))
                                    .build();

                            regionEditor.addAfterCloseListener(new RegionEditorCloseListener());
                            regionEditor.show();
                        }
                    })
                    .build();

            entityTreeLookup.show();
        }

        protected void openRegionEditor(ReportRegion item) {
            item.setRegionPropertiesRootNode(wizard.getItem().getEntityTreeRootNode());

            Map<String, Object> editorParams = new HashMap<>();
            editorParams.put("rootEntity", wizard.getItem().getEntityTreeRootNode());
            editorParams.put("scalarOnly", Boolean.TRUE);
            editorParams.put("persistentOnly", ReportType.LIST_OF_ENTITIES_WITH_QUERY == wizard.reportTypeRadioButtonGroup.getValue());

            RegionEditor regionEditor = wizard.screenBuilders.editor(ReportRegion.class, wizard)
                    .withScreenClass(RegionEditor.class)
                    .editEntity(item)
                    .withOpenMode(OpenMode.DIALOG)
                    .withContainer(wizard.reportRegionsDc)
                    .withOptions(new MapScreenOptions(editorParams))
                    .build();

            regionEditor.addAfterCloseListener(new RegionEditorCloseListener());
            regionEditor.show();
        }

        protected class RegionEditorCloseListener implements Consumer<Screen.AfterCloseEvent> {
            @Override
            public void accept(Screen.AfterCloseEvent afterCloseEvent) {
                StandardCloseAction standardCloseAction = (StandardCloseAction) afterCloseEvent.getCloseAction();
                if (Window.COMMIT_ACTION_ID.equals(standardCloseAction.getActionId())) {
//                    wizard.regionsTable.refresh();
                    wizard.setupButtonsVisibility();
                }
            }
        }
    }

    protected class AddSimpleRegionAction extends AddRegionAction {
        public AddSimpleRegionAction() {
            super(ADD_SIMPLE_REGION_ACTION_ID);
        }

        @Override
        public void actionPerform(Component component) {
            openRegionEditor(createReportRegion(false));
        }
    }

    protected class AddTabulatedRegionAction extends AddRegionAction {
        public AddTabulatedRegionAction() {
            super(ADD_TABULATED_REGION_ACTION_ID);
        }

        @Override
        public void actionPerform(Component component) {
            openTabulatedRegionEditor(createReportRegion(true));
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
            BoxLayout mainLayout = wizard.uiComponents.create(VBoxLayout.class);
            mainLayout.setWidth(WIDTH_PERCENT_100);
            mainLayout.add(createFirstTwoRowsLayout());
            mainLayout.add(createThirdRowAttrsLayout());
            return mainLayout;
        }

        private BoxLayout createFirstTwoRowsLayout() {
            BoxLayout firstTwoRowsLayout = wizard.uiComponents.create(HBoxLayout.class);
            BoxLayout expandedAttrsLayout = createExpandedAttrsLayout();
            firstTwoRowsLayout.setWidth(WIDTH_PERCENT_100);
            firstTwoRowsLayout.add(expandedAttrsLayout);
            firstTwoRowsLayout.add(createBtnsLayout());
            firstTwoRowsLayout.expand(expandedAttrsLayout);
            return firstTwoRowsLayout;
        }

        private BoxLayout createExpandedAttrsLayout() {
            BoxLayout expandedAttrsLayout = wizard.uiComponents.create(HBoxLayout.class);
            expandedAttrsLayout.setWidth(WIDTH_PERCENT_100);
            expandedAttrsLayout.add(createFirstRowAttrsLayout());
            expandedAttrsLayout.add(createSecondRowAttrsLayout());
            return expandedAttrsLayout;
        }

        private BoxLayout createFirstRowAttrsLayout() {
            BoxLayout firstRowAttrsLayout = wizard.uiComponents.create(HBoxLayout.class);
            firstRowAttrsLayout.setSpacing(true);
            Label regionLbl = wizard.uiComponents.create(Label.class);
            regionLbl.setStyleName(BOLD_LABEL_STYLE);
            regionLbl.setValue(wizard.getMessage("region"));
            Label regionValueLbl = wizard.uiComponents.create(Label.class);
            regionValueLbl.setValue(currentReportRegionGeneratedColumn.getName());
            regionValueLbl.setWidth(WIDTH_PERCENT_100);
            firstRowAttrsLayout.add(regionLbl);
            firstRowAttrsLayout.add(regionValueLbl);
            return firstRowAttrsLayout;
        }

        private BoxLayout createSecondRowAttrsLayout() {
            BoxLayout secondRowAttrsLayout = wizard.uiComponents.create(HBoxLayout.class);
            secondRowAttrsLayout.setSpacing(true);
            Label entityLbl = wizard.uiComponents.create(Label.class);
            entityLbl.setStyleName(BOLD_LABEL_STYLE);
            entityLbl.setValue(wizard.getMessage("entity"));
            Label entityValueLbl = wizard.uiComponents.create(Label.class);
            MetaClass wrapperMetaClass = currentReportRegionGeneratedColumn.getRegionPropertiesRootNode().getWrappedMetaClass();

            entityValueLbl.setValue(wizard.messageTools.getEntityCaption(wrapperMetaClass));
            entityValueLbl.setWidth(WIDTH_PERCENT_100);
            secondRowAttrsLayout.add(entityLbl);
            secondRowAttrsLayout.add(entityValueLbl);
            return secondRowAttrsLayout;
        }

        private BoxLayout createBtnsLayout() {
            BoxLayout btnsLayout = wizard.uiComponents.create(HBoxLayout.class);
            btnsLayout.setSpacing(true);
            btnsLayout.setStyleName("on-hover-visible-layout");
            return btnsLayout;
        }

        private BoxLayout createThirdRowAttrsLayout() {
            BoxLayout thirdRowAttrsLayout = wizard.uiComponents.create(HBoxLayout.class);
            thirdRowAttrsLayout.setSpacing(true);
            Label entityLbl = wizard.uiComponents.create(Label.class);
            entityLbl.setStyleName(BOLD_LABEL_STYLE);
            entityLbl.setValue(wizard.getMessage("attributes"));
            Button editBtn = wizard.uiComponents.create(Button.class);
            editBtn.setCaption(generateAttrsBtnCaption());
            editBtn.setStyleName("link");
            editBtn.setWidth(WIDTH_PERCENT_100);
            editBtn.setAction(editRegionAction);
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

    protected class RemoveRegionAction extends AbstractAction {
        public RemoveRegionAction() {
            super("removeRegion");
        }

        @Override
        public void actionPerform(Component component) {
            if (wizard.regionsTable.getSingleSelected() != null) {
                wizard.dialogs.createOptionDialog()
                        .withCaption(wizard.getMessage("dialogs.Confirmation"))
                        .withMessage(wizard.formatMessage("deleteRegion", wizard.regionsTable.getSingleSelected().getName()))
                        .withActions(
                                new DialogAction(DialogAction.Type.YES).withHandler(e -> {
                                    wizard.reportRegionsDc.getMutableItems().remove(wizard.regionsTable.getSingleSelected());
                                    normalizeRegionPropertiesOrderNum();
                                    wizard.setupButtonsVisibility();
                                }),
                                new DialogAction(DialogAction.Type.NO).withPrimary(true)
                        ).show();
            }
        }

        @Override
        public String getCaption() {
            return "";
        }

        protected void normalizeRegionPropertiesOrderNum() {
            long normalizedIdx = 0;
            List<ReportRegion> allItems = new ArrayList<>(wizard.reportRegionsDc.getItems());
            for (ReportRegion item : allItems) {
                item.setOrderNum(++normalizedIdx); //first must to be 1
            }
        }
    }

    protected class EditRegionAction extends AddRegionAction {
        public EditRegionAction() {
            super("removeRegion");
        }

        @Override
        public void actionPerform(Component component) {
            if (wizard.regionsTable.getSingleSelected() != null) {
                Map<String, Object> editorParams = new HashMap<>();
                editorParams.put("rootEntity", wizard.regionsTable.getSingleSelected().getRegionPropertiesRootNode());
                editorParams.put("scalarOnly", Boolean.TRUE);
                editorParams.put("persistentOnly", ReportType.LIST_OF_ENTITIES_WITH_QUERY == wizard.reportTypeRadioButtonGroup.getValue());

                RegionEditor regionEditor = wizard.screenBuilders.editor(ReportRegion.class, wizard)
                        .withScreenClass(RegionEditor.class)
                        .editEntity(wizard.regionsTable.getSingleSelected())
                        .withContainer(wizard.reportRegionsDc)
                        .withOpenMode(OpenMode.DIALOG)
                        .withOptions(new MapScreenOptions(editorParams))
                        .build();
                regionEditor.addAfterCloseListener(new RegionEditorCloseListener());
                regionEditor.show();
            }
        }

        @Override
        public String getCaption() {
            return "";
        }
    }

    protected class InitRegionsStepFrameHandler implements InitStepFrameHandler {
        @Override
        public void initFrame() {
            addSimpleRegionAction = new AddSimpleRegionAction();
            addTabulatedRegionAction = new AddTabulatedRegionAction();
            wizard.addSimpleRegionBtn.setAction(addSimpleRegionAction);
            wizard.addTabulatedRegionBtn.setAction(addTabulatedRegionAction);
            wizard.addRegionPopupBtn.addAction(addSimpleRegionAction);
            wizard.addRegionPopupBtn.addAction(addTabulatedRegionAction);
            wizard.regionsTable.addGeneratedColumn("regionsGeneratedColumn", new ReportRegionTableColumnGenerator());
            editRegionAction = new EditRegionAction();
            removeRegionAction = new RemoveRegionAction();

            wizard.moveDownBtn.setAction(new OrderableItemMoveAction<>("downItem", Direction.DOWN, wizard.regionsTable));
            wizard.moveUpBtn.setAction(new OrderableItemMoveAction<>("upItem", Direction.UP, wizard.regionsTable));
            wizard.removeBtn.setAction(removeRegionAction);
        }
    }

    protected class BeforeShowRegionsStepFrameHandler implements BeforeShowStepFrameHandler {
        @Override
        public void beforeShowFrame() {
            wizard.setupButtonsVisibility();
            wizard.runBtn.setAction(new AbstractAction("runReport") {
                @Override
                public void actionPerform(Component component) {
                    if (wizard.getItem().getReportRegions().isEmpty()) {
                        wizard.notifications.create(Notifications.NotificationType.TRAY)
                                .withCaption(wizard.getMessage("addRegionsWarn"))
                                .show();
                        return;
                    }
                    wizard.lastGeneratedTmpReport = wizard.buildReport(true);

                    if (wizard.lastGeneratedTmpReport != null) {
                        wizard.reportGuiManager.runReport(
                                wizard.lastGeneratedTmpReport,
                                wizard);
                    }
                }
            });

            showAddRegion();
            wizard.setCorrectReportOutputType();
            //TODO dialog options
//            wizard.getDialogOptions()
//                    .setHeight(wizard.wizardHeight).setHeightUnit(SizeUnit.PIXELS)
//                    .center();
        }

        private void showAddRegion() {
            if (wizard.reportRegionsDc.getItems().isEmpty()) {
                if (((ReportType) wizard.reportTypeRadioButtonGroup.getValue()).isList()) {
                    if (wizard.entityTreeHasSimpleAttrs) {
                        addTabulatedRegionAction.actionPerform(wizard.regionsStepFrame.getFrame());
                    }
                } else {
                    if (wizard.entityTreeHasSimpleAttrs && wizard.entityTreeHasCollections) {
                        addSimpleRegionAction.actionPerform(wizard.regionsStepFrame.getFrame());
                    } else if (wizard.entityTreeHasSimpleAttrs) {
                        addSimpleRegionAction.actionPerform(wizard.regionsStepFrame.getFrame());
                    } else if (wizard.entityTreeHasCollections) {
                        addTabulatedRegionAction.actionPerform(wizard.regionsStepFrame.getFrame());
                    }
                }
            }
        }
    }

    protected class BeforeHideRegionsStepFrameHandler implements BeforeHideStepFrameHandler {
        @Override
        public void beforeHideFrame() {
        }
    }
}