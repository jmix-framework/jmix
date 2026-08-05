/*
 * Copyright 2022 Haulmont.
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
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.CollectionLoader
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import test_support.entity.Foo
import test_support.entity.Zoo
import test_support.entity.element_collection.EcAlpha
import test_support.spec.DataContextSpec

import java.util.function.Consumer

class CollectionLoaderTest extends DataContextSpec {

    @Autowired
    DataManager dataManager
    @Autowired
    DataComponents factory

    def "successful load"() {
        CollectionLoader<Foo> loader = factory.createCollectionLoader()
        CollectionContainer<Foo> container = factory.createCollectionContainer(Foo)

        Consumer preLoadListener = Mock()
        loader.addPreLoadListener(preLoadListener)

        Consumer postLoadListener = Mock()
        loader.addPostLoadListener(postLoadListener)

        Foo foo = new Foo()
        dataManager.save(foo)

        when:

        loader.setContainer(container)
        loader.setQuery('select e from test_Foo e')
        loader.load()

        then:

        container.getItems() == [foo]

        1 * preLoadListener.accept(_)
        1 * postLoadListener.accept(_)

        cleanup:

        deleteRecord(foo)
    }

    def "prevent load by PreLoadEvent"() {
        CollectionLoader<Foo> loader = factory.createCollectionLoader()
        CollectionContainer<Foo> container = factory.createCollectionContainer(Foo)

        Consumer preLoadListener = { CollectionLoader.PreLoadEvent e -> e.preventLoad() }
        loader.addPreLoadListener(preLoadListener)

        Consumer postLoadListener = Mock()
        loader.addPostLoadListener(postLoadListener)

        Foo foo = new Foo()
        dataManager.save(foo)

        when:

        loader.setContainer(container)
        loader.setQuery('select e from test_Foo e')
        loader.load()

        then:

        container.getItems() == []

        0 * postLoadListener.accept(_)

        cleanup:

        deleteRecord(foo)
    }

    def "simplified queries"() {
        CollectionLoader<Foo> loader = factory.createCollectionLoader()
        CollectionContainer<Foo> container = factory.createCollectionContainer(Foo)
        loader.setContainer(container)

        Consumer<CollectionLoader.PreLoadEvent> preLoadListener = Mock()
        loader.addPreLoadListener(preLoadListener)

        when:

        loader.setQuery('from test_Foo f')
        loader.load()

        then:

        1 * preLoadListener.accept({ it.loadContext.query.queryString == 'select f from test_Foo f' })

        when:

        loader.setQuery('e.name = :name')
        loader.setParameter('name', 'name')
        loader.load()

        then:

        1 * preLoadListener.accept({ it.loadContext.query.queryString == 'select e from test_Foo e where e.name = :name' })
    }

    def "distinct is not set for a condition crossing a to-many association"() {
        CollectionLoader<Zoo> loader = factory.createCollectionLoader()
        CollectionContainer<Zoo> container = factory.createCollectionContainer(Zoo)
        loader.setContainer(container)
        loader.setQuery('select e from test_Zoo e')

        when: "condition on a nested attribute of a m2m collection"

        loader.setCondition(PropertyCondition.contains('animals.name', 'Rex'))
        def loadContext = loader.createLoadContext()

        then: "the condition is generated as an 'exists' subquery producing no duplicates, so no 'select distinct' \
is added (distinct fails on Oracle if the entity contains a LOB attribute)"

        !loadContext.query.distinct

        when: "condition on an own attribute"

        loader.setCondition(PropertyCondition.contains('name', 'Zoo'))
        loadContext = loader.createLoadContext()

        then:

        !loadContext.query.distinct
    }

    def "distinct is set for a condition on an element collection"() {
        CollectionLoader<EcAlpha> loader = factory.createCollectionLoader()
        CollectionContainer<EcAlpha> container = factory.createCollectionContainer(EcAlpha)
        loader.setContainer(container)
        loader.setQuery('select e from test_EcAlpha e')

        when:

        loader.setCondition(PropertyCondition.contains('tags', 'x'))
        def loadContext = loader.createLoadContext()

        then: "element collections keep join-based condition generation which can multiply rows"

        loadContext.query.distinct
    }
}
