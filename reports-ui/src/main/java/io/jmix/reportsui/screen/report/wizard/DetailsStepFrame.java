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

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.entity.wizard.ReportData.ReportType;
import io.jmix.reports.entity.wizard.TemplateFileType;
import io.jmix.reportsui.screen.report.wizard.step.StepFrame;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasValue;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Consumer;


public class DetailsStepFrame extends StepFrame {
    //todo
//    protected ConditionsTree conditionsTree;
//    protected Filter filter;
//    protected FilterEntity filterEntity;

    public DetailsStepFrame(ReportWizardCreator wizard) {
        super(wizard, wizard.getMessage("reportDetails"), "detailsStep");

        isFirst = true;
        initFrameHandler = new InitDetailsStepFrameHandler();
        beforeShowFrameHandler = new BeforeShowDetailsStepFrameHandler();
    }

    protected class InitDetailsStepFrameHandler implements InitStepFrameHandler {
        @Override
        public void initFrame() {
            initReportTypeOptionGroup();
            initTemplateFormatLookupField();
            initEntityLookupField();

            wizard.entity.addValueChangeListener(new ChangeReportNameListener());

            wizard.setQueryButton.setAction(new SetQueryAction());
        }

        protected void initEntityLookupField() {
            wizard.entity.setOptionsMap(getAvailableEntities());
            wizard.entity.addValueChangeListener(new ClearRegionListener(
                    new DialogActionWithChangedValue(DialogAction.Type.YES) {
                        @Override
                        public void actionPerform(Component component) {
                            wizard.getItem().getReportRegions().clear();
//                            wizard.regionsTable.refresh(); //for web6
                            wizard.needUpdateEntityModel = true;
                            wizard.entity.setValue((MetaClass) newValue);

                            clearQueryAndFilter();
                        }
                    }));
        }

        protected void initTemplateFormatLookupField() {
            wizard.templateFileFormat.setOptionsMap(getAvailableTemplateFormats());
            wizard.templateFileFormat.setTextInputAllowed(false);
            wizard.templateFileFormat.setValue(TemplateFileType.DOCX);
        }

        protected void initReportTypeOptionGroup() {
            wizard.reportTypeRadioButtonGroup.setOptionsMap(getListedReportOptionsMap());
            wizard.reportTypeRadioButtonGroup.setValue(ReportType.SINGLE_ENTITY);
            wizard.reportTypeRadioButtonGroup.addValueChangeListener(new ClearRegionListener(
                    new DialogActionWithChangedValue(DialogAction.Type.YES) {
                        @Override
                        public void actionPerform(Component component) {
                            wizard.getItem().getReportRegions().clear();
//                            wizard.regionsTable.refresh(); //for web6
                            wizard.reportTypeRadioButtonGroup.setValue(newValue);
                        }
                    }));
        }

        protected Map<String, Object> getListedReportOptionsMap() {
            Map<String, Object> result = new LinkedHashMap<>(3);
            result.put(wizard.getMessage("singleEntityReport"), ReportType.SINGLE_ENTITY);
            result.put(wizard.getMessage("listOfEntitiesReport"), ReportType.LIST_OF_ENTITIES);
            result.put(wizard.getMessage("listOfEntitiesReportWithQuery"), ReportType.LIST_OF_ENTITIES_WITH_QUERY);
            return result;
        }

        protected Map<String, TemplateFileType> getAvailableTemplateFormats() {
            Messages messages = wizard.messages;
            Map<String, TemplateFileType> result = new LinkedHashMap<>(4);
            result.put(messages.getMessage(TemplateFileType.XLSX), TemplateFileType.XLSX);
            result.put(messages.getMessage(TemplateFileType.DOCX), TemplateFileType.DOCX);
            result.put(messages.getMessage(TemplateFileType.HTML), TemplateFileType.HTML);
            result.put(messages.getMessage(TemplateFileType.CSV), TemplateFileType.CSV);
            result.put(messages.getMessage(TemplateFileType.TABLE), TemplateFileType.TABLE);
//            WindowConfig windowConfig = AppBeans.get(WindowConfig.NAME);
//            if (windowConfig.hasWindow(ShowChartController.JSON_CHART_SCREEN_ID)) {
//                result.put(messages.getMessage(TemplateFileType.CHART), TemplateFileType.CHART);
//            }
            return result;
        }

        protected Map<String, MetaClass> getAvailableEntities() {
            Map<String, MetaClass> result = new TreeMap<>(String::compareTo);
            Collection<MetaClass> classes = wizard.metadataTools.getAllPersistentMetaClasses();
            for (MetaClass metaClass : classes) {
                MetaClass effectiveMetaClass = wizard.extendedEntities.getEffectiveMetaClass(metaClass);
                if (!wizard.reportWizardService.isEntityAllowedForReportWizard(effectiveMetaClass)) {
                    continue;
                }
                result.put(wizard.messageTools.getEntityCaption(effectiveMetaClass) + " (" + effectiveMetaClass.getName() + ")", effectiveMetaClass);
            }
            return result;
        }
    }

    @Override
    public List<String> validateFrame() {
        ArrayList<String> errors = new ArrayList<>(super.validateFrame());
        if (wizard.reportTypeRadioButtonGroup.getValue() == ReportType.LIST_OF_ENTITIES_WITH_QUERY && wizard.query == null) {
            errors.add(wizard.getMessage("fillReportQuery"));
        }

        return errors;
    }

    protected class ChangeReportNameListener implements Consumer<HasValue.ValueChangeEvent<MetaClass>> {

        public ChangeReportNameListener() {
        }

        @Override
        public void accept(HasValue.ValueChangeEvent e) {
            setGeneratedReportName((MetaClass) e.getPrevValue(), (MetaClass) e.getValue());
            wizard.outputFileName.setValue("");
        }

        protected void setGeneratedReportName(MetaClass prevValue, MetaClass value) {
            String oldReportName = wizard.reportName.getValue();
            MessageTools messageTools = wizard.messageTools;
            if (StringUtils.isBlank(oldReportName)) {
                String newText = wizard.formatMessage("reportNamePattern", messageTools.getEntityCaption(value));
                wizard.reportName.setValue(newText);
            } else {
                if (prevValue != null) {
                    //if old text contains MetaClass name substring, just replace it
                    String prevEntityCaption = messageTools.getEntityCaption(prevValue);
                    if (StringUtils.contains(oldReportName, prevEntityCaption)) {

                        String newText = oldReportName;
                        int index = oldReportName.lastIndexOf(prevEntityCaption);
                        if (index > -1) {
                            newText = StringUtils.substring(oldReportName, 0, index)
                                    + messageTools.getEntityCaption(value)
                                    + StringUtils.substring(oldReportName, index + prevEntityCaption.length(), oldReportName.length());
                        }

                        wizard.reportName.setValue(newText);
                        if (!oldReportName.equals(wizard.formatMessage("reportNamePattern", prevEntityCaption))) {
                            //if user changed auto generated report name and we have changed it, we show message to him
                            wizard.notifications.create(Notifications.NotificationType.TRAY)
                                    .withCaption(wizard.getMessage("reportNameChanged"))
                                    .show();
                        }
                    }
                }
            }
        }
    }

    protected class SetQueryAction extends AbstractAction {
        public SetQueryAction() {
            super("setQuery");
        }

        @Override
        public boolean isVisible() {
            return ReportType.LIST_OF_ENTITIES_WITH_QUERY == wizard.reportTypeRadioButtonGroup.getValue();
        }

        @Override
        public void actionPerform(Component component) {
            MetaClass entityMetaClass = wizard.entity.getValue();
            if (entityMetaClass == null) {
                wizard.notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(wizard.getMessage("fillEntityMsg"))
                        .show();
                return;
            }
//TODO Fake filter
//            FakeFilterSupport fakeFilterSupport = new FakeFilterSupport(wizard, entityMetaClass);
//            if (filter == null) {
//                filter = fakeFilterSupport.createFakeFilter();
//                filterEntity = fakeFilterSupport.createFakeFilterEntity(null);
//                conditionsTree = fakeFilterSupport.createFakeConditionsTree(filter, filterEntity);
//            }

//            List<Op> hideOperations = Collections.singletonList(Op.DATE_INTERVAL);

            Map<String, Object> params = new HashMap<>();
//            params.put("filterEntity", filterEntity);
//            params.put("filter", filter);
//            params.put("conditionsTree", conditionsTree);
            params.put("useShortConditionForm", true);
            params.put("showConditionHiddenOption", true);
//            params.put("hideOperations", hideOperations);

            //FilterEditor filterEditor = (FilterEditor) wizard.openWindow("filterEditor", OpenType.DIALOG, params);

        }
    }

    protected class DialogActionWithChangedValue extends DialogAction {
        protected Object newValue;

        public DialogActionWithChangedValue(Type type) {
            super(type);
        }

        public DialogActionWithChangedValue setValue(Object value) {
            this.newValue = value;
            return this;
        }
    }

    protected class ClearRegionListener implements Consumer<HasValue.ValueChangeEvent<MetaClass>> {
        protected DialogActionWithChangedValue okAction;

        public ClearRegionListener(DialogActionWithChangedValue okAction) {
            this.okAction = okAction;
        }

        @Override
        public void accept(HasValue.ValueChangeEvent e) {
            if (!wizard.getItem().getReportRegions().isEmpty()) {
                wizard.dialogs.createOptionDialog()
                        .withCaption(wizard.getMessage("dialogs.Confirmation"))
                        .withMessage(wizard.getMessage("regionsClearConfirm"))
                        .withActions(
                                okAction.setValue(e.getValue()),
                                new DialogAction(DialogAction.Type.NO, Action.Status.PRIMARY)
                        )
                        .show();
            } else {
                wizard.needUpdateEntityModel = true;
                clearQueryAndFilter();
            }

            if (wizard.setQueryButton != null) {
                wizard.setQueryButton.setVisible(
                        wizard.reportTypeRadioButtonGroup.getValue() == ReportType.LIST_OF_ENTITIES_WITH_QUERY);
            }
        }
    }

    protected void clearQueryAndFilter() {
        wizard.query = null;
        wizard.queryParameters = null;
        //filter = null;
//        filterEntity = null;
        //conditionsTree = null;
        wizard.setQueryButton.setCaption(wizard.getMessage("setQuery"));
    }

    protected class BeforeShowDetailsStepFrameHandler implements BeforeShowStepFrameHandler {
        @Override
        public void beforeShowFrame() {
            //TODO dialog options
//            wizard.getDialogOptions()
//                    .setHeight(wizard.wizardHeight).setHeightUnit(SizeUnit.PIXELS)
//                    .center();
        }
    }
}