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

package localized_string

import io.jmix.core.Metadata
import io.jmix.core.Sort
import io.jmix.core.security.ClientDetails
import io.jmix.core.security.SecurityContextHelper
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.AbstractAuthenticationToken
import test_support.entity.localized_string.LsHolder
import test_support.entity.localized_string.LsItem
import test_support.entity.localized_string.LsLink
import test_support.entity.localized_string.LsNode
import test_support.entity.localized_string.LsNumbered
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(properties = ["jmix.core.available-locales=en,de"])
class LocalizedStringContainerSortTest extends FlowuiTestSpecification {

    static final Locale DE = Locale.GERMAN

    @Autowired
    DataComponents dataComponents
    @Autowired
    Metadata metadata

    CollectionContainer<LsItem> container

    void setup() {
        container = dataComponents.createCollectionContainer(LsItem)
        def token = SecurityContextHelper.getAuthentication() as AbstractAuthenticationToken
        token.details = ClientDetails.builder().locale(DE).build()
    }

    LsItem item(String code, String name) {
        def e = metadata.create(LsItem)
        e.code = code
        e.name = name
        return e
    }

    def "a container sorts a localized property by the text of the current locale, ignoring case"() {
        given:
        container.items = [item('1', 'zeta\nde=birne'), item('2', 'alpha\nde=Zitrone'), item('3', 'mu\nde=Apfel'),
                           item('4', 'Plain'), item('5', 'omega\nde=Äpfel')]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('name')))

        then: "Apfel, birne, Plain, Zitrone, Äpfel: compareToIgnoreCase ignores case but orders by code point, so " +
                "an umlaut sorts after z, where a German collation would sort it next to a"
        container.items*.code == ['3', '1', '4', '2', '5']

        when:
        container.sorter.sort(Sort.by(Sort.Order.desc('name')))

        then:
        container.items*.code == ['5', '2', '4', '1', '3']
    }

    def "a container sorts by a nested localized path of an entity that has no localized property itself"() {
        given: "the stored strings and the German texts order the rows the opposite way"
        def container = dataComponents.createCollectionContainer(LsHolder)
        container.items = [holder('1', item('x', 'alpha\nde=Birne')), holder('2', item('y', 'zeta\nde=Apfel'))]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('item.name')))

        then:
        container.items*.code == ['2', '1']
    }

    def "a container sorts a reference by its instance name when that name is a localized string"() {
        given: "the reference itself is the sorted property, the way a grid column bound to it is sorted"
        def container = dataComponents.createCollectionContainer(LsHolder)
        container.items = [holder('1', item('x', 'alpha\nde=Birne')), holder('2', item('y', 'zeta\nde=Apfel'))]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('item')))

        then: "by the text the grid shows, not by the stored strings the standard comparator would descend into"
        container.items*.code == ['2', '1']
    }

    def "a reference whose localized name is null keeps a null sort key"() {
        given:
        def container = dataComponents.createCollectionContainer(LsHolder)
        container.items = [holder('1', item('x', null)), holder('2', item('y', 'Apfel')), holder('3', item('z', ''))]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('item')))

        then: "the store of this entity sorts nulls first, and an empty text sorts before any text"
        container.items*.code == ['1', '3', '2']

        when:
        container.sorter.sort(Sort.by(Sort.Order.desc('item')))

        then: "the order that tells a null name from an empty one, which comparing by the instance name mixes up"
        container.items*.code == ['2', '3', '1']
    }

    def "a reference whose instance name is made of several properties is compared by each of them in turn"() {
        given: "the same German name, numbers 10 and 9, and stored default values that order the rows 1, 2"
        def container = dataComponents.createCollectionContainer(LsHolder)
        container.items = [numberedHolder('1', 'Alpha\nde=Apfel', 10), numberedHolder('2', 'Zeta\nde=Apfel', 9)]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('numbered')))

        then: "by the resolved name, then by the number as a number, the way the database orders them; the " +
                "formatted instance name would put '10 ...' first"
        container.items*.code == ['2', '1']
    }

    def "a reference whose localized name lies one reference deeper is sorted by the text of the current locale"() {
        given: "the instance name of the link is its item, whose instance name is the localized name"
        def container = dataComponents.createCollectionContainer(LsHolder)
        container.items = [linkHolder('1', 'alpha\nde=Birne'), linkHolder('2', 'zeta\nde=Apfel')]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('link')))

        then: "Apfel before Birne, the way the database orders them; the stored strings order 1 before 2"
        container.items*.code == ['2', '1']
    }

    def "a reference whose instance name refers to its own entity is compared by the data, not by the model"() {
        given: "the instance name of a node is its localized name and its parent"
        def birne = node('zeta\nde=Birne', null)
        def container = dataComponents.createCollectionContainer(LsHolder)
        container.items = [nodeHolder('1', birne), nodeHolder('2', node('alpha\nde=Apfel', birne)),
                           nodeHolder('3', node('omega\nde=Apfel', null))]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('node')))

        then: "by the German name, then by the parent, where the comparison stops at a node without one; the " +
                "stored strings order 2, 3, 1"
        container.items*.code == ['3', '2', '1']
    }

    LsHolder linkHolder(String code, String name) {
        def link = metadata.create(LsLink)
        link.item = item('x', name)
        def e = holder(code, null)
        e.link = link
        return e
    }

    LsNode node(String name, LsNode parent) {
        def e = metadata.create(LsNode)
        e.name = name
        e.parent = parent
        return e
    }

    LsHolder nodeHolder(String code, LsNode node) {
        def e = holder(code, null)
        e.node = node
        return e
    }

    LsHolder numberedHolder(String code, String name, Integer number) {
        def numbered = metadata.create(LsNumbered)
        numbered.name = name
        numbered.number = number
        def e = holder(code, null)
        e.numbered = numbered
        return e
    }

    def "a collection whose elements have a localized instance name is left to the standard comparator"() {
        given:
        def container = dataComponents.createCollectionContainer(LsHolder)
        def first = holder('1', null)
        first.items = [item('x', 'alpha\nde=Birne')]
        def second = holder('2', null)
        second.items = [item('y', 'zeta\nde=Apfel')]
        container.items = [first, second]

        when: "the way a grid column bound to the collection is sorted"
        container.sorter.sort(Sort.by(Sort.Order.asc('items')))

        then: "a list has no instance name to compare by"
        noExceptionThrown()
        container.items*.code as Set == ['1', '2'] as Set
    }

    LsHolder holder(String code, LsItem item) {
        def e = metadata.create(LsHolder)
        e.code = code
        e.item = item
        return e
    }

    def "a @Lob localized property is sorted by the stored string, as the database sorts it"() {
        given:
        def first = item('1', 'x')
        first.description = 'zeta\nde=Apfel'
        def second = item('2', 'y')
        second.description = 'alpha\nde=Birne'
        container.items = [first, second]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('description')))

        then: "the resolved texts would order 1 before 2; the stored strings order 2 before 1"
        container.items*.code == ['2', '1']
    }

    def "a null value keeps a null sort key and is not read as an empty text"() {
        given:
        container.items = [item('1', null), item('2', 'Apfel'), item('3', '')]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('name')))

        then: "the store of this entity sorts nulls first, and an empty text sorts before any text"
        container.items*.code == ['1', '3', '2']

        when:
        container.sorter.sort(Sort.by(Sort.Order.desc('name')))

        then: "this is the order that tells the two apart: resolving a null value to an empty text would make " +
                "the null row and the empty row interchangeable and leave them in their original order"
        container.items*.code == ['2', '3', '1']
    }

    def "plain properties of the same container keep the standard comparator"() {
        given: "codes that read as localized values, whose texts would order the rows the other way round"
        container.items = [item('b\nde=1', 'x'), item('a\nde=2', 'y')]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('code')))

        then:
        container.items*.code == ['a\nde=2', 'b\nde=1']
    }

    def "a comparator set for the property wins over the localized one"() {
        given: "the localized texts order the rows 3, 1, 2; the custom comparator orders them by code, reversed"
        container.items = [item('1', 'zeta\nde=birne'), item('2', 'alpha\nde=Zitrone'), item('3', 'mu\nde=Apfel')]
        container.sorter.propertyComparators = ['name': { a, b -> b.code <=> a.code } as Comparator]

        when:
        container.sorter.sort(Sort.by(Sort.Order.asc('name')))

        then:
        container.items*.code == ['3', '2', '1']
    }
}
