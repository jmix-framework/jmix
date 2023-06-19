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
package io.jmix.reports.yarg.formatters.impl.inline;

import com.sun.star.text.XText;
import com.sun.star.text.XTextRange;
import io.jmix.reports.yarg.formatters.impl.doc.OfficeComponent;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.wml.Text;
import org.xlsx4j.sml.Cell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle tags in format strings
 */
public interface ContentInliner {

    /**
     * Get Regexp Pattern for match format string
     *
     * @return Pattern
     */
    Pattern getTagPattern();

    /**
     * Inline content to xlsx template
     */
    void inlineToXlsx(SpreadsheetMLPackage pkg, WorksheetPart worksheetPart, Cell newCell, Object paramValue, Matcher matcher);

    /**
     * Inline content into doc template
     */
    void inlineToDoc(OfficeComponent officeComponent, XTextRange textRange, XText destination, Object paramValue, Matcher paramsMatcher)
            throws Exception;

    /**
     * Inline content into docx template
     */
    void inlineToDocx(WordprocessingMLPackage wordPackage, Text destination, Object paramValue, Matcher paramsMatcher);

    /**
     * Inline content into xls template
     */
    void inlineToXls(HSSFPatriarch patriarch, HSSFCell destination, Object paramValue, Matcher paramsMatcher);
}