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

package pagination_component

import com.vaadin.data.provider.Query
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.core.Messages
import io.jmix.data.DataConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.testassist.spec.ScreenSpecification
import io.jmix.ui.widget.JmixPagination
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import pagination_component.screen.PaginationComponentTestScreen
import pagination_component.screen.TablePaginationMetaClassTestScreen
import pagination_component.screen.TablePaginationTestScreen
import test_support.UiTestConfiguration
import test_support.entity.sales.Customer

import java.util.stream.Collectors

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration, UiTestConfiguration])
class PaginationComponentTest extends ScreenSpecification {

    @Autowired
    JdbcTemplate jdbc

    @Autowired
    DataManager dataManager

    @Autowired
    Messages messages

    def loaderMaxResult = 2

    @Override
    void setup() {
        exportScreensPackages(["pagination_component"])

        10.times { dataManager.save(metadata.create(Customer)) }
    }

    @Override
    void cleanup() {
        jdbc.update("delete from TEST_CUSTOMER")
    }

    def "Pagination click on next button"() {
        given: "We have 5 pages"
        showTestMainScreen()

        when: "Click on 'next' button"
        def screen = getScreens().create(PaginationComponentTestScreen)
        screen.show()

        def vPagination = screen.pagination.unwrap(JmixPagination)
        vPagination.nextButton.click()

        then: "All buttons should be visible"
        vPagination.firstButton.isVisible()
        vPagination.prevButton.isVisible()

        vPagination.lastButton.isVisible()
        vPagination.nextButton.isVisible()
    }

    def "Pagination click on last button"() {
        given: "We have 5 pages"
        showTestMainScreen()

        when: "Click on 'last' button"
        def screen = getScreens().create(PaginationComponentTestScreen)
        screen.show()

        def vPagination = screen.pagination.unwrap(JmixPagination)
        vPagination.lastButton.click()

        then: "Last and next buttons should be hidden"
        vPagination.firstButton.isVisible()
        vPagination.prevButton.isVisible()

        !vPagination.lastButton.isVisible()
        !vPagination.nextButton.isVisible()
    }

    def "Pagination click on first button"() {
        given: "We have 5 pages"
        showTestMainScreen()

        when: "Set the last page on Pagination"
        def screen = getScreens().create(PaginationComponentTestScreen)
        screen.show()

        def vPagination = screen.pagination.unwrap(JmixPagination)
        vPagination.lastButton.click() // set the last page

        then: "First and previous buttons should be visible"
        vPagination.firstButton.isVisible()
        vPagination.prevButton.isVisible()

        when: "Click on 'first' button"
        vPagination.firstButton.click() // set the first page

        then: "First and previous buttons should be hidden"
        !vPagination.firstButton.isVisible()
        !vPagination.prevButton.isVisible()
    }

    def "Pagination click on previous button"() {
        given: "We have 5 pages"
        showTestMainScreen()

        when: "Click on 'next' button, then click on 'previous'"
        def screen = getScreens().create(PaginationComponentTestScreen)
        screen.show()

        def vPagination = screen.pagination.unwrap(JmixPagination)
        vPagination.nextButton.click()
        vPagination.prevButton.click() // return to the first page

        then: "First and previous buttons should be hidden"
        !vPagination.firstButton.isVisible()
        !vPagination.prevButton.isVisible()
    }

    def "Pagination with custom max result options"() {
        showTestMainScreen()

        when: "Load options with the order: 12, 9, 23, -6, 41, 0"
        def screen = getScreens().create(PaginationComponentTestScreen)
        screen.show()

        def vPagination = screen.paginationCustomSMR.unwrap(JmixPagination)
        def maxResults = vPagination.maxResultComboBox.getDataProvider()
                .fetch(new Query<Integer, ?>())
                .collect(Collectors.toList())

        def expectedMaxResults = [9, 12, 23, 41]

        then: "Component should skip values less than or equal to 0"
        maxResults == expectedMaxResults
    }

    def "Change Pagination MaxResults visibility at runtime"() {
        showTestMainScreen()

        when: "Show screen"
        def screen = getScreens().create(PaginationComponentTestScreen)
        screen.show()

        then: """
              Pagination without MaxResults should load items according to Loader's maxResult.
              Pagination with MaxResults should load according to option value from ComboBox.
              """
        // Firstly if Loader's value does not exist in MaxResult ComboBox
        // component will find the nearest value for Loader's maxResult
        screen.customersLdNoSMR.container.items.size() == loaderMaxResult
        // just used value from loader
        screen.customersLdSMR.container.items.size() == 1

        when: "Show MaxResult in pagination"
        screen.paginationNoSMR.setShowMaxResults(true)

        then: "Value from MaxResult ComboBox should be used"
        screen.customersLdNoSMR.container.items.size() == 1

        when: "Hide MaxResult in pagination"
        screen.paginationSMR.setShowMaxResults(false)

        then: "Value from Loader's maxResult should be used"
        screen.customersLdSMR.container.items.size() == loaderMaxResult
    }

    def "Pagination with postponed setting Loader"() {
        showTestMainScreen()

        when: "Show screen"
        def screen = getScreens().create(PaginationComponentTestScreen)
        screen.show()

        then: """
              Pagination without MaxResults should have "0 rows" label.
              Pagination with MaxResults should have disabled ComboBox.
              """
        def vPaginationNoSMR = screen.postponedPaginationNoSMR.unwrap(JmixPagination)
        vPaginationNoSMR.label.value == messages.getMessage("", "pagination.status.label.disabledValue")

        def vPaginationSMR = screen.postponedPaginationSMR.unwrap(JmixPagination)
        !vPaginationSMR.maxResultComboBox.enabled

        when: "Set loader with items to Pagination without MaxResult"
        screen.postponedPaginationNoSMR.setLoaderTarget(screen.customersLdPostponed)

        then: "Label message should be updated"
        vPaginationNoSMR.label.value != messages.getMessage("", "pagination.status.label.disabledValue")

        when: "Set loader with items to Pagination with MaxResult"
        screen.postponedPaginationSMR.setLoaderTarget(screen.customersLdPostponed)

        then: """
              ComboBox should be enabled and loader should reload items
              according to the ComboBox's value.
              """
        vPaginationSMR.maxResultComboBox.enabled
        screen.customersLdPostponed.maxResults != loaderMaxResult
    }

    def "TablePagination table with data container"() {
        showTestMainScreen()

        when: "Open screen with Table and Pagination"
        def screen = getScreens().create(TablePaginationTestScreen)
        screen.show()

        then:
        noExceptionThrown()
    }

    def "TablePagination empty table with MetaClass"() {
        showTestMainScreen()

        when: "Load empty table with MetaClass"
        def screen = getScreens().create(TablePaginationMetaClassTestScreen)
        screen.show()

        then: "No exception should be thrown and Pagination should be hidden"
        noExceptionThrown()

        def vPagination = screen.customerTableMetaClass.getPagination().unwrap(JmixPagination)
        !vPagination.maxResultComboBox.isEnabled()
    }
}
