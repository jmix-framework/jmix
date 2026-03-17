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

package io.jmix.reports.impl;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.outside_reports.scanner.DynamicLoadingGroup;
import io.jmix.outside_reports.scanner.DynamicLoadingReport;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AnnotatedReportScannerDuplicateTest {

    @Autowired
    AnnotatedReportScanner annotatedReportScanner;
    @Autowired
    AnnotatedReportGroupHolder reportGroupHolder;
    @Autowired
    AnnotatedReportHolder reportHolder;
    @Autowired
    UnconstrainedDataManager dataManager;

    @Test
    public void testLoadReportGroupClassDuplicateInDb() {
        // given
        ReportGroup dbGroup = dataManager.create(ReportGroup.class);
        dbGroup.setCode(DynamicLoadingGroup.CODE);
        dbGroup.setTitle("DB Group");
        dataManager.save(dbGroup);

        try {
            // when & then
            assertThrows(IllegalStateException.class, () ->
                    annotatedReportScanner.loadReportGroupClass(DynamicLoadingGroup.class.getName()));
        } finally {
            dataManager.remove(dbGroup);
        }
    }

    @Test
    public void testLoadReportClassDuplicateInDb() {
        // given
        Report dbReport = dataManager.create(Report.class);
        dbReport.setCode(DynamicLoadingReport.CODE);
        dbReport.setName("DB Report");
        dataManager.save(dbReport);

        try {
            // when & then
            assertThrows(IllegalStateException.class, () ->
                    annotatedReportScanner.loadReportClass(DynamicLoadingReport.class.getName()));
        } finally {
            dataManager.remove(dbReport);
        }
    }

    @AfterEach
    void tearDown() {
        reportGroupHolder.clear();
        reportHolder.clear();
    }
}
