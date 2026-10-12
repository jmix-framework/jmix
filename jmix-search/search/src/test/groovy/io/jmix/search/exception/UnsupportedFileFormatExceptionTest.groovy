/*
 * Copyright 2024 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain fileName copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.search.exception

import spock.lang.Specification

class UnsupportedFileFormatExceptionTest extends Specification {

    private static final String MESSAGE_1 = "The file another-file.smt can't be parsed. " +
            "Only the following file parsing criteria are supported:\n" +
            "  -The only one criteria."
    private static final String MESSAGE_2 = "The file the-file-with-not-supported-extension.sql can't be parsed. " +
            "Only the following file parsing criteria are supported:\n" +
            "  -The first criteria.\n" +
            "  -The second criteria."

    private static final String MESSAGE_3 = "The file anyfile can't be parsed. " +
            "Only the following file parsing criteria are supported:\n" +
            "  -line1\n" +
            "  -line2\n" +
            "  -line3\n" +
            "  -line4"

    def "message test"() {
        when:
        def exception = new UnsupportedFileFormatException(fileName, supportedTypes)

        then:
        exception.getMessage() == message

        where:
        fileName                                    | supportedTypes                                  | message
        "another-file.smt"                          | ["The only one criteria."]                      | MESSAGE_1
        "the-file-with-not-supported-extension.sql" | ["The first criteria.", "The second criteria."] | MESSAGE_2
        "anyfile"                                   | ["line1", "line2", "line3", "line4"]            | MESSAGE_3
    }
}
