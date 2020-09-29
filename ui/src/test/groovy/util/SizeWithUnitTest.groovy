/*
 * Copyright 2020 Haulmont.
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

package util

import io.jmix.ui.component.SizeUnit
import io.jmix.ui.component.SizeWithUnit
import spock.lang.Specification
import spock.lang.Unroll

class SizeWithUnitTest extends Specification {

    @Unroll
    def "Test pixel size"(String sizeString) {
        when:
        SizeWithUnit size = SizeWithUnit.parseStringSize(sizeString)
        float expected = Float.parseFloat(sizeString.substring(0, sizeString.length() - 2))

        then:
        expected == size.size
        SizeUnit.PIXELS == size.unit

        where:
        sizeString << [
                "999999px",
                "1000px",
                "500px",
                "1px",
                "0px"
        ]
    }

    @Unroll
    def "Test percentage size"(String sizeString) {
        when:
        SizeWithUnit size = SizeWithUnit.parseStringSize(sizeString)
        float expected = Float.parseFloat(sizeString.substring(0, sizeString.length() - 1))

        then:
        expected == size.size
        SizeUnit.PERCENTAGE == size.unit

        where:
        sizeString << [
                "999999%",
                "1000%",
                "500%",
                "1%",
                "0%"
        ]
    }

    @Unroll
    def "Test unitless"(String sizeString) {
        when:
        SizeWithUnit size = SizeWithUnit.parseStringSize(sizeString)
        float expected = Float.parseFloat(sizeString)

        then:
        expected == size.size
        SizeUnit.PIXELS == size.unit

        where:
        sizeString << [
                "999999",
                "1000",
                "500",
                "1",
                "0"
        ]
    }

    @Unroll
    def "Test default unit"(String sizeString) {
        when:
        SizeWithUnit size = SizeWithUnit.parseStringSize(sizeString, SizeUnit.PERCENTAGE)
        float expected = Float.parseFloat(sizeString)

        then:
        expected == size.size
        SizeUnit.PERCENTAGE == size.unit

        where:
        sizeString << [
                "999999",
                "1000",
                "500",
                "1",
                "0"
        ]
    }

    @Unroll
    def "Test auto size"(String sizeString) {
        when:
        SizeWithUnit size = SizeWithUnit.parseStringSize(sizeString)

        then:
        size.size == -1.0f

        where:
        sizeString << [
                "-1px",
                "AUTO",
                "-1",
                "",
                null
        ]
    }

    @Unroll
    def "Test invalid size"(String sizeString) {
        when:
        SizeWithUnit.parseStringSize(sizeString)

        then:
        thrown(IllegalArgumentException)

        where:
        sizeString << [
                "600em",
                "600rem",
                "600ex",
                "600in",
                "600cm",
                "600mm",
                "600pt",
                "600pc",
                "ten"
        ]
    }
}
