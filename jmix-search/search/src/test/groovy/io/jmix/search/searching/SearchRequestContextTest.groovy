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

import io.jmix.search.index.IndexConfiguration
import spock.lang.Specification

import static io.jmix.search.searching.SearchRequestContext.ProcessingState.*


class SearchRequestContextTest extends Specification {

    def "RequestContext.getting initial parameters"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new SearchRequestContext<>(initialBuilder, initialSearchContext)

        when:
        def builder = requestContext.getRequestBuilder()
        def context = requestContext.getSearchContext()

        then:
        builder == initialBuilder
        context == initialSearchContext
        requestContext.getProcessingResult() == UNPROCESSED
        !requestContext.isRequestPossible()
        notThrown(Exception)
    }

    def 'RequestContext.getIndexesSearchData. INITIAL_STATE'() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new SearchRequestContext<>(initialBuilder, initialSearchContext)

        when:
        def builder = requestContext.getIndexSearchRequestScopes()

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "Request preparing is not finished."
    }

    def "RequestContext.getEffectiveIndexes. INITIAL_STATE"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new SearchRequestContext<>(initialBuilder, initialSearchContext)

        when:
        def builder = requestContext.getEffectiveIndexes()

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "Request preparing is not finished."
    }

    def 'RequestContext.getIndexesSearchData. NO_AVAILABLE_ENTITIES_FOR_SEARCHING'() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new SearchRequestContext<>(initialBuilder, initialSearchContext)

        when:
        requestContext.setEmptyResult()
        def builder = requestContext.getIndexSearchRequestScopes()

        then:
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "No entities for searching."
    }

    def "RequestContext.getEffectiveIndexes. NO_AVAILABLE_ENTITIES_FOR_SEARCHING"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new SearchRequestContext<>(initialBuilder, initialSearchContext)

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
        def requestContext = new SearchRequestContext<>(initialBuilder, initialSearchContext)
        and:
        IndexConfiguration configuration1 = Mock(IndexConfiguration)
        configuration1.getIndexName() >> "firstIndex"
        IndexConfiguration configuration2 = Mock(IndexConfiguration)
        configuration2.getIndexName() >> "secondIndex"
        and:
        List<IndexSearchRequestScope> scopes = List.of(
                new IndexSearchRequestScope(configuration1, Set.of("field1_1", "field1_2", "field1_3")),
                new IndexSearchRequestScope(configuration2, Set.of("field2_1"))
        )

        when:
        requestContext.setPositiveResult(scopes)
        def indexesWithFields = requestContext.getIndexSearchRequestScopes()
        def indexes = requestContext.getEffectiveIndexes()
        def builder = requestContext.getRequestBuilder()
        def context = requestContext.getSearchContext()

        then:
        indexes == Set.of("firstIndex", "secondIndex")
        indexesWithFields == scopes
        builder == initialBuilder
        context == initialSearchContext
        requestContext.getProcessingResult() == READY
        requestContext.isRequestPossible()
    }

    def "RequestContext.getting builder.NO_AVAILABLE_ENTITIES_FOR_SEARCHING"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new SearchRequestContext<>(initialBuilder, initialSearchContext)

        when:
        requestContext.setEmptyResult()
        requestContext.getRequestBuilder()

        then:
        requestContext.getProcessingResult() == NO_AVAILABLE_ENTITIES
        !requestContext.isRequestPossible()
        def exception = thrown(IllegalStateException)
        exception.getMessage() == "No entities for searching."
    }

    def "RequestContext.getting SearchContext.NO_AVAILABLE_ENTITIES_FOR_SEARCHING"() {
        given:
        def initialBuilder = new TestBuilder()
        def initialSearchContext = Mock(SearchContext)
        and:
        def requestContext = new SearchRequestContext<>(initialBuilder, initialSearchContext)

        when:
        requestContext.setEmptyResult()
        def context = requestContext.getSearchContext()

        then:
        context == initialSearchContext
        requestContext.getProcessingResult() == NO_AVAILABLE_ENTITIES
        !requestContext.isRequestPossible()
        notThrown(Exception)
    }

    private class TestBuilder {

    }
}
