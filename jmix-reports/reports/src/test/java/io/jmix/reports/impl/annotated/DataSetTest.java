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

public class DataSetTest extends BaseAnnotatedReportExecutionTest {

    @Test
    public void testJpqlDataSet() {
        // todo maybe better test with more attributes
        // given
        List<String> usernames = List.of("lmedrano", "shelton");
        String reportCode = UsersAndAchievementsReport.CODE;

        List<UserRegistration> users = unconstrainedDataManager.load(UserRegistration.class)
                .query("select ur from UserRegistration ur where ur.username in :names")
                .parameter("names", usernames)
                .list();

        // when
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .addParam(UsersAndAchievementsReport.PARAM_USERS, users)
                .run();

        // then
        assertThat(outputDocument.getReportOutputType()).isEqualTo(ReportOutputType.xlsx);

        // game name goes from JPQL band
        Object[][] expectedCells = new Object[][] {
                // row, column, cell content
                {2, 2, "Destiny"},
                {5, 2, "Mario Kart DS"},
                {8, 2, "Mario Kart DS"},
                {11, 2, "Modern Warfare 3"}
        };
        Sheet firstSheet = readFirstSheetFromBytes(outputDocument.getContent());
        for (Object[] entry : expectedCells) {
            assertThat(stringCellValue(firstSheet, (Integer) entry[0], (Integer) entry[1])).isEqualTo(entry[2]);
        }
    }

    @Test
    public void testSqlDataSet() {
        // given
        List<String> usernames = List.of("lmedrano", "shelton");
        String reportCode = UsersAndAchievementsReport.CODE;

        List<UserRegistration> users = unconstrainedDataManager.load(UserRegistration.class)
                .query("select ur from UserRegistration ur where ur.username in :names")
                .parameter("names", usernames)
                .list();

        // when
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .addParam(UsersAndAchievementsReport.PARAM_USERS, users)
                .run();

        // then
        assertThat(outputDocument.getReportOutputType()).isEqualTo(ReportOutputType.xlsx);

        // achievement name and date come from SQL band
        Object[][] expectedCells = new Object[][] {
                // row, column, cell content
                {3, 3, "Defend position alone"},
                {3, 4, "25.02.2025 15:13"},

                {4, 3, "1000 target hit"},
                {4, 4, "25.02.2025 23:25"},

                {6, 3, "Launch the game"},
                {6, 4, "15.01.2025 18:45"},

                {9, 3, "Launch the game"},
                {9, 4, "03.02.2025 15:10"},

                {10, 3, "Survive maximum speed"},
                {10, 4, "03.02.2025 16:30"},
        };
        Sheet firstSheet = readFirstSheetFromBytes(outputDocument.getContent());
        for (Object[] entry : expectedCells) {
            assertThat(stringCellValue(firstSheet, (Integer) entry[0], (Integer) entry[1])).isEqualTo(entry[2]);
        }
    }
}
