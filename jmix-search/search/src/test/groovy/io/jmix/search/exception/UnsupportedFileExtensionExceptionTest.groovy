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

class UnsupportedFileExtensionExceptionTest extends Specification {


    private static final String MESSAGE_1 = "The file the-file-with-not-supported-extension.sql with 'sql' " +
            "extension is not supported. Only following file extensions are supported: txt, rtf."
    private static final String MESSAGE_2 = "The file another-file.smt with 'smt' extension is not supported. " +
            "Only following file extensions are supported: abc, def."

    def "message test"() {
        when:
        def exception = new UnsupportedFileExtensionException(fileName, supportedTypes)

        then:
        exception.getMessage() == message

        where:
        fileName                                    |supportedTypes| message
        "the-file-with-not-supported-extension.sql" |["txt", "rtf"]| MESSAGE_1
        "another-file.smt"                          |["abc", "def"]| MESSAGE_2
    }
}
