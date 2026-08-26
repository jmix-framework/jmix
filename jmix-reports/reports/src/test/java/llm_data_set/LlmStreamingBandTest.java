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
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.Report;
import io.jmix.reports.impl.StreamingReportValidationSupport;
import io.jmix.reports.yarg.reporting.StreamingReportValidator.Violation;
import io.jmix.reports.yarg.reporting.StreamingReportValidator.ViolationType;
import llm_data_set.test_support.LlmDataSetTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A streaming band writes its rows out as they are fetched, which the LLM loader cannot do: it hands the whole
 * result of one call over at once. The validator has to say so even where the loader itself is available,
 * hence the LLM-enabled context — without it the type would be refused for merely being unknown.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportsTestConfiguration.class, LlmDataSetTestConfiguration.class})
class LlmStreamingBandTest {

    @Autowired
    protected StreamingReportValidationSupport validationSupport;

    @Autowired
    protected Metadata metadata;

    @Test
    void testLlmDataSetInAStreamingBandIsReported() {
        Report report = reportWithStreamingBand(DataSetType.LLM);

        List<Violation> violations = validationSupport.validate(report.getRootBandDefinition());

        assertThat(violations).extracting(Violation::type).contains(ViolationType.LOADER_NOT_STREAMING);
    }

    @Test
    void testTheSameBandWithAJpqlDataSetIsAccepted() {
        Report report = reportWithStreamingBand(DataSetType.JPQL);

        assertThat(validationSupport.validate(report.getRootBandDefinition())).isEmpty();
    }

    protected Report reportWithStreamingBand(DataSetType dataSetType) {
        Report report = metadata.create(Report.class);
        report.setName("Streaming report");
        report.setBands(new HashSet<>());

        BandDefinition rootBand = metadata.create(BandDefinition.class);
        rootBand.setReport(report);
        rootBand.setName("Root");
        rootBand.setOrientation(Orientation.HORIZONTAL);
        rootBand.setPosition(0);
        report.getBands().add(rootBand);

        BandDefinition dataBand = metadata.create(BandDefinition.class);
        dataBand.setReport(report);
        dataBand.setName("Data");
        dataBand.setOrientation(Orientation.HORIZONTAL);
        dataBand.setPosition(0);
        dataBand.setStreaming(true);
        dataBand.setParentBandDefinition(rootBand);
        rootBand.getChildrenBandDefinitions().add(dataBand);
        report.getBands().add(dataBand);

        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName("Data");
        dataSet.setBandDefinition(dataBand);
        dataSet.setType(dataSetType);
        dataSet.setText("Orders of this month");
        dataBand.setDataSets(List.of(dataSet));

        return report;
    }
}
