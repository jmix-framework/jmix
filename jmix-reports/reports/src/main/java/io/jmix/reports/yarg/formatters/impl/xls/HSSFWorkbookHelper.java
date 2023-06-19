/*
 * Copyright 2014 Haulmont
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

import io.jmix.reports.yarg.formatters.impl.xls.caches.XslStyleHelper;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class HSSFWorkbookHelper {

    protected static final Constructor<HSSFCellStyle> styleConstructor = getHssfCellStyleConstructor();

    public static ExtendedFormatRecord createExtendedFormat() {
        // CAUTION copied from org.apache.poi.hssf.model.InternalWorkbook#createExtendedFormat

        ExtendedFormatRecord retval = new ExtendedFormatRecord();

        retval.setFontIndex((short) 0);
        retval.setFormatIndex((short) 0x0);
        retval.setCellOptions((short) 0x1);
        retval.setAlignmentOptions((short) 0x20);
        retval.setIndentionOptions((short) 0);
        retval.setBorderOptions((short) 0);
        retval.setPaletteOptions((short) 0);
        retval.setAdtlPaletteOptions((short) 0);
        retval.setFillPaletteOptions((short) 0x20c0);
        retval.setTopBorderPaletteIdx(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        retval.setBottomBorderPaletteIdx(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        retval.setLeftBorderPaletteIdx(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        retval.setRightBorderPaletteIdx(HSSFColor.HSSFColorPredefined.BLACK.getIndex());
        return retval;
    }

    public static HSSFCellStyle createDetachedCellStyle(HSSFWorkbook workbook) {
        ExtendedFormatRecord xfr = createExtendedFormat();

        HSSFCellStyle cellStyle;
        try {
            cellStyle = styleConstructor.newInstance((short) 0, xfr, workbook);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Unable to create HSSFCellStyle");
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to create HSSFCellStyle");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to create HSSFCellStyle");
        }

        return cellStyle;
    }

    protected static Constructor<HSSFCellStyle> getHssfCellStyleConstructor() {
        Constructor<HSSFCellStyle> styleConstructor;
        try {
            styleConstructor = HSSFCellStyle.class.getDeclaredConstructor(
                    short.class,
                    ExtendedFormatRecord.class,
                    HSSFWorkbook.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find HSSFCellStyle constructor");
        }

        styleConstructor.setAccessible(true);
        return styleConstructor;
    }

    public static HSSFCellStyle adoptDetachedCellStyle(HSSFWorkbook workbook, HSSFCellStyle detachedCellStyle) {
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        XslStyleHelper.cloneStyleRelations(detachedCellStyle, cellStyle);
        return cellStyle;
    }
}