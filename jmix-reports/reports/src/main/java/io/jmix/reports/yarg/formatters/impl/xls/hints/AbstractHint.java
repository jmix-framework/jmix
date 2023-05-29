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
package io.jmix.reports.yarg.formatters.impl.xls.hints;

import io.jmix.reports.yarg.structure.BandData;
import org.apache.poi.hssf.usermodel.HSSFCell;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractHint implements XlsHint {

    protected static class DataObject {
        protected final HSSFCell resultCell;
        protected final HSSFCell templateCell;
        protected final BandData bandData;

        public DataObject(HSSFCell resultCell, HSSFCell templateCell, BandData bandData) {
            this.resultCell = resultCell;
            this.templateCell = templateCell;
            this.bandData = bandData;
        }
    }

    protected List<DataObject> data = new ArrayList<DataObject>();

    protected String patternStr;
    protected Pattern pattern;


    protected AbstractHint(String patternStr) {
        this.patternStr = patternStr;
        this.pattern = Pattern.compile(patternStr);
    }

    @Override
    public void add(HSSFCell templateCell, HSSFCell resultCell, BandData bandData) {
        data.add(new DataObject(resultCell, templateCell, bandData));
    }

    @Override
    public CheckResult check(String templateCellValue) {
        Matcher matcher = pattern.matcher(templateCellValue);
        if (matcher.find()) {
            return new CheckResult(true, templateCellValue.replaceAll(patternStr, ""));
        } else {
            return CheckResult.NEGATIVE;
        }
    }
}