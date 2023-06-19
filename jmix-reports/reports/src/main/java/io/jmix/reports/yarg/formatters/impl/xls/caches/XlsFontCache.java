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

import org.apache.poi.hssf.usermodel.HSSFFont;

import java.util.HashMap;
import java.util.Map;

public class XlsFontCache {

    private Map<HSSFFontCacheKey, HSSFFont> fonts = new HashMap<>();

    public HSSFFont getFontByTemplate(HSSFFont font){
        return fonts.get(new HSSFFontCacheKey(font));
    }

    public void addCachedFont(HSSFFont templateFont, HSSFFont font) {
        fonts.put(new HSSFFontCacheKey(templateFont), font);
    }
}