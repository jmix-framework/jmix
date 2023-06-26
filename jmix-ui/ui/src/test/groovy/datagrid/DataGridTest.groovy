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

package datagrid

import datagrid.screen.DataGridTestScreen
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
import test_support.entity.sales.Order

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class DataGridTest extends ScreenSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    void setup() {
        exportScreensPackages(['datagrid.screen'])

        dataManager.save(dataManager.create(Order))
    }

    void cleanup() {
        jdbcTemplate.update('delete from TEST_ORDER')
    }

    def "hide and show generated column"() {
        showTestMainScreen()

        when: "Screen is opened and hideBtn is clicked"

        def screen = screens.create(DataGridTestScreen)
        screen.show()

        screen.hideBtn.click()

        then: "Generated column should be hidden"

        noExceptionThrown()
        !screen.dataGrid.getColumnNN("generated").visible

        when: "showBtn is clicked"

        screen.showBtn.click()

        then: "Generated column should be visible"
        noExceptionThrown()
        screen.dataGrid.getColumnNN("generated").visible
    }
}
