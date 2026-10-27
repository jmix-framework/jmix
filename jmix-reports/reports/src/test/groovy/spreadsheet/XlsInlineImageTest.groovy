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

package spreadsheet

import io.jmix.reports.yarg.formatters.factory.DefaultFormatterFactory
import io.jmix.reports.yarg.structure.ReportOutputType
import org.apache.poi.hssf.usermodel.HSSFPicture
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellReference
import org.apache.poi.ss.util.ImageUtils
import xlsx.BaseXlsxRenderTest

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.apache.poi.util.Units.EMU_PER_PIXEL

/**
 * An image inlined into an XLS document is stretched to the width and height of its format, in pixels.
 */
class XlsInlineImageTest extends BaseXlsxRenderTest {

    def "an inline image gets the width and height of its format"() {
        given: "a header band and a 30x30 image in cell B2 with the 120x40 format"
            def template = buildTemplate(new HSSFWorkbook()) { Workbook wb ->
                def s = sheet(wb)
                cell(s, 0, 0, "Image")
                cell(s, 1, 1, '${image}')
                defineBand(wb, "Header", 0, 0, 0, 0)
                defineBand(wb, "Data", 1, 1, 1, 1)
            }
            def root = rootBand("Header", "Data")
            withFieldFormats(root, fieldFormat("Data.image", '${bitmap:120x40}'))
            addBand(root, "Header", [:])
            addBand(root, "Data", [image: png(30, 30)])

        when:
            def result = read(render(template, root, ReportOutputType.xls,
                    new DefaultFormatterFactory().&createFormatter))

        then: "the picture starts in the top-left corner of B2 and is 120x40 pixels"
            def picture = result.getSheet(SHEET).getDrawingPatriarch().getChildren()
                    .find { it instanceof HSSFPicture } as HSSFPicture
            with(picture.clientAnchor) {
                new CellReference(row1, col1).formatAsString() == "B2"
                dx1 == 0
                dy1 == 0
            }
            def size = ImageUtils.getDimensionFromAnchor(picture)
            Math.round(size.getWidth() / EMU_PER_PIXEL) == 120
            Math.round(size.getHeight() / EMU_PER_PIXEL) == 40
    }

    protected static byte[] png(int width, int height) {
        def image = new ByteArrayOutputStream()
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", image)
        return image.toByteArray()
    }
}
