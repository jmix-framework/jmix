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

package io.jmix.search.exception

import spock.lang.Specification

class EmptyFileExtensionExceptionTest extends Specification {

    private static final String MESSAGE_1 = "Extension of the file someName is empty. " +
            "Only following file extensions are supported: txt, xls."
    private static final String MESSAGE_2 = "Extension of the file someName2 is empty. " +
            "Only following file extensions are supported: rtf, pdf."

    def "message test"() {
        given:
        def exception = new EmptyFileExtensionException(fileName, extensions)

        expect:
        exception.getMessage() == message

        where:
        fileName   | extensions     | message
        "someName" | ["txt", "xls"] | MESSAGE_1
        "someName2" | ["rtf", "pdf"] | MESSAGE_2

    }
}
