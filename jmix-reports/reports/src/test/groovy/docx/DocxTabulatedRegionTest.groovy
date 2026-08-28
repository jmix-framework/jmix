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

package docx

import io.jmix.reports.entity.ReportTemplate
import io.jmix.reports.yarg.formatters.factory.FormatterFactoryInput
import io.jmix.reports.yarg.formatters.impl.DocxFormatter
import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.ReportOutputType
import org.docx4j.Docx4J
import org.docx4j.TraversalUtil
import org.docx4j.XmlUtils
import org.docx4j.jaxb.Context
import org.docx4j.model.table.TblFactory
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.wml.ObjectFactory
import org.docx4j.wml.P
import org.docx4j.wml.Tbl
import org.docx4j.wml.Tc
import org.docx4j.wml.Text
import org.docx4j.wml.Tr
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.IgnoreIf
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicLong

/**
 * Rendering of a tabulated region as the report wizard generates it for DOCX: a table whose header row
 * declares the band ({@code ##band=Parent}) and whose second row holds aliases of the band's own fields
 * ({@code ${name}}). The alias row is copied once per band of the report.
 */
class DocxTabulatedRegionTest extends Specification {

    private static final Logger log = LoggerFactory.getLogger(DocxTabulatedRegionTest)

    private static final String BAND_NAME = "Parent"

    def "copies the alias row once per band and fills it with the band's values"() {
        given:
        def rootBand = buildBands(3, new AtomicLong())
        def output = new ByteArrayOutputStream()

        when:
        renderDocument(rootBand, output)

        then: "the header row is followed by a row per band, in the order of the bands"
        def rows = readTableRows(output.toByteArray())
        rows.size() == 4
        rows[0] == ["Name", "Amount"]
        rows[1] == ["name-0", "amount-0"]
        rows[2] == ["name-1", "amount-1"]
        rows[3] == ["name-2", "amount-2"]
    }

    /**
     * Regression test for the tabulated DOCX report that never finished rendering: the band path of an alias
     * was resolved once per band row, and a resolution walked the whole band tree because an alias of the
     * band's own field matches no band name. The resolution does not depend on the row being filled, so the
     * number of visited band-tree nodes must stay proportional to the number of rows.
     */
    def "resolves alias band paths without traversing the band tree per row"() {
        given: "a report with enough rows for a quadratic traversal to stand out"
        int rows = 1000
        def visitedNodes = new AtomicLong()
        def rootBand = buildBands(rows, visitedNodes)

        when:
        renderDocument(rootBand, new ByteArrayOutputStream())

        then: "the render visits a linear, not a quadratic, number of band-tree nodes"
        // A quadratic resolution visited 3 * rows * (rows + 1) nodes, i.e. about 3000 nodes per row.
        visitedNodes.get() > 0
        visitedNodes.get() / rows < 50
    }

    /**
     * Renders the volume at which the quadratic band lookup and the quadratic search for the insert position
     * of a copied row used to make the report unusable, and logs what the render costs now.
     */
    @IgnoreIf({ env["slowTests"] != 'true' })
    def "renders a tabulated region of many rows"() {
        given:
        int rows = 50_000
        def rootBand = buildBands(rows, new AtomicLong())
        def output = new ByteArrayOutputStream()

        when:
        long startNanos = System.nanoTime()
        renderDocument(rootBand, output)
        long elapsedMs = (System.nanoTime() - startNanos).intdiv(1_000_000L)

        then:
        log.info("Rendered {} rows in {} ms, output={} KB", rows, elapsedMs, output.size() >> 10)
        countTableRows(output.toByteArray()) == rows + 1
    }

    private static void renderDocument(BandData rootBand, ByteArrayOutputStream output) {
        def template = new ReportTemplate()
        template.setContent(buildWizardTemplate())
        new DocxFormatter(new FormatterFactoryInput("docx", rootBand, template, ReportOutputType.docx, output))
                .renderDocument()
    }

    private static BandData buildBands(int rows, AtomicLong visitedNodes) {
        def rootBand = new CountingBandData(BandData.ROOT_BAND_NAME, null, visitedNodes)
        rootBand.setFirstLevelBandDefinitionNames([BAND_NAME].toSet())
        for (int i = 0; i < rows; i++) {
            def band = new CountingBandData(BAND_NAME, rootBand, visitedNodes)
            band.setData(["name": "name-$i" as String, "amount": "amount-$i" as String] as Map<String, Object>)
            rootBand.addChild(band)
        }
        return rootBand
    }

    /**
     * Counts how many band-tree nodes a render walks over: a band lookup collects the children of every node
     * it visits.
     */
    private static class CountingBandData extends BandData {

        private final AtomicLong visitedNodes

        CountingBandData(String name, BandData parentBand, AtomicLong visitedNodes) {
            super(name, parentBand)
            this.visitedNodes = visitedNodes
        }

        @Override
        List<BandData> getChildrenList() {
            visitedNodes.incrementAndGet()
            return super.getChildrenList()
        }
    }

    /** Builds the template the report wizard generates for a tabulated DOCX region. */
    private static byte[] buildWizardTemplate() {
        def wordPackage = WordprocessingMLPackage.createPackage()
        def mainDocumentPart = wordPackage.getMainDocumentPart()
        def factory = Context.getWmlObjectFactory()

        mainDocumentPart.addParagraphOfText("")
        int writableWidthTwips = wordPackage.getDocumentModel().getSections().get(0)
                .getPageDimensions().getWritableWidthTwips()
        Tbl table = TblFactory.createTable(2, 2, (int) Math.floor(writableWidthTwips / 2.0d))
        fillRow(["##band=$BAND_NAME Name" as String, "Amount"], factory, (Tr) table.getContent().get(0))
        fillRow(['${name}', '${amount}'], factory, (Tr) table.getContent().get(1))
        mainDocumentPart.addObject(table)

        def bos = new ByteArrayOutputStream()
        Docx4J.save(wordPackage, bos, Docx4J.FLAG_NONE)
        return bos.toByteArray()
    }

    private static void fillRow(List<String> values, ObjectFactory factory, Tr row) {
        int column = 0
        for (String value : values) {
            Tc cell = (Tc) row.getContent().get(column++)
            P paragraph = (P) cell.getContent().get(0)
            def text = factory.createText()
            text.setValue(value)
            def run = factory.createR()
            run.getContent().add(text)
            paragraph.getContent().add(run)
        }
    }

    private static List<List<String>> readTableRows(byte[] documentBytes) {
        return findTable(documentBytes).getContent()
                .collect { XmlUtils.unwrap(it) }
                .findAll { it instanceof Tr }
                .collect { cellValues((Tr) it) }
    }

    private static int countTableRows(byte[] documentBytes) {
        return findTable(documentBytes).getContent().count { XmlUtils.unwrap(it) instanceof Tr }
    }

    private static Tbl findTable(byte[] documentBytes) {
        def wordPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(documentBytes))
        return (Tbl) wordPackage.getMainDocumentPart().getContent()
                .collect { XmlUtils.unwrap(it) }
                .find { it instanceof Tbl }
    }

    private static List<String> cellValues(Tr row) {
        List<String> values = []
        new TraversalUtil(row, new TraversalUtil.CallbackImpl() {
            @Override
            List<Object> apply(Object o) {
                if (o instanceof Text) {
                    values.add(((Text) o).getValue())
                }
                return null
            }
        })
        return values
    }
}
