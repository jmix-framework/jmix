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

package io.jmix.reports.yarg.formatters.impl.docx;

import io.jmix.reports.yarg.formatters.impl.DocxFormatterDelegate;
import io.jmix.reports.yarg.formatters.impl.docx.TableCollector;
import io.jmix.reports.yarg.formatters.impl.docx.TableManager;
import io.jmix.reports.yarg.formatters.impl.docx.TextVisitor;
import io.jmix.reports.yarg.formatters.impl.docx.TextWrapper;
import org.docx4j.TraversalUtil;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import java.util.List;
import java.util.Set;

public class DocumentWrapper {
    protected DocxFormatterDelegate docxFormatter;
    protected WordprocessingMLPackage wordprocessingMLPackage;
    protected MainDocumentPart mainDocumentPart;
    protected Set<io.jmix.reports.yarg.formatters.impl.docx.TableManager> tables;
    protected Set<TextWrapper> texts;

    public DocumentWrapper(DocxFormatterDelegate docxFormatter, WordprocessingMLPackage wordprocessingMLPackage) {
        this.docxFormatter = docxFormatter;
        this.wordprocessingMLPackage = wordprocessingMLPackage;
        this.mainDocumentPart = wordprocessingMLPackage.getMainDocumentPart();
        collectData();
    }

    protected void collectDataFromObjects(Object... objects) {
        for (Object object : objects) {
            if (object != null) {
                TextVisitor collectAliasesCallback = new TextVisitor(docxFormatter);
                new TraversalUtil(object, collectAliasesCallback);
                texts.addAll(collectAliasesCallback.textWrappers);
            }
        }
    }

    protected void collectData() {
        collectTables();
        collectTexts();
        collectHeadersAndFooters();
    }

    protected void collectHeadersAndFooters() {//collect data from headers
        List<SectionWrapper> sectionWrappers = wordprocessingMLPackage.getDocumentModel().getSections();
        for (SectionWrapper sw : sectionWrappers) {
            HeaderFooterPolicy hfp = sw.getHeaderFooterPolicy();
            collectDataFromObjects(hfp.getFirstHeader(), hfp.getDefaultHeader(), hfp.getEvenHeader(), hfp.getFirstFooter(), hfp.getDefaultFooter(), hfp.getEvenFooter());
        }
    }

    protected void collectTexts() {
        TextVisitor collectAliasesCallback = new TextVisitor(docxFormatter);
        new TraversalUtil(mainDocumentPart, collectAliasesCallback);
        texts = collectAliasesCallback.textWrappers;
    }

    protected void collectTables() {
        TableCollector collectTablesCallback = new TableCollector(docxFormatter);
        new TraversalUtil(mainDocumentPart, collectTablesCallback);
        tables = collectTablesCallback.tableManagers;
    }

    public Set<TableManager> getTables() {
        return tables;
    }

    public Set<TextWrapper> getTexts() {
        return texts;
    }
}
