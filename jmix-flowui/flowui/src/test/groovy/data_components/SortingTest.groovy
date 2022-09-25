/*
 * Copyright (c) 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package data_components


import io.jmix.core.DataManager
import io.jmix.core.LoadContext
import io.jmix.core.Metadata
import io.jmix.core.Sort
import io.jmix.flowui.UiComponents
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.CollectionLoader
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class SortingTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents
    @Autowired
    Metadata metadata

    private CollectionContainer<Order> container
    private CollectionLoader loader

    @Override
    void setup() {
        container = dataComponents.createCollectionContainer(Order)
        loader = dataComponents.createCollectionLoader()
        loader.setContainer(container)
    }

    def "sort in memory when all data is loaded"() {

        def o1 = metadata.create(Order)
        o1.number = 'o1'
        def o2 = metadata.create(Order)
        o2.number = 'o2'

        def orders = [o1, o2]

        def dataManager = Mock(DataManager)

        when:

        loader.dataManager = dataManager
        loader.setQuery('select e from test_Order e')
        loader.setFirstResult(0)
        loader.setMaxResults(3)
        loader.load()

        then:
        1 * dataManager.loadList(_ as LoadContext) >> orders
        container.items[0].number == 'o1'

        when:

        container.getSorter().sort(Sort.by(Sort.Direction.DESC, 'number'))

        then:

        0 * dataManager.loadList(_ as LoadContext) >> orders
        container.items[0].number == 'o2'
    }

    def "sort on middleware when not all data is loaded"() {
        def o1 = metadata.create(Order)
        o1.number = 'o1'
        def o2 = metadata.create(Order)
        o2.number = 'o2'
        def o3 = metadata.create(Order)
        o3.number = 'o3'

        def orders = [o1, o2, o3]

        def dataManager = Mock(DataManager)

        when:

        loader.dataManager = dataManager
        loader.setQuery('select e from test_Order e')
        loader.setFirstResult(0)
        loader.setMaxResults(3)
        loader.load()

        then:

        1 * dataManager.loadList(_ as LoadContext) >> orders
        container.items[0].number == 'o1'

        when:

        container.getSorter().sort(Sort.by(Sort.Direction.DESC, 'number'))

        then:

        1 * dataManager.loadList(_ as LoadContext) >> orders.sort { it.number }.reverse()
        container.items[0].number == 'o3'
    }
}
