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

package llm_data_set;

import io.jmix.core.Metadata;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.Report;
import io.jmix.reports.yarg.structure.BandOrientation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class LlmDataSetSerializationTest {

    protected static final String PROMPT = "Orders placed this month with their amounts";

    protected static final String GENERATED_QUERY = """
            {"jpql":"select o.number as orderNumber from sales_Order o where o.date >= :dateFrom",\
            "resultProperties":["orderNumber"],\
            "parameters":[{"name":"dateFrom","javaType":"java.time.LocalDate"}],\
            "explanation":"Orders from the given date",\
            "warnings":[]}""";

    @Autowired
    protected ReportsSerialization reportsSerialization;

    @Autowired
    protected Metadata metadata;

    @Test
    void testLlmPropertiesSurviveSerializationRoundTrip() {
        Report report = createReportWithLlmDataSet();

        Report restored = reportsSerialization.convertToReport(reportsSerialization.convertToString(report));

        DataSet restoredDataSet = findDataDataSet(restored);
        assertThat(restoredDataSet.getType()).isEqualTo(DataSetType.LLM);
        assertThat(restoredDataSet.getText()).isEqualTo(PROMPT);
        assertThat(restoredDataSet.getLlmGeneratedQuery()).isEqualTo(GENERATED_QUERY);
    }

    @Test
    void testLlmSettingsArePublishedAsAdditionalParams() {
        DataSet dataSet = findDataDataSet(createReportWithLlmDataSet());

        // The loader is handed a query and the params of the run, and it tells its own band's values from
        // another band's by these two: they have to reach it alongside the stored query.
        assertThat(dataSet.getAdditionalParams())
                .containsEntry(DataSet.LLM_GENERATED_QUERY, GENERATED_QUERY)
                .containsEntry(DataSet.BAND_NAME, "Data")
                .containsEntry(DataSet.BAND_ORIENTATION, BandOrientation.HORIZONTAL);
    }

    @Test
    void testDataSetWithoutABandPublishesNoBandOfItsOwn() {
        // A data set is created before it is attached to a band, and the designer asks it for its params
        // meanwhile.
        DataSet detached = metadata.create(DataSet.class);
        detached.setType(DataSetType.LLM);

        assertThat(detached.getAdditionalParams())
                .containsEntry(DataSet.BAND_NAME, null)
                .containsEntry(DataSet.BAND_ORIENTATION, null);
    }

    protected Report createReportWithLlmDataSet() {
        Report report = metadata.create(Report.class);
        report.setName("LLM data set report");

        BandDefinition rootBand = metadata.create(BandDefinition.class);
        rootBand.setName("Root");
        rootBand.setPosition(0);
        rootBand.setReport(report);

        BandDefinition dataBand = metadata.create(BandDefinition.class);
        dataBand.setName("Data");
        dataBand.setPosition(0);
        dataBand.setOrientation(Orientation.HORIZONTAL);
        dataBand.setReport(report);
        dataBand.setParentBandDefinition(rootBand);
        rootBand.getChildrenBandDefinitions().add(dataBand);

        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName("Data");
        dataSet.setBandDefinition(dataBand);
        dataSet.setType(DataSetType.LLM);
        dataSet.setText(PROMPT);
        dataSet.setLlmGeneratedQuery(GENERATED_QUERY);
        dataBand.setDataSets(List.of(dataSet));

        Set<BandDefinition> bands = new LinkedHashSet<>();
        bands.add(rootBand);
        bands.add(dataBand);
        report.setBands(bands);
        return report;
    }

    protected DataSet findDataDataSet(Report report) {
        return report.getBands().stream()
                .filter(band -> "Data".equals(band.getName()))
                .findFirst()
                .orElseThrow()
                .getDataSets()
                .get(0);
    }
}
