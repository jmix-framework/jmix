/*
 * Copyright 2026 Haulmont.
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

package xlsx.streaming

import io.jmix.reports.entity.ReportTemplate
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.StreamingXlsxFormatter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.ss.usermodel.WorkbookFactory
import spock.lang.Specification

/**
 * Regression for a field-reported bug: a template produced by the report wizard (whose docx4j engine
 * writes every cell in its own {@code <row r="N">} element with a repeated row index) rendered only the
 * last column through the streaming engine, because POI keeps only the last such {@code <row>} on read.
 * The formatter normalizes duplicate rows before reading, so all columns must render.
 */
class StreamingXlsxWizardTemplateTest extends Specification {

    def "wizard-generated template with duplicate row elements renders all band columns"() {
        given: "a real wizard template (4 columns, each cell in its own duplicate <row>)"
        byte[] template = getClass().getResourceAsStream("/xlsx/streaming/wizardUsersTemplate.xlsx").bytes

        and: "a static header instance plus two Users rows"
        def root = new BandData(BandData.ROOT_BAND_NAME)
        root.setFirstLevelBandDefinitionNames(["headerUsers", "Users"] as Set)
        def header = new BandData("headerUsers", root)
        header.setData([:])
        root.addChild(header)
        [["alice", "Alice", "A", "alice@x"], ["bob", "Bob", "B", "bob@x"]].each { r ->
            def u = new BandData("Users", root)
            u.setData([username: r[0], firstName: r[1], lastName: r[2], email: r[3]])
            root.addChild(u)
        }

        when:
        def out = new ByteArrayOutputStream()
        def reportTemplate = new ReportTemplate()
        reportTemplate.setContent(template)
        def input = new FormatterFactoryInput("xlsx", root, reportTemplate, ReportOutputType.xlsx, out)
        new StreamingXlsxFormatter(input).renderDocument()
        def sheet = WorkbookFactory.create(new ByteArrayInputStream(out.toByteArray())).getSheetAt(0)

        then: "the header row keeps all four columns, not just the last"
        def headerRow = rowContaining(sheet, "Username")
        headerRow != null
        headerRow.getCell(0).toString() == "Username"
        headerRow.getCell(1).toString() == "First name"
        headerRow.getCell(2).toString() == "Last name"
        headerRow.getCell(3).toString() == "Email"

        and: "each data row keeps all four columns"
        def aliceRow = rowContaining(sheet, "alice")
        aliceRow != null
        aliceRow.getCell(0).toString() == "alice"
        aliceRow.getCell(1).toString() == "Alice"
        aliceRow.getCell(2).toString() == "A"
        aliceRow.getCell(3).toString() == "alice@x"
    }

    private static org.apache.poi.ss.usermodel.Row rowContaining(sheet, String firstColumnValue) {
        for (def row : sheet) {
            def cell = row.getCell(0)
            if (cell != null && cell.toString() == firstColumnValue) {
                return row
            }
        }
        return null
    }
}
