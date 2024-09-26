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

package io.jmix.search.index.fileparsing

import io.jmix.core.FileRef
import org.apache.tika.parser.Parser
import spock.lang.Specification

class AbstractExtensionBasedFileParserResolverTest extends Specification {

    def "GetCriteriaDescription"() {
        given:
        def resolver = new TestExtensionBasedFileParserResolver(Mock(Parser), extensions as Set<String>)

        expect:
        resolver.getCriteriaDescription() == criteriaDescription

        where:
        extensions               | criteriaDescription
        ["ext1"]                 | "The file extension should be one of the following: ext1."
        ["ext1", "ext2"]         | "The file extension should be one of the following: ext1, ext2."
        ["ext1", "ext2", "ext3"] | "The file extension should be one of the following: ext1, ext2, ext3."
    }

    def "Supports"() {
        given:
        def resolver = new TestExtensionBasedFileParserResolver(Mock(Parser), extensions as Set<String>)

        and:
        def fileRef = Mock(FileRef)
        fileRef.getFileName() >> fileName

        expect:
        resolver.supports(fileRef) == supports

        where:
        fileName      | extensions               | supports
        "file1.ext1"  | ["ext1"]                 | true
        "file1.ext11" | ["ext1"]                 | false
        "file1..ext1" | ["ext1"]                 | true
        "file1..ext"  | ["ext1"]                 | false
        "file1.ext1"  | ["ext1", "ext2"]         | true
        "file1.ext2"  | ["ext1", "ext2"]         | true
        "file1.ext3"  | ["ext1", "ext2", "ext3"] | true
        "file1.ext33" | ["ext1", "ext2", "ext3"] | false
        "file1.doc"   | ["docx"]                 | false
        "file1."      | ["docx"]                 | false
        "file"        | ["ext1"]                 | false
    }

    private static class TestExtensionBasedFileParserResolver extends AbstractExtensionBasedFileParserResolver {
        private Parser parser
        private Set<String> extensions

        TestExtensionBasedFileParserResolver(Parser parser, Set<String> extensions) {
            this.parser = parser
            this.extensions = extensions
        }

        @Override
        Set<String> getSupportedExtensions() {
            return extensions;
        }

        @Override
        Parser getParser() {
            return parser
        }
    }
}
