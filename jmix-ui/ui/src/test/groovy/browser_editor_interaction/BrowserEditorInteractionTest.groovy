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

package browser_editor_interaction

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.ScreenBuilders
import io.jmix.ui.UiConfiguration
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Customer

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration, EclipselinkConfiguration,
        UiTestConfiguration])
class BrowserEditorInteractionTest extends ScreenSpecification {

    @Autowired
    JdbcTemplate jdbc
    @Autowired
    ScreenBuilders screenBuilders

    @Override
    void setup() {
        exportScreensPackages(['browser_editor_interaction'])
    }

    void cleanup() {
        jdbc.update('delete from TEST_CUSTOMER')
    }

    def "browser's DataContext contains edited entity"() {
        screens.create("main", OpenMode.ROOT).show()

        when:
        def browser = screens.create(CustomerBrowseWithReadWriteDataContext)
        browser.show()

        def customer = metadata.create(Customer)
        customer.name = 'Joe'

        def editor = screenBuilders.editor(browser.customersTable)
                .withScreenClass(CustomerEdit)
                .newEntity(customer)
                .build()
        editor.show()
        editor.closeWithCommit()

        then:
        browser.customersDc.items.size() == 1
        def createdCustomer = browser.customersDc.items[0]
        createdCustomer == customer
        browser.dataContext.contains(createdCustomer)
        browser.dataContext.find(createdCustomer).is(createdCustomer)
        !browser.dataContext.hasChanges()

        when:
        editor = screenBuilders.editor(browser.customersTable)
                .withScreenClass(CustomerEdit)
                .editEntity(createdCustomer)
                .build()
        editor.show()
        editor.getEditedEntity().name = 'Bob'
        editor.closeWithCommit()

        then:
        def editedCustomer = browser.customersDc.items[0]
        editedCustomer.name == 'Bob'
        browser.dataContext.contains(editedCustomer)
        browser.dataContext.find(editedCustomer).is(editedCustomer)
        !browser.dataContext.hasChanges()
    }
}
