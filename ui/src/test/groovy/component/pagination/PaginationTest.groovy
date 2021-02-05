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
import component.pagination.screen.PaginationTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.testassist.spec.ScreenSpecification
import io.jmix.ui.widget.JmixPagination
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Customer

import java.util.stream.Collectors

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class PaginationTest extends ScreenSpecification {

    @Autowired
    JdbcTemplate jdbc

    @Autowired
    DataManager dataManager

    @Override
    void setup() {
        exportScreensPackages(["component.pagination"])

        10.times { dataManager.save(metadata.create(Customer)) }
    }

    @Override
    void cleanup() {
        jdbc.update('delete from TEST_CUSTOMER')
    }

    def "pagination clicks on: last, previous, first, next"() {
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
        def screen = (PaginationTestScreen) getScreens().create(PaginationTestScreen)
        screen.show()

        then: """
              Pagination without ItemsPerPage should use loader's maxResult.
              Pagination with ItemsPerPage should load according to option value from ComboBox.
              """

        // Pagination WITHOUT ItemsPerPage will use loader's maxResult
        screen.pagination.dataBinder.size() == 2

        // Pagination WITH ItemsPerPage will try to use entityPageSize, but if options
        // don't contain this value, component will find the closest value in options.
        screen.getPaginationCustomOptionsCB().getValue() == 41

        // Pagination with itemsPerPageDefaultValue = 9
        screen.getPaginationDefaultValueCB().getValue() == 9
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
}
