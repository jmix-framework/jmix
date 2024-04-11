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

import io.jmix.reports.yarg.exception.ReportFormattingException;
import io.jmix.reports.yarg.formatters.impl.doc.OfficeComponent;
import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XDocumentInsertable;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextRange;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
import org.xlsx4j.sml.Cell;

import java.io.File;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.jmix.reports.yarg.formatters.impl.doc.UnoConverter.as;

/**
 * Handle HTML with format string: ${html}
 */
public class HtmlContentInliner implements ContentInliner {

    public final static String REGULAR_EXPRESSION = "\\$\\{html\\}";

    private static final String ENCODING_HEADER = "<META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=utf-8\">";

    private static final String OPEN_HTML_TAGS = "<html> <head> </head> <body>";
    private static final String CLOSE_HTML_TAGS = "</body> </html>";

    private Pattern tagPattern;

    public HtmlContentInliner() {
        tagPattern = Pattern.compile(REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    }

    public Pattern getTagPattern() {
        return tagPattern;
    }

    public void inlineToDoc(OfficeComponent officeComponent,
                            XTextRange textRange, XText destination,
                            Object paramValue, Matcher matcher) throws Exception {
        try {
            boolean inserted = false;
            if (paramValue != null) {
                String htmlContent = paramValue.toString();
                if (!StringUtils.isEmpty(htmlContent)) {
                    insertHTML(destination, textRange, htmlContent);
                    inserted = true;
                }
            }

            if (!inserted)
                destination.getText().insertString(textRange, "", true);
        } catch (Exception e) {
            throw new ReportFormattingException("An error occurred while inserting html to doc file", e);
        }
    }

    public void inlineToDocx(WordprocessingMLPackage wordPackage, Text text, Object paramValue, Matcher matcher) {
        try {
            R run = (R) text.getParent();
            wordPackage.getContentTypeManager().addDefaultContentType("xhtml", "text/xhtml");
            MainDocumentPart mainDocumentPart = wordPackage.getMainDocumentPart();
            mainDocumentPart.addAltChunk(AltChunkType.Xhtml, paramValue.toString().getBytes(), run);
            text.setValue("");
        } catch (Exception e) {
            throw new ReportFormattingException("An error occurred while inserting html to docx file", e);
        }
    }

    @Override
    public void inlineToXls(HSSFPatriarch patriarch, HSSFCell resultCell, Object paramValue, Matcher matcher) {
        throw new UnsupportedOperationException("Inline html content to XSL is not supported");
    }

    @Override
    public void inlineToXlsx(SpreadsheetMLPackage pkg, WorksheetPart worksheetPart, Cell newCell, Object paramValue, Matcher matcher) {
        throw new UnsupportedOperationException("Inline html content to XSLX is not supported");
    }

    private void insertHTML(XText destination, XTextRange textRange, String htmlContent)
            throws Exception {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), ".htm");

            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append(ENCODING_HEADER);
            contentBuilder.append(OPEN_HTML_TAGS);
            contentBuilder.append(htmlContent);
            contentBuilder.append(CLOSE_HTML_TAGS);

            FileUtils.writeByteArrayToFile(tempFile, contentBuilder.toString().getBytes());
            String fileUrl = "file:///" + tempFile.getCanonicalPath().replace("\\", "/");

            XTextCursor textCursor = destination.createTextCursorByRange(textRange);
            XDocumentInsertable insertable = as(XDocumentInsertable.class, textCursor);

            insertable.insertDocumentFromURL(fileUrl, new PropertyValue[0]);
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
    }
}