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

package xlsx

import io.jmix.reports.yarg.structure.BandData
import io.jmix.reports.yarg.structure.BandOrientation

/**
 * Runs the {@link BaseXlsxBandLayoutTest} layout contract against the in-memory {@code XlsxFormatter}, plus
 * the one layout rule that is specific to this engine: vertical bands grow rightwards.
 *
 * <p>Vertical layout cannot be part of the shared contract — the streaming engine rejects vertical bands
 * outright (asserted in the streaming structure spec), because forward-only writing cannot revisit a row to
 * append another column.
 */
class XlsxBandLayoutTest extends BaseXlsxBandLayoutTest {

    def "a vertical band is repeated rightwards for every data row"() {
        given:
            def template = buildTemplate { wb ->
                def sheet = sheet(wb)
                cell(sheet, 0, 0, '${value}')
                defineBand(wb, "Data", 0, 0, 0, 0)
            }
            def root = rootBand("Data")
            verticalBand(root, "Data", [value: "x"])
            verticalBand(root, "Data", [value: "y"])
            verticalBand(root, "Data", [value: "z"])

        when:
            def sheet = renderAndReadFirstSheet(template, root)

        then: "values go into consecutive columns of the same row"
            stringValue(sheet, 0, 0) == "x"
            stringValue(sheet, 0, 1) == "y"
            stringValue(sheet, 0, 2) == "z"
    }

    protected BandData verticalBand(BandData parent, String name, Map<String, Object> data) {
        def band = new BandData(name, parent, BandOrientation.VERTICAL)
        band.setData(data)
        parent.addChild(band)
        return band
    }
}
