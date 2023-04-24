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

package pessimistic_locking

import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.core.pessimisticlocking.LockManager
import io.jmix.core.pessimisticlocking.LockNotSupported
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.ScreenBuilders
import io.jmix.ui.UiConfiguration
import io.jmix.ui.screen.EditorScreen
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import pessimistic_locking.screen.CustomerEdit
import pessimistic_locking.screen.CustomerMasterDetail
import test_support.UiTestConfiguration
import test_support.entity.sales.Address
import test_support.entity.sales.Customer

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class PessimisticLockEditScreenTest extends ScreenSpecification {

    @Autowired
    ScreenBuilders screenBuilders
    @Autowired
    DataManager dataManager
    @Autowired
    LockManager lockManager
    @Autowired
    Metadata metadata

    @Override
    void setup() {
        exportScreensPackages(['pessimistic_locking'])
    }

    def "entity is locked by edit screen"() {
        def customer = dataManager.save(new Customer(name: 'c1', address: new Address()))

        def mainScreen = showTestMainScreen()

        when: "open first editor"
        def editor1 = screenBuilders.editor(Customer, mainScreen)
                .withScreenClass(CustomerEdit)
                .editEntity(customer)
                .withOpenMode(OpenMode.DIALOG)
                .build()
        editor1.show()

        then: "entity is locked"
        def lockInfo = lockManager.getLockInfo(metadata.getClass(Customer).name, customer.id.toString())
        lockInfo != null && !(lockInfo instanceof LockNotSupported)

        when: "open second editor"
        def editor2 = screenBuilders.editor(Customer, mainScreen)
                .withScreenClass(CustomerEdit)
                .editEntity(customer)
                .build()
        editor2.show()

        then: "second editor is read-only"
        !editor2.getWindow().getActionNN(EditorScreen.WINDOW_COMMIT_AND_CLOSE).enabled

        when: "close first editor and open third editor"
        editor1.closeWithDefaultAction()
        def editor3 = screenBuilders.editor(Customer, mainScreen)
                .withScreenClass(CustomerEdit)
                .editEntity(customer)
                .build()
        editor3.show()

        then: "third editor can save changes"
        editor3.getWindow().getActionNN(EditorScreen.WINDOW_COMMIT_AND_CLOSE).enabled

        cleanup:
        dataManager.remove(customer)
    }

    def "entity is locked by master-detail screen"() {
        def customer = dataManager.save(new Customer(name: 'c1', address: new Address()))

        def mainScreen = showTestMainScreen()

        when: "open first screen"
        def screen1 = screenBuilders.screen(mainScreen)
                .withScreenClass(CustomerMasterDetail)
                .build()
        screen1.show()
        screen1.table.setSelected(screen1.customersDc.items[0])
        screen1.tableEdit.actionPerform(null)

        then: "entity is locked"
        def lockInfo = lockManager.getLockInfo(metadata.getClass(Customer).name, customer.id.toString())
        lockInfo != null && !(lockInfo instanceof LockNotSupported)

        when: "open second screen"
        def screen2 = screenBuilders.screen(mainScreen)
                .withScreenClass(CustomerMasterDetail)
                .build()
        screen2.show()
        screen2.table.setSelected(screen2.customersDc.items[0])
        screen2.tableEdit.actionPerform(null)

        then:
        !screen2.actionsPane.visible

        when: "close first screen and open third screen"
        screen1.cancelBtn.buttonClicked(null)
        def screen3 = screenBuilders.screen(mainScreen)
                .withScreenClass(CustomerMasterDetail)
                .build()
        screen3.show()
        screen3.table.setSelected(screen3.customersDc.items[0])
        screen3.tableEdit.actionPerform(null)

        then:
        screen3.actionsPane.visible

        cleanup:
        dataManager.remove(customer)
    }
}
