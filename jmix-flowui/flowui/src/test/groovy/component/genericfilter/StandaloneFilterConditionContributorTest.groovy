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
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent
import io.jmix.flowui.model.CollectionLoader
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import static component.genericfilter.TestFilterConditions.hasPropertyConditionOn

/**
 * With {@code jmix.ui.component.standalone-filter-contributes-condition = true} a standalone
 * filter component participates in loading as a condition contributor of its data loader: the
 * loader's condition slot stays with the other parties, and the contribution survives a rebuild
 * of the slot by a composing filter component.
 */
@SpringBootTest(properties = ["jmix.ui.component.standalone-filter-contributes-condition = true"])
class StandaloneFilterConditionContributorTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "standalone filter leaves the loader condition slot untouched and contributes at load"() {
        when: "the view opens: a standalone GroupFilter and a standalone PropertyFilter on one loader"
        GfStandaloneContributorTestView view = navigateToView(GfStandaloneContributorTestView)
        CollectionLoader loader = view.groupFilter.dataLoader as CollectionLoader

        then: "the loader condition slot is not touched by the standalone filter"
        !hasPropertyConditionOn(loader.condition, "amount")

        and: "the load itself sees the standalone contribution"
        hasPropertyConditionOn(loader.createLoadContext().getQuery().getCondition(), "amount")
    }

    def "contribution survives a rebuild of the loader condition by the composing filter"() {
        given: "the standalone PropertyFilter contributes to the shared loader"
        GfStandaloneContributorTestView view = navigateToView(GfStandaloneContributorTestView)
        CollectionLoader loader = view.groupFilter.dataLoader as CollectionLoader

        when: "a structural change makes the group rebuild the loader condition from scratch"
        view.groupFilter.setOperation(LogicalFilterComponent.Operation.OR)

        then: "the standalone contribution is still applied on load"
        hasPropertyConditionOn(loader.createLoadContext().getQuery().getCondition(), "amount")

        and: "without being written into the rebuilt slot"
        !hasPropertyConditionOn(loader.condition, "amount")
    }

    def "delegating condition modification withdraws the contribution"() {
        given: "the standalone PropertyFilter contributes to the shared loader"
        GfStandaloneContributorTestView view = navigateToView(GfStandaloneContributorTestView)
        CollectionLoader loader = view.standaloneFilter.dataLoader as CollectionLoader
        assert hasPropertyConditionOn(loader.createLoadContext().getQuery().getCondition(), "amount")

        when: "an owner takes over condition management of the filter"
        view.standaloneFilter.setConditionModificationDelegated(true)

        then: "the filter no longer contributes on its own"
        !hasPropertyConditionOn(loader.createLoadContext().getQuery().getCondition(), "amount")
    }
}
