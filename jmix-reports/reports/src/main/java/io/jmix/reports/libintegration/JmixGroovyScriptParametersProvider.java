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


import io.jmix.reports.yarg.exception.ValidationException;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.reports.ReportsProperties;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("report_GroovyParametersProvider")
public class JmixGroovyScriptParametersProvider implements GroovyScriptParametersProvider {

    @Autowired
    protected ReportsProperties reportsProperties;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected ApplicationContext applicationContext;

    @Override
    public Map<String, Object> prepareParameters(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParameters) {

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("reportQuery", reportQuery);
        scriptParams.put("parentBand", parentBand);
        scriptParams.put("params", reportParameters);
        scriptParams.put("currentAuthentication", currentAuthentication);
        scriptParams.put("metadata", metadata);
        scriptParams.put("dataManager", dataManager);
        scriptParams.put("timeSource", timeSource);
        scriptParams.put("applicationContext", applicationContext);
        scriptParams.put("validationException", new MethodClosure(this, "validationException"));

        return scriptParams;
    }

    protected void validationException(String message) {
        throw new ValidationException(message);
    }
}
