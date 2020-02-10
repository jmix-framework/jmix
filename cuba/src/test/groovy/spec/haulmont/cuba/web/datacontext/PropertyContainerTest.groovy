/*
 * Copyright (c) 2008-2019 Haulmont.
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

package spec.haulmont.cuba.web.datacontext


import io.jmix.core.DataManager
import io.jmix.ui.model.DataComponents
import spec.haulmont.cuba.web.UiScreenSpec
import spock.lang.Ignore

import javax.inject.Inject
import java.util.function.Consumer

class PropertyContainerTest extends UiScreenSpec {

    @Inject
    private DataManager dataManager
    @Inject
    private DataComponents factory

    @Ignore
    def 'setItem for nested container leads to parent container PropertyChangedEvent'() {
        def masterContainer = factory.createInstanceContainer(OrderLine)
        def nestedContainer = factory.createInstanceContainer(Product, masterContainer, 'product')

        def masterPropertyChangeListener = Mock(Consumer)
        masterContainer.addItemPropertyChangeListener(masterPropertyChangeListener)

        def nestedItemChangeListener = Mock(Consumer)
        nestedContainer.addItemChangeListener(nestedItemChangeListener)

        def masterEntity = metadata.create(OrderLine)
        def product = metadata.create(Product)

        when: 'An entity is set to master container'
        masterContainer.setItem(masterEntity)

        then: 'The entity is accessible through nested container'
        nestedContainer.master.item == masterEntity

        when: 'An entity is set to nested container'
        nestedContainer.item = product

        then: 'Nested Container item change listener triggered, master property change triggered, item property changed'
        1 * nestedItemChangeListener.accept(_)
        1 * masterPropertyChangeListener.accept(_)
        masterEntity.product == product
    }
}
