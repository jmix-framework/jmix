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

import io.jmix.core.Metadata;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.Report;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class BandDefinitionStreamingFlagTest {

    @Autowired
    protected ReportsSerialization reportsSerialization;

    @Autowired
    protected Metadata metadata;

    @Test
    void testStreamingFlagSurvivesSerializationRoundTrip() {
        Report report = metadata.create(Report.class);
        report.setName("Streaming flag report");

        BandDefinition rootBand = metadata.create(BandDefinition.class);
        rootBand.setName("Root");
        rootBand.setPosition(0);
        rootBand.setReport(report);

        BandDefinition dataBand = metadata.create(BandDefinition.class);
        dataBand.setName("Data");
        dataBand.setPosition(0);
        dataBand.setReport(report);
        dataBand.setParentBandDefinition(rootBand);
        dataBand.setStreaming(true);
        rootBand.getChildrenBandDefinitions().add(dataBand);

        Set<BandDefinition> bands = new LinkedHashSet<>();
        bands.add(rootBand);
        bands.add(dataBand);
        report.setBands(bands);

        String serialized = reportsSerialization.convertToString(report);
        Report restored = reportsSerialization.convertToReport(serialized);

        BandDefinition restoredData = restored.getBands().stream()
                .filter(band -> "Data".equals(band.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(restoredData.getStreaming()).isTrue();
        assertThat(restoredData.isStreaming()).isTrue();

        BandDefinition restoredRoot = restored.getBands().stream()
                .filter(band -> "Root".equals(band.getName()))
                .findFirst()
                .orElseThrow();
        assertThat(restoredRoot.isStreaming()).isFalse();
    }
}
