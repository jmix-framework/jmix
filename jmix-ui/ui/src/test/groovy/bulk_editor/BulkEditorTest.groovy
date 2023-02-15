/*
 * Copyright 2021 Haulmont.
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

package bulk_editor

import bulk_editor.screen.BulkEditTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.testassistspock.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.OrderLine

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class BulkEditorTest extends ScreenSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        exportScreensPackages(["bulk_editor.screen", "io.jmix.ui.app.bulk"])

        2.times {dataManager.save(dataManager.create(OrderLine))}
    }

    @Override
    void cleanup() {
        jdbcTemplate.update("delete from TEST_ORDER_LINE")
    }

    def "open bulkEdit screen"() {
        showTestMainScreen()

        def screen = (BulkEditTestScreen) screens.create(BulkEditTestScreen)
        screen.show()

        when: "Select all items and execute the BulkEdit action"
        screen.table.setSelected(screen.orderLineDc.items)

        screen.tableBulkEdit.execute()

        then: "No exception should be thrown after the BulkEdit dialog is shown"
        noExceptionThrown()
    }
}
