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
import io.jmix.core.Metadata
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.search.index.EntityDeletionTarget
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.IndexResult
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.mapping.IndexMappingConfiguration
import spock.lang.Specification
import test_support.entity.TestRootEntity

/**
 * The tenant of a deleted record cannot be determined after the fact, so it travels with the deletion. When it does
 * arrive, the document is deleted from that tenant's index alone; when it doesn't, it is deleted from every index of
 * the entity, because the document lives in exactly one of them and the rest report it as missing.
 */
class EntityDeletionRoutingTest extends Specification {

    static final String ENTITY_NAME = "test_TestRootEntity"
    static final String SHARED_INDEX = "search_index_test_rootentity"
    static final String INDEX_A = "search_index_test_rootentity_tenanta"
    static final String INDEX_B = "search_index_test_rootentity_tenantb"

    def indexLayout = Mock(IndexLayout)
    def idSerialization = Mock(IdSerialization)
    def metadata = Mock(Metadata)
    def indexConfigurationManager = Mock(IndexConfigurationManager)
    def configuration = Mock(IndexConfiguration)

    def setup() {
        def metaClass = Mock(MetaClass)
        metaClass.getName() >> ENTITY_NAME
        metadata.getClass(TestRootEntity) >> metaClass
        configuration.getEntityName() >> ENTITY_NAME
        indexConfigurationManager.getIndexConfigurationByEntityNameOpt(ENTITY_NAME) >> Optional.of(configuration)
        idSerialization.idToString(_) >> { Id id -> id.getValue().toString() }
    }

    def "a deletion that carries its tenant goes to that tenant's index alone"() {
        given:
        splitByTenants()
        def record = new TestRootEntity()

        when:
        def documents = documentsToDelete(new EntityDeletionTarget(Id.of(record), "tenanta"))

        then:
        documents.collect { it.indexName() } == [INDEX_A]
        documents.every { it.entityId() == record.id.toString() }
    }

    def "a deletion without a tenant goes to every index of the entity"() {
        given:
        splitByTenants()
        def record = new TestRootEntity()

        when:
        def documents = documentsToDelete(EntityDeletionTarget.tenantUnknown(Id.of(record)))

        then: "the document lives in one of them, the others report it as missing"
        documents.collect { it.indexName() }.toSorted() == [INDEX_A, INDEX_B]
    }

    def "an entity that is not split by tenants is deleted from its single index either way"() {
        given:
        notSplitByTenants()
        def record = new TestRootEntity()

        expect:
        documentsToDelete(target).collect { it.indexName() } == [SHARED_INDEX]

        where:
        target << [EntityDeletionTarget.tenantUnknown(Id.of(new TestRootEntity())),
                   new EntityDeletionTarget(Id.of(new TestRootEntity()), "tenanta")]
    }

    def "a tenant with no index of its own leaves nothing to delete"() {
        given:
        splitByTenants()
        indexLayout.indexName(configuration, "gone") >> null

        expect:
        documentsToDelete(new EntityDeletionTarget(Id.of(new TestRootEntity()), "gone")).isEmpty()
    }

    protected void splitByTenants() {
        indexLayout.isSplitByTenants(configuration) >> true
        indexLayout.indexName(configuration, "tenanta") >> INDEX_A
        indexLayout.indexName(configuration, "tenantb") >> INDEX_B
        indexLayout.allIndexes(configuration) >> [new IndexLayout.TenantIndex("tenanta", INDEX_A),
                                                  new IndexLayout.TenantIndex("tenantb", INDEX_B)]
    }

    protected void notSplitByTenants() {
        indexLayout.isSplitByTenants(configuration) >> false
        indexLayout.indexName(configuration, _) >> SHARED_INDEX
        indexLayout.allIndexes(configuration) >> [new IndexLayout.TenantIndex(null, SHARED_INDEX)]
    }

    protected List<BaseEntityIndexer.DocumentToDelete> documentsToDelete(EntityDeletionTarget target) {
        def prepared = indexer().prepareIndexIdsByTargets([target])
        return prepared.isEmpty() ? [] : new ArrayList<>(prepared.get(configuration))
    }

    protected BaseEntityIndexer indexer() {
        def indexer = new DeletingTestEntityIndexer(indexConfigurationManager, metadata, idSerialization)
        indexer.indexLayout = indexLayout
        return indexer
    }
}

class DeletingTestEntityIndexer extends BaseEntityIndexer {

    DeletingTestEntityIndexer(IndexConfigurationManager indexConfigurationManager,
                              Metadata metadata,
                              IdSerialization idSerialization) {
        super(null, null, indexConfigurationManager, metadata, idSerialization, null, null, null, null, null)
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
