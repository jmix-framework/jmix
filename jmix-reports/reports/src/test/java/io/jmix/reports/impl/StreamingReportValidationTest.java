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
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.Report;
import io.jmix.reports.yarg.reporting.StreamingReportValidator.Violation;
import io.jmix.reports.yarg.reporting.StreamingReportValidator.ViolationType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class StreamingReportValidationTest {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected StreamingReportValidationSupport validationSupport;

    @Test
    void testValidStreamingReportHasNoViolations() {
        Report report = report();
        addStreamingBand(report, "Data", DataSetType.SQL);

        assertThat(validationSupport.validate(rootBand(report))).isEmpty();
    }

    @Test
    void testReportWithoutStreamingBandsIsNotValidated() {
        Report report = report();
        BandDefinition vertical = addBand(report, "Side", DataSetType.SQL);
        vertical.setOrientation(Orientation.VERTICAL);

        assertThat(validationSupport.validate(rootBand(report))).isEmpty();
    }

    @Test
    void testSecondStreamingBandIsReported() {
        Report report = report();
        addStreamingBand(report, "Data", DataSetType.SQL);
        addStreamingBand(report, "Data2", DataSetType.SQL);

        List<Violation> violations = validationSupport.validate(rootBand(report));
        assertThat(violations).extracting(Violation::type).contains(ViolationType.MULTIPLE_STREAMING_BANDS);
    }

    @Test
    void testVerticalSiblingBandIsReported() {
        Report report = report();
        addStreamingBand(report, "Data", DataSetType.SQL);
        BandDefinition vertical = addBand(report, "Side", DataSetType.SQL);
        vertical.setOrientation(Orientation.VERTICAL);

        List<Violation> violations = validationSupport.validate(rootBand(report));
        assertThat(violations).extracting(Violation::type)
                .contains(ViolationType.NON_HORIZONTAL_BAND_IN_REPORT);
    }

    @Test
    void testGroovyLoaderIsReported() {
        Report report = report();
        addStreamingBand(report, "Data", DataSetType.GROOVY);

        List<Violation> violations = validationSupport.validate(rootBand(report));
        assertThat(violations).extracting(Violation::type).contains(ViolationType.LOADER_NOT_STREAMING);
    }

    @Test
    void testDatasetWithoutTypeIsReported() {
        Report report = report();
        addStreamingBand(report, "Data", null);

        List<Violation> violations = validationSupport.validate(rootBand(report));
        assertThat(violations).extracting(Violation::type).contains(ViolationType.LOADER_NOT_STREAMING);
    }

    @Test
    void testNestedStreamingBandIsReported() {
        Report report = report();
        BandDefinition middle = addBand(report, "Middle", DataSetType.SQL);
        BandDefinition nested = addStreamingBand(report, "Data", DataSetType.SQL);
        rootBand(report).getChildrenBandDefinitions().remove(nested);
        nested.setParentBandDefinition(middle);
        middle.getChildrenBandDefinitions().add(nested);

        List<Violation> violations = validationSupport.validate(rootBand(report));
        assertThat(violations).extracting(Violation::type).contains(ViolationType.NOT_FIRST_LEVEL);
    }

    @Test
    void testVerticalStreamingBandIsReported() {
        Report report = report();
        BandDefinition streaming = addStreamingBand(report, "Data", DataSetType.SQL);
        streaming.setOrientation(Orientation.VERTICAL);

        List<Violation> violations = validationSupport.validate(rootBand(report));
        assertThat(violations).extracting(Violation::type).contains(ViolationType.NOT_HORIZONTAL);
    }

    @Test
    void testStreamingBandWithChildBandIsReported() {
        Report report = report();
        BandDefinition streaming = addStreamingBand(report, "Data", DataSetType.SQL);
        BandDefinition child = addBand(report, "Child", DataSetType.SQL);
        rootBand(report).getChildrenBandDefinitions().remove(child);
        child.setParentBandDefinition(streaming);
        streaming.getChildrenBandDefinitions().add(child);

        List<Violation> violations = validationSupport.validate(rootBand(report));
        assertThat(violations).extracting(Violation::type).contains(ViolationType.HAS_CHILDREN);
    }

    @Test
    void testStreamingBandWithoutDatasetIsReported() {
        Report report = report();
        BandDefinition streaming = addStreamingBand(report, "Data", DataSetType.SQL);
        streaming.setDataSets(List.of());

        List<Violation> violations = validationSupport.validate(rootBand(report));
        assertThat(violations).extracting(Violation::type).contains(ViolationType.NOT_SINGLE_QUERY);
    }

    @Test
    void testStreamingBandWithMultipleDatasetsIsReported() {
        Report report = report();
        BandDefinition streaming = addStreamingBand(report, "Data", DataSetType.SQL);
        DataSet second = metadata.create(DataSet.class);
        second.setName("Data2");
        second.setType(DataSetType.SQL);
        second.setText("select 2");
        streaming.setDataSets(List.of(streaming.getDataSets().get(0), second));

        List<Violation> violations = validationSupport.validate(rootBand(report));
        assertThat(violations).extracting(Violation::type).contains(ViolationType.NOT_SINGLE_QUERY);
    }

    protected Report report() {
        Report report = metadata.create(Report.class);
        report.setName("validation");
        BandDefinition root = metadata.create(BandDefinition.class);
        root.setReport(report);
        root.setName("Root");
        root.setOrientation(Orientation.HORIZONTAL);
        root.setPosition(0);
        report.setBands(new HashSet<>(Set.of(root)));
        return report;
    }

    protected BandDefinition rootBand(Report report) {
        return report.getBands().stream()
                .filter(b -> b.getParentBandDefinition() == null).findFirst().orElseThrow();
    }

    protected BandDefinition addBand(Report report, String name, DataSetType type) {
        BandDefinition root = rootBand(report);
        BandDefinition band = metadata.create(BandDefinition.class);
        band.setReport(report);
        band.setName(name);
        band.setOrientation(Orientation.HORIZONTAL);
        band.setPosition(0);
        band.setParentBandDefinition(root);
        root.getChildrenBandDefinitions().add(band);
        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName(name);
        dataSet.setType(type);
        dataSet.setText("select 1");
        band.setDataSets(List.of(dataSet));
        report.getBands().add(band);
        return band;
    }

    protected BandDefinition addStreamingBand(Report report, String name, DataSetType type) {
        BandDefinition band = addBand(report, name, type);
        band.setStreaming(true);
        return band;
    }
}
