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

import com.vaadin.flow.data.provider.BackEndDataProvider
import com.vaadin.flow.data.provider.Query
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.security.SystemAuthenticator
import io.jmix.flowui.UiComponentProperties
import io.jmix.flowui.component.ComponentGenerationContext
import io.jmix.flowui.component.UiComponentsGenerator
import io.jmix.flowui.component.combobox.EntityComboBox
import io.jmix.flowui.component.valuepicker.EntityPicker
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.lookup_field.LfMemNote
import test_support.entity.lookup_field.LfMemNoteHolder
import test_support.entity.lookup_field.LfNoteHolder
import test_support.entity.lookup_field.LfOrder
import test_support.entity.lookup_field.LfPerson
import test_support.entity.lookup_field.LfProduct
import test_support.entity.lookup_field.LfSupplier
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class LookupFieldAnnotationGenerationTest extends FlowuiTestSpecification {

    @Autowired
    UiComponentsGenerator uiComponentsGenerator
    @Autowired
    Metadata metadata
    @Autowired
    DataComponents dataComponents
    @Autowired
    UiComponentProperties componentProperties
    @Autowired
    DataManager dataManager
    @Autowired
    SystemAuthenticator systemAuthenticator
    @Autowired
    JdbcTemplate jdbcTemplate

    void cleanup() {
        componentProperties.entityFieldFqn.remove('test_LfCity')
        componentProperties.entityFieldActions.remove('test_LfCity')
    }

    protected generate(String property) {
        def metaClass = metadata.getClass(LfOrder)
        def context = new ComponentGenerationContext(metaClass, property)
        def container = dataComponents.createInstanceContainer(LfOrder)
        context.setValueSource(new ContainerValueSource(container, property))
        return uiComponentsGenerator.generate(context)
    }

    def "class-level DROPDOWN produces EntityComboBox"() {
        expect:
        generate('country') instanceof EntityComboBox
    }

    def "field-level annotation beats class-level annotation"() {
        expect:
        generate('viewCountry') instanceof EntityPicker
        generate('dropdownCity') instanceof EntityComboBox
    }

    def "class-level VIEW produces EntityPicker with annotation actions"() {
        when:
        def field = generate('city')

        then:
        field instanceof EntityPicker
        (field as EntityPicker).actions*.id == ['entity_lookup', 'entity_open', 'entity_clear']
    }

    def "application property beats class-level annotation"() {
        when:
        componentProperties.entityFieldFqn['test_LfCity'] =
                'io.jmix.flowui.component.combobox.EntityComboBox'
        def field = generate('city')

        then:
        field instanceof EntityComboBox
    }

    def "field-level annotation beats application property"() {
        when:
        componentProperties.entityFieldFqn['test_LfCity'] =
                'io.jmix.flowui.component.combobox.EntityComboBox'
        def field = generate('viewCity')

        then:
        field instanceof EntityPicker
        (field as EntityPicker).actions*.id == ['entity_open']
    }

    def "property actions beat class-level annotation actions"() {
        when:
        componentProperties.entityFieldActions['test_LfCity'] = ['entity_clear']
        def field = generate('city')

        then:
        (field as EntityPicker).actions*.id == ['entity_clear']
    }

    def "field-level annotation actions beat property actions"() {
        when:
        componentProperties.entityFieldActions['test_LfCity'] = ['entity_clear']
        def field = generate('viewCity')

        then:
        (field as EntityPicker).actions*.id == ['entity_open']
    }

    def "byInstanceName produces lazy callback that filters by instance name"() {
        setup:
        systemAuthenticator.runWithSystem {
            ['Apple', 'Apricot', 'Banana'].each { name ->
                def p = dataManager.create(LfProduct)
                p.name = name
                dataManager.save(p)
            }
        }

        when:
        def field = generate('product') as EntityComboBox

        then:
        field.dataProvider instanceof BackEndDataProvider

        when:
        def items = systemAuthenticator.withSystem {
            field.dataProvider.fetch(new Query(0, 10, null, null, 'ap')).toList()
        }

        then:
        items*.name == ['Apple', 'Apricot']

        cleanup:
        jdbcTemplate.execute('delete from TEST_LF_PRODUCT')
    }

    def "byInstanceName with searchStringFormat ignores the format"() {
        setup: "LfProduct also configures a searchStringFormat, which must be ignored"
        systemAuthenticator.runWithSystem {
            ['Apple', 'Apricot', 'Banana'].each { name ->
                def p = dataManager.create(LfProduct)
                p.name = name
                dataManager.save(p)
            }
        }

        when:
        def field = generate('product') as EntityComboBox
        def items = systemAuthenticator.withSystem {
            field.dataProvider.fetch(new Query(0, 10, null, null, 'ap')).toList()
        }

        then: "matching is a plain case-insensitive substring search regardless of searchStringFormat"
        items*.name == ['Apple', 'Apricot']

        cleanup:
        jdbcTemplate.execute('delete from TEST_LF_PRODUCT')
    }

    def "byInstanceName works for DTO entity in a custom data store"() {
        setup:
        def notes = []
        systemAuthenticator.runWithSystem {
            ['Alpha note', 'Beta note', 'Alphabet'].each { t ->
                def n = dataManager.create(LfMemNote)
                n.title = t
                dataManager.save(n)
                notes << n
            }
        }

        when:
        def metaClass = metadata.getClass(LfMemNoteHolder)
        def context = new ComponentGenerationContext(metaClass, 'note')
        def container = dataComponents.createInstanceContainer(LfMemNoteHolder)
        context.setValueSource(new ContainerValueSource(container, 'note'))
        def field = uiComponentsGenerator.generate(context) as EntityComboBox

        then:
        field.dataProvider instanceof BackEndDataProvider

        when:
        def items = systemAuthenticator.withSystem {
            field.dataProvider.fetch(new Query(0, 10, null, null, 'alpha')).toList()
        }

        then:
        items*.title == ['Alpha note', 'Alphabet']

        cleanup: "remove mem1 instances"
        systemAuthenticator.runWithSystem {
            notes.each { dataManager.remove(it) }
        }
    }

    def "byInstanceName infers query from method-based instance name over string properties"() {
        setup:
        systemAuthenticator.runWithSystem {
            [['John', 'Smith'], ['Jane', 'Brown'], ['Jack', 'Jones']].each { first, last ->
                def p = dataManager.create(LfPerson)
                p.firstName = first
                p.lastName = last
                dataManager.save(p)
            }
        }

        when: "search matches either firstName or lastName"
        def field = generate('person') as EntityComboBox
        def items = systemAuthenticator.withSystem {
            field.dataProvider.fetch(new Query(0, 10, null, null, 'j')).toList()
        }

        then:
        items.size() == 3

        cleanup:
        jdbcTemplate.execute('delete from TEST_LF_PERSON')
    }

    def "explicit query wins over byInstanceName and filters as written"() {
        setup:
        systemAuthenticator.runWithSystem {
            [['Acme', true], ['Apex', false]].each { name, active ->
                def s = dataManager.create(LfSupplier)
                s.name = name
                s.active = active
                dataManager.save(s)
            }
        }

        when:
        def field = generate('supplier') as EntityComboBox
        def items = systemAuthenticator.withSystem {
            field.dataProvider.fetch(new Query(0, 10, null, null, 'a')).toList()
        }

        then: "inactive supplier is excluded by the explicit query"
        items*.name == ['Acme']

        cleanup:
        jdbcTemplate.execute('delete from TEST_LF_SUPPLIER')
    }

    def "byInstanceName with no string instance-name properties degrades to eager"() {
        when:
        def field = generate('event') as EntityComboBox

        then:
        !(field.dataProvider instanceof BackEndDataProvider)
    }

    def "explicit query without searchString parameter degrades to eager"() {
        when:
        def field = generate('broken') as EntityComboBox

        then:
        !(field.dataProvider instanceof BackEndDataProvider)
    }

    def "itemsQuery on VIEW type is ignored"() {
        expect:
        generate('tag') instanceof EntityPicker
    }

    def "DROPDOWN on noop-store entity degrades to view lookup"() {
        // LfNote has a byInstanceName itemsQuery configured, degradation must happen
        // regardless of it, because the noop store cannot load items at all
        when:
        def metaClass = metadata.getClass(LfNoteHolder)
        def context = new ComponentGenerationContext(metaClass, 'note')
        def container = dataComponents.createInstanceContainer(LfNoteHolder)
        context.setValueSource(new ContainerValueSource(container, 'note'))
        def field = uiComponentsGenerator.generate(context)

        then:
        field instanceof EntityPicker
    }
}
