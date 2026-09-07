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

import component.genericfilter.view.GfStandaloneContributorTestView
import io.jmix.flowui.model.CollectionLoader
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import static component.genericfilter.TestFilterConditions.hasPropertyConditionOn

/**
 * Pins the default behavior of a standalone filter component: with
 * {@code jmix.ui.component.standalone-filter-contributes-condition} off (the default) the filter
 * appends its condition into the loader's condition tree, exactly as before contributors existed.
 */
@SpringBootTest
class StandaloneFilterLegacyConditionTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "by default a standalone filter appends its condition into the loader condition tree"() {
        when: "the view opens: a standalone GroupFilter and a standalone PropertyFilter on one loader"
        GfStandaloneContributorTestView view = navigateToView(GfStandaloneContributorTestView)
        CollectionLoader loader = view.standaloneFilter.dataLoader as CollectionLoader

        then: "the standalone filter's condition sits in the loader condition slot"
        hasPropertyConditionOn(loader.condition, "amount")

        and: "the load context takes the slot as is - the very same condition instance"
        loader.createLoadContext().getQuery().getCondition().is(loader.condition)
    }
}
