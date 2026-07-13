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
package io.jmix.reports.yarg.formatters.impl.streaming;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Clones template cell styles into the result workbook once each, keyed by the template style index.
 * Deduplication keeps the result workbook far below the XLSX ~64k cell-style limit even when millions
 * of cells reuse the same template styles.
 */
public class StreamingStyleCache {

    protected final SXSSFWorkbook resultWorkbook;
    protected final Map<Short, CellStyle> byTemplateIndex = new HashMap<>();

    public StreamingStyleCache(SXSSFWorkbook resultWorkbook) {
        this.resultWorkbook = resultWorkbook;
    }

    @Nullable
    public CellStyle resultStyleFor(@Nullable XSSFCellStyle templateStyle) {
        if (templateStyle == null) {
            return null;
        }
        return byTemplateIndex.computeIfAbsent(templateStyle.getIndex(), i -> {
            CellStyle copy = resultWorkbook.createCellStyle();
            copy.cloneStyleFrom(templateStyle);
            return copy;
        });
    }
}
