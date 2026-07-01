/*
 * Copyright 2022 Haulmont.
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

package commons

import io.jmix.core.common.util.Dom4j
import spock.lang.Specification

import java.nio.file.Files

class Dom4jTest extends Specification {

    def "Dom4j does not expose content from an external entity"() {
        given:
        def secret = Files.createTempFile('jmix-dom4j-xxe-', '.txt')
        secret.toFile().deleteOnExit()
        Files.writeString(secret, 'JMIX_XXE_CANARY')

        def xml = """<?xml version="1.0" encoding="UTF-8"?>
                           <!DOCTYPE root [
                             <!ENTITY xxe SYSTEM "${secret.toUri()}">
                           ]>
                           <root>&xxe;</root>
                           """

        String text = null
        RuntimeException exception = null

        when:
        try {
            text = Dom4j.readDocument(xml).rootElement.text
        } catch (RuntimeException e) {
            exception = e
        }

        then:
        exception != null || text != 'JMIX_XXE_CANARY'
    }
}