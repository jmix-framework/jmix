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
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.reports.test_support.entity.UserRegistration;
import io.jmix.reports.test_support.report.GameCriticScoresReport;
import io.jmix.reports.test_support.report.RevenueByGameReport;
import io.jmix.reports.test_support.report.UserProfileReport;
import io.jmix.reports.test_support.report.UsersAndAchievementsReport;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.yarg.structure.ReportOutputType;
import org.apache.poi.ss.usermodel.Sheet;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
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

    @Test
    public void testDelegateDataSet() throws Exception {
        // given
        String reportCode = RevenueByGameReport.CODE;

        // when
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .run();

        // then
        CSVReader csvReader = readCsvContent(outputDocument);
        csvReader.readNext(); // title

        String[] line = csvReader.readNext();
        assertThat(line[0]).isEqualTo("Assassin's Creed");
        assertThat(line[1]).isEqualTo("2");

        line = csvReader.readNext();
        assertThat(line[0]).isEqualTo("Mario Kart DS");
        assertThat(line[1]).isEqualTo("1");

        line = csvReader.readNext();
        assertThat(line[0]).isEqualTo("Tetris");
        assertThat(line[1]).isEqualTo("1");

        line = csvReader.readNext();
        assertThat(line).isNull();

        csvReader.close();
    }

    @Test
    public void testNestedCollectionDataSet() throws DocumentException {
        // given
        String reportCode = UserProfileReport.CODE;
        String username = "lola18";

        // when
        UserRegistration user = unconstrainedDataManager.load(UserRegistration.class)
                .condition(PropertyCondition.equal("username", username))
                .one();
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .addParam(UserProfileReport.PARAM_USER, user)
                // default template
                .run();

        // then
        String xml = new String(outputDocument.getContent(), StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(xml);
        Element root = document.getRootElement();

        assertThat(root.elements()).hasSize(1);  // parent band
        assertThat(root.elements().get(0).getName()).isEqualTo("User");

        assertThat(root.elements().get(0).elements()).hasSize(3); // exactly 3 rows in nested collection band
        assertThat(root.elements().get(0).elements()).allMatch(e -> e.getName().equals("Purchase"));
    }

    @Test
    public void testFetchPlanProvider() {
        // given
        String reportCode = UserProfileReport.CODE;
        String username = "lola18";

        // when
        UserRegistration user = unconstrainedDataManager.load(UserRegistration.class)
                .condition(PropertyCondition.equal("username", username))
                .one();
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .addParam(UserProfileReport.PARAM_USER, user)
                // default template
                .run();

        // then
        String xml = new String(outputDocument.getContent(), StandardCharsets.UTF_8);
        assertThat(xml).contains("""
                <Purchase game="Mario Kart DS" purchaseDate="2025-05-12T00:00:00" userRating="8"/>
                """);
    }

    @Test
    public void testJsonDataSetWithLinkParameter() throws Exception {
        // given
        String reportCode = GameCriticScoresReport.CODE;
        String publisherName = "tendo"; // nintendo

        // when
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .addParam(GameCriticScoresReport.PARAM_PUBLISHER_NAME, publisherName)
                .run();

        // then
        CSVReader csvReader = readCsvContent(outputDocument);

        List<String[]> averageScores = csvReader.readAll();
        assertThat(averageScores)
                .contains(
                        new String[]{"Tetris", "5", "9.9"},
                        new String[]{"Mario Kart DS", "8", "9.1"}
                );

        csvReader.close();
    }
}
