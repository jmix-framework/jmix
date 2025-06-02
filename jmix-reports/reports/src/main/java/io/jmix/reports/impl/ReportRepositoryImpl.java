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
import io.jmix.reports.ReportRepository;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component("report_ReportRepository")
public class ReportRepositoryImpl implements ReportRepository {

    protected final AnnotatedReportProvider annotatedReportProvider;
    protected final DataManager dataManager;

    protected final AnnotatedReportGroupProvider annotatedReportGroupProvider;

    public ReportRepositoryImpl(AnnotatedReportProvider annotatedReportProvider, DataManager dataManager,
                                AnnotatedReportGroupProvider annotatedReportGroupProvider) {
        this.annotatedReportProvider = annotatedReportProvider;
        this.dataManager = dataManager;
        this.annotatedReportGroupProvider = annotatedReportGroupProvider;
    }

    @Override
    public Collection<Report> getAllReports() {
        Collection<Report> annotatedReports = annotatedReportProvider.getAllReports();

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
        Collection<ReportGroup> annotatedGroups = annotatedReportGroupProvider.getAllGroups();
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

    @EventListener
    public void handleApplicationStartedEvent(ApplicationStartedEvent event) {
        annotatedReportGroupProvider.importGroupDefinitions();
        annotatedReportProvider.importReportDefinitions();
    }
}
