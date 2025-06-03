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

import io.jmix.reports.entity.Report;
import io.jmix.reports.impl.builder.AnnotatedReportBuilder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component("report_AnnotatedReportProvider")
public class AnnotatedReportHolderImpl implements AnnotatedReportHolder {

    protected final AnnotatedReportBuilder annotatedReportBuilder;

    /**
     * Map: report code -> report model object.
     */
    protected Map<String, Report> reportsByCode;

    public AnnotatedReportHolderImpl(AnnotatedReportBuilder annotatedReportBuilder) {
        this.annotatedReportBuilder = annotatedReportBuilder;
        this.reportsByCode = new ConcurrentHashMap<>();
    }

    @Override
    public Collection<Report> getAllReports() {
        return reportsByCode.values();
    }

    @Override
    public Report getByCode(String code) {
        return reportsByCode.get(code);
    }

    @Override
    public void put(Report report) {
        if (reportsByCode.containsKey(report.getCode())) {
            throw new IllegalStateException(
                    String.format("Duplicate report code: %s", report.getCode())
            );
        }
        reportsByCode.put(report.getCode(), report);
    }

    @Override
    public void clear() {
        reportsByCode.clear();
    }
}
