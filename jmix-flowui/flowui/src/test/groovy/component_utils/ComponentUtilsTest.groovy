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

package component_utils

import io.jmix.flowui.kit.component.ComponentUtils
import spock.lang.Specification

class ComponentUtilsTest extends Specification {

    def "Check isAutoSize()"(String size, boolean expected) {
        expect:
        ComponentUtils.isAutoSize(size) == expected

        where:
        size        | expected
        null        | true
        ""          | true
        "auto"      | true
        "AuTo"      | true
        "12.12"     | true // Incorrect CSS value, default 'auto' will be used
        "px"        | true // Incorrect CSS value, default 'auto' will be used
        "12.12asdf" | true // Incorrect CSS value, default 'auto' will be used
        "12.12px"   | false
        "-12em"     | false
        "-1px"      | false
    }
}
