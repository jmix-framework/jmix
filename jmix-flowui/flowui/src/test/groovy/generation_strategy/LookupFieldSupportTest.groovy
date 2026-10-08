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
package generation_strategy

import io.jmix.core.Metadata
import io.jmix.core.entity.annotation.LookupType
import io.jmix.flowui.component.factory.EffectiveLookupConfig
import io.jmix.flowui.component.factory.EffectiveLookupConfig.ItemsMode
import io.jmix.flowui.UiComponentProperties
import io.jmix.flowui.component.factory.LookupFieldSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.lookup_field.LfOrder
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class LookupFieldSupportTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata
    @Autowired
    LookupFieldSupport lookupFieldSupport
    @Autowired
    UiComponentProperties componentProperties

    void cleanup() {
        componentProperties.entityFieldActions.remove('test_LfCity')
    }

    EffectiveLookupConfig resolve(String property) {
        def prop = metadata.getClass(LfOrder).getProperty(property)
        return lookupFieldSupport.resolve(prop, prop.range.asClass())
    }

    def "class-level DROPDOWN with no itemsQuery resolves to EAGER"() {
        when:
        def c = resolve('country')

        then:
        c.componentType() == LookupType.DROPDOWN
        !c.fieldLevel()
        c.itemsMode() == ItemsMode.EAGER
        c.actions().isEmpty()
    }

    def "field-level VIEW beats class-level annotation and is marked fieldLevel"() {
        when:
        def c = resolve('viewCountry')

        then:
        c.componentType() == LookupType.VIEW
        c.fieldLevel()
    }

    def "class-level VIEW carries annotation actions"() {
        when:
        def c = resolve('city')

        then:
        c.componentType() == LookupType.VIEW
        c.actions() == ['entity_lookup', 'entity_open', 'entity_clear']
    }

    def "field-level VIEW actions win"() {
        when:
        def c = resolve('viewCity')

        then:
        c.componentType() == LookupType.VIEW
        c.fieldLevel()
        c.actions() == ['entity_open']
    }

    def "byInstanceName itemsQuery resolves to BY_INSTANCE_NAME"() {
        when:
        def c = resolve('product')

        then:
        c.componentType() == LookupType.DROPDOWN
        c.itemsMode() == ItemsMode.BY_INSTANCE_NAME
        c.searchStringFormat() == null
    }

    def "explicit query with searchString resolves to QUERY"() {
        when:
        def c = resolve('supplier')

        then:
        c.componentType() == LookupType.DROPDOWN
        c.itemsMode() == ItemsMode.QUERY
        c.query().contains(':searchString')
    }

    def "explicit query without searchString downgrades to EAGER"() {
        when:
        def c = resolve('broken')

        then:
        c.componentType() == LookupType.DROPDOWN
        c.itemsMode() == ItemsMode.EAGER
    }

    def "itemsQuery on VIEW type is ignored"() {
        when:
        def c = resolve('tag')

        then:
        c.componentType() == LookupType.VIEW
        c.itemsMode() == ItemsMode.EAGER
        c.query() == null
    }

    def "entity-field-actions property beats class-level annotation actions"() {
        given:
        componentProperties.entityFieldActions['test_LfCity'] = ['entity_clear']

        when:
        def c = resolve('city')

        then:
        c.componentType() == LookupType.VIEW
        c.actions() == ['entity_clear']
    }

    def "field-level annotation actions beat entity-field-actions property"() {
        given:
        componentProperties.entityFieldActions['test_LfCity'] = ['entity_clear']

        when:
        def c = resolve('viewCity')

        then:
        c.componentType() == LookupType.VIEW
        c.fieldLevel()
        c.actions() == ['entity_open']
    }
}
