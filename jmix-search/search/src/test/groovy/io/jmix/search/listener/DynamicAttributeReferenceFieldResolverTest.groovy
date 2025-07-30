package io.jmix.search.listener

import io.jmix.core.MetadataTools
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaProperty
import spock.lang.Specification

class DynamicAttributeReferenceFieldResolverTest extends Specification {

    def "GetFieldName. Composite key"() {
        given:
        def metaClass = Mock(MetaClass)
        metaClass.getName() >> "some_EntityName"

        and:
        def metadataTools = Mock(MetadataTools)
        metadataTools.hasCompositePrimaryKey(metaClass) >> true

        when:
        def resolver = new DynamicAttributeReferenceFieldResolver(metadataTools)
        resolver.getFieldName(metaClass)

        then:
        def e = thrown(IllegalStateException)
        e.message == "Composite keys are not supported in the dynamic attributes. The entity type is some_EntityName."
    }

    def "GetFieldName. Supported type"() {
        given:
        def metaClass = Mock(MetaClass)

        and:
        def metaProperty = Mock(MetaProperty)
        metaProperty.getJavaType() >> keyClass

        and:
        def metadataTools = Mock(MetadataTools)
        metadataTools.hasCompositePrimaryKey(metaClass) >> false
        metadataTools.getPrimaryKeyProperty(metaClass) >> metaProperty

        when:
        def resolver = new DynamicAttributeReferenceFieldResolver(metadataTools)
        def resolvedFieldName = resolver.getFieldName(metaClass)

        then:
        resolvedFieldName == fieldName

        where:
        keyClass   || fieldName
        UUID.class || "entityId"
        String.class || "stringEntityId"
        Integer.class || "intEntityId"
        Long.class || "longEntityId"
    }

}
