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
import io.jmix.core.FetchPlan;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component("report_ReportRepository")
public class ReportRepositoryImpl implements ReportRepository {
    public static final String FULL_FETCH_PLAN = "report.edit";

    protected final AnnotatedReportHolder annotatedReportHolder;
    protected final AnnotatedReportGroupHolder annotatedReportGroupHolder;
    protected final AnnotatedReportScanner reportScanner;
    protected final DataManager dataManager;

    public ReportRepositoryImpl(AnnotatedReportHolder annotatedReportHolder, AnnotatedReportScanner reportScanner, DataManager dataManager,
                                AnnotatedReportGroupHolder annotatedReportGroupHolder) {
        this.annotatedReportHolder = annotatedReportHolder;
        this.reportScanner = reportScanner;
        this.dataManager = dataManager;
        this.annotatedReportGroupHolder = annotatedReportGroupHolder;
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

    @Override
    public Collection<ReportGroup> getAllGroups() {
        Collection<ReportGroup> annotatedGroups = annotatedReportGroupHolder.getAllGroups();
        List<ReportGroup> groupsFromDb = loadGroupsFromDatabase();

        List<ReportGroup> allGroups = new ArrayList<>(annotatedGroups);
        allGroups.addAll(groupsFromDb);
        return allGroups;
    }

    protected List<ReportGroup> loadGroupsFromDatabase() {
        return dataManager.load(ReportGroup.class)
                .all()
                .fetchPlan(FetchPlan.BASE)
                .list();
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
                .parameter("code", reportCode)
                .fetchPlan(FULL_FETCH_PLAN)
                .optional()
                .orElse(null);

        return report;
    }

    @EventListener
    public void handleApplicationStartedEvent(ApplicationStartedEvent event) {
        reportScanner.importGroupDefinitions();
        reportScanner.importReportDefinitions();
    }
}
