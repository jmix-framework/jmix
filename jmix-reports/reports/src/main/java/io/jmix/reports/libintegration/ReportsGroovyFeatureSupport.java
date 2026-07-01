/*
 * Copyright 2026 Haulmont.
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

import io.jmix.core.CoreProperties;
import io.jmix.reports.ReportsProperties;
import io.jmix.reports.yarg.exception.ReportingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component("report_ReportsGroovyFeatureSupport")
public class ReportsGroovyFeatureSupport {

    private static final Logger log = LoggerFactory.getLogger(ReportsGroovyFeatureSupport.class);

    protected final ReportsProperties reportsProperties;
    protected final CoreProperties coreProperties;

    public ReportsGroovyFeatureSupport(ReportsProperties reportsProperties, CoreProperties coreProperties) {
        this.reportsProperties = reportsProperties;
        this.coreProperties = coreProperties;
    }

    public boolean isGroovyEnabled() {
        return coreProperties.isUnsafeRuntimeFeaturesEnabled() && reportsProperties.isGroovyEnabled();
    }

    public List<Map<String, Object>> getDisabledDataSetResult(String dataSetName) {
        log.warn("Groovy execution is disabled for reports. Returning an empty data set result for '{}'.", dataSetName);
        return Collections.emptyList();
    }

    public String getDisabledJsonResult(String dataSetName) {
        log.warn("Groovy execution is disabled for reports. Returning an empty JSON array for '{}'.", dataSetName);
        return "[]";
    }

    public String getDisabledFormatterResult(String parameterName) {
        log.warn("Groovy execution is disabled for reports. Returning an empty formatted value for '{}'.", parameterName);
        return "";
    }

    public Object getDisabledTransformationResult(String parameterName, Object originalValue) {
        log.warn("Groovy execution is disabled for reports. Returning the original parameter value for '{}'.", parameterName);
        return originalValue;
    }

    public String getDisabledQueryTemplateResult(String dataSetName, String query) {
        log.warn("Groovy execution is disabled for reports. Returning the original query template for '{}'.", dataSetName);
        return query;
    }

    public void throwGroovyDisabled(String featureDescription) {
        throw new ReportingException("Groovy execution is disabled for reports: " + featureDescription);
    }
}
