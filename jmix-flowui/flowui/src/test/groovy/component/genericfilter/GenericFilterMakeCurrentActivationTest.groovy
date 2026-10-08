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

import component.genericfilter.view.GfActivationBeforeShowTestView
import component.genericfilter.view.GfActivationDoubleLoadTestView
import component.genericfilter.view.GfActivationOnInitDlcTestView
import component.genericfilter.view.GfActivationOnInitNoDlcTestView
import component.genericfilter.view.GfActivationPlainSetCurrentTestView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import static component.genericfilter.TestFilterConditions.countPropertyConditions

/**
 * Integration tests for the variability of configuration activation via the GenericFilter builder:
 * the lifecycle phase in which a configuration is made current, whether a {@code DataLoadCoordinator}
 * is present, and the resulting load behavior. Complements {@code GenericFilterInitialConditionTest},
 * which covers the core "switching does not stack conditions" invariant for the {@code onInit} path.
 */
@SpringBootTest
class GenericFilterMakeCurrentActivationTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "makeCurrent in onInit activates the configuration synchronously"() {
        when: "the view opens; c1 is made current via makeCurrent() in onInit"
        def view = navigateToView(GfActivationOnInitNoDlcTestView)

        then: "the configuration was made current immediately during onInit (right after buildAndRegister)"
        view.currentRightAfterMakeCurrent.id == "c1"

        and: "it remains current after the view is shown"
        view.genericFilter.currentConfiguration.id == "c1"
    }

    def "without a DataLoadCoordinator, makeCurrent triggers a single filtered load on open"() {
        when: "the view opens (no DataLoadCoordinator); applyFilterIfNeeded loads the default-valued config"
        def view = navigateToView(GfActivationOnInitNoDlcTestView)

        then: "exactly one load happened, filtered by the active configuration"
        view.loadCount == 1
        countPropertyConditions(view.genericFilter.dataLoader.condition) == 1
    }

    def "with a DataLoadCoordinator, makeCurrent yields exactly one filtered load on open"() {
        when: "the view opens (with a DataLoadCoordinator)"
        def view = navigateToView(GfActivationOnInitDlcTestView)

        then: "exactly one load happened, with the configuration's condition applied"
        view.loadCount == 1
        countPropertyConditions(view.genericFilter.dataLoader.condition) == 1
    }

    def "activation in BeforeShow (filter already attached) is synchronous and switching applies only the new configuration"() {
        when: "the view opens; c1 is made current via makeCurrent() in BeforeShow"
        def view = navigateToView(GfActivationBeforeShowTestView)

        then: "c1 is current and only its condition is applied"
        view.genericFilter.currentConfiguration.id == "c1"
        countPropertyConditions(view.genericFilter.dataLoader.condition) == 1

        when: "switching to c2"
        view.switchTo("c2")

        then: "only c2's condition is applied, not c1 AND c2"
        countPropertyConditions(view.genericFilter.dataLoader.condition) == 1
    }

    def "a current default configuration plus a deferred makeCurrent triggers exactly one load (no double load)"() {
        when: "the view opens: a default-like config is current in onInit, another is makeCurrent (deferred), no DataLoadCoordinator"
        def view = navigateToView(GfActivationDoubleLoadTestView)

        then: "exactly one load happened — the deferred activation did not add a second one"
        view.loadCount == 1
    }

    def "plain setCurrentConfiguration in onInit does not pollute the baseline"() {
        given: "the view opens; c1 is activated via base-API setCurrentConfiguration in onInit"
        def view = navigateToView(GfActivationPlainSetCurrentTestView)

        when: "switching to c2"
        view.genericFilter.setCurrentConfiguration(view.genericFilter.getConfiguration("c2"))

        then: "only c2's condition is applied"
        countPropertyConditions(view.genericFilter.dataLoader.condition) == 1
    }

}
