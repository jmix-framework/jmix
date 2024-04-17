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

import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.usermodel.HSSFFont;

import java.io.Serializable;

public class HSSFFontCacheKey implements Serializable {

    private static final long serialVersionUID = 7503724004378911912L;

    protected final HSSFFont font;
    protected final FontRecord fontRecord;

    public HSSFFontCacheKey(HSSFFont font) {
        this.font = font;
        if (font != null) {
            this.fontRecord = XslStyleHelper.getFontRecord(font);
        } else {
            this.fontRecord = null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HSSFFontCacheKey) {
            HSSFFontCacheKey objKey = (HSSFFontCacheKey) obj;
            HSSFFont objFont = objKey.font;
            if (font == objFont) {
                return true;
            }
            if (objFont == null) {
                return false;
            }
            if (fontRecord == null) {
                if (objKey.fontRecord != null) {
                    return false;
                }
            } else if (!fontRecord.equals(objKey.fontRecord)) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return fontRecord == null ? 0 : fontRecord.hashCode();
    }
}