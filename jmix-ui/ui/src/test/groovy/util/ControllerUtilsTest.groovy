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

import io.jmix.ui.sys.ControllerUtils
import spock.lang.Specification

class ControllerUtilsTest extends Specification {

    def "Test getLocationWithoutParams method"() {
        when:
        URI localUrl = new URI("http://localhost:8080/app?a")
        then:
        ControllerUtils.getLocationWithoutParams(localUrl) == "http://localhost:8080/app/"

        when:
        URI externalUrl = new URI("http://ya.ru/app/sample/?param=value")
        then:
        ControllerUtils.getLocationWithoutParams(externalUrl) == "http://ya.ru/app/sample/"

        when:
        URI debugUrl = new URI("http://localhost:8080/app/?debug#!")
        then:
        ControllerUtils.getLocationWithoutParams(debugUrl) == "http://localhost:8080/app/"

        when:
        URI encodedUrl = new URI("http://localhost:8080/#login?redirectTo=employees%2Fview")
        then:
        ControllerUtils.getLocationWithoutParams(encodedUrl) == "http://localhost:8080/"
    }
}
