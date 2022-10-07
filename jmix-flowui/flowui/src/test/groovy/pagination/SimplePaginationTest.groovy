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

package pagination

import io.jmix.core.DataManager
import io.jmix.flowui.component.pagination.ItemsPerPage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pagination.view.SimplePaginationConsistenceTestView
import pagination.view.SimplePaginationDefaultValueTestView
import pagination.view.SimplePaginationTestView
import test_support.entity.sales.Customer
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class SimplePaginationTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    List<Customer> customers;
    private int customerItems = 10;

    @Override
    void setup() {
        registerScreenBasePackages("pagination")

        customers = new ArrayList<>(customerItems);
        customerItems.times { customers.add(dataManager.create(Customer)) }
        dataManager.save(customers.toArray())
    }

    @Override
    void cleanup() {
        dataManager.remove(customers)
        customers.clear()
    }

    def "SimplePagination clicks on last, previous, first, next"() {
        given: "We have 5 pages"

        def view = (SimplePaginationTestView) openScreen(SimplePaginationTestView)

        def firstBtn = view.simplePagination.firstButton
        def previousBtn = view.simplePagination.previousButton
        def nextBtn = view.simplePagination.nextButton
        def lastBtn = view.simplePagination.lastButton

        when: "Click on 'last' button"
        lastBtn.click()

        then: "Last and next buttons should be disabled"
        def firstPreviousEnabled = [(firstBtn.isEnabled()), previousBtn.isEnabled(), nextBtn.isEnabled(), lastBtn.isEnabled()]
        firstPreviousEnabled == [true, true, false, false]

        when: "Click on 'previous' button"
        previousBtn.click()

        then: "All buttons should be enabled"
        def allEnabled = [(firstBtn.isEnabled()), previousBtn.isEnabled(), nextBtn.isEnabled(), lastBtn.isEnabled()]
        allEnabled == [true, true, true, true]

        when: "Click on 'first' button"
        firstBtn.click()

        then: "First and previous buttons should be disabled"
        def lastNextEnabled = [(firstBtn.isEnabled()), previousBtn.isEnabled(), nextBtn.isEnabled(), lastBtn.isEnabled()]
        lastNextEnabled == [false, false, true, true]

        when: "Click on 'next' button"
        nextBtn.click()

        then: "All buttons should be enabled"
        def allEnabled1 = [(firstBtn.isEnabled()), previousBtn.isEnabled(), nextBtn.isEnabled(), lastBtn.isEnabled()]
        allEnabled1 == [true, true, true, true]
    }

    def "SimplePagination with custom ItemsPerPage items"() {
        when: "Load items with the order: 12, 9, 23, 41, 1, -10, 99999"
        def view = (SimplePaginationTestView) openScreen(SimplePaginationTestView)

        def itemsPerPage = (ItemsPerPage) view.simplePaginationCustomItems.jmixRowsPerPage

        def maxResults = itemsPerPage.processedItems

        def expectedMaxResults = [1, 9, 12, 23, 41, 10000]
        then: """
              Component should skip items less than or equal to 0.
              Items greater than entity's max fetch size will be replaced by it
              """
        maxResults == expectedMaxResults
    }

    def "SimplePagination initial ItemsPerPage value"() {
        when: "Show view"
        def view = (SimplePaginationDefaultValueTestView) openScreen(SimplePaginationDefaultValueTestView)

        then: """
              SimplePagination should have fetch size equal to computed value based on default value, items and
              visibility of ItemsPerPage Select.
              """
        // takes value from itemsPerPageDefaultValue
        view.simplePagination1.paginationLoader.maxResults == 1

        // takes closest value from items
        view.simplePagination2.paginationLoader.maxResults == 2

        // takes closest value from 'jmix.flowui.component.paginationItemsPerPageItems' as items were not explicitly set
        // and 'itemsPerPageVisible = true'
        view.simplePagination3.paginationLoader.maxResults == 20

        // takes closest value from items
        view.simplePagination4.paginationLoader.maxResults == 2

        // takes value from entityPageSize (or defaultPageSize)
        view.simplePagination5.paginationLoader.maxResults == 50

        // takes closest value from items
        view.simplePagination6.paginationLoader.maxResults == 4

        // takes closest value from 'jmix.flowui.component.paginationItemsPerPageItems' as items were not explicitly set
        // and 'itemsPerPageVisible = true'
        view.simplePagination7.paginationLoader.maxResults == 50

        !view.simplePagination8.itemsPerPage.isEnabled()
    }

    def "SimplePagination without loader"() {
        when: "Show View"

        def view = (SimplePaginationTestView) openScreen(SimplePaginationTestView)

        then: "SimplePagination should have disabled change buttons and ItemsPerPage Select"

        def simplePagination = view.simplePaginationWithoutLoader

        !view.simplePaginationWithoutLoader.itemsPerPage.enabled

        !simplePagination.firstButton.isEnabled()
        !simplePagination.previousButton.isEnabled()
        !simplePagination.nextButton.isEnabled()
        !simplePagination.lastButton.isEnabled()
    }

    def "SimplePagination changes affect another SimplePagination with the same loader"() {
        def view = (SimplePaginationConsistenceTestView) openScreen(SimplePaginationConsistenceTestView)

        when: "Change page from first SimplePagination"
        view.simplePagination1.nextButton.click()

        then: "Second SimplePagination change page accordingly"
        view.simplePagination2.rowsStatusSpan.text.contains("5-8")

        when: """
              Change ItemsPerPage value from first SimplePagination. Note for ItemsPerPage
              is used Select component. It doesn't enable to select value that is not in items.
              """
        view.simplePagination1.itemsPerPage.itemsPerPageValue = 2

        then: "Second SimplePagination should change value accordingly"
        view.simplePagination2.itemsPerPage.itemsPerPageValue == 2

        // vice versa

        when: "Change page from second SimplePagination"
        view.simplePagination2.nextButton.click()

        then: "First SimplePagination should change page accordingly"
        view.simplePagination1.rowsStatusSpan.text.contains("3-4")

        when: """
              Change ItemsPerPage value from second SimplePagination. Note for ItemsPerPage
              is used Select component. It doesn't enable to select value that is not in items.
              """
        view.simplePagination2.itemsPerPage.itemsPerPageValue = 1

        then: "SimplePagination should change value accordingly"
        view.simplePagination1.itemsPerPage.itemsPerPageValue == 1
    }
}
