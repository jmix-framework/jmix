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

package io.jmix.search.searching

import spock.lang.Specification

import static io.jmix.search.searching.SearchContextProcessingResult.*

class RequestContextTest extends Specification {

    def "RequestContext.getting initial parameters"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new RequestContext<>(initialBuilder, initialSearchContext)

        when:
        def builder = requestContext.getRequestBuilder()
        def context = requestContext.getSearchContext()

        then:
        builder == initialBuilder
        context == initialSearchContext
        requestContext.getProcessingResult() == INITIAL_STATE
        !requestContext.isRequestPossible()
        notThrown(Exception)
    }

    def "RequestContext.getEffectiveIndexesWithFields. INITIAL_STATE"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new RequestContext<>(initialBuilder, initialSearchContext)

        when:
        def builder = requestContext.getEffectiveIndexesWithFields()

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "Request preparing is not finished."
    }

    def "RequestContext.getEffectiveIndexes. INITIAL_STATE"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new RequestContext<>(initialBuilder, initialSearchContext)

        when:
        def builder = requestContext.getEffectiveIndexes()

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "Request preparing is not finished."
    }

    def "RequestContext.getEffectiveIndexesWithFields. NO_AVAILABLE_ENTITIES_FOR_SEARCHING"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new RequestContext<>(initialBuilder, initialSearchContext)

        when:
        requestContext.setEmptyResult()
        def builder = requestContext.getEffectiveIndexesWithFields()

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "No entities for searching."
    }

    def "RequestContext.getEffectiveIndexes. NO_AVAILABLE_ENTITIES_FOR_SEARCHING"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new RequestContext<>(initialBuilder, initialSearchContext)

        when:
        requestContext.setEmptyResult()
        requestContext.getEffectiveIndexes()

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "No entities for searching."
    }

    def "RequestContext. Normal flow with positive result"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new RequestContext<>(initialBuilder, initialSearchContext)
        and:
        Map<String, Set<String>> resultMap = Map.of(
                "firstIndex", Set.of("field1_1", "field1_2", "field1_3"),
                "secondIndex", Set.of("field2_1")
        )

        when:
        requestContext.setPositiveResult(resultMap)
        def indexesWithFields = requestContext.getEffectiveIndexesWithFields()
        def indexes = requestContext.getEffectiveIndexes()
        def builder = requestContext.getRequestBuilder()
        def context = requestContext.getSearchContext()

        then:
        indexes == Set.of("firstIndex", "secondIndex")
        indexesWithFields == resultMap
        builder == initialBuilder
        context == initialSearchContext
        requestContext.getProcessingResult() == REQUEST_IS_POSSIBLE
        requestContext.isRequestPossible()
    }

    def "RequestContext.getting builder.NO_AVAILABLE_ENTITIES_FOR_SEARCHING"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new RequestContext<>(initialBuilder, initialSearchContext)

        when:
        requestContext.setEmptyResult()
        requestContext.getRequestBuilder()

        then:
        requestContext.getProcessingResult() == NO_AVAILABLE_ENTITIES_FOR_SEARCHING
        !requestContext.isRequestPossible()
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "No entities for searching."
    }

    def "RequestContext.getting SearchContext.NO_AVAILABLE_ENTITIES_FOR_SEARCHING"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new RequestContext<>(initialBuilder, initialSearchContext)

        when:
        requestContext.setEmptyResult()
        def context = requestContext.getSearchContext()

        then:
        context == initialSearchContext
        requestContext.getProcessingResult() == NO_AVAILABLE_ENTITIES_FOR_SEARCHING
        !requestContext.isRequestPossible()
        notThrown(Exception)
    }

    private class TestBuilder {

    }
}
