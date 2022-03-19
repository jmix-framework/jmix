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
package io.jmix.reportsui.screen.report.run;

import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reportsui.runner.FluentUiReportRunner;
import io.jmix.reportsui.runner.ParametersDialogShowMode;
import io.jmix.reportsui.runner.UiReportRunner;
import io.jmix.reportsui.screen.ReportParameterValidator;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

@UiController("report_InputParameters.dialog")
@UiDescriptor("input-parameters-dialog.xml")
public class InputParametersDialog extends Screen {
    public static final String INPUT_PARAMETER = "inputParameter";
    public static final String REPORT_PARAMETER = "report";

    protected String templateCode;

    protected String outputFileName;

    protected boolean bulkPrint;

    protected Report report;

    protected ReportInputParameter inputParameter;

    protected Map<String, Object> parameters;

    protected Collection selectedEntities;

    @Autowired
    protected UiReportRunner uiReportRunner;

    @Autowired
    protected InputParametersFragment inputParametersFragment;

    @Autowired
    protected ReportParameterValidator reportParameterValidator;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Messages messages;

    @Autowired
    protected ScreenValidation screenValidation;

    @Autowired
    protected UiProperties properties;

    protected boolean inBackground;

    public void setTemplateCode(@Nullable String templateCode) {
        this.templateCode = templateCode;
    }

    public void setOutputFileName(@Nullable String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setBulkPrint(boolean bulkPrint) {
        this.bulkPrint = bulkPrint;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public void setInputParameter(@Nullable ReportInputParameter inputParameter) {
        this.inputParameter = inputParameter;
    }

    public void setParameters(@Nullable Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setInBackground(boolean inBackground) {
        this.inBackground = inBackground;
    }

    @Subscribe("printReportButton")
    public void onPrintReportButtonClick(Button.ClickEvent event) {
        if (inputParametersFragment.getReport() != null) {
            ValidationErrors validationErrors = screenValidation.validateUiComponents(getWindow());
            crossValidateParameters(validationErrors);
            if (validationErrors.isEmpty()) {
                ReportTemplate template = inputParametersFragment.getReportTemplate();
                if (template != null) {
                    templateCode = template.getCode();
                }
                Report report = inputParametersFragment.getReport();
                Map<String, Object> parameters = inputParametersFragment.collectParameters();
                FluentUiReportRunner fluentRunner = uiReportRunner.byReportEntity(report)
                        .withParams(parameters)
                        .withTemplateCode(templateCode)
                        .withOutputNamePattern(outputFileName)
                        .withOutputType(inputParametersFragment.getOutputType())
                        .withParametersDialogShowMode(ParametersDialogShowMode.NO);
                if (inBackground) {
                    fluentRunner.inBackground(this);
                }

                if (bulkPrint) {
                    fluentRunner.runMultipleReports(inputParameter.getAlias(), selectedEntities);
                } else {
                    fluentRunner.runAndShow();
                }
            } else {
                screenValidation.showValidationErrors(this, validationErrors);
            }
        }
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        if (bulkPrint) {
            Preconditions.checkNotNullArgument(inputParameter, String.format("%s is null for bulk print", INPUT_PARAMETER));
            //noinspection unchecked
            selectedEntities = (Collection) parameters.get(inputParameter.getAlias());
        }

        if (report != null) {
            inputParametersFragment.setReport(report);
            inputParametersFragment.setInputParameter(inputParameter);
            inputParametersFragment.setParameters(parameters);
            inputParametersFragment.setBulkPrint(bulkPrint);
            inputParametersFragment.initLayout();
        }
        inputParametersFragment.initTemplateAndOutputSelect();
    }

    protected void crossValidateParameters(ValidationErrors validationErrors) {
        if (BooleanUtils.isTrue(inputParametersFragment.getReport().getValidationOn())) {
            try {
                reportParameterValidator.crossValidateParameters(inputParametersFragment.getReport(),
                        inputParametersFragment.collectParameters());
            } catch (ReportParametersValidationException e) {
                validationErrors.add(e.getMessage());
            }
        }
    }

    @Subscribe("cancelButton")
    protected void cancel(Button.ClickEvent event) {
        closeWithDefaultAction();
    }
}