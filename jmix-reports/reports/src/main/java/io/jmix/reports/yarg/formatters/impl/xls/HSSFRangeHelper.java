/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package io.jmix.reports.yarg.formatters.impl.xls;

import org.apache.poi.hssf.usermodel.HSSFName;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;

public final class HSSFRangeHelper {
    private HSSFRangeHelper() {
    }

    public static boolean isMergeRegionInsideNamedRange(Integer rangeFirstRow, Integer rangeFirstColumn, Integer rangeLastRow, Integer rangeLastColumn, Integer regionFirstRow, Integer regionFirstColumn, Integer regionLastRow, Integer regionLastColumn) {
        return regionFirstColumn >= rangeFirstColumn && regionFirstColumn <= rangeLastColumn &&
                regionLastColumn >= rangeFirstColumn && regionLastColumn <= rangeLastColumn &&
                regionFirstRow >= rangeFirstRow && regionFirstRow <= rangeLastRow &&
                regionLastRow >= rangeFirstRow && regionLastRow <= rangeLastRow;
    }

    public static boolean isNamedRangeInsideMergeRegion(Integer rangeFirstRow, Integer rangeFirstColumn, Integer rangeLastRow, Integer rangeLastColumn, Integer regionFirstRow, Integer regionFirstColumn, Integer regionLastRow, Integer regionLastColumn) {
        return regionFirstColumn <= rangeFirstColumn && regionFirstColumn <= rangeLastColumn &&
                regionLastColumn >= rangeFirstColumn && regionLastColumn >= rangeLastColumn &&
                regionFirstRow <= rangeFirstRow && regionFirstRow <= rangeLastRow &&
                regionLastRow >= rangeFirstRow && regionLastRow >= rangeLastRow;
    }

    public static CellReference[] getRangeContent(HSSFWorkbook workbook, String rangeName) {
        AreaReference areaForRange = getAreaForRange(workbook, rangeName);
        if (areaForRange == null) {
            return null;
        }

        return areaForRange.getAllReferencedCells();
    }

    public static AreaReference getAreaForRange(HSSFWorkbook workbook, String rangeName) {
        int rangeNameIdx = workbook.getNameIndex(rangeName);
        if (rangeNameIdx == -1) return null;

        HSSFName aNamedRange = workbook.getNameAt(rangeNameIdx);
        return new AreaReference(aNamedRange.getRefersToFormula(), SpreadsheetVersion.EXCEL97);
    }

    public static HSSFSheet getTemplateSheetForRangeName(HSSFWorkbook workbook, String rangeName) {
        int rangeNameIdx = workbook.getNameIndex(rangeName);
        if (rangeNameIdx == -1) return null;

        HSSFName aNamedRange = workbook.getNameAt(rangeNameIdx);
        String sheetName = aNamedRange.getSheetName();
        return workbook.getSheet(sheetName);
    }
}
