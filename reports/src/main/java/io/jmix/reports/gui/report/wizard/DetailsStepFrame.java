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

package io.jmix.reports.gui.report.wizard;

import io.jmix.core.MessageTools;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Dom4j;
import io.jmix.core.metamodel.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.filter.ConditionsTree;
import com.haulmont.cuba.gui.components.filter.FilterParser;
import com.haulmont.cuba.gui.components.filter.Param;
import com.haulmont.cuba.gui.components.filter.condition.AbstractCondition;
import com.haulmont.cuba.gui.components.filter.edit.FilterEditor;
import com.haulmont.cuba.security.entity.FilterEntity;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.PredefinedTransformation;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportData.ReportType;
import io.jmix.reports.entity.wizard.TemplateFileType;
import io.jmix.reports.gui.report.run.ParameterClassResolver;
import io.jmix.reports.gui.report.run.ShowChartController;
import io.jmix.reports.gui.report.wizard.step.StepFrame;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.action.AbstractAction;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.*;
import io.jmix.ui.filter.*;
import io.jmix.ui.gui.OpenType;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.persistence.TemporalType;
import java.util.*;
import java.util.function.Consumer;

public class DetailsStepFrame extends StepFrame {
    protected ConditionsTree conditionsTree;
    protected Filter filter;
    protected FilterEntity filterEntity;

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
                            wizard.regionsTable.refresh(); //for web6
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
            wizard.reportTypeOptionGroup.setOptionsMap(getListedReportOptionsMap());
            wizard.reportTypeOptionGroup.setValue(ReportType.SINGLE_ENTITY);
            wizard.reportTypeOptionGroup.addValueChangeListener(new ClearRegionListener(
                    new DialogActionWithChangedValue(DialogAction.Type.YES) {
                        @Override
                        public void actionPerform(Component component) {
                            wizard.getItem().getReportRegions().clear();
                            wizard.regionsTable.refresh(); //for web6
                            wizard.reportTypeOptionGroup.setValue(newValue);
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
            Messages messages = AppBeans.get(Messages.NAME);
            Map<String, TemplateFileType> result = new LinkedHashMap<>(4);
            result.put(messages.getMessage(TemplateFileType.XLSX), TemplateFileType.XLSX);
            result.put(messages.getMessage(TemplateFileType.DOCX), TemplateFileType.DOCX);
            result.put(messages.getMessage(TemplateFileType.HTML), TemplateFileType.HTML);
            result.put(messages.getMessage(TemplateFileType.CSV), TemplateFileType.CSV);
            result.put(messages.getMessage(TemplateFileType.TABLE), TemplateFileType.TABLE);
            WindowConfig windowConfig = AppBeans.get(WindowConfig.NAME);
            if (windowConfig.hasWindow(ShowChartController.JSON_CHART_SCREEN_ID)) {
                result.put(messages.getMessage(TemplateFileType.CHART), TemplateFileType.CHART);
            }
            return result;
        }

        protected Map<String, MetaClass> getAvailableEntities() {
            Map<String, MetaClass> result = new TreeMap<>(String::compareTo);
            Collection<MetaClass> classes = wizard.metadataTools.getAllPersistentMetaClasses();
            for (MetaClass metaClass : classes) {
                MetaClass effectiveMetaClass = wizard.metadata.getExtendedEntities().getEffectiveMetaClass(metaClass);
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
        if (wizard.reportTypeOptionGroup.getValue() == ReportType.LIST_OF_ENTITIES_WITH_QUERY && wizard.query == null) {
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
                            wizard.showNotification(wizard.getMessage("reportNameChanged"), Frame.NotificationType.TRAY);
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
            return ReportType.LIST_OF_ENTITIES_WITH_QUERY == wizard.reportTypeOptionGroup.getValue();
        }

        @Override
        public void actionPerform(Component component) {
            MetaClass entityMetaClass = wizard.entity.getValue();
            if (entityMetaClass == null) {
                wizard.showNotification(wizard.getMessage("fillEntityMsg"), Frame.NotificationType.TRAY_HTML);
                return;
            }
//TODO Fake filter
//            FakeFilterSupport fakeFilterSupport = new FakeFilterSupport(wizard, entityMetaClass);
//            if (filter == null) {
//                filter = fakeFilterSupport.createFakeFilter();
//                filterEntity = fakeFilterSupport.createFakeFilterEntity(null);
//                conditionsTree = fakeFilterSupport.createFakeConditionsTree(filter, filterEntity);
//            }

            List<Op> hideOperations = Collections.singletonList(Op.DATE_INTERVAL);

            Map<String, Object> params = new HashMap<>();
            params.put("filterEntity", filterEntity);
            params.put("filter", filter);
            params.put("conditionsTree", conditionsTree);
            params.put("useShortConditionForm", true);
            params.put("showConditionHiddenOption", true);
            params.put("hideOperations", hideOperations);

            FilterEditor filterEditor = (FilterEditor) wizard.openWindow("filterEditor", OpenType.DIALOG, params);
            filterEditor.addCloseListener(new Window.CloseListener() {
                private ParameterClassResolver parameterClassResolver = AppBeans.get(ParameterClassResolver.NAME);

                @Override
                public void windowClosed(String actionId) {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        filterEntity = filterEditor.getFilterEntity();
                        collectQueryAndParametersFromFilter();
                    }
                }

                protected void collectQueryAndParametersFromFilter() {
                    FilterParser filterParser = AppBeans.get(FilterParser.class);
                    filterEntity.setXml(filterParser.getXml(filterEditor.getConditions(), Param.ValueProperty.DEFAULT_VALUE));
                    if (filterEntity.getXml() != null) {
                        Element element = Dom4j.readDocument(filterEntity.getXml()).getRootElement();
                        //TODO new query filter
//                        QueryFilter queryFilter = new QueryFilter(element);
//                        conditionsTree = filterEditor.getConditionsTree();
//                        filter = filterEditor.getFilter();
//                        wizard.query = collectQuery(queryFilter);
//                        wizard.queryParameters = collectQueryParameters(queryFilter);
                    } else {
                        wizard.showNotification(wizard.getMessage("defaultQueryHasBeenSet"), Frame.NotificationType.HUMANIZED);
                        wizard.query = filter.getDatasource().getQuery();
                        wizard.queryParameters = Collections.emptyList();
                    }

                    wizard.setQueryButton.setCaption(wizard.getMessage("changeQuery"));
                }

                protected List<ReportData.Parameter> collectQueryParameters(QueryFilter queryFilter) {
                    List<ReportData.Parameter> newParametersList = new ArrayList<>();
                    int i = 1;
                    for (ParameterInfo parameterInfo : queryFilter.getCompiledParameters()) {
                        Condition condition = findConditionByParameter(queryFilter.getRoot(), parameterInfo);
                        String conditionName = parameterInfo.getConditionName();
                        if (conditionName == null) {
                            conditionName = "parameter";
                        }

                        Boolean hiddenConditionPropertyValue = findHiddenPropertyValueByConditionName(conditionName);
                        TemporalType temporalType = getTemporalType(conditionName);

                        conditionName = conditionName.replaceAll("\\.", "_");

                        String parameterName = conditionName + i;
                        i++;
                        Class parameterClass = parameterInfo.getJavaClass();
                        ParameterType parameterType = getParameterType(parameterInfo, temporalType, parameterClass);

                        String parameterValue = parameterInfo.getValue();
                        parameterValue = !"NULL".equals(parameterValue) ? parameterValue : null;

                        newParametersList.add(new ReportData.Parameter(
                                parameterName,
                                parameterClass,
                                parameterType,
                                parameterValue,
                                resolveParameterTransformation(condition),
                                hiddenConditionPropertyValue));

                        wizard.query = wizard.query.replace(":" + parameterInfo.getName(), "${" + parameterName + "}");
                    }
                    return newParametersList;
                }

                protected ParameterType getParameterType(ParameterInfo parameterInfo, TemporalType temporalType, Class parameterClass) {
                    ParameterType parameterType;

                    if (temporalType != null) {
                        switch (temporalType) {
                            case TIME:
                                parameterType = ParameterType.TIME;
                                break;
                            case DATE:
                                parameterType = ParameterType.DATE;
                                break;
                            case TIMESTAMP:
                                parameterType = ParameterType.DATETIME;
                                break;
                            default:
                                parameterType = parameterClassResolver.resolveParameterType(parameterClass);
                        }
                    } else {
                        parameterType = parameterClassResolver.resolveParameterType(parameterClass);
                    }

                    if (parameterType == null) {
                        parameterType = ParameterType.TEXT;
                    }

                    if (parameterType == ParameterType.ENTITY) {
                        boolean inExpr = conditionsTree.toConditionsList().stream()
                                .filter(cond -> Objects.nonNull(cond.getParamName()))
                                .filter(cond -> cond.getParamName().equals(parameterInfo.getName()))
                                .map(AbstractCondition::getInExpr)
                                .findFirst()
                                .orElse(Boolean.FALSE);
                        if (inExpr) {
                            parameterType = ParameterType.ENTITY_LIST;
                        }
                    }
                    return parameterType;
                }

                protected String collectQuery(QueryFilter queryFilter) {
                    Collection<ParameterInfo> parameterDescriptorsFromFilter = queryFilter.getCompiledParameters();
                    Map<String, Object> params = new HashMap<>();
                    for (ParameterInfo parameter : parameterDescriptorsFromFilter) {
                        params.put(parameter.getName(), "___");
                    }
                    return queryFilter.processQuery(filter.getDatasource().getQuery(), params);
                }

                protected Condition findConditionByParameter(Condition condition, ParameterInfo parameterInfo) {
                    if (!(condition instanceof LogicalCondition)) {
                        //TODO compiled parameters
//                        Set<ParameterInfo> parameters = condition.getCompiledParameters();
//                        if (parameters != null && parameters.contains(parameterInfo)) {
//                            return condition;
//                        }
                    }
                    //TODO find conditions
//                    if (condition.getConditions() != null) {
//                        for (Condition it : condition.getConditions()) {
//                            return findConditionByParameter(it, parameterInfo);
//                        }
//                    }
                    return null;
                }

                protected PredefinedTransformation resolveParameterTransformation(Condition condition) {
                    if (condition instanceof Clause) {
                        Clause clause = (Clause) condition;
                        if (clause.getOperator() != null) {
                            switch (clause.getOperator()) {
                                case STARTS_WITH:
                                    return PredefinedTransformation.STARTS_WITH;
                                case ENDS_WITH:
                                    return PredefinedTransformation.ENDS_WITH;
                                case CONTAINS:
                                    return PredefinedTransformation.CONTAINS;
                                case DOES_NOT_CONTAIN:
                                    return PredefinedTransformation.CONTAINS;
                            }
                        }
                    }
                    return null;
                }

                protected Boolean findHiddenPropertyValueByConditionName(String propertyName) {
                    return conditionsTree.toConditionsList().stream()
                            .filter(condition -> Objects.nonNull(condition.getName()))
                            .filter(condition -> condition.getName().equals(propertyName))
                            .map(AbstractCondition::getHidden)
                            .findFirst()
                            .orElse(Boolean.FALSE);
                }

                protected TemporalType getTemporalType(String propertyName) {
                    for (AbstractCondition condition : conditionsTree.toConditionsList()) {
                        if (condition.getName() != null && condition.getName().equals(propertyName)
                                && condition.getParam() != null && condition.getParam().getProperty() != null) {
                            Map annotations = condition.getParam().getProperty().getAnnotations();
                            return (TemporalType) annotations.get(MetadataTools.TEMPORAL_ANN_NAME);
                        }
                    }
                    return null;
                }
            });
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
                wizard.showOptionDialog(
                        wizard.getMessage("dialogs.Confirmation"),
                        wizard.getMessage("regionsClearConfirm"),
                        Frame.MessageType.CONFIRMATION,
                        new AbstractAction[]{
                                okAction.setValue(e.getValue()),

                                new DialogAction(DialogAction.Type.NO, Action.Status.PRIMARY)
                        });
            } else {
                wizard.needUpdateEntityModel = true;
                clearQueryAndFilter();
            }

            if (wizard.setQueryButton != null) {
                wizard.setQueryButton.setVisible(
                        wizard.reportTypeOptionGroup.getValue() == ReportType.LIST_OF_ENTITIES_WITH_QUERY);
            }
        }
    }

    protected void clearQueryAndFilter() {
        wizard.query = null;
        wizard.queryParameters = null;
        filter = null;
        filterEntity = null;
        conditionsTree = null;
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