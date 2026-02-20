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

package io.jmix.search.index.mapping.propertyvalue.impl

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.jmix.core.FileRef
import io.jmix.search.exception.UnsupportedFileFormatException
import io.jmix.search.utils.FileProcessor
import spock.lang.Specification

import static io.jmix.search.utils.LogbackMocker.cleanUpAppender
import static io.jmix.search.utils.LogbackMocker.createAttachedAppender


class FilePropertyValueExtractorTest extends Specification {

    private ListAppender<ILoggingEvent> appender

    void setup() {
        appender = createAttachedAppender(
                FilePropertyValueExtractor.class,
                Level.WARN)
    }

    def "nothing should be thrown if fileProcessor throws a ParserResolvingException but should be logged"() {
        given:
        FileRef fileRef = Mock()

        and:
        def exceptionMock = Mock(UnsupportedFileFormatException)
        exceptionMock.getMessage() >> "Some exception message."

        and:
        FileProcessor fileProcessor = Mock()
        fileProcessor.extractFileContent(fileRef) >> { throw exceptionMock }

        and:
        FilePropertyValueExtractor extractor = new FilePropertyValueExtractor(fileProcessor)

        when:
        extractor.addFileContent(null, fileRef)

        then:
        this.appender.list.size() == 1
        def loggingEvent = this.appender.list.get(0)
        loggingEvent.getLevel() == Level.WARN
        loggingEvent.getMessage() == exceptionMock.getMessage()
    }

    void cleanup() {
        cleanUpAppender(FilePropertyValueExtractor.class, appender)
    }
}
