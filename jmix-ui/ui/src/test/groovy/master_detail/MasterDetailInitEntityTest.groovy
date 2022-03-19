/*
 * Copyright (c) 2020 Haulmont.
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

package master_detail

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.Table
import io.jmix.ui.screen.MasterDetailScreen
import io.jmix.ui.testassist.spec.ScreenSpecification
import master_detail.screen.OrderMasterDetailTestScreen
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

import java.util.function.Consumer

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class MasterDetailInitEntityTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["master_detail"])
    }

    def "MasterDetailScreen fires InitEntityEvent on Create"() {
        showTestMainScreen()

        def initEntityListener = Mock(Consumer)

        def masterDetail = screens.create(OrderMasterDetailTestScreen)
        masterDetail.addInitEntityListener(initEntityListener)
        masterDetail.show()

        def table = (Table) masterDetail.getWindow().getComponentNN("table")
        def createAction = table.getAction("create")

        when:
        createAction.actionPerform(table)
        def order = masterDetail.getEditedOrder()

        then:
        order != null
        order.number == "New number"

        1 * initEntityListener.accept(_) >> { MasterDetailScreen.InitEntityEvent event ->
            assert event.getEntity() != null
        }
    }
}