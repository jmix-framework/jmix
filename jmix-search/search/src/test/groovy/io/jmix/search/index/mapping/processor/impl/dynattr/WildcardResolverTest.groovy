/*
 * Copyright 2025 Haulmont.
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

package io.jmix.search.index.mapping.processor.impl.dynattr

import spock.lang.Specification

class WildcardResolverTest extends Specification {
    def "GetMatchingElements"() {
        given:
        Map<String, String> sourceElements = Map.of(
                "name", "nameElement",
                "name2", "name2Element",
                "name3", "name3Element")

        when:
        def resolver = new WildcardResolver()
        def resultElements = new ArrayList<>(resolver.getMatchingElements(sourceElements, excludedNames))

        then:
        Collections.sort(resultElements)
        Collections.sort(expectedResult)
        resultElements == expectedResult
        where:
        excludedNames              || expectedResult
        []                         || []
        ["name2"]                  || ["name2Element"]
        ["name", "name3"]          || ["nameElement", "name3Element"]
        ["name", "name2", "name3"] || ["nameElement", "name2Element", "name3Element"]
        ["name*"]                  || ["nameElement", "name2Element", "name3Element"]
        ["n*"]                     || ["nameElement", "name2Element", "name3Element"]
        ["*3"]                     || ["name3Element"]
        ["n*3"]                    || ["name3Element"]
        ["*ame*"]                  || ["nameElement", "name2Element", "name3Element"]
        ["*ame"]                   || ["nameElement"]
        ["*ame*2"]                 || ["name2Element"]
    }
}
