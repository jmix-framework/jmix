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


import io.jmix.reports.yarg.formatters.impl.xlsx.XlsxUtils
import spock.lang.Specification


class XlsxUtilsTest extends Specification {

    def "computeColumnIndex"() throws IOException, URISyntaxException {
        when: "Cell names is: A4"
            def cellName1 = "A4"
        then: "The column index must be eq to: 1"
            XlsxUtils.computeColumnIndex(cellName1) == 1

        when: "Cell name is: B3"
            def cellName2 = "B3"
        then: "The column index must be eq to: 2"
            XlsxUtils.computeColumnIndex(cellName2) == 2

        when: "Cell name is: F6"
            def cellName3 = "F6"
        then: "The column index must be eq to: 6"
            XlsxUtils.computeColumnIndex(cellName3) == 6

        when: "Cell name is: H8"
            def cellName4 = "H8"
        then: "The column index must be eq to: 8"
            XlsxUtils.computeColumnIndex(cellName4) == 8

        when: "Cell name is: Z6"
            def cellName5 = "Z6"
        then: "The column index must be eq to: 26"
            XlsxUtils.computeColumnIndex(cellName5) == 26

        when: "Cell name is: AB15"
            def cellName6 = "AB15"
        then: "The column index must be eq to: 28"
            XlsxUtils.computeColumnIndex(cellName6) == 28

        when: "Cell name is: CV122"
            def cellName7 = "CV122"
        then: "The column index must be eq to: 100"
            XlsxUtils.computeColumnIndex(cellName7) == 100

        when: "Cell name is: AAA50"
            def cellName8 = "AAA50"
        then: "The column index must be eq to: 703"
            XlsxUtils.computeColumnIndex(cellName8) == 703
    }
}
