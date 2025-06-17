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

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroupInfo;
import io.jmix.reports.entity.ReportSource;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component("report_ReportRepository")
public class ReportRepositoryImpl implements ReportRepository {
    public static final String FULL_FETCH_PLAN = "report.edit";

    protected final AnnotatedReportHolder annotatedReportHolder;
    protected final AnnotatedReportScanner reportScanner;
    protected final DataManager dataManager;
    protected final Metadata metadata;

    public ReportRepositoryImpl(AnnotatedReportHolder annotatedReportHolder, AnnotatedReportScanner reportScanner, DataManager dataManager,
                                Metadata metadata) {
        this.annotatedReportHolder = annotatedReportHolder;
        this.reportScanner = reportScanner;
        this.dataManager = dataManager;
        this.metadata = metadata;
    }

    @Override
    public Collection<Report> getAllReports() {
        Collection<Report> annotatedReports = annotatedReportHolder.getAllReports();

        List<Report> reportsFromDb = loadReportsFromDatabase();

        List<Report> allReports = new ArrayList<>(annotatedReports);
        allReports.addAll(reportsFromDb);
        return allReports;
    }

    protected List<Report> loadReportsFromDatabase() {
        List<Report> reportsFromDb = dataManager.load(Report.class)
                .all()
                .fetchPlan("report.run") // todo
                .list();
        return reportsFromDb;
    }

    @Nullable
    @Override
    public Report loadFullReportByCode(String reportCode) {
        Report report = annotatedReportHolder.getByCode(reportCode);
        if (report != null) {
            return report;
        }
        report = dataManager.load(Report.class)
                .condition(PropertyCondition.equal("code", reportCode))
                .fetchPlan(FULL_FETCH_PLAN)
                .optional()
                .orElse(null);

        return report;
    }

    @Override
    public boolean existsReportByGroup(ReportGroupInfo group) {
        if (group.getSource() == ReportSource.ANNOTATED_CLASS) {
            for (Report report : annotatedReportHolder.getAllReports()) {
                if (report.getGroup() != null && report.getGroup().getCode().equals(group.getCode())) {
                    return true;
                }
            }
        }

        Optional<Report> report = dataManager.load(Report.class)
                .query("select r from report_Report r where r.group.id = :groupId")
                .parameter("groupId", group.getId())
                .fetchPlan("report.view")
                .optional();

        return report.isPresent();
    }

    @EventListener
    public void handleApplicationStartedEvent(ApplicationStartedEvent event) {
        reportScanner.importGroupDefinitions();
        reportScanner.importReportDefinitions();
    }
}
