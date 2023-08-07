/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dataimport.extractor.data.impl;

import io.jmix.dataimport.InputDataFormat;
import io.jmix.dataimport.configuration.ImportConfiguration;
import io.jmix.dataimport.exception.ImportException;
import io.jmix.dataimport.extractor.data.ImportedData;
import io.jmix.dataimport.extractor.data.ImportedDataExtractor;
import io.jmix.dataimport.extractor.data.ImportedDataItem;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

@Component("datimp_ExcelDataExtractor")
public class ExcelDataExtractor implements ImportedDataExtractor {

    @Override
    public ImportedData extract(ImportConfiguration importConfiguration, InputStream inputStream) {
        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (IOException e) {
            throw new ImportException(e, "I/O error occurs during Excel data reading:" + e.getMessage());
        }
        return getImportedData(workbook);
    }

    @Override
    public ImportedData extract(ImportConfiguration importConfiguration, byte[] inputData) {
        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(new ByteArrayInputStream(inputData));
        } catch (IOException e) {
            throw new ImportException(e, "I/O error occurs during Excel data reading:" + e.getMessage());
        }
        return getImportedData(workbook);
    }

    @Override
    public String getSupportedDataFormat() {
        return InputDataFormat.XLSX;
    }

    protected ImportedData getImportedData(Workbook workbook) {
        ImportedData importedData = new ImportedData();
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.rowIterator();
        Row headerRow = rowIterator.next();
        Iterator iterator = headerRow.cellIterator();

        while (iterator.hasNext()) {
            Cell cell = (Cell) iterator.next();
            String columnName = cell.getStringCellValue();
            importedData.addDataFieldName(columnName);
        }
        List<String> columnNames = importedData.getDataFieldNames();

        DataFormatter dataFormatter = new DataFormatter();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            ImportedDataItem dataItem = new ImportedDataItem();
            dataItem.setItemIndex(row.getRowNum());
            for (int i = 0; i < columnNames.size(); i++) {
                Cell cell = row.getCell(i);
                String value = dataFormatter.formatCellValue(cell);
                dataItem.addRawValue(columnNames.get(i), value);
            }
            importedData.addItem(dataItem);
        }

        return importedData;
    }
}
