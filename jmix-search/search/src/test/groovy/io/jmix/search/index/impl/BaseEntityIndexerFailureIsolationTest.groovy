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

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.jmix.core.Id
import io.jmix.core.IdSerialization
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.IndexResult
import io.jmix.search.index.mapping.IndexMappingConfiguration
import spock.lang.Specification

import java.util.function.Predicate
import test_support.entity.TestRootEntity

/**
 * Everything that turns an instance into a document is application code, so one instance can throw. It must not
 * take the rest of the batch with it: the exception would otherwise leave queue processing altogether, and the
 * next run would take the same batch and throw again.
 */
class BaseEntityIndexerFailureIsolationTest extends Specification {

    static final String INDEX_NAME = "test_root_entity_index"

    def indexStateRegistry = Mock(IndexStateRegistry)
    def indexLayout = Mock(IndexLayout)
    def idSerialization = Mock(IdSerialization)

    def "a failing instance is reported as a failure and the rest of the batch is still indexed"() {
        given:
        def failing = new TestRootEntity()
        def healthy = new TestRootEntity()
        def configuration = configurationRejecting(failing)

        and:
        def indexer = indexer()
        def documents = []
        def postponed = []

        when:
        indexer.addDocuments(Map.entry(configuration, [failing, healthy]), configuration, documents, postponed)

        then: "the healthy instance is on its way to the engine"
        documents.size() == 1

        and: "the failing one is reported, so it stays in the queue instead of being considered processed"
        postponed.size() == 1
        postponed[0].id == failing.id.toString()
        postponed[0].index == INDEX_NAME
        postponed[0].cause.contains("broken instance")
    }

    def "a failure of every instance leaves the batch empty rather than propagating"() {
        given:
        def first = new TestRootEntity()
        def second = new TestRootEntity()
        def configuration = configurationRejecting(first, second)

        and:
        def documents = []
        def postponed = []

        when:
        indexer().addDocuments(Map.entry(configuration, [first, second]), configuration, documents, postponed)

        then:
        noExceptionThrown()
        documents.isEmpty()
        postponed.size() == 2
    }

    protected IndexConfiguration configurationRejecting(TestRootEntity... rejected) {
        def rejectedIds = rejected.collect { it.id } as Set
        def configuration = Mock(IndexConfiguration)
        configuration.getEntityName() >> "test_TestRootEntity"
        configuration.getMapping() >> Mock(IndexMappingConfiguration)
        Predicate<Object> indexablePredicate = { instance ->
            if (rejectedIds.contains(((TestRootEntity) instance).id)) {
                throw new IllegalStateException("broken instance")
            }
            return true
        } as Predicate
        configuration.getIndexablePredicate() >> indexablePredicate
        indexLayout.isSplitByTenants(configuration) >> false
        indexLayout.indexName(configuration, null) >> INDEX_NAME
        indexStateRegistry.isIndexAvailable(INDEX_NAME) >> true
        return configuration
    }

    protected BaseEntityIndexer indexer() {
        idSerialization.idToString(_) >> { Id id -> id.getValue().toString() }
        def indexer = new TestEntityIndexer(idSerialization, indexStateRegistry)
        indexer.indexLayout = indexLayout
        return indexer
    }
}

class TestEntityIndexer extends BaseEntityIndexer {

    TestEntityIndexer(IdSerialization idSerialization, IndexStateRegistry indexStateRegistry) {
        super(null, null, null, null, idSerialization, indexStateRegistry, null, null, null, null)
    }

    @Override
    protected IndexDocumentData generateIndexDocument(IndexMappingConfiguration mappingConfiguration,
                                                      String indexName,
                                                      Object instance) {
        return new IndexDocumentData(indexName, ((TestRootEntity) instance).id.toString(),
                JsonNodeFactory.instance.objectNode())
    }

    @Override
    protected IndexResult indexDocuments(List<IndexDocumentData> documents) {
        return new IndexResult(documents.size(), List.of())
    }

    @Override
    protected IndexResult deleteByGroupedDocIds(List<DocumentToDelete> documents) {
        return new IndexResult(documents.size(), List.of())
    }
}
