/*
 * Copyright 2026 Haulmont.
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

package component.codeeditor

import spock.lang.Specification

class CodeEditorStylesTest extends Specification {

    def "Content styles reference images only by data URI"() {
        def css = getClass().getResource('/META-INF/frontend/src/code-editor/styles/jmix-code-editor-content-styles.js').text

        when: "url() values of the shadow styles are collected"
        def urls = (css =~ /url\(\s*["']?([^"')\s]*)/).collect { it[1] }

        then: "each one is a data URI, since a relative URL resolves against the page and points to no file"
        !urls.isEmpty()
        urls.findAll { !it.startsWith('data:') } == []
    }
}
