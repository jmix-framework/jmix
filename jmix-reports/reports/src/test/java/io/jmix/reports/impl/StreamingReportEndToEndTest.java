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
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.libintegration.JmixReporting;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.yarg.formatters.ReportFormatter;
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput;
import io.jmix.reports.yarg.formatters.factory.ReportFormatterFactory;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.loaders.StreamingReportDataLoader;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.ReportQuery;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public class StreamingReportEndToEndTest {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ReportRunner reportRunner;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected JmixReporting reporting;

    @Test
    void testStreamingSqlReportRendersAllRows() throws Exception {
        jdbcTemplate.execute("create table if not exists STREAM_SRC (id int, val varchar(32))");
        jdbcTemplate.update("delete from STREAM_SRC");
        for (int i = 0; i < 500; i++) {
            jdbcTemplate.update("insert into STREAM_SRC values (?, ?)", i, "v" + i);
        }

        Report report = buildStreamingReport(
                "select id as \"num\", val as \"txt\" from STREAM_SRC order by id", DataSetType.SQL);

        ReportOutputDocument document = reportRunner.byReportEntity(report).run();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(document.getContent()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getNumericCellValue()).isEqualTo(0);
            assertThat(sheet.getRow(499).getCell(0).getNumericCellValue()).isEqualTo(499);
            assertThat(sheet.getRow(499).getCell(1).getStringCellValue()).isEqualTo("v499");
            assertThat(sheet.getLastRowNum()).isEqualTo(499);
        }
    }

    @Test
    void testOutputNamePatternReferencingStreamingBand() throws Exception {
        jdbcTemplate.execute("create table if not exists STREAM_SRC (id int, val varchar(32))");
        jdbcTemplate.update("delete from STREAM_SRC");
        jdbcTemplate.update("insert into STREAM_SRC values (?, ?)", 1, "first");
        jdbcTemplate.update("insert into STREAM_SRC values (?, ?)", 2, "second");

        Report report = buildStreamingReport(
                "select id as \"num\", val as \"txt\" from STREAM_SRC order by id", DataSetType.SQL);
        report.getDefaultTemplate().setOutputNamePattern("${Data.txt}");

        ReportOutputDocument document = reportRunner.byReportEntity(report).run();

        assertThat(document.getDocumentName()).isEqualTo("first.xlsx");
    }

    @Test
    void testNonStreamingTemplateFallsBackToMaterializedRendering() throws Exception {
        jdbcTemplate.execute("create table if not exists STREAM_SRC (id int, val varchar(32))");
        jdbcTemplate.update("delete from STREAM_SRC");
        for (int i = 0; i < 3; i++) {
            jdbcTemplate.update("insert into STREAM_SRC values (?, ?)", i, "v" + i);
        }

        Report report = buildStreamingReport(
                "select id as \"num\", val as \"txt\" from STREAM_SRC order by id", DataSetType.SQL);
        // xlsm resolves to the non-streaming XlsxFormatter; the report must render anyway.
        report.getDefaultTemplate().setName("streaming.xlsm");

        ReportOutputDocument document = reportRunner.byReportEntity(report).run();

        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(document.getContent()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("v0");
            assertThat(sheet.getRow(2).getCell(1).getStringCellValue()).isEqualTo("v2");
        }
    }

    @Test
    void testStreamingBandWithGroovyLoaderIsRejected() throws Exception {
        Report report = buildStreamingReport("return [[num: 1, txt: 'x']]", DataSetType.GROOVY);

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> reportRunner.byReportEntity(report).run());
        // Pin the loader-specific violation, not just the shared "streaming XLSX engine" prefix.
        assertThat(e.getMessage().toLowerCase()).contains("sql or jpql");
    }

    @Test
    void testNestedStreamingBandIsRejected() throws Exception {
        jdbcTemplate.execute("create table if not exists STREAM_SRC (id int, val varchar(32))");
        jdbcTemplate.update("delete from STREAM_SRC");
        jdbcTemplate.update("insert into STREAM_SRC values (?, ?)", 1, "x");

        Report report = buildStreamingReport(
                "select id as \"num\", val as \"txt\" from STREAM_SRC order by id", DataSetType.SQL);
        // Re-parent the streaming band under an intermediate band: Root -> Middle -> Data(streaming).
        BandDefinition rootBand = report.getBands().stream()
                .filter(b -> b.getParentBandDefinition() == null).findFirst().orElseThrow();
        BandDefinition dataBand = report.getBands().stream()
                .filter(b -> Boolean.TRUE.equals(b.getStreaming())).findFirst().orElseThrow();

        BandDefinition middleBand = metadata.create(BandDefinition.class);
        middleBand.setReport(report);
        middleBand.setName("Middle");
        middleBand.setOrientation(Orientation.HORIZONTAL);
        middleBand.setPosition(0);
        middleBand.setParentBandDefinition(rootBand);

        rootBand.getChildrenBandDefinitions().remove(dataBand);
        rootBand.getChildrenBandDefinitions().add(middleBand);
        dataBand.setParentBandDefinition(middleBand);
        middleBand.setChildrenBandDefinitions(new java.util.ArrayList<>(List.of(dataBand)));
        report.setBands(Set.of(rootBand, middleBand, dataBand));

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> reportRunner.byReportEntity(report).run());
        assertThat(e.getMessage().toLowerCase()).contains("first-level");
    }

    @Test
    void testFactoryReturningNonStreamingFormatterFallsBackToMaterializedRendering() throws Exception {
        jdbcTemplate.execute("create table if not exists STREAM_SRC (id int, val varchar(32))");
        jdbcTemplate.update("delete from STREAM_SRC");
        for (int i = 0; i < 3; i++) {
            jdbcTemplate.update("insert into STREAM_SRC values (?, ?)", i, "v" + i);
        }
        Report report = buildStreamingReport(
                "select id as \"num\", val as \"txt\" from STREAM_SRC order by id", DataSetType.SQL);

        ReportFormatterFactory original =
                (ReportFormatterFactory) ReflectionTestUtils.getField(reporting, "formatterFactory");
        // Claims streaming support but always builds the non-streaming formatter (simulates an
        // application that replaced the xlsx creator with a customized XlsxFormatter).
        ReportFormatterFactory nonStreaming = new ReportFormatterFactory() {
            @Override
            public ReportFormatter createFormatter(FormatterFactoryInput input) {
                return original.createFormatter(new FormatterFactoryInput(
                        input.getTemplateExtension(), input.getRootBand(), input.getReportTemplate(),
                        input.getOutputType(), input.getOutputStream()));
            }

            @Override
            public boolean supportsStreaming(String templateExtension) {
                return true;
            }
        };
        ReflectionTestUtils.setField(reporting, "formatterFactory", nonStreaming);
        try {
            ReportOutputDocument document = reportRunner.byReportEntity(report).run();
            try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(document.getContent()))) {
                Sheet sheet = workbook.getSheetAt(0);
                assertThat(sheet.getRow(2).getCell(1).getStringCellValue()).isEqualTo("v2");
            }
        } finally {
            ReflectionTestUtils.setField(reporting, "formatterFactory", original);
        }
    }

    @Test
    void testLoaderFailureAfterSuccessfulRenderPropagatesOriginalException() throws Exception {
        jdbcTemplate.execute("create table if not exists STREAM_SRC (id int, val varchar(32))");
        jdbcTemplate.update("delete from STREAM_SRC");
        jdbcTemplate.update("insert into STREAM_SRC values (?, ?)", 1, "x");
        Report report = buildStreamingReport(
                "select id as \"num\", val as \"txt\" from STREAM_SRC order by id", DataSetType.SQL);

        ReportLoaderFactory original =
                (ReportLoaderFactory) ReflectionTestUtils.getField(reporting, "loaderFactory");
        ReportLoaderFactory failing = loaderType -> {
            ReportDataLoader real = original.createDataLoader(loaderType);
            if (!(real instanceof StreamingReportDataLoader streamingReal)) {
                return real;
            }
            return new FailingAfterCallbackLoader(streamingReal);
        };
        ReflectionTestUtils.setField(reporting, "loaderFactory", failing);
        try {
            IllegalStateException e = assertThrows(IllegalStateException.class,
                    () -> reportRunner.byReportEntity(report).run());
            assertThat(e.getMessage()).isEqualTo("post-callback teardown failure");
        } finally {
            ReflectionTestUtils.setField(reporting, "loaderFactory", original);
        }
    }

    /** Delegates the streaming load, then fails in "teardown" after the callback succeeded. */
    protected static class FailingAfterCallbackLoader implements ReportDataLoader, StreamingReportDataLoader {

        protected final StreamingReportDataLoader delegate;

        protected FailingAfterCallbackLoader(StreamingReportDataLoader delegate) {
            this.delegate = delegate;
        }

        @Override
        public List<Map<String, Object>> loadData(ReportQuery query, BandData parentBand,
                                                  Map<String, Object> params) {
            return ((ReportDataLoader) delegate).loadData(query, parentBand, params);
        }

        @Override
        public <T> T loadDataStreaming(ReportQuery query, BandData parentBand,
                                       Map<String, Object> params,
                                       Function<Iterator<Map<String, Object>>, T> work) {
            delegate.loadDataStreaming(query, parentBand, params, work);
            throw new IllegalStateException("post-callback teardown failure");
        }
    }

    @Test
    void testSecondStreamingBandIsRejectedAtRuntime() throws Exception {
        jdbcTemplate.execute("create table if not exists STREAM_SRC (id int, val varchar(32))");
        jdbcTemplate.update("delete from STREAM_SRC");
        jdbcTemplate.update("insert into STREAM_SRC values (?, ?)", 1, "x");

        Report report = buildStreamingReport(
                "select id as \"num\", val as \"txt\" from STREAM_SRC order by id", DataSetType.SQL);
        BandDefinition rootBand = report.getBands().stream()
                .filter(b -> b.getParentBandDefinition() == null).findFirst().orElseThrow();
        BandDefinition dataBand = report.getBands().stream()
                .filter(b -> "Data".equals(b.getName())).findFirst().orElseThrow();
        BandDefinition second = metadata.create(BandDefinition.class);
        second.setReport(report);
        second.setName("Data2");
        second.setOrientation(Orientation.HORIZONTAL);
        second.setPosition(1);
        second.setParentBandDefinition(rootBand);
        second.setStreaming(true);
        DataSet secondDataSet = metadata.create(DataSet.class);
        secondDataSet.setName("Data2");
        secondDataSet.setType(DataSetType.SQL);
        secondDataSet.setText("select id as \"num\" from STREAM_SRC");
        second.setDataSets(List.of(secondDataSet));
        rootBand.getChildrenBandDefinitions().add(second);
        report.setBands(Set.of(rootBand, dataBand, second));

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> reportRunner.byReportEntity(report).run());
        assertThat(e.getMessage()).contains("only one streaming band");
    }

    protected Report buildStreamingReport(String script, DataSetType loaderType) throws Exception {
        Report report = metadata.create(Report.class);
        report.setName("Streaming e2e " + loaderType);

        BandDefinition rootBand = metadata.create(BandDefinition.class);
        rootBand.setReport(report);
        rootBand.setName("Root");
        rootBand.setOrientation(Orientation.HORIZONTAL);
        rootBand.setPosition(0);

        BandDefinition dataBand = metadata.create(BandDefinition.class);
        dataBand.setReport(report);
        dataBand.setName("Data");
        dataBand.setOrientation(Orientation.HORIZONTAL);
        dataBand.setPosition(0);
        dataBand.setParentBandDefinition(rootBand);
        dataBand.setStreaming(true);
        rootBand.getChildrenBandDefinitions().add(dataBand);

        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName("Data");
        dataSet.setType(loaderType);
        dataSet.setText(script);
        dataBand.setDataSets(List.of(dataSet));

        report.setBands(Set.of(rootBand, dataBand));

        ReportTemplate template = metadata.create(ReportTemplate.class);
        template.setReport(report);
        template.setCode("default");
        template.setReportOutputType(ReportOutputType.XLSX);
        template.setName("streaming.xlsx");
        template.setContent(buildXlsxTemplate());
        report.setTemplates(List.of(template));
        report.setDefaultTemplate(template);
        return report;
    }

    /** One-row template: {@code ${num} | ${txt}} covered by named range "Data"; calcPr injected. */
    protected byte[] buildXlsxTemplate() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Sheet1");
            var row = sheet.createRow(0);
            row.createCell(0).setCellValue("${num}");
            row.createCell(1).setCellValue("${txt}");
            var name = workbook.createName();
            name.setNameName("Data");
            name.setRefersToFormula("'Sheet1'!$A$1:$B$1");
            var bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return injectCalcPr(bos.toByteArray());
        }
    }

    protected static byte[] injectCalcPr(byte[] xlsx) throws Exception {
        var zis = new ZipInputStream(new ByteArrayInputStream(xlsx));
        var bos = new ByteArrayOutputStream();
        var zos = new ZipOutputStream(bos);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            byte[] content = zis.readAllBytes();
            if (entry.getName().equals("xl/workbook.xml")) {
                content = new String(content, StandardCharsets.UTF_8)
                        .replace("</workbook>", "<calcPr/></workbook>")
                        .getBytes(StandardCharsets.UTF_8);
            }
            zos.putNextEntry(new ZipEntry(entry.getName()));
            zos.write(content);
            zos.closeEntry();
        }
        zis.close();
        zos.close();
        return bos.toByteArray();
    }
}
