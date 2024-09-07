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


    public static final String MESSAGE_1 = "The file the-file-with-not-supported-extension.sql with 'sql' extension " +
            "is not supported. " +
            "Only following file extensions are supported pdf, doc, xls, docx, xlsx, odt, ods, rtf, rtf."
    public static final String MESSAGE_2 = "The file another-file.smt with 'smt' extension is not supported. " +
            "Only following file extensions are supported pdf, doc, xls, docx, xlsx, odt, ods, rtf, rtf."

    def "message test"() {
        when:
        def exception = new UnsupportedFileExtensionException(fileName)

        then:
        exception.getMessage() == b

        where:
        fileName | b
        "the-file-with-not-supported-extension.sql" | MESSAGE_1
        "another-file.smt"                          | MESSAGE_2
    }
}
