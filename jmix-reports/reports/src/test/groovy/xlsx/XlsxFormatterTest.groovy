/*
 * Copyright 2024 Haulmont.
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

package xlsx

import io.jmix.reports.entity.ReportTemplate
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.XlsxFormatter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.xssf.usermodel.XSSFShape
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths


class XlsxFormatterTest extends Specification {

    def "renderDocument from template with images"() throws IOException, URISyntaxException {

        when: "Data has 7 items, template has 3 band images and 1 non-band"
            def os = new ByteArrayOutputStream()
            def rootBand = new BandData("Root")
            rootBand.setFirstLevelBandDefinitionNames(["Root", "Users", "Users2"].toSet())
            def user1Band = new BandData("Users", rootBand)
            user1Band.setData([id: "1", email: "mail1@example.com"])
            rootBand.addChild(user1Band)
            def user2Band = new BandData("Users", rootBand)
            user2Band.setData([id: "2", email: "mail2@example.com"])
            rootBand.addChild(user2Band)
            def user3Band = new BandData("Users", rootBand)
            user3Band.setData([id: "3", email: "mail3@example.com"])
            rootBand.addChild(user3Band)
            def user4Band = new BandData("Users", rootBand)
            user4Band.setData([id: "4", email: "mail4@example.com"])
            rootBand.addChild(user4Band)
            def user5Band = new BandData("Users2", rootBand)
            user5Band.setData([id: "5", email: "mail5@example.com"])
            rootBand.addChild(user5Band)
            def user6Band = new BandData("Users2", rootBand)
            user6Band.setData([id: "6", email: "mail6@example.com"])
            rootBand.addChild(user6Band)
            def user7Band = new BandData("Users2", rootBand)
            user7Band.setData([id: "7", email: "mail7@example.com"])
            rootBand.addChild(user7Band)
            def template = new ReportTemplate()
            template.setContent(readFile("template.xlsx"))
            def formatter = new XlsxFormatter(new FormatterFactoryInput("xlsx", rootBand, template, ReportOutputType.xlsx, os))
            formatter.renderDocument()
        then: "The result document must have (2 * 4) + (1 * 3) + 1 = 12 images"
            def byteArray = os.toByteArray()
            def bis = new ByteArrayInputStream(byteArray)
            def workbook = new XSSFWorkbook(bis)
            def cnt = 0;
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                def srcSH = workbook.getSheetAt(i)
                def srcDraw = srcSH.createDrawingPatriarch()
                cnt += srcDraw.getShapes().size();
            }
            cnt == 12
    }

    protected byte[] readFile(String fileName) throws IOException, URISyntaxException {
        URL resource = XlsxFormatterTest.class
                .getResource("/xlsx/" + fileName)
        byte[] encoded = Files.readAllBytes(Paths.get(resource.toURI()))
        return encoded
    }
}
