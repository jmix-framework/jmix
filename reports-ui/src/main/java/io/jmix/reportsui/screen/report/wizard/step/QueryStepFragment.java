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

import io.jmix.data.QueryParser;
import io.jmix.data.QueryTransformerFactory;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.wizard.QueryParameter;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.ui.Dialogs;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.DialogAction;
import io.jmix.ui.component.HasContextHelp;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@UiController("report_QueryStep.fragment")
@UiDescriptor("query-step-fragment.xml")
public class QueryStepFragment extends StepFragment {

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

    @Subscribe
    public void onInit(InitEvent event) {
        initQueryReportSourceCode();
    }

    @Subscribe("reportParameterTable.generate")
    public void onReportParameterTableGenerate(Action.ActionPerformedEvent event) {
        if (!queryParametersDc.getItems().isEmpty()) {
            dialogs.createOptionDialog()
                    .withCaption(messages.getMessage("dialogs.Confirmation"))
                    .withMessage(messages.getMessage(getClass(), "clearQueryParameterConfirm"))
                    .withActions(
                            new DialogAction(DialogAction.Type.OK).withHandler(e -> generateQueryParameter()),
                            new DialogAction(DialogAction.Type.CANCEL))
                    .show();
        } else {
            generateQueryParameter();
        }
    }

    @Subscribe("reportParameterTable.add")
    public void onReportParameterTableAdd(Action.ActionPerformedEvent event) {
        QueryParameter queryParameter = metadata.create(QueryParameter.class);

        queryParametersDc.getMutableItems().add(queryParameter);
    }

    protected void generateQueryParameter() {
        List<QueryParameter> queryParameterList = queryParametersDc.getMutableItems();
        queryParameterList.clear();

        String query = reportDataDc.getItem().getQuery();

        if (query != null) {
            QueryParser queryParser = queryTransformerFactory.parser(query);
            Set<String> paramNames = queryParser.getParamNames();

            for (String paramName : paramNames) {
                //todo rethink a generation
                QueryParameter queryParameter = createQueryParameter(paramName, null, null);
                queryParameterList.add(queryParameter);
            }
        }
    }

    private QueryParameter createQueryParameter(String name, ParameterType parameterType, String defaultValue) {
        QueryParameter queryParameter = metadata.create(QueryParameter.class);
        queryParameter.setName(name);
        queryParameter.setParameterType(parameterType);
        queryParameter.setDefaultValue(defaultValue);

        return queryParameter;
    }

    protected void initQueryReportSourceCode() {
        reportQueryCodeEditor.setHighlightActiveLine(false);
        reportQueryCodeEditor.setShowGutter(false);
        reportQueryCodeEditor.setMode(SourceCodeEditor.Mode.SQL);
    }

    @Install(to = "reportQueryCodeEditor", subject = "contextHelpIconClickHandler")
    private void reportQueryCodeEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage(getClass(), "reportQueryHelpCaption"))
                .withMessage(messages.getMessage(getClass(), "reportQueryHelp"))
                .withModal(false)
                .withWidth("560px")
                .withHtmlSanitizer(true)
                .show();
    }

    @Override
    public String getCaption() {
        return messages.getMessage(getClass(), "reportQueryCaption");
    }

    @Override
    public String getDescription() {
        return messages.getMessage(getClass(), "enterQuery");
    }

    @Override
    public void beforeShow() {
        String entityName = reportDataDc.getItem().getEntityName();

        String query = String.format("select e from %s e", entityName);
        reportQueryCodeEditor.setValue(query);
    }
}