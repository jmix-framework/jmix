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

package io.jmix.reportsflowui.runner;

import io.jmix.flowui.view.View;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.runner.FluentReportRunner;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * Class is used to run a report using various additional criteria:
 * <ul>
 *     <li>{@link Report} entity or report code</li>
 *     <li>{@link ReportTemplate} entity or template code: if none of these fields is set, the default template is used.</li>
 *     <li>Output type</li>
 *     <li>Output name pattern</li>
 *     <li>Input parameters</li>
 *     <li>Screen: screen or screen fragment from which the report runs</li>
 *     <li>Show a dialog to input the report parameters (defined by {@link ParametersDialogShowMode})</li>
 *     <li>Run a report synchronously or in the background (by default, a report runs synchronously)</li>
 * </ul>
 * <br>
 * Use the {@link UiReportRunner} bean to obtain an instance of the {@link FluentUiReportRunner}.
 */
@Component("report_FluentUiReportRunner")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FluentUiReportRunner {

    @Autowired
    protected ObjectProvider<FluentReportRunner> fluentReportRunners;

    private FluentReportRunner fluentReportRunner;
    private View originFrameOwner;
    private boolean inBackground;
    private ParametersDialogShowMode parametersDialogShowMode;

    private UiReportRunner uiReportRunner;

    @Autowired
    public void setUiReportRunner(UiReportRunner uiReportRunner) {
        this.uiReportRunner = uiReportRunner;
    }

    public FluentUiReportRunner init(Report report) {
        this.fluentReportRunner = fluentReportRunners.getObject(report);
        return this;
    }

    public FluentUiReportRunner init(String reportCode) {
        this.fluentReportRunner = fluentReportRunners.getObject(reportCode);
        return this;
    }

    /**
     * Sets a map with input parameters.
     *
     * @param params input parameters
     * @return current instance of fluent runner
     */
    public FluentUiReportRunner withParams(Map<String, Object> params) {
        this.fluentReportRunner.withParams(params);
        return this;
    }

    /**
     * Adds an input parameter to the parameter map.
     *
     * @param alias parameter alias
     * @param value parameter value
     * @return current instance of fluent runner
     */
    public FluentUiReportRunner addParam(String alias, Object value) {
        this.fluentReportRunner.addParam(alias, value);
        return this;
    }

    /**
     * Sets a code of template that will be used to run a report.
     *
     * @param templateCode template code
     * @return current instance of fluent runner
     */
    public FluentUiReportRunner withTemplateCode(String templateCode) {
        this.fluentReportRunner.withTemplateCode(templateCode);
        return this;
    }

    /**
     * Sets a template that will be used to run a report.
     *
     * @param template report template
     * @return current instance of fluent runner
     */
    public FluentUiReportRunner withTemplate(ReportTemplate template) {
        this.fluentReportRunner.withTemplate(template);
        return this;
    }

    /**
     * Sets a type of output document.
     *
     * @param outputType type of output document.
     * @return current instance of fluent runner
     */
    public FluentUiReportRunner withOutputType(ReportOutputType outputType) {
        this.fluentReportRunner.withOutputType(outputType);
        return this;
    }

    /**
     * Sets a name pattern of an output document.
     *
     * @param outputNamePattern name pattern of an output document
     * @return current instance of fluent runner
     */
    public FluentUiReportRunner withOutputNamePattern(@Nullable String outputNamePattern) {
        this.fluentReportRunner.withOutputNamePattern(outputNamePattern);
        return this;
    }

    /**
     * Sets a property to run a report in the background.
     *
     * @param originFrameOwner screen or screen fragment from which the report runs
     * @return current instance of fluent runner
     */
    public FluentUiReportRunner inBackground(View originFrameOwner) {
        this.inBackground = true;
        this.originFrameOwner = originFrameOwner;
        return this;
    }

    /**
     * Sets a mode to show a dialog to input the report parameter before report run.
     * If a mode is not specified, the {@link ParametersDialogShowMode#IF_REQUIRED} is used by default.
     *
     * @param mode mode to show a dialog to input the report parameter before report run
     * @return current instance of fluent runner
     */
    public FluentUiReportRunner withParametersDialogShowMode(ParametersDialogShowMode mode) {
        this.parametersDialogShowMode = mode;
        return this;
    }

    /**
     * Creates an instance of {@link UiReportRunContext} based on the parameters specified for the fluent runner.
     *
     * @return run context
     */
    public UiReportRunContext buildContext() {
        return new UiReportRunContext()
                .setReportRunContext(this.fluentReportRunner.buildContext())
                .setOriginFrameOwner(this.originFrameOwner)
                .setInBackground(this.inBackground)
                .setParametersDialogShowMode(this.parametersDialogShowMode);
    }

    /**
     * Builds a {@link UiReportRunContext} instance, runs a report using this run context and shows the result.
     */
    public void runAndShow() {
        uiReportRunner.runAndShow(buildContext());
    }

    /**
     * Builds a {@link UiReportRunContext} instance, runs a report for each object from the specified collection.
     * Objects in the collection should have the same type as an input parameter with specified alias.
     * If the report has other parameters besides the specified one, values for these parameters are copied for each report run.
     * As result, the ZIP archive with executed reports is downloaded.
     *
     * @param multiParamAlias alias of the parameter for which a value from the collection is used for report execution
     * @param multiParamValues collection of values
     */
    public void runMultipleReports(String multiParamAlias, Collection multiParamValues) {
        uiReportRunner.runMultipleReports(buildContext(), multiParamAlias, multiParamValues);
    }
}
