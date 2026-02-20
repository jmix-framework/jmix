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

package reportsrest.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.core.DataManager;
import io.jmix.core.Resources;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.reports.ReportImportExport;
import io.jmix.reports.entity.Report;
import io.jmix.reports.impl.AnnotatedReportHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reportsrest.controller.ReportRestController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.ReportsRestTestConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = ReportsRestTestConfiguration.class)
@ExtendWith({SpringExtension.class})
public class ReportRestControllerTest {

    static final String FIXTURE_PATH = "classpath:test_support/rt-test-1.zip";
    static final String REPORT_CODE = "rt-test-1";
    static final String DESIGN_TIME_REPORT_CODE = "dt-test-1";

    @Autowired
    ReportRestController reportRestController;
    @Autowired
    ReportImportExport reportImportExport;
    @Autowired
    DataManager dataManager;
    @Autowired
    Resources resources;
    @Autowired
    SystemAuthenticator systemAuthenticator;
    @Autowired
    AnnotatedReportScanner annotatedReportScanner;
    @Autowired
    AnnotatedReportHolder annotatedReportHolder;

    ObjectMapper objectMapper = new ObjectMapper();
    UUID reportId;
    UUID designTimeReportId;

    @BeforeEach
    void setUp() throws IOException {
        systemAuthenticator.begin();

        String zipPath = resources.getResource(FIXTURE_PATH).getFile().getAbsolutePath();
        reportImportExport.importReports(zipPath);
        annotatedReportHolder.clear();
        annotatedReportScanner.importReportDefinitions();

        Report report = dataManager.load(Report.class)
                .query("select r from report_Report r where r.code = :code")
                .parameter("code", REPORT_CODE)
                .one();
        reportId = report.getId();

        Report designTimeReport = annotatedReportHolder.getByCode(DESIGN_TIME_REPORT_CODE);
        assertNotNull(designTimeReport);
        designTimeReportId = designTimeReport.getId();
    }

    @AfterEach
    void tearDown() {
        List<Report> importedReports = dataManager.load(Report.class)
                .query("select r from report_Report r where r.code = :code")
                .parameter("code", REPORT_CODE)
                .list();
        dataManager.remove(importedReports);
        annotatedReportHolder.clear();

        systemAuthenticator.end();
    }

    @Test
    void testLoadReportsListReturnsImportedReport() throws Exception {
        String body = reportRestController.loadReportsList();

        JsonNode json = objectMapper.readTree(body);
        JsonNode reportJson = findReportByCode(json);
        assertNotNull(reportJson);
        assertEquals(reportId.toString(), reportJson.get("id").asText());
        assertEquals(REPORT_CODE, reportJson.get("code").asText());
    }

    @Test
    void testLoadReportReturnsImportedReportDetails() throws Exception {
        String body = reportRestController.loadReport(reportId.toString());

        JsonNode json = objectMapper.readTree(body);
        assertEquals(reportId.toString(), json.get("id").asText());
        assertEquals(REPORT_CODE, json.get("code").asText());
        assertEquals(REPORT_CODE, json.get("name").asText());
        assertTrue(json.get("templates").isArray());
        JsonNode defaultTemplate = findDefaultTemplate(json.get("templates"));
        assertNotNull(defaultTemplate);
        assertEquals("CSV", defaultTemplate.get("outputType").asText());
    }

    @Test
    void testRunReportReturnsCsvContentAndHeaders() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        reportRestController.runReport(reportId.toString(), "{}", response);

        String contentDisposition = response.getHeader("Content-Disposition");
        String contentType = response.getHeader("Content-Type");

        assertEquals("no-cache", response.getHeader("Cache-Control"));
        assertNotNull(contentDisposition);
        assertNotNull(contentType);
        assertTrue(contentDisposition.startsWith("inline;"));
        assertTrue(contentDisposition.contains("filename=\"rt-test-1.csv\""));
        assertTrue(contentType.contains("application/csv"));
        assertTrue(response.getContentAsString().contains("val1"));
        assertTrue(response.getContentAsString().contains("val2"));
    }

    @Test
    void testRunReportWithAttachmentTrueSetsAttachmentDisposition() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        reportRestController.runReport(reportId.toString(), "{\"attachment\":true}", response);

        String contentDisposition = response.getHeader("Content-Disposition");
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.startsWith("attachment;"));
    }

    @Test
    void testLoadReportsListReturnsDesignTimeReport() throws Exception {
        String body = reportRestController.loadReportsList();
        JsonNode json = objectMapper.readTree(body);
        JsonNode reportJson = findReportByCode(json, DESIGN_TIME_REPORT_CODE);
        assertNotNull(reportJson);
        assertEquals(designTimeReportId.toString(), reportJson.get("id").asText());
    }

    @Test
    void testLoadReportReturnsDesignTimeReportDetails() throws Exception {
        String body = reportRestController.loadReport(designTimeReportId.toString());

        JsonNode json = objectMapper.readTree(body);
        assertEquals(designTimeReportId.toString(), json.get("id").asText());
        assertEquals(DESIGN_TIME_REPORT_CODE, json.get("code").asText());
        assertEquals("Test Report", json.get("name").asText());
        JsonNode defaultTemplate = findDefaultTemplate(json.get("templates"));
        assertNotNull(defaultTemplate);
        assertEquals("CSV", defaultTemplate.get("outputType").asText());
    }

    @Test
    void testRunDesignTimeReportReturnsCsvContentAndHeaders() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        reportRestController.runReport(designTimeReportId.toString(), "{}", response);

        String contentDisposition = response.getHeader("Content-Disposition");
        String contentType = response.getHeader("Content-Type");

        assertEquals("no-cache", response.getHeader("Cache-Control"));
        assertNotNull(contentDisposition);
        assertNotNull(contentType);
        assertTrue(contentDisposition.startsWith("inline;"));
        assertTrue(contentDisposition.contains("filename=\"dt-test-1.csv\""));
        assertTrue(contentType.contains("application/csv"));
        assertTrue(response.getContentAsString().contains("value1"));
        assertTrue(response.getContentAsString().contains("value2"));
    }

    JsonNode findReportByCode(JsonNode reports) {
        return findReportByCode(reports, REPORT_CODE);
    }

    JsonNode findReportByCode(JsonNode reports, String reportCode) {
        for (JsonNode report : reports) {
            if (reportCode.equals(report.get("code").asText())) {
                return report;
            }
        }
        return null;
    }

    JsonNode findDefaultTemplate(JsonNode templates) {
        for (JsonNode template : templates) {
            if ("DEFAULT".equals(template.get("code").asText())) {
                return template;
            }
        }
        return null;
    }
}
