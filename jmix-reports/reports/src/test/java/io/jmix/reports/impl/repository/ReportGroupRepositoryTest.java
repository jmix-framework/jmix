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

import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.reports.ReportGroupFilter;
import io.jmix.reports.ReportGroupLoadContext;
import io.jmix.reports.ReportGroupRepository;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reports.impl.AnnotatedReportGroupHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.test_support.ReportGroupUtil;
import io.jmix.reports.test_support.report.DemoReportGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class ReportGroupRepositoryTest {
    @Autowired
    protected ReportGroupRepository reportGroupRepository;
    @Autowired
    protected AnnotatedReportGroupHolder reportGroupHolder;
    @Autowired
    protected AnnotatedReportScanner annotatedReportScanner;
    @Autowired
    protected ReportGroupUtil reportGroupUtil;
    @Autowired
    protected SystemAuthenticator systemAuthenticator;

    @AfterEach
    void tearDown() {
        reportGroupHolder.clear();
        reportGroupUtil.cleanupDatabaseReportGroups();
    }

    @Test
    public void testLoadAll(){
        List<ReportGroup> reportGroups = reportGroupRepository.loadAll();
        assertThat(reportGroups).isNotNull();
        assertThat(reportGroups).size().isEqualTo(0);

        annotatedReportScanner.importGroupDefinitions();

        reportGroups = reportGroupRepository.loadAll();
        assertThat(reportGroups).isNotNull();
        assertThat(reportGroups).size().isEqualTo(1);

        reportGroupUtil.createAndSaveSimpleReportGroup();

        reportGroups = reportGroupRepository.loadAll();
        assertThat(reportGroups).size().isEqualTo(2);
    }

    @Test
    public void testLoadAllWithoutRights() {
        annotatedReportScanner.importGroupDefinitions();

        List<ReportGroup> reportGroups = systemAuthenticator
                .withUser("with-no-access-user", () -> reportGroupRepository.loadAll());

        assertThat(reportGroups).size().isEqualTo(0);
    }

    @Test
    public void testLoadList() {
        reportGroupHolder.clear();
        annotatedReportScanner.importGroupDefinitions();
        reportGroupUtil.createAndSaveSimpleReportGroup();

        ReportGroupFilter reportGroupFilter = new ReportGroupFilter();
        reportGroupFilter.setCodeContains("DEMOS");
        ReportGroupLoadContext reportGroupLoadContext = new ReportGroupLoadContext(reportGroupFilter, null, 0, 0);

        List<ReportGroup> reportGroups = reportGroupRepository.loadList(reportGroupLoadContext);

        assertThat(reportGroups).isNotNull();
        assertThat(reportGroups).size().isEqualTo(1);

        reportGroupFilter.setCodeContains("NOT_EXISTING_CODE");
        reportGroups = reportGroupRepository.loadList(reportGroupLoadContext);
        assertThat(reportGroups).size().isEqualTo(0);
    }

    @Test
    public void testLoadListWithoutRights() {
        annotatedReportScanner.importGroupDefinitions();
        reportGroupUtil.createAndSaveSimpleReportGroup();

        ReportGroupFilter reportGroupFilter = new ReportGroupFilter();
        reportGroupFilter.setCodeContains("DEMOS");
        ReportGroupLoadContext reportGroupLoadContext = new ReportGroupLoadContext(reportGroupFilter, null, 0, 0);

        List<ReportGroup> reportGroups = systemAuthenticator
                .withUser("with-no-access-user", () -> reportGroupRepository.loadList(reportGroupLoadContext));

        assertThat(reportGroups).size().isEqualTo(0);
    }

    @Test
    public void testGetTotalCount() {
        annotatedReportScanner.importGroupDefinitions();
        reportGroupUtil.createAndSaveSimpleReportGroup();

        int totalCount;
        ReportGroupFilter reportGroupFilter = new ReportGroupFilter();

        reportGroupFilter.setCodeContains("DEMOS");
        totalCount = reportGroupRepository.getTotalCount(reportGroupFilter);
        assertThat(totalCount).isEqualTo(1);

        reportGroupFilter.setCodeContains(ReportGroupUtil.SIMPLE_RUNTIME_REPORT_GROUP_CODE);
        totalCount = reportGroupRepository.getTotalCount(reportGroupFilter);
        assertThat(totalCount).isEqualTo(1);

        reportGroupFilter.setTitleContains(ReportGroupUtil.SIMPLE_RUNTIME_REPORT_GROUP_NAME);
        totalCount = reportGroupRepository.getTotalCount(reportGroupFilter);
        assertThat(totalCount).isEqualTo(1);

        reportGroupFilter.setCodeContains("NOT_EXISTING_CODE");
        totalCount = reportGroupRepository.getTotalCount(reportGroupFilter);
        assertThat(totalCount).isEqualTo(0);

        reportGroupFilter.setTitleContains("NOT_EXISTING_TITLE");
        totalCount = reportGroupRepository.getTotalCount(reportGroupFilter);
        assertThat(totalCount).isEqualTo(0);
    }

    @Test
    public void testGetTotalCountWithoutRights() {
        int totalCount;
        ReportGroupFilter reportGroupFilter = new ReportGroupFilter();
        reportGroupFilter.setCodeContains("DEMOS");

        annotatedReportScanner.importGroupDefinitions();

        totalCount = systemAuthenticator.withUser("with-no-access-user", () -> reportGroupRepository.getTotalCount(reportGroupFilter));

        assertThat(totalCount).isEqualTo(0);
    }

    @Test
    public void testExistsGroupByCode() {
        annotatedReportScanner.importGroupDefinitions();
        reportGroupUtil.createAndSaveSimpleReportGroup();

        String annotatedReportGroupCode = DemoReportGroup.CODE;
        String runtimeReportGroupCode = ReportGroupUtil.SIMPLE_RUNTIME_REPORT_GROUP_CODE;
        String notExistingReportGroupCode = "NOT_EXISTING_CODE";

        assertThat(reportGroupRepository.existsGroupByCode(annotatedReportGroupCode)).isEqualTo(true);
        assertThat(reportGroupRepository.existsGroupByCode(runtimeReportGroupCode)).isEqualTo(true);
        assertThat(reportGroupRepository.existsGroupByCode(notExistingReportGroupCode)).isEqualTo(false);
    }

    @Test
    public void testExistsGroupByCodeWithoutRights() {
        annotatedReportScanner.importGroupDefinitions();
        reportGroupUtil.createAndSaveSimpleReportGroup();

        String annotatedReportGroupCode = DemoReportGroup.CODE;
        String runtimeReportGroupCode = ReportGroupUtil.SIMPLE_RUNTIME_REPORT_GROUP_CODE;

        assertThatThrownBy(() -> systemAuthenticator.withUser("with-no-access-user", () -> reportGroupRepository.existsGroupByCode(annotatedReportGroupCode)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("entity");
        assertThatThrownBy(() -> systemAuthenticator.withUser("with-no-access-user", () -> reportGroupRepository.existsGroupByCode(runtimeReportGroupCode)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("entity");
    }
}
