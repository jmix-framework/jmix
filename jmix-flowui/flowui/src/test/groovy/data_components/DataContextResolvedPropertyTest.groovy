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

package data_components

import io.jmix.core.DataManager
import io.jmix.core.entity.EntityValues
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.DataContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.TestResolvedPropertyConfiguration
import test_support.TestResolvedPropertyState
import test_support.entity.sales.Order
import test_support.spec.DataContextSpec

import static test_support.TestResolvedPropertyState.PROPERTY

/**
 * Changes of a property that is not in the entity's own metadata but is resolved by a
 * {@code MetaPropertyPathResolver}, like a dynamic attribute, see jmix-framework/jmix#5930.
 */
@ContextConfiguration(classes = [TestResolvedPropertyConfiguration])
class DataContextResolvedPropertyTest extends DataContextSpec {

    @Autowired
    DataComponents factory
    @Autowired
    DataManager dataManager

    def "a change of a resolved property is a modified attribute"() {
        DataContext context = factory.createDataContext()
        Order order = context.merge(savedOrder())

        when:
        EntityValues.setValue(order, PROPERTY, 'n1')

        then:
        context.getModifiedAttributes(order) == [PROPERTY] as Set

        when: "the value is set back"
        EntityValues.setValue(order, PROPERTY, null)

        then:
        context.getModifiedAttributes(order).isEmpty()
    }

    Order savedOrder() {
        Order order = makeSaved(dataManager.create(Order).tap { number = '1' })
        TestResolvedPropertyState.addTo(order)
        order
    }
}
