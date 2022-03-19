/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsui.screen.report.wizard.template.generators;

import io.jmix.reports.entity.wizard.RegionProperty;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.entity.wizard.ReportRegion;
import org.docx4j.model.table.TblFactory;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tr;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("report_DocxGenerator")
public class DocxGenerator extends AbstractOfficeGenerator {

    @Override
    protected OpcPackage generatePackage(ReportData reportData) throws Docx4JException {
        WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.createPackage();
        MainDocumentPart mainDocumentPart = wordMLPackage.getMainDocumentPart();
        org.docx4j.wml.ObjectFactory factory = org.docx4j.jaxb.Context.getWmlObjectFactory();
        for (ReportRegion reportRegion : reportData.getReportRegions()) {
            if (reportRegion.isTabulatedRegion()) {
                mainDocumentPart.addParagraphOfText("");
                int writableWidthTwips = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips();
                int cols = reportRegion.getRegionProperties().size();
                int cellWidthTwips = (int) Math.floor((writableWidthTwips / (double) cols));
                Tbl table = TblFactory.createTable(2, reportRegion.getRegionProperties().size(), cellWidthTwips);
                boolean isFirstHeaderCellFounded = false; //for adding band name in table header row
                List<String> tableHeaderRowData = new ArrayList<>(reportRegion.getRegionProperties().size());
                for (RegionProperty rp : reportRegion.getRegionProperties()) {

                    if (!isFirstHeaderCellFounded) {
                        tableHeaderRowData.add("##band=" + reportRegion.getNameForBand() + " " + rp.getHierarchicalLocalizedNameExceptRoot());

                        isFirstHeaderCellFounded = true;
                    } else {
                        tableHeaderRowData.add(rp.getHierarchicalLocalizedNameExceptRoot());
                    }
                }
                fillWordTableRow(tableHeaderRowData, factory, (Tr) table.getContent().get(0));

                List<String> tablePlaceholdersRowData = new ArrayList<>(reportRegion.getRegionProperties().size());
                for (RegionProperty rp : reportRegion.getRegionProperties()) {
                    tablePlaceholdersRowData.add(reportTemplatePlaceholder.getPlaceholderValue(rp.getHierarchicalNameExceptRoot(), reportRegion));
                }
                fillWordTableRow(tablePlaceholdersRowData, factory, (Tr) table.getContent().get(1));

                mainDocumentPart.addObject(table);
            } else {
                for (RegionProperty rp : reportRegion.getRegionProperties()) {
                    mainDocumentPart.addParagraphOfText(rp.getHierarchicalLocalizedNameExceptRoot() + ": " + reportTemplatePlaceholder.getPlaceholderValueWithBandName(rp.getHierarchicalNameExceptRoot(), reportRegion));
                }
            }
        }
        return wordMLPackage;
    }
}