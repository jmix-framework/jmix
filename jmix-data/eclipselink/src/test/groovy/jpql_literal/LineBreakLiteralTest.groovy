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

package jpql_literal

import io.jmix.core.DataManager
import io.jmix.core.Sort
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.sales.Customer

/**
 * A line break is the record separator of localized string values. It cannot be written as a literal:
 * {@code JpqlQueryBuilder} replaces every line break of a query with a space before parsing. This test proves
 * that the separator can be produced by the database instead, through {@code function('char', 10)}
 * ({@code chr} on PostgreSQL and Oracle), and that such an expression works in a where clause and in an order by.
 */
class LineBreakLiteralTest extends DataSpec {

    @Autowired
    DataManager dataManager

    def "a line break written as a literal is replaced with a space by the query builder"() {
        given:
        def customer = dataManager.create(Customer)
        customer.name = 'Default\nde=Wert'
        dataManager.save(customer)

        when:
        def found = dataManager.load(Customer)
                .query("select e from sales_Customer e where locate('\nde=', e.name) > 0")
                .sort(Sort.by('name'))
                .list()

        and: "the same query with the text the literal was turned into, as a positive control that the row is " +
                "there and reachable"
        def control = dataManager.load(Customer)
                .query("select e from sales_Customer e where locate('de=', e.name) > 0")
                .sort(Sort.by('name'))
                .list()

        then: "the literal became ' de=' and matches nothing, while the row itself is found"
        found.isEmpty()
        control*.id == [customer.id]
    }

    def "a line break produced by function('char', 10) is parsed by Jmix, accepted by EclipseLink and evaluated by HSQLDB"() {
        given:
        def localized = dataManager.create(Customer)
        localized.name = 'Default\nde=Wert'
        def plain = dataManager.create(Customer)
        plain.name = 'Plain'
        dataManager.save(localized, plain)
        def nl = "function('char', 10)"

        when: "the separator is used in a where clause and a sort forces the Jmix query transformer to parse the query"
        def found = dataManager.load(Customer)
                .query("select e from sales_Customer e where locate(concat(${nl}, 'de='), concat(${nl}, e.name, ${nl})) > 0")
                .sort(Sort.by('name'))
                .list()

        then:
        found*.id == [localized.id]

        when: "the separator is used inside an order by expression"
        def ordered = dataManager.load(Customer)
                .query("select e from sales_Customer e order by case when locate(${nl}, e.name) > 0" +
                        " then substring(e.name, 1, locate(${nl}, e.name) - 1) else e.name end asc")
                .list()

        then:
        ordered*.name == ['Default\nde=Wert', 'Plain']
    }
}
