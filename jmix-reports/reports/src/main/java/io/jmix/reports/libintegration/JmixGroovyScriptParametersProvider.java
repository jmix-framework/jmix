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

package io.jmix.reports.libintegration;


import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reports.yarg.exception.ValidationException;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component("report_GroovyParametersProvider")
public class JmixGroovyScriptParametersProvider implements GroovyScriptParametersProvider {

    protected final ReportsProperties reportsProperties;
    protected final Metadata metadata;
    protected final DataManager dataManager;
    protected final CurrentAuthentication currentAuthentication;
    protected final TimeSource timeSource;
    protected final ApplicationContext applicationContext;
    protected final Messages messages;

    public JmixGroovyScriptParametersProvider(ReportsProperties reportsProperties,
                                              Metadata metadata,
                                              DataManager dataManager,
                                              CurrentAuthentication currentAuthentication,
                                              TimeSource timeSource,
                                              ApplicationContext applicationContext,
                                              Messages messages) {
        this.reportsProperties = reportsProperties;
        this.metadata = metadata;
        this.dataManager = dataManager;
        this.currentAuthentication = currentAuthentication;
        this.timeSource = timeSource;
        this.applicationContext = applicationContext;
        this.messages = messages;
    }

    @Override
    public Map<String, Object> getParametersForDatasetParameters(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParameters) {
        Map<String, Object> scriptParams = getCommonParameters();

        scriptParams.put("reportQuery", reportQuery);
        scriptParams.put("parentBand", parentBand);
        scriptParams.put("params", reportParameters);
        scriptParams.put("showErrorMessage", throwDatasetValidationException());
        scriptParams.put("timeSource", timeSource);

        return scriptParams;
    }

    @Override
    public Map<String, Object> getParametersForValidationParameters() {
        Map<String, Object> params = getCommonParameters();
        params.put("showErrorMessage", throwParameterValidationException());
        return params;
    }

    protected Map<String, Object> getCommonParameters() {
        Map<String, Object> params = new HashMap<>();

        params.put("currentAuthentication", currentAuthentication);
        params.put("applicationContext", applicationContext);
        params.put("dataManager", dataManager);
        params.put("metadata", metadata);

        return params;
    }

    protected ExceptionCallable throwParameterValidationException() {
        return (arguments) -> {
            String message = (String) Arrays.stream(arguments)
                    .findFirst()
                    .orElse(messages.getMessage("validationFieldFail.defaultDescriptionMessage"));
            throw new ReportParametersValidationException(message);
        };
    }

    protected ExceptionCallable throwDatasetValidationException() {
        return (arguments) -> {
            String message = (String) Arrays.stream(arguments)
                    .findFirst()
                    .orElse(messages.getMessage("validationFieldFail.defaultDescriptionMessage"));
            throw new ValidationException(message);
        };
    }

    @FunctionalInterface
    public interface ExceptionCallable {
        void call(Object[] arguments) throws Exception;
    }
}
