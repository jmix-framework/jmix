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

package io.jmix.reports.yarg.formatters.impl.xls.caches;

import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static java.lang.String.format;

public class XslStyleHelper {

    private XslStyleHelper() {
    }

    public static ExtendedFormatRecord getFormatFromStyle(HSSFCellStyle style) {
        return XslStyleHelper.getFieldValue(style, "_format");
    }

    public static InternalWorkbook getWorkbookFromStyle(HSSFCellStyle style) {
        return XslStyleHelper.getFieldValue(style, "_workbook");
    }

    public static void cloneStyleRelations(HSSFCellStyle source, HSSFCellStyle target) {
        //First we need to clone the extended format record
        getFormatFromStyle(target).cloneStyleFrom(getFormatFromStyle(source));
        //Handle matching things if we cross workbooks
        InternalWorkbook sourceWorkbook = getWorkbookFromStyle(source);
        InternalWorkbook targetWorkbook = getWorkbookFromStyle(target);
        if (targetWorkbook != sourceWorkbook) {
            //Then we need to clone the format string, and update the format record for this
            short fmt = targetWorkbook.getFormat(source.getDataFormatString(), true);
            target.setDataFormat(fmt);
        }
    }

    public static void cloneFont(HSSFCellStyle source, HSSFCellStyle target) {
        // Handle matching things if we cross workbooks
        InternalWorkbook sourceWorkbook = getWorkbookFromStyle(source);
        InternalWorkbook targetWorkbook = getWorkbookFromStyle(target);
        if (targetWorkbook != sourceWorkbook) {
            // Finally we need to clone the font, and update the format record for this
            FontRecord fr = targetWorkbook.createNewFont();
            fr.cloneStyleFrom(sourceWorkbook.getFontRecordAt(source.getFontIndex()));
            HSSFFont font = newInstance(HSSFFont.class, new Class[]{int.class, FontRecord.class},
                    (short)targetWorkbook.getFontIndex(fr), fr);
            target.setFont(font);
        }
    }


    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(obj);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(format("Unable to access field '%s' for %s", fieldName, obj.getClass().getSimpleName()), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(format("Unable to access field '%s' for %s", fieldName, obj.getClass().getSimpleName()), e);
        }
    }

    protected static <T> T newInstance(Class<T> clazz, Class<?>[] parameterTypes, Object... args) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(format("Unable to instantiate %s", clazz), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(format("Unable to instantiate %s", clazz), e);
        } catch (InstantiationException e) {
            throw new IllegalStateException(format("Unable to instantiate %s", clazz), e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(format("Unable to instantiate %s", clazz), e);
        }
    }
}
