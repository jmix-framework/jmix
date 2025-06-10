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

import io.jmix.outside_reports.scanner.DynamicLoadingGroup;
import io.jmix.outside_reports.scanner.DynamicLoadingReport;
import io.jmix.reports.ReportsTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({SpringExtension.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AnnotatedReportScannerTest {

    @Autowired
    AnnotatedReportScanner annotatedReportScanner;

    @Autowired
    AnnotatedReportGroupHolder reportGroupHolder;

    @Autowired
    AnnotatedReportHolder reportHolder;

    @Test
    public void testLoadReportGroupClass() {
        // Here we rely on the effect that this method can load definition located outside of scan package

        // given
        String groupClassName = DynamicLoadingGroup.class.getName();
        assertThat(reportGroupHolder.getGroupByCode(DynamicLoadingGroup.CODE)).isNull();

        // when
        annotatedReportScanner.loadReportGroupClass(groupClassName);

        // then
        assertThat(reportGroupHolder.getGroupByCode(DynamicLoadingGroup.CODE)).isNotNull();
    }

    @Test
    public void testLoadReportClass() {
        // Here we rely on the effect that this method can load definition located outside of scan package

        // given
        String reportClassName = DynamicLoadingReport.class.getName();
        assertThat(reportHolder.getByCode(DynamicLoadingReport.CODE)).isNull();

        // when
        annotatedReportScanner.loadReportClass(reportClassName);

        // then
        assertThat(reportHolder.getByCode(DynamicLoadingReport.CODE)).isNotNull();
    }

    @AfterEach
    void tearDown() {
        reportGroupHolder.clear();
        reportHolder.clear();
    }
}
