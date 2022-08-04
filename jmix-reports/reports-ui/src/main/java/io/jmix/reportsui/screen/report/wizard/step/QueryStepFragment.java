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

import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import io.jmix.core.metamodel.datatype.FormatStringsRegistry;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.wizard.QueryParameter;
import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import io.jmix.reportsui.runner.FluentUiReportRunner;
import io.jmix.reportsui.runner.ParametersDialogShowMode;
import io.jmix.reportsui.runner.UiReportRunner;
import io.jmix.reportsui.screen.ReportsClientProperties;
import io.jmix.reportsui.screen.report.wizard.ReportWizardCreator;
import io.jmix.reportsui.screen.report.wizard.query.JpqlQueryBuilder;
import io.jmix.ui.Dialogs;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ContentMode;
import io.jmix.ui.component.HasContextHelp;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.component.autocomplete.AutoCompleteSupport;
import io.jmix.ui.component.autocomplete.JpqlUiSuggestionProvider;
import io.jmix.ui.component.autocomplete.Suggester;
import io.jmix.ui.component.autocomplete.Suggestion;
import io.jmix.ui.model.CollectionChangeType;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@UiController("report_QueryStep.fragment")
@UiDescriptor("query-step-fragment.xml")
public class QueryStepFragment extends StepFragment implements Suggester {

    @Autowired
    protected InstanceContainer<ReportData> reportDataDc;

    @Autowired
    protected CollectionPropertyContainer<QueryParameter> queryParametersDc;

    @Autowired
    protected SourceCodeEditor reportQueryCodeEditor;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected JpqlUiSuggestionProvider jpqlUiSuggestionProvider;

    protected Report lastGeneratedTmpReport;

    @Autowired
    protected ObjectToStringConverter objectToStringConverter;

    @Autowired
    protected UiReportRunner uiReportRunner;

    @Autowired
    protected ReportsClientProperties reportsClientProperties;

    @Autowired
    protected FormatStringsRegistry formatStringsRegistry;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    protected boolean regenerateQuery = false;

    @Subscribe
    public void onInit(InitEvent event) {
        initQueryReportSourceCode();
    }

    @Subscribe("reportParameterTable.generate")
    public void onReportParameterTableGenerate(Action.ActionPerformedEvent event) {
        if (!queryParametersDc.getItems().isEmpty()) {
            dialogs.createOptionDialog()
                    .withCaption(messages.getMessage("dialogs.Confirmation"))
                    .withMessage(messageBundle.getMessage("clearQueryParameterConfirm"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK).withHandler(e -> generateQueryParameters()),
                            new DialogAction(DialogAction.Type.CANCEL))
                    .show();
        } else {
            generateQueryParameters();
        }
    }

    @Subscribe(id = "reportRegionsDc", target = Target.DATA_CONTAINER)
    public void onReportRegionsDcCollectionChange(CollectionContainer.CollectionChangeEvent<ReportRegion> event) {
        regenerateQuery = event.getChangeType() == CollectionChangeType.ADD_ITEMS;
    }

    @Subscribe(id = "regionPropertiesDc", target = Target.DATA_CONTAINER)
    public void onRegionPropertiesDcCollectionChange(CollectionContainer.CollectionChangeEvent<RegionProperty> event) {
        regenerateQuery = true;
    }

    @Subscribe("runBtn")
    public void onRunBtnClick(Button.ClickEvent event) {
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

    protected void generateQueryParameters() {
        List<QueryParameter> queryParameterList = queryParametersDc.getMutableItems();
        queryParameterList.clear();

        String query = reportDataDc.getItem().getQuery();

        if (query != null) {
            QueryParser queryParser = queryTransformerFactory.parser(query);
            Set<String> paramNames = queryParser.getParamNames();

            for (String paramName : paramNames) {
                QueryParameter queryParameter = createQueryParameter(paramName);
                queryParameterList.add(queryParameter);
            }
        }
    }

    private QueryParameter createQueryParameter(String name) {
        QueryParameter queryParameter = metadata.create(QueryParameter.class);
        queryParameter.setName(name);
        queryParameter.setParameterType(ParameterType.TEXT);
        queryParameter.setJavaClassName(String.class.getName());
        queryParameter.setDefaultValueString(null);

        return queryParameter;
    }

    protected void initQueryReportSourceCode() {
        reportQueryCodeEditor.setHighlightActiveLine(false);
        reportQueryCodeEditor.setShowGutter(false);
        reportQueryCodeEditor.setMode(SourceCodeEditor.Mode.SQL);
        reportQueryCodeEditor.setSuggester(this);
    }

    @Install(to = "reportQueryCodeEditor", subject = "contextHelpIconClickHandler")
    private void reportQueryCodeEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messageBundle.getMessage("reportQueryHelpCaption"))
                .withMessage(messageBundle.getMessage("reportQueryHelp"))
                .withModal(false)
                .withWidth("560px")
                .withContentMode(ContentMode.HTML)
                .withHtmlSanitizer(true)
                .show();
    }

    @Override
    public String getCaption() {
        return messageBundle.getMessage("reportQueryCaption");
    }

    @Override
    public String getDescription() {
        return messageBundle.getMessage("enterQuery");
    }

    @Override
    public void beforeShow() {
        ReportData item = reportDataDc.getItem();
        String resultQuery = item.getQuery();
        if (StringUtils.isEmpty(resultQuery) || regenerateQuery) {
            item.setQuery(String.format("select e from %s e", item.getEntityName()));
            if (CollectionUtils.isNotEmpty(item.getReportRegions())) {
                resultQuery = new JpqlQueryBuilder(item, item.getReportRegions().get(0)).buildInitialQuery();
            }
            queryParametersDc.getMutableItems().clear();
            regenerateQuery = false;
        }
        reportQueryCodeEditor.setValue(resultQuery);
    }

    @Override
    public List<Suggestion> getSuggestions(AutoCompleteSupport source, String text, int cursorPosition) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        int queryPosition = cursorPosition - 1;

        return jpqlUiSuggestionProvider.getSuggestions(text, queryPosition, source);
    }

    @Install(to = "reportParameterTable.defaultValueString", subject = "valueProvider")
    protected Object reportParameterTableDefaultStringValueProvider(QueryParameter queryParameter) {
        Object defaultValue = queryParameter.getDefaultValue();
        if (defaultValue != null) {
            ParameterType parameterType = queryParameter.getParameterType();
            switch (parameterType) {
                case DATE:
                    String dateFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getDateFormat();
                    return new SimpleDateFormat(dateFormat).format(defaultValue);
                case TIME:
                    String timeFormat = formatStringsRegistry.getFormatStrings(currentAuthentication.getLocale()).getTimeFormat();
                    return new SimpleDateFormat(timeFormat).format(defaultValue);
                default:
                    return defaultValue;
            }
        }

        return StringUtils.EMPTY;
    }

    @Install(to = "reportParameterTable.edit", subject = "afterCommitHandler")
    protected void reportParameterTableEditAfterCommitHandler(QueryParameter queryParameter) {
        setDefaultValue(queryParameter);
    }

    @Install(to = "reportParameterTable.create", subject = "afterCommitHandler")
    protected void reportParameterTableCreateAfterCommitHandler(QueryParameter queryParameter) {
        setDefaultValue(queryParameter);
    }

    protected void setDefaultValue(QueryParameter queryParameter) {
        try {
            Object value = objectToStringConverter.convertFromString(Class.forName(queryParameter.getJavaClassName()), queryParameter.getDefaultValueString());
            queryParameter.setDefaultValue(value);
            queryParametersDc.replaceItem(queryParameter);
        } catch (ClassNotFoundException e) {

        }
    }
}