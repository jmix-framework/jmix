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

package io.jmix.search.index.impl

import spock.lang.Specification

class IndexStateRegistryTest extends Specification {

    def "index is unavailable until it is marked as available"() {
        given:
        def registry = new IndexStateRegistry()

        expect:
        !registry.isIndexAvailable("order_index")
    }

    def "markIndexAsAvailable marks index as available"() {
        given:
        def registry = new IndexStateRegistry()

        when:
        registry.markIndexAsAvailable("order_index")

        then:
        registry.isIndexAvailable("order_index")
    }

    def "markIndexAsUnavailable marks index as unavailable"() {
        given:
        def registry = new IndexStateRegistry()
        registry.markIndexAsAvailable("order_index")

        when:
        registry.markIndexAsUnavailable("order_index")

        then:
        !registry.isIndexAvailable("order_index")
    }

    def "availability of one index doesn't affect the others"() {
        given:
        def registry = new IndexStateRegistry()

        when:
        registry.markIndexAsAvailable("order_index")

        then:
        registry.isIndexAvailable("order_index")
        !registry.isIndexAvailable("customer_index")
    }

    def "clean makes all indexes unavailable"() {
        given:
        def registry = new IndexStateRegistry()
        registry.markIndexAsAvailable("order_index")
        registry.markIndexAsAvailable("customer_index")

        when:
        registry.clean()

        then:
        !registry.isIndexAvailable("order_index")
        !registry.isIndexAvailable("customer_index")
    }

    def "clean on empty registry works without errors"() {
        given:
        def registry = new IndexStateRegistry()

        when:
        registry.clean()

        then:
        noExceptionThrown()
        !registry.isIndexAvailable("order_index")
    }

    def "index can be marked as available after clean"() {
        given:
        def registry = new IndexStateRegistry()
        registry.markIndexAsAvailable("order_index")

        when:
        registry.clean()
        registry.markIndexAsAvailable("customer_index")

        then:
        !registry.isIndexAvailable("order_index")
        registry.isIndexAvailable("customer_index")
    }
}
