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

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import java.util.HashMap;
import java.util.Map;

public class XlsStyleCache {

    private Map<HSSFStyleCacheKey, HSSFCellStyle> cellStyles = new HashMap<>();

    private Map<HSSFStyleCacheKey, HSSFCellStyle> namedCellStyles = new HashMap<>();

    private Map<String, HSSFCellStyle> styleMap = new HashMap<>();

    public HSSFCellStyle processCellStyle(HSSFCellStyle cellStyle) {
        HSSFCellStyle cachedCellStyle = cellStyles.get(new HSSFStyleCacheKey(cellStyle));
        if (cachedCellStyle == null)
            cellStyles.put(new HSSFStyleCacheKey(cellStyle), cellStyle);
        else
            cellStyle = cachedCellStyle;

        return cellStyle;
    }

    public HSSFCellStyle getCellStyleByTemplate(HSSFCellStyle templateCellStyle) {
        return cellStyles.get(new HSSFStyleCacheKey(templateCellStyle));
    }

    public void addCachedStyle(HSSFCellStyle templateCellStyle, HSSFCellStyle cellStyle) {
        cellStyles.put(new HSSFStyleCacheKey(templateCellStyle), cellStyle);
    }

    public void addNamedStyle(HSSFCellStyle cellStyle) {
        styleMap.put(cellStyle.getUserStyleName(), cellStyle);
    }

    public HSSFCellStyle getStyleByName(String styleName) {
        return styleMap.get(styleName);
    }

    public HSSFCellStyle getNamedCachedStyle(HSSFCellStyle namedCellStyle) {
        return namedCellStyles.get(new HSSFStyleCacheKey(namedCellStyle));
    }

    public void addCachedNamedStyle(HSSFCellStyle namedCellStyle, HSSFCellStyle cellStyle) {
        namedCellStyles.put(new HSSFStyleCacheKey(namedCellStyle), cellStyle);
    }
}