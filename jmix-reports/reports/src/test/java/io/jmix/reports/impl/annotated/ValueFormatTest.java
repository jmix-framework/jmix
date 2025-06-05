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

package io.jmix.reports.impl.annotated;

import io.jmix.reports.test_support.entity.UserRegistration;
import io.jmix.reports.test_support.report.UsersAndAchievementsReport;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ValueFormatTest extends BaseAnnotatedReportExecutionTest {

    @Test
    public void testStaticValueFormat() {
        // given
        List<String> userNames = List.of("shelton");
        String reportCode = UsersAndAchievementsReport.CODE;

        List<UserRegistration> users = unconstrainedDataManager.load(UserRegistration.class)
                .query("select ur from UserRegistration ur where ur.username in :names")
                .parameter("names", userNames)
                .list();

        // when
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .addParam(UsersAndAchievementsReport.PARAM_USERS, users)
                .run();

        // then
        Object[][] expectedCells = new Object[][] {
                // row, column, cell content
                {3, 4, "03.02.2025 15:10"}, // shelton, mario kart, launch
                {4, 4, "03.02.2025 16:30"}, // shelton, mario kart, survive
        };
        Sheet firstSheet = readFirstSheetFromBytes(outputDocument.getContent());
        for (Object[] entry : expectedCells) {
            assertThat(stringCellValue(firstSheet, (Integer) entry[0], (Integer) entry[1])).isEqualTo(entry[2]);
        }
    }


}
