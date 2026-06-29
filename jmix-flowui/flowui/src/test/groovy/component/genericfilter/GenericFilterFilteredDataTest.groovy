/*
 * Copyright 2026 Haulmont.
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

package component.genericfilter

import component.genericfilter.view.GfDlcFilteredLoadTestView
import component.genericfilter.view.GfDlcPlainSetCurrentTestView
import io.jmix.core.DataManager
import io.jmix.flowui.component.genericfilter.GenericFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

/**
 * Data-level integration tests: they seed real {@code Order} rows and assert the rows actually
 * loaded into the data container (not just the loader's condition object), with a
 * {@code DataLoadCoordinator} present.
 *
 * Seed: 3 orders with number = FLT_MATCH, 2 with number = FLT_OTHER.
 */
@SpringBootTest
class GenericFilterFilteredDataTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    List<Order> seeded = []

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
        seeded = [
                dataManager.save(new Order(number: "FLT_MATCH")),
                dataManager.save(new Order(number: "FLT_MATCH")),
                dataManager.save(new Order(number: "FLT_MATCH")),
                dataManager.save(new Order(number: "FLT_OTHER")),
                dataManager.save(new Order(number: "FLT_OTHER")),
        ]
    }

    void cleanup() {
        seeded.each { dataManager.remove(it) }
    }

    def "with a DataLoadCoordinator, the initial grid shows only the active configuration's rows"() {
        when: "the view opens; configuration 'match' (number = FLT_MATCH) is active via makeCurrent() in onInit"
        GenericFilter filter = navigateToView(GfDlcFilteredLoadTestView).genericFilter
        def items = filter.dataLoader.container.items

        then: "only the 3 matching orders are loaded, not all seeded orders"
        items.size() == 3
        items.every { it.number == "FLT_MATCH" }
    }

    def "switching configuration loads only the target configuration's rows"() {
        given:
        GenericFilter filter = navigateToView(GfDlcFilteredLoadTestView).genericFilter

        when: "switching to configuration 'other' (number = FLT_OTHER) and applying"
        filter.setCurrentConfiguration(filter.getConfiguration("other"))
        filter.apply()
        def items = filter.dataLoader.container.items

        then: "only the 2 'other' orders are loaded (not 0 from condition stacking)"
        items.size() == 2
        items.every { it.number == "FLT_OTHER" }
    }

    def "plain setCurrentConfiguration in onInit: initial load filtered and switching loads only the target rows"() {
        when: "the view opens; 'match' is activated via base-API setCurrentConfiguration in onInit (no builder)"
        GenericFilter filter = navigateToView(GfDlcPlainSetCurrentTestView).genericFilter

        then: "initial load is filtered to the 3 matching rows"
        filter.dataLoader.container.items.size() == 3
        filter.dataLoader.container.items.every { it.number == "FLT_MATCH" }

        when: "switching to 'other'"
        filter.setCurrentConfiguration(filter.getConfiguration("other"))
        filter.apply()

        then: "only the 2 'other' rows are loaded (not 0 from condition stacking)"
        filter.dataLoader.container.items.size() == 2
        filter.dataLoader.container.items.every { it.number == "FLT_OTHER" }
    }

    def "repeated configuration switching does not accumulate conditions (issue #2406 guard)"() {
        given:
        GenericFilter filter = navigateToView(GfDlcFilteredLoadTestView).genericFilter

        when: "switching back and forth several times, applying each time"
        filter.setCurrentConfiguration(filter.getConfiguration("other"))
        filter.apply()
        def afterOther1 = filter.dataLoader.container.items.collect { it.number }

        filter.setCurrentConfiguration(filter.getConfiguration("match"))
        filter.apply()
        def afterMatch = filter.dataLoader.container.items.collect { it.number }

        filter.setCurrentConfiguration(filter.getConfiguration("other"))
        filter.apply()
        def afterOther2 = filter.dataLoader.container.items.collect { it.number }

        then: "each switch yields exactly the target rows — no accumulation/corruption across applies"
        afterOther1.size() == 2 && afterOther1.every { it == "FLT_OTHER" }
        afterMatch.size() == 3 && afterMatch.every { it == "FLT_MATCH" }
        afterOther2.size() == 2 && afterOther2.every { it == "FLT_OTHER" }
    }
}
