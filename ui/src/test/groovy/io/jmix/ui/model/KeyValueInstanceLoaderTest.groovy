/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.model

import io.jmix.core.DataManager
import io.jmix.core.entity.KeyValueEntity
import io.jmix.data.PersistenceTools
import io.jmix.ui.test.DataContextSpec
import io.jmix.ui.test.entity.Foo

import javax.inject.Inject
import java.util.function.Consumer

class KeyValueInstanceLoaderTest extends DataContextSpec {

    @Inject DataManager dataManager
    @Inject DataComponents factory
    @Inject PersistenceTools persistenceTools

    def "successful load"() {
        KeyValueInstanceLoader loader = factory.createKeyValueInstanceLoader()
        KeyValueContainer container = factory.createKeyValueContainer()

        Consumer preLoadListener = Mock()
        loader.addPreLoadListener(preLoadListener)

        Consumer postLoadListener = Mock()
        loader.addPostLoadListener(postLoadListener)

        Foo foo = new Foo()
        dataManager.commit(foo)

        when:

        container.addProperty('id', UUID)
        container.addProperty('name', String)

        loader.setContainer(container)
        loader.setQuery('select e.id, e.name from test_Foo e where e.id = :id')
        loader.setParameter('id', foo.id)
        loader.load()

        then:

        container.getItem() instanceof KeyValueEntity
        container.getItem().getValue('id') == foo.id
        container.getItem().getValue('name') == foo.name


        1 * preLoadListener.accept(_)
        1 * postLoadListener.accept(_)

        cleanup:

        persistenceTools.deleteRecord(foo)
    }

    def "prevent load by PreLoadEvent"() {
        KeyValueInstanceLoader loader = factory.createKeyValueInstanceLoader()
        KeyValueContainer container = factory.createKeyValueContainer()

        Consumer preLoadListener = { KeyValueInstanceLoader.PreLoadEvent e -> e.preventLoad() }
        loader.addPreLoadListener(preLoadListener)

        Consumer postLoadListener = Mock()
        loader.addPostLoadListener(postLoadListener)

        Foo foo = new Foo()
        dataManager.commit(foo)

        when:

        container.addProperty('id', UUID)
        container.addProperty('name', String)

        loader.setContainer(container)
        loader.setQuery('select e.id, e.name from test_Foo e where e.id = :id')
        loader.setParameter('id', foo.id)
        loader.load()

        then:

        container.getItemOrNull() == null

        0 * postLoadListener.accept(_)

        cleanup:

        persistenceTools.deleteRecord(foo)
    }
}
