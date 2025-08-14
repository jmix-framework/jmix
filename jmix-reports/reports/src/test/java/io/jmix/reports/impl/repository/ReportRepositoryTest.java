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

package io.jmix.reports.impl.repository;

import io.jmix.core.DataManager;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.outside_reports.*;
import io.jmix.reports.ReportFilter;
import io.jmix.reports.ReportLoadContext;
import io.jmix.reports.ReportRepository;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.impl.AnnotatedReportGroupHolder;
import io.jmix.reports.impl.AnnotatedReportHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.test_support.RuntimeReportUtil;
import io.jmix.reports.test_support.report.UsersAndAchievementsReport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class ReportRepositoryTest {

    @Autowired
    protected ReportRepository reportRepository;
    @Autowired
    protected AnnotatedReportGroupHolder reportGroupHolder;
    @Autowired
    protected AnnotatedReportHolder annotatedReportHolder;
    @Autowired
    protected AnnotatedReportScanner annotatedReportScanner;
    @Autowired
    protected RuntimeReportUtil runtimeReportUtil;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected SystemAuthenticator systemAuthenticator;

    protected ReportGroup correctGroup;

    @BeforeEach
    void setUp() {
        //annotatedReportScanner.importReportDefinitions();

        correctGroup = dataManager.create(ReportGroup.class);
        correctGroup.setCode(CorrectReportGroup.CODE);
        correctGroup.setTitle("Correct group");
        reportGroupHolder.put(correctGroup);
    }

    @AfterEach
    void tearDown() {
        reportGroupHolder.clear();
        annotatedReportHolder.clear();
        runtimeReportUtil.cleanupDatabaseReports();

    }

    @Test
    public void testSuccessfulGetAllReports() {
        // Before adding to repository
        assertThat(reportRepository.getAllReports().size()).isEqualTo(0);
        annotatedReportScanner.importReportDefinitions();

        Report runtimeReport = runtimeReportUtil.constructSimpleRuntimeReport();
        reportRepository.save(runtimeReport);


        List<Report> reportsList = (List<Report>) reportRepository.getAllReports();


        // After adding to repository
        assertThat(reportsList.size()).isEqualTo(7);
    }

    @Test
    public void testGetAllReportsWithoutRights() {
        Report runtimeReport = runtimeReportUtil.constructSimpleRuntimeReport();
        reportRepository.save(runtimeReport);

        // TODO: make the ReportRepositoryImpl#isReadPermitted() flag triggered
        // The user does not have read permissions to receive all reports with the roles set
        assertThat(systemAuthenticator.withUser("with-no-access-user", () -> reportRepository.getAllReports().size()))
                .isEqualTo(0);
    }

    @Test
    public void testExistingReportByGroup() {
        //Report createdAnnotatedReport = annotatedReportScanner.loadReportClass();
        Report createdRuntimeReport = runtimeReportUtil.constructSimpleRuntimeReport();
        ReportGroup notAssignedCorrectReportGroup = dataManager.create(ReportGroup.class);

        createdRuntimeReport.setGroup(correctGroup);
        reportRepository.save(createdRuntimeReport);

        // SimpleReport is assigned to ReportGroup
        assertThat(reportRepository.existsReportByGroup(correctGroup)).isEqualTo(true);
        // No reports have been assigned to the group with another group id
        assertThat(reportRepository.existsReportByGroup(notAssignedCorrectReportGroup)).isEqualTo(false);
    }

    @Test
    public void testExistingReportByGroupWithoutRights() {
        Report report = runtimeReportUtil.constructSimpleRuntimeReport();
        reportRepository.save(report);

        assertThat(systemAuthenticator.withUser("with-no-access-user", () -> reportRepository.existsReportByGroup(correctGroup)))
                .isEqualTo(false);
    }

    @Test
    public void testLoadList() {
        ReportFilter reportFilter = new ReportFilter();
        ReportLoadContext reportLoadContext = new ReportLoadContext(reportFilter);
        String reportName1 = RuntimeReportUtil.SIMPLE_RUNTIME_REPORT_NAME;
        String reportName2 = "Simple runtime report 2";
        Report report1 = runtimeReportUtil.constructSimpleRuntimeReport();
        Report report2 = runtimeReportUtil.constructSimpleRuntimeReport();

        // sets different name for 2nd report
        report2.setName(reportName2);

        // sets unique code
        report1.setCode("report-code-1");
        report2.setCode("report-code-2");

        // before saving to repository
        assertThat(reportRepository.loadList(reportLoadContext).size()).isEqualTo(0);

        reportRepository.save(report1);
        reportRepository.save(report2);

        List<Report> resultList = reportRepository.loadList(reportLoadContext);

        // after saving to repository
        assertThat(resultList.size()).isEqualTo(2);
        assertThat(resultList).anyMatch(r -> r.getName().equals(reportName1));
        assertThat(resultList).anyMatch(r -> r.getName().equals(reportName2));
    }

    @Test
    public void testLoadListWithoutRights() {
        ReportFilter reportFilter = new ReportFilter();
        ReportLoadContext reportLoadContext = new ReportLoadContext(reportFilter);
        Report report = runtimeReportUtil.constructSimpleRuntimeReport();

        reportRepository.save(report);

        assertThat(systemAuthenticator.withUser("with-no-access-user", () -> reportRepository.loadList(reportLoadContext).size()))
                .isEqualTo(0);
    }

    @Test
    public void testGetTotalCount() {
        ReportFilter reportFilter = new ReportFilter();
        annotatedReportScanner.importReportDefinitions();
        String reportName2 = "Simple runtime report 2";
        String reportCode1 = "report-code-1";
        String reportCode2 = "report-code-2";
        Report report1 = runtimeReportUtil.constructSimpleRuntimeReport();
        Report report2 = runtimeReportUtil.constructSimpleRuntimeReport();

        // sets different name for 2nd report
        report2.setName(reportName2);

        // sets unique code
        report1.setCode(reportCode1);
        report2.setCode(reportCode2);

        // before saving runtime reports to repository
        assertThat(reportRepository.getTotalCount(reportFilter)).isEqualTo(6);

        reportRepository.save(report1);
        reportRepository.save(report2);

        reportFilter.setCodeContains(UsersAndAchievementsReport.CODE);
        // expects one of the annotated reports to be found.
        assertThat(reportRepository.getTotalCount(reportFilter)).isEqualTo(1);

        reportFilter.setCodeContains(reportCode1);
        // expects one of the runtime reports to be found.
        assertThat(reportRepository.getTotalCount(reportFilter)).isEqualTo(1);
        // resetting the report filter and setting a new filtering condition
        reportFilter.setCodeContains(null);
        reportFilter.setNameContains("simple");
        assertThat(reportRepository.getTotalCount(reportFilter)).isEqualTo(2);
    }

    @Test
    public void testGetTotalCountWithoutRights() {
        ReportFilter reportFilter = new ReportFilter();
        annotatedReportScanner.importReportDefinitions();

        assertThat(systemAuthenticator.withUser("with-no-access-user", () ->reportRepository.getTotalCount(reportFilter)))
                .isEqualTo(0);
    }

    @Test
    public void testLoadForRunningByCode() {
        Report runtimeReport = runtimeReportUtil.constructSimpleRuntimeReport();
        String runtimeReportCode = "report-code-1";

        runtimeReport.setCode(runtimeReportCode);
        annotatedReportScanner.importReportDefinitions();

        // before saving runtime report to repository
        assertThat(reportRepository.loadForRunningByCode(runtimeReportCode)).isNull();
        assertThat(Objects.requireNonNull(reportRepository.loadForRunningByCode(UsersAndAchievementsReport.CODE)).getCode())
                .isEqualTo(UsersAndAchievementsReport.CODE);

        reportRepository.save(runtimeReport);

        // after saving to repository
        assertThat(reportRepository.loadForRunningByCode(runtimeReportCode)).isEqualTo(runtimeReport);
    }

    @Test
    public void testLoadForRunningByCodeWithoutRights() {
        Report runtimeReport = runtimeReportUtil.constructSimpleRuntimeReport();
        String runtimeReportCode = "report-code-1";

        runtimeReport.setCode(runtimeReportCode);
        annotatedReportScanner.importReportDefinitions();

        // before saving runtime report to repository
        assertThat(systemAuthenticator.withUser("with-no-access-user", () -> reportRepository.loadForRunningByCode(runtimeReportCode)))
                .isNull();
        assertThat(systemAuthenticator
                .withUser("with-no-access-user", () -> reportRepository.loadForRunningByCode(UsersAndAchievementsReport.CODE)))
                .isNull();

        reportRepository.save(runtimeReport);

        // after saving to repository
        assertThat(systemAuthenticator.withUser("with-no-access-user", () -> reportRepository.loadForRunningByCode(runtimeReportCode)))
                .isNull();
    }

    @Test
    public void testSave() {
        Report report1 = runtimeReportUtil.constructSimpleRuntimeReport();
        Report report2 = runtimeReportUtil.constructSimpleRuntimeReport();
        String reportCode = "report-code-1";
        report1.setCode(reportCode);
        report2.setCode(reportCode);

        // saving the same reports
        reportRepository.save(report1);

        assertThat(reportRepository.getAllReports().size()).isEqualTo(1);

        reportRepository.save(report2);

        // TODO: restriction on saving reports with the same code
        // the return value must be 1.
        // Reports with same codes are not saved
        assertThat(reportRepository.getAllReports().size()).isEqualTo(2);
    }

    @Test
    public void testUnauthorizedSave() {
        Report report = runtimeReportUtil.constructSimpleRuntimeReport();

        // Saving a report from a user without access rights to the report
        assertThatThrownBy(() -> systemAuthenticator.withUser("with-no-access-user", () -> reportRepository.save(report)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("entity");
    }

//    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    public void testReloadForRunning() {
        Report savedRuntimeReport = runtimeReportUtil.createAndSaveSimpleRuntimeReport();
        annotatedReportHolder.clear();
        annotatedReportScanner.importReportDefinitions();

        List<Report> annotatedReportsList = annotatedReportHolder.getAllReports().stream().toList();
        Optional<Report> annotatedReport = annotatedReportsList.stream()
                .filter(r -> r.getCode().equals(UsersAndAchievementsReport.CODE))
                .findAny();
        //Report savedReport = reportRepository.save(report);
        assertThat(annotatedReport.get()).isNotNull();

        // isLoadedWithFetchPlan case
        assertThat(reportRepository.reloadForRunning(savedRuntimeReport))
                .isEqualTo(savedRuntimeReport);

        assertThat(reportRepository.reloadForRunning(annotatedReport.get()))
                .isEqualTo(annotatedReport.get());
    }

    @Test
    public void testReloadForRunningWithoutRights() {
        Report savedRuntimeReport = runtimeReportUtil.createAndSaveSimpleRuntimeReport();

        assertThatThrownBy(() -> systemAuthenticator.withUser("with-no-access-user", () -> reportRepository.reloadForRunning(savedRuntimeReport)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("entity");
    }

    @Test
    public void testReloadTemplateForRunning() {
        Report unloadedReport = runtimeReportUtil.constructSimpleRuntimeReport();
        ReportTemplate templateFromUnloadedReport = unloadedReport.getDefaultTemplate();

        assertThat(reportRepository.reloadTemplateForRunning(templateFromUnloadedReport)).isEqualTo(templateFromUnloadedReport);

        Report loadedReport = reportRepository.save(unloadedReport);
        ReportTemplate templateFromLoadedReport = loadedReport.getDefaultTemplate();

        assertThat(reportRepository.reloadTemplateForRunning(templateFromLoadedReport)).isEqualTo(templateFromLoadedReport);
    }
}
