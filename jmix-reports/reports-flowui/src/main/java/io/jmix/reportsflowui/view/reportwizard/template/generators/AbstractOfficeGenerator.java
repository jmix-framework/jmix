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

package io.jmix.reportsflowui.view.reportwizard.template.generators;

import io.jmix.reportsflowui.view.reportwizard.template.Generator;
import io.jmix.reportsflowui.view.reportwizard.template.ReportTemplatePlaceholder;
import io.jmix.reports.entity.wizard.ReportData;
import io.jmix.reports.exception.TemplateGenerationException;
import jakarta.xml.bind.JAXBException;
import org.apache.poi.ss.util.CellReference;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.packages.OpcPackage;
import org.docx4j.wml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import org.xlsx4j.sml.STCellType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class AbstractOfficeGenerator implements Generator {

    @Autowired
    protected ReportTemplatePlaceholder reportTemplatePlaceholder;

    @Override
    public byte[] generate(ReportData reportData) throws TemplateGenerationException {
        byte[] template;
        OpcPackage basePackage;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            basePackage = generatePackage(reportData);
            Save saver = new Save(basePackage);
            saver.save(byteArrayOutputStream);
            template = byteArrayOutputStream.toByteArray();
        } catch (Docx4JException | JAXBException | IOException e) {
            throw new TemplateGenerationException(e);
        }
        return template;
    }

    public Row createRow(org.xlsx4j.sml.ObjectFactory factory, String stringContent, int colNum, long rowNum) {
        Row row = factory.createRow();
        row.setR(rowNum);
        Cell cell = createCell(factory, stringContent, colNum, rowNum);
        cell.setS(1L);
        row.getC().add(cell);
        return row;
    }

    public Cell createCell(org.xlsx4j.sml.ObjectFactory factory, String stringContent, int colNum, long rowNum) {
        Cell cell = factory.createCell();
        cell.setT(STCellType.STR);
        cell.setV(stringContent);
        cell.setR(CellReference.convertNumToColString(colNum - 1) + "" + rowNum);
        cell.setS(0L);
        return cell;
    }

    public void fillWordTableRow(List<String> stringData, ObjectFactory factory, Tr tableRow) {
        int columnNumber = 0;
        for (String s : stringData) {
            Tc column = (Tc) tableRow.getContent().get(columnNumber++);
            P columnParagraph = (P) column.getContent().get(0);
            Text text = factory.createText();
            text.setValue(s);
            R run = factory.createR();
            run.getContent().add(text);
            columnParagraph.getContent().add(run);
        }
    }

    protected abstract OpcPackage generatePackage(ReportData reportData) throws TemplateGenerationException, Docx4JException, JAXBException;
}
