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
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.impl.AnnotatedReportHolder;
import io.jmix.reports.impl.AnnotatedReportScanner;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.test_support.entity.TestDataInitializer;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class})
public abstract class BaseAnnotatedReportExecutionTest {

    @Autowired
    protected ReportRunner reportRunner;

    @Autowired
    protected UnconstrainedDataManager unconstrainedDataManager;

    @BeforeAll
    public static void setup(@Autowired AnnotatedReportScanner annotatedReportScanner,
                             @Autowired AnnotatedReportHolder annotatedReportHolder, @Autowired TestDataInitializer testDataInitializer) {
        // For some reason application startup events are not fired in tests
        // So I manually invoke on-startup logic
        importAnnotatedReports(annotatedReportHolder, annotatedReportScanner);

        testDataInitializer.init();
    }

    private static void importAnnotatedReports(AnnotatedReportHolder annotatedReportHolder, AnnotatedReportScanner annotatedReportScanner) {
        if (annotatedReportHolder.getAllReports().isEmpty()) {
            annotatedReportScanner.importGroupDefinitions();
            annotatedReportScanner.importReportDefinitions();
        }
    }

    protected Sheet readFirstSheetFromBytes(byte[] bytes) {
        org.apache.poi.ss.usermodel.Workbook workbook;
        try {
            workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return workbook.getSheetAt(0);
    }

    protected String stringCellValue(Sheet sheet, int row, int column) {
        Cell cell = sheet.getRow(row).getCell(column);
        return cell.getStringCellValue();
    }

    protected Object cellValue(Sheet sheet, int row, int column) {
        Cell cell = sheet.getRow(row).getCell(column);
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return cell.getStringCellValue();
    }

    protected CSVReader readCsvContent(ReportOutputDocument outputDocument) {
        return new CSVReader(new InputStreamReader(
                new ByteArrayInputStream(outputDocument.getContent()), StandardCharsets.UTF_8));
    }

}
