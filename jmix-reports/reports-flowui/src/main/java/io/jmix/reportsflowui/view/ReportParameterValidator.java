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

package io.jmix.reportsflowui.view;

import io.jmix.core.common.util.ParamsMap;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reports.exception.ReportingException;
import io.jmix.reports.libintegration.GroovyScriptParametersProvider;
import io.jmix.reports.yarg.util.groovy.Scripting;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("report_ReportParameterValidator")
public class ReportParameterValidator {


    protected final GroovyScriptParametersProvider groovyScriptParametersProvider;
    protected final Scripting scripting;


    public ReportParameterValidator(GroovyScriptParametersProvider groovyScriptParametersProvider,
                                    Scripting scripting) {
        this.groovyScriptParametersProvider = groovyScriptParametersProvider;
        this.scripting = scripting;
    }

    /**
     * Checking validation for an input parameter field before running the report.
     *
     * @param parameter data info which describes report's parameter
     * @param value     parameter's value
     */
    public void validateParameterValue(ReportInputParameter parameter, Object value) {
        String groovyScript = parameter.getValidationScript();
        Map<String, Object> scriptContext = createScriptContext(ParamsMap.of("value", value));
        runValidationScript(groovyScript, scriptContext);
    }

    /**
     * Performs cross field parameters validation before running the report.
     *
     * @param report           report instance
     * @param reportParameters map of parameters values taken from components
     */
    public void crossValidateParameters(Report report, Map<String, Object> reportParameters) {
        String groovyScript = report.getValidationScript();
        Map<String, Object> scriptContext = createScriptContext(ParamsMap.of("params", reportParameters));
        runValidationScript(groovyScript, scriptContext);
    }

    protected void runValidationScript(String groovyScript, Map<String, Object> scriptContext) {
        if (StringUtils.isNotBlank(groovyScript)) {
            try {
                scripting.evaluateGroovy(groovyScript, scriptContext);
            } catch (ReportParametersValidationException e) {
                throw e;
            } catch (Exception e) {
                String message = "Error applying field validation Groovy script. \n" + e;
                throw new ReportingException(message);
            }
        }
    }

    protected Map<String, Object> createScriptContext(Map<String, Object> contextParameters) {
        Map<String, Object> context = groovyScriptParametersProvider.getParametersForValidationParameters();
        context.putAll(contextParameters);
        return context;
    }
}
