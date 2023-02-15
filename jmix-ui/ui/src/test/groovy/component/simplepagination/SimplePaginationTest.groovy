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

package component.simplepagination

import component.simplepagination.screen.PaginationListComponentTestScreen
import component.simplepagination.screen.SimplePaginationTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import io.jmix.ui.widget.JmixSimplePagination
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Customer

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class SimplePaginationTest extends ScreenSpecification {

    @Autowired
    DataManager dataManager

    List<Customer> customers;

    @Override
    void setup() {
        exportScreensPackages(["component.simplepagination"])

        customers = new ArrayList<>(10);
        10.times { customers.add(dataManager.create(Customer)) }
        dataManager.save(customers.toArray())
    }

    @Override
    void cleanup() {
        dataManager.remove(customers)
        customers.clear()
    }

    def "SimplePagination clicks on: last, previous, first, next"() {
        given: "We have 5 pages"
        showTestMainScreen()

        def screen = (SimplePaginationTestScreen) getScreens().create(SimplePaginationTestScreen)
        screen.show()

        def firstBtn = screen.pagination.unwrap(JmixSimplePagination).firstButton
        def previousBtn = screen.pagination.unwrap(JmixSimplePagination).prevButton
        def nextBtn = screen.pagination.unwrap(JmixSimplePagination).nextButton
        def lastBtn = screen.pagination.unwrap(JmixSimplePagination).lastButton

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

    def "pagination without data source provider"() {
        showTestMainScreen()

        when: "Show screen"
        def screen = getScreens().create(SimplePaginationTestScreen)
        screen.show()

        then: "SimplePagination's navigation buttons and ComboBox should be disabled"

        def jmixSimplePagination = screen.paginationWithoutDataSource.unwrap(JmixSimplePagination)

        !jmixSimplePagination.itemsPerPageLayout.itemsPerPageComboBox.enabled

        !jmixSimplePagination.firstButton.isEnabled()
        !jmixSimplePagination.prevButton.isEnabled()
        !jmixSimplePagination.nextButton.isEnabled()
        !jmixSimplePagination.lastButton.isEnabled()
    }

    def "SimplePagination in ListComponent test"() {
        showTestMainScreen()

        when: "Open screen with Table, DataGrid"
        def screen = getScreens().create(PaginationListComponentTestScreen)
        screen.show()

        then:
        noExceptionThrown()

        def paginationSimple = screen.customerTableMetaClassSimple.getPagination()
        !paginationSimple.unwrap(JmixSimplePagination).itemsPerPageComboBox.isEnabled()

        def paginationDataGridSimple = screen.customerTableMetaClassSimple.getPagination()
        !paginationDataGridSimple.unwrap(JmixSimplePagination).itemsPerPageComboBox.isEnabled()
    }
}
