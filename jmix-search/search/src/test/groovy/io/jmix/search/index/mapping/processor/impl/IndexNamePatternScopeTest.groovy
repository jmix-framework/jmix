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

import io.jmix.core.MetadataTools
import io.jmix.core.impl.method.ContextArgumentResolverComposite
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.search.index.IndexNameGenerator
import io.jmix.search.index.impl.IndexLayout
import spock.lang.Specification

/**
 * Which rule the declared index name of an entity is judged by.
 *
 * <p>A pattern of a split entity has to carry {@code {tenantId}} and a pattern of an unsplit one must not, so
 * the check has to ask the same question the layout answers when it builds the name. Asking a different one -
 * the application-wide switch, say, which knows nothing about the store of the entity - leaves an entity for
 * which no pattern is right: one spelling is rejected at startup, the other produces a name with an empty tail.
 */
class IndexNamePatternScopeTest extends Specification {

    static final String SOURCE = "Index definition OrderIndexDefinition of entity 'demo_Order'"

    def indexNameGenerator = Mock(IndexNameGenerator)
    def indexLayout = Mock(IndexLayout)
    def orderMetaClass = Mock(MetaClass) { getName() >> "demo_Order" }

    def "the pattern of a split entity is judged by the rule of a split index"() {
        given:
        indexLayout.isSplitByTenants(orderMetaClass) >> true

        when:
        processor().resolveIndexNamePattern("orders_{tenantId}", SOURCE, orderMetaClass)

        then:
        1 * indexNameGenerator.validateEntityIndexNamePattern("orders_{tenantId}", SOURCE, true)
    }

    def "the pattern of an entity the layout does not split is judged by the rule of a single index"() {
        given: """the entity carries a tenant attribute and the mode is on, but its data is not stored in JPA,
                  so the layout gives it one shared index - and a pattern for one index is what it needs"""
        indexLayout.isSplitByTenants(orderMetaClass) >> false

        when:
        processor().resolveIndexNamePattern("orders", SOURCE, orderMetaClass)

        then:
        1 * indexNameGenerator.validateEntityIndexNamePattern("orders", SOURCE, false)
    }

    def "an entity that declares no pattern is not checked at all"() {
        when:
        def resolved = processor().resolveIndexNamePattern(declared, SOURCE, orderMetaClass)

        then:
        resolved == null
        0 * indexNameGenerator._

        where:
        declared << [null, "", "   "]
    }

    protected AnnotatedIndexDefinitionProcessor processor() {
        def processor = new AnnotatedIndexDefinitionProcessor(
                null, null, null, null, Mock(ContextArgumentResolverComposite),
                List.of(), null, Mock(MetadataTools), indexNameGenerator)
        processor.indexLayout = indexLayout
        return processor
    }
}
