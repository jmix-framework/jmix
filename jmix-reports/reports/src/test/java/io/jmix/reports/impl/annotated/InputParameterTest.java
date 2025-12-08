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
import io.jmix.reports.exception.ReportingException;
import io.jmix.reports.test_support.report.GameCriticScoresReport;
import io.jmix.reports.test_support.report.UserProfileReport;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InputParameterTest extends BaseAnnotatedReportExecutionTest {

    @Test
    public void testMissingRequiredParameter() {
        // given
        String reportCode = UserProfileReport.CODE; // it has 1 required parameter

        // when + then
        assertThatThrownBy(() -> reportRunner.byReportCode(reportCode)
                .run())
                .isInstanceOf(ReportingException.class)
                .hasMessageContaining("Required report parameter")
                .hasMessageContaining("not found");

    }

    @Test
    public void testDefaultValueProvider() throws Exception {
        // given
        String reportCode = GameCriticScoresReport.CODE;

        // when
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                // with default parameter value
                .run();

        // then
        CSVReader csvReader = readCsvContent(outputDocument);

        List<String> titles = csvReader.readAll().stream().map(array -> array[0]).toList();
        assertThat(titles).hasSize(1 + 2);
        assertThat(titles).contains("Modern Warfare 3", "Destiny");

        csvReader.close();
    }

    @Test
    public void testTransformer() throws Exception {
        // given
        String reportCode = GameCriticScoresReport.CODE;
        String publisherName = "tendo"; // nintendo

        // when
        ReportOutputDocument outputDocument = reportRunner.byReportCode(reportCode)
                .addParam(GameCriticScoresReport.PARAM_PUBLISHER_NAME, publisherName)
                .run();

        // then
        CSVReader csvReader = readCsvContent(outputDocument);

        List<String> firstValues = csvReader.readAll().stream().map(array -> array[0]).toList();
        assertThat(firstValues)
                .contains("Tetris", "Mario Kart DS");

        csvReader.close();
    }
}