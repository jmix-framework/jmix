/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reports.impl;

import io.jmix.reports.AnnotatedReportProvider;
import io.jmix.reports.annotation.ReportDef;
import io.jmix.reports.entity.Report;
import io.jmix.reports.impl.builder.AnnotatedReportBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component("report_AnnotatedReportProvider")
public class AnnotatedReportProviderImpl implements AnnotatedReportProvider, BeanPostProcessor {

    protected final AnnotatedReportBuilder annotatedReportBuilder;

    /**
     * Map: report code -> report model object.
     */
    protected Map<String, Report> reportsByCode;

    public AnnotatedReportProviderImpl(AnnotatedReportBuilder annotatedReportBuilder) {
        this.annotatedReportBuilder = annotatedReportBuilder;
        this.reportsByCode = new HashMap<>();
    }

    @Override
    public Collection<Report> getAllReports() {
        return reportsByCode.values();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if (beanClass.isAnnotationPresent(ReportDef.class)) {
            importReportDefinition(bean, beanName);
        }
        return bean;
    }

    protected void importReportDefinition(Object bean, String beanName) {
        Report report = annotatedReportBuilder.createReportFromDefinition(bean);
        if (reportsByCode.containsKey(report.getCode())) {
            throw new IllegalStateException(
                    String.format("Duplicate report code: %s, bean name: %s", report.getCode(), beanName)
            );
        }
        reportsByCode.put(report.getCode(), report);
    }
}
