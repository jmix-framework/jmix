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

package component.pagination

import com.vaadin.data.provider.Query
import component.pagination.screen.PaginationConsistenceTestScreen
import component.pagination.screen.PaginationDefaultValueTestScreen
import component.pagination.screen.PaginationTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.screen.UiControllerUtils
import io.jmix.ui.testassist.spec.ScreenSpecification
import io.jmix.ui.widget.JmixPagination
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Customer
import test_support.entity.sales.Order

import java.util.stream.Collectors

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class PaginationTest extends ScreenSpecification {

    @Autowired
    DataManager dataManager

    List<Customer> customers;
    Order order;

    private int customerItems = 10;

    @Override
    void setup() {
        exportScreensPackages(["component.pagination"])

        customers = new ArrayList<>(customerItems);
        customerItems.times { customers.add(dataManager.create(Customer)) }
        dataManager.save(customers.toArray())
    }

    @Override
    void cleanup() {
        dataManager.remove(customers)
        customers.clear()

        if (order != null) {
            dataManager.remove(order)
        }
    }

    def "pagination clicks on last, previous, first, next"() {
        given: "We have 5 pages"
        showTestMainScreen()

        def screen = (PaginationTestScreen) getScreens().create(PaginationTestScreen)
        screen.show()

        def firstBtn = screen.pagination.unwrap(JmixPagination).firstButton
        def previousBtn = screen.pagination.unwrap(JmixPagination).prevButton
        def nextBtn = screen.pagination.unwrap(JmixPagination).nextButton
        def lastBtn = screen.pagination.unwrap(JmixPagination).lastButton

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

    def "pagination with custom ItemsPerPage options"() {
        showTestMainScreen()

        when: "Load options with the order: 12, 9, 23, 41, 1, -10, 99999"
        def screen = (PaginationTestScreen) getScreens().create(PaginationTestScreen)
        screen.show()

        def itemsPerPageComoBox = screen.paginationCustomOptions.unwrap(JmixPagination).itemsPerPageComboBox
        def maxResults = itemsPerPageComoBox.getDataProvider()
                .fetch(new Query<Integer, ?>())
                .collect(Collectors.toList())

        def expectedMaxResults = [1, 9, 12, 23, 41, 10000]
        then: """
              Component should skip options less than or equal to 0.
              Options greater than entity's max fetch size will be replaced by it
              """
        maxResults == expectedMaxResults
    }

    def "pagination initial ItemsPerPage value"() {
        showTestMainScreen()

        when: "Show screen"
        def screen = (PaginationDefaultValueTestScreen) getScreens().create(PaginationDefaultValueTestScreen)
        screen.show()

        then: """
              Pagination should have fetch size equal to computed value based on default value, options and
              visibility of ItemsPerPage ComboBox.
              """
        // takes value from itemsPerPageDefaultValue
        screen.pagination1.dataBinder.maxResults == 1

        // takes closest value from options
        screen.pagination2.dataBinder.maxResults == 2

        // takes closest value from 'jmix.ui.component.paginationItemsPerPageOptions' as options were not explicitly set
        // and 'itemsPerPageVisible = true'
        screen.pagination3.dataBinder.maxResults == 20

        // takes closest value from options
        screen.pagination4.dataBinder.maxResults == 2

        // takes value from entityPageSize (or defaultPageSize)
        screen.pagination5.dataBinder.maxResults == 50

        // takes closest value from options
        screen.pagination6.dataBinder.maxResults == 4

        // takes closest value from 'jmix.ui.component.paginationItemsPerPageOptions' as options were not explicitly set
        // and 'itemsPerPageVisible = true'
        screen.pagination7.dataBinder.maxResults == 50

        // takes closest value from options
        screen.pagination8.dataBinder.maxResults == 4
    }

    def "pagination changes affect another pagination with the same loader"() {
        showTestMainScreen()

        def screen = (PaginationConsistenceTestScreen) getScreens().create(PaginationConsistenceTestScreen)
        screen.show()

        when: "Change page from SimplePagination"
        screen.jmixSimplePagination.nextButton.click()

        then: "Pagination change page accordingly"
        screen.pagination.unwrap(JmixPagination).getCurrentPageNumber() == 2

        when: "Change ItemsPerPage value from SimplePagination"
        screen.jmixSimplePagination.itemsPerPageComboBox.setValue(2)

        then: "Pagination should change value accordingly"
        screen.jmixSimplePagination.itemsPerPageComboBox.getValue() == 2

        // vice versa

        when: "Change page from Pagination"
        screen.jmixPagination.nextButton.click()

        then: "SimplePagination should change page accordingly"
        screen.jmixSimplePagination.label.value.contains("3-4")

        when: "Change ItemsPerPage value from Pagination"
        screen.jmixPagination.itemsPerPageLayout.itemsPerPageComboBox.setValue(1)

        then: "SimplePagination should change value accordingly"
        screen.jmixSimplePagination.itemsPerPageComboBox.getValue() == 1
    }

    def "pagination without data source provider"() {
        showTestMainScreen()

        when: "Show screen"

        def screen = (PaginationTestScreen) getScreens().create(PaginationTestScreen)
        screen.show()

        then: "Pagination should have disabled change buttons and ItemsPerPage ComboBox"

        def jmixPagination = screen.paginationWithoutDataSource.unwrap(JmixPagination)

        !jmixPagination.itemsPerPageComboBox.enabled

        !jmixPagination.firstButton.isEnabled()
        !jmixPagination.prevButton.isEnabled()
        !jmixPagination.nextButton.isEnabled()
        !jmixPagination.lastButton.isEnabled()
    }

    def "remove the only item in last page"() {
        showTestMainScreen()

        def screen = (PaginationTestScreen) getScreens().create(PaginationTestScreen)
        screen.show()

        def jmixPagination = screen.paginationRemoveLastItem.unwrap(JmixPagination)

        when: "Select last page and remove last item"
        jmixPagination.selectLastPage()

        def dataContext = UiControllerUtils.getScreenData(screen).dataContext

        def lastCustomer = screen.customersRemoveLastItemCODc.mutableItems.get(0)

        screen.customersRemoveLastItemCODc.mutableItems.remove(lastCustomer)
        dataContext.remove(lastCustomer)

        then: "Due to no item in the last page, Pagination component should select previous page."

        jmixPagination.getCurrentPageNumber() == customerItems - 1
    }

    def "create new item while data container is empty"() {
        showTestMainScreen()

        def screen = (PaginationTestScreen) getScreens().create(PaginationTestScreen)
        screen.show()

        def jmixPagination = screen.paginationEmptyContainer.unwrap(JmixPagination)

        when: "Save new item and add it to the data container"

        order = dataManager.create(Order)
        dataManager.save(order)
        screen.ordersEmptyContainerDc.mutableItems.add(order)

        then: "Pagination should create 1 page"

        jmixPagination.pages.size() == 1
    }
}
