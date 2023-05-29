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

import org.apache.poi.hssf.usermodel.HSSFCell;

import java.util.regex.Matcher;

public class CustomWidthHint extends AbstractHint {

    public CustomWidthHint() {
        super("##width=([A-z0-9]+)");
    }

    @Override
    public void apply() {
        for (DataObject dataObject : data) {
            HSSFCell resultCell = dataObject.resultCell;
            HSSFCell templateCell = dataObject.templateCell;

            String templateCellValue = templateCell.getStringCellValue();

            Matcher matcher = pattern.matcher(templateCellValue);
            if (matcher.find()) {
                String paramName = matcher.group(1);
                Integer width = (Integer) dataObject.bandData.getParameterValue(paramName);
                if (width != null) {
                    resultCell.getSheet().setColumnWidth(resultCell.getColumnIndex(), width);
                }
            }
        }
    }
}