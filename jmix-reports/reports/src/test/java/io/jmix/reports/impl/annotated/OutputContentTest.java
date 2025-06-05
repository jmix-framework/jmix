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

import com.opencsv.CSVReader;
import io.jmix.reports.test_support.entity.UserRegistration;
import io.jmix.reports.test_support.report.RevenueByGameReport;
import io.jmix.reports.test_support.report.UsersAndAchievementsReport;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OutputContentTest extends BaseAnnotatedReportExecutionTest {

    @Test
    public void testXlsxOutput() {
        // given
        List<String> usernames = List.of("lmedrano");
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
        assertThat(outputDocument.getDocumentName()).endsWith(".xlsx");
        assertThat(outputDocument.getReportOutputType()).isEqualTo(ReportOutputType.xlsx);
        assertThat(outputDocument.getContent()).isNotNull();

        // few basic checks of content, detailed checks are in other tests
        Sheet firstSheet = readFirstSheetFromBytes(outputDocument.getContent());
        assertThat(stringCellValue(firstSheet, 0, 0)).isEqualTo("First name"); // header
        assertThat(stringCellValue(firstSheet, 1, 1)).isEqualTo("Medrano"); // surname of first user
    }

    @Test
    public void testXlsxStaticBand() {
        // given
        List<String> usernames = List.of("lmedrano");
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
        Object[][] expectedCells = new Object[][] {
                // row, column, cell content
                {0, 0, "First name"},
                {0, 1, "Last name"},
                {0, 2, "Game"},
                {0, 3, "Achievement"},
                {0, 4, "Earned date"}
        };
        Sheet firstSheet = readFirstSheetFromBytes(outputDocument.getContent());
        for (Object[] entry : expectedCells) {
            assertThat(stringCellValue(firstSheet, (Integer) entry[0], (Integer) entry[1])).isEqualTo(entry[2]);
        }
    }

    @Test
    public void testXlsxNestedHorizontalBands() {
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
        // check that nested bands content goes in correct order:
        //   users >> users' games >> games' achievements
        Object[][] expectedCells = new Object[][] {
                // row, column, cell content
                {1, 0, "Liliana"},
                {1, 1, "Medrano"},

                {2, 2, "Destiny"},
                {3, 3, "Defend position alone"},
                {4, 3, "1000 target hit"},

                {5, 2, "Mario Kart DS"},
                {6, 3, "Launch the game"},

                {7, 0, "Issac"},
                {7, 1, "Shelton"},

                {8, 2, "Mario Kart DS"},
                {9, 3, "Launch the game"},
                {10, 3, "Survive maximum speed"},

                {11, 2, "Modern Warfare 3"}
        };
        Sheet firstSheet = readFirstSheetFromBytes(outputDocument.getContent());
        for (Object[] entry : expectedCells) {
            assertThat(stringCellValue(firstSheet, (Integer) entry[0], (Integer) entry[1])).isEqualTo(entry[2]);
        }
    }

    @Test
    public void testCsvOutput() throws Exception {
        // given
        String reportCode = RevenueByGameReport.CODE;

        // when
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .run();

        // then
        assertThat(outputDocument.getDocumentName()).endsWith(".csv");
        assertThat(outputDocument.getReportOutputType()).isEqualTo(ReportOutputType.csv);
        assertThat(outputDocument.getContent()).isNotNull();

        // few basic checks of content
        CSVReader csvReader = readCsvContent(outputDocument);
        String[] line = csvReader.readNext();
        assertThat(line).isEqualTo(new String[]{"Game title", "Purchase count", "Revenue"});
        assertThat(csvReader.readAll()).hasSize(3); // 3 games were purchased during that period

        csvReader.close();
    }
}
