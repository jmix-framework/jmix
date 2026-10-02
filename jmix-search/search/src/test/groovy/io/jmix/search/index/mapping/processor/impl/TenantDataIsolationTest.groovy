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

package io.jmix.search.index.mapping.processor.impl

import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.core.metamodel.model.Range
import io.jmix.core.MetadataTools
import io.jmix.search.exception.IndexDefinitionRejectedException
import io.jmix.search.index.impl.IndexLayout
import io.jmix.search.index.mapping.DisplayedNameDescriptor
import io.jmix.search.index.mapping.IndexMappingConfiguration
import io.jmix.search.index.mapping.MappingFieldDescriptor
import io.jmix.core.impl.method.ContextArgumentResolverComposite
import spock.lang.Specification

/**
 * A value of a mapped property is copied into the document, so an entity stored in a single index shared by all
 * tenants must not map anything that belongs to a tenant.
 */
class TenantDataIsolationTest extends Specification {

    MetaClass sharedEntity = metaClass("demo_Product")
    MetaClass tenantEntity = metaClass("demo_Supplier")
    MetaClass anotherSharedEntity = metaClass("demo_Category")

    MetadataTools metadataTools = Mock(MetadataTools)
    IndexLayout indexLayout = Mock(IndexLayout)

    def setup() {
        metadataTools.isTenantAware(sharedEntity) >> false
        metadataTools.isTenantAware(anotherSharedEntity) >> false
        metadataTools.isTenantAware(tenantEntity) >> true
        indexLayout.isSplitByTenantsEnabled() >> true
    }

    def "a shared index that maps a property of a tenant-aware entity is rejected"() {
        given:
        def mapping = mappingOf(sharedEntity, ["supplier.name": pathThrough(tenantEntity)], [])

        when:
        processor().checkNoTenantDataInSharedIndex(ProductIndexDefinition.simpleName, mapping)

        then:
        def exception = thrown(IndexDefinitionRejectedException)
        exception.message.contains("demo_Product")
        exception.message.contains("demo_Supplier")
        exception.message.contains("supplier.name")
        exception.message.contains("ProductIndexDefinition")
    }

    def "a tenant-aware entity reached through the instance name is rejected too"() {
        given: "the instance name of the shared entity is built from a property of the tenant-aware one"
        def mapping = mappingOf(sharedEntity, [:], [pathThrough(tenantEntity)])

        when:
        processor().checkNoTenantDataInSharedIndex(ProductIndexDefinition.simpleName, mapping)

        then:
        thrown(IndexDefinitionRejectedException)
    }

    def "a tenant-aware entity may map anything: its index belongs to one tenant"() {
        given:
        def mapping = mappingOf(tenantEntity, ["customer.name": pathThrough(tenantEntity)], [])

        when:
        processor().checkNoTenantDataInSharedIndex(ProductIndexDefinition.simpleName, mapping)

        then:
        noExceptionThrown()
    }

    def "a shared index that maps another shared entity is fine"() {
        given:
        def mapping = mappingOf(sharedEntity, ["category.name": pathThrough(anotherSharedEntity)], [])

        when:
        processor().checkNoTenantDataInSharedIndex(ProductIndexDefinition.simpleName, mapping)

        then:
        noExceptionThrown()
    }

    def "nothing is checked when the application manages no tenants"() {
        given:
        def layoutWithoutTenants = Mock(IndexLayout)
        layoutWithoutTenants.isSplitByTenantsEnabled() >> false
        def mapping = mappingOf(sharedEntity, ["supplier.name": pathThrough(tenantEntity)], [])

        when:
        processor(layoutWithoutTenants).checkNoTenantDataInSharedIndex(ProductIndexDefinition.simpleName, mapping)

        then: "there are no tenants, so there is nothing to leak between"
        noExceptionThrown()
    }

    protected AnnotatedIndexDefinitionProcessor processor(IndexLayout layout = indexLayout) {
        def processor = new AnnotatedIndexDefinitionProcessor(
                null, null, null, null, Mock(ContextArgumentResolverComposite),
                List.of(), null, metadataTools, null)
        processor.indexLayout = layout
        return processor
    }

    protected MetaClass metaClass(String name) {
        def metaClass = Mock(MetaClass)
        metaClass.getName() >> name
        return metaClass
    }

    /**
     * A property path that steps into the given entity and then reads a simple property of it.
     */
    protected MetaPropertyPath pathThrough(MetaClass referencedEntity) {
        def referenceRange = Mock(Range)
        referenceRange.isClass() >> true
        referenceRange.asClass() >> referencedEntity
        def referenceProperty = Mock(MetaProperty)
        referenceProperty.getRange() >> referenceRange

        def simpleRange = Mock(Range)
        simpleRange.isClass() >> false
        def simpleProperty = Mock(MetaProperty)
        simpleProperty.getRange() >> simpleRange

        def path = Mock(MetaPropertyPath)
        path.getMetaProperties() >> ([referenceProperty, simpleProperty] as MetaProperty[])
        path.toPathString() >> "instanceNameProperty"
        return path
    }

    protected IndexMappingConfiguration mappingOf(MetaClass root,
                                                 Map<String, MetaPropertyPath> fields,
                                                 List<MetaPropertyPath> instanceNameProperties) {
        def fieldDescriptors = fields.collectEntries { propertyName, path ->
            def descriptor = Mock(MappingFieldDescriptor)
            descriptor.getMetaPropertyPath() >> path
            descriptor.getEntityPropertyFullName() >> propertyName
            [(propertyName): descriptor]
        }

        def displayedNameDescriptor = Mock(DisplayedNameDescriptor)
        displayedNameDescriptor.getInstanceNameRelatedProperties() >> instanceNameProperties

        def mapping = Mock(IndexMappingConfiguration)
        mapping.getEntityMetaClass() >> root
        mapping.getFields() >> fieldDescriptors
        mapping.getDisplayedNameDescriptor() >> displayedNameDescriptor
        return mapping
    }
}

/**
 * Stands for an index definition interface: the check only needs its name for the error message.
 */
interface ProductIndexDefinition {
}
