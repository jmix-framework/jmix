/*
 * Copyright 2020 Haulmont.
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

package spec.haulmont.cuba.web.components.tokenlist

import com.google.common.collect.Lists
import com.haulmont.cuba.core.model.sales.OrderLine
import io.jmix.ui.component.data.options.ContainerOptions
import io.jmix.ui.component.data.options.ListEntityOptions
import io.jmix.ui.component.data.options.MapEntityOptions
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.components.tokenlist.screen.TokenListTestScreen

class TokenListTest extends UiScreenSpec {

    protected OrderLine orderLine1
    protected OrderLine orderLine2

    @Override
    void setup() {
        exportScreensPackages(["spec.haulmont.cuba.web.components.tokenlist.screen"])

        orderLine1 = metadata.create(OrderLine)
        orderLine2 = metadata.create(OrderLine)
    }

    def "List options are set to TokenList optionsContainer using setOptionsList"() {
        showTestMainScreen()

        def tokenListScreen = screens.create(TokenListTestScreen)
        tokenListScreen.show()

        def tokenList = tokenListScreen.tokenList
        def list = Lists.newArrayList(orderLine1, orderLine2)

        when: 'List options are set to TokenList using setOptionsList'
        tokenList.setOptionsList(list)

        then: 'No exception must be thrown'
        noExceptionThrown()

        and: 'Options class must be ListEntityOptions'
        tokenList.getOptions().getClass() == ListEntityOptions

        and: 'Items of options must be equal to List specified by the setOptionsList'
        ((ListEntityOptions) tokenList.getOptions()).getItemsCollection() == list
    }

    def "Map options are set to TokenList optionsContainer using setOptionsMap"() {
        showTestMainScreen()

        def tokenListScreen = screens.create(TokenListTestScreen)
        tokenListScreen.show()

        def tokenList = tokenListScreen.tokenList

        def map = new HashMap<String, OrderLine>()
        map.put("OrderLine 1", orderLine1)
        map.put("OrderLine 2", orderLine2)

        when: 'Map options are set to TokenList using setOptionsMap'
        tokenList.setOptionsMap(map)

        then: 'No exception must be thrown'
        noExceptionThrown()

        and: 'Options class must be MapEntityOptions'
        tokenList.getOptions().getClass() == MapEntityOptions

        and: 'Items of options must be equal to Map specified by the setOptionsMap'
        ((MapEntityOptions) tokenList.getOptions()).getItemsCollection() == map
    }

    def "ContainerOptions are set to TokenList optionsContainer using setOptions"() {
        showTestMainScreen()

        def tokenListScreen = screens.create(TokenListTestScreen)
        tokenListScreen.show()

        def tokenList = tokenListScreen.tokenList

        def container = dataComponents.createCollectionContainer(OrderLine)
        container.setItems(Lists.newArrayList(orderLine1, orderLine2))
        def options = new ContainerOptions(container)

        when: 'ContainerOptions are set to TokenList using setOptions'
        tokenList.setOptions(options)

        then: 'No exception must be thrown'
        noExceptionThrown()

        and: 'Options class must be ContainerOptions'
        tokenList.getOptions().getClass() == ContainerOptions

        then: 'Options must be equal to initial options'
        tokenList.getOptions() == options

        and: 'Container of options must be equal to CollectionContainer'
        ((ContainerOptions) tokenList.getOptions()).getContainer() == container
    }
}
