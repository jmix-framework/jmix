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

package facet.url_query_parameters

import com.vaadin.flow.router.QueryParameters
import facet.url_query_parameters.view.GenericFilterBaseLeakTestView
import facet.url_query_parameters.view.GenericFilterConfigsTestView
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.component.genericfilter.Configuration
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.UrlQueryParametersFacet
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * Opening a view directly by a URL that already carries generic filter parameters (as happens when
 * returning to a list view from a detail view) must not leave a stale copy of the URL condition in
 * the data loader.
 */
@SpringBootTest
class GenericFilterBaseLeakTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")
    }

    def "a URL condition matched onto a configuration baseline is not left behind in the loader"() {
        given: "a view opened by a URL that selects a configuration and sets its condition value"
        def screen = navigateToView(GenericFilterBaseLeakTestView)
        def binder = getBinder(screen)
        Configuration byName = screen.ownersFilter.getConfiguration("byName")

        expect: "the filter has not touched the loader condition yet"
        screen.ownersDl.condition == null

        when:
        binder.updateState(QueryParameters.simple([
                (binder.configurationParam): "byName",
                (binder.conditionParam)    : "property:name_equal_lane"
        ]))

        then: "the URL value landed on the configuration's own condition"
        propertyFilterOn(byName, "name").value == "lane"

        when: "the user changes the filter value, as if typing a new one in the field"
        propertyFilterOn(byName, "name").setValue("mey")
        screen.ownersFilter.apply()

        then: "the loader still filters by 'name' exactly once, by the new value"
        namePropertyConditions(screen.ownersDl.condition)*.parameterValue == ["mey"]
    }

    def "a configuration activated during init: URL value change must not accumulate loader conditions"() {
        given: "a view whose configuration is made current during init (as a default configuration is)"
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = GenericFilterReNavigationTest.getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")

        when: "the view is opened by a URL carrying that configuration and a condition value"
        binder.updateState(QueryParameters.simple([
                (binder.configurationParam): "active",
                (binder.conditionParam)    : "property:name_equal_lane"
        ]))

        and: "the user changes the filter value"
        GenericFilterReNavigationTest.propertyFilterOn(active, "name").setValue("mey")
        screen.ownersFilter.apply()

        then: "the loader filters by 'name' exactly once, by the new value"
        namePropertyConditions(screen.ownersFilter.dataLoader.condition)*.parameterValue == ["mey"]
    }

    def "a non-logical base condition set by the application must not absorb the URL condition"() {
        given: "a view opened by a URL with filter parameters"
        def screen = navigateToView(GenericFilterBaseLeakTestView)
        def binder = getBinder(screen)
        Configuration byName = screen.ownersFilter.getConfiguration("byName")

        and: "the application has its own single (non-logical) condition on the loader"
        screen.ownersDl.condition = PropertyCondition.contains("email", "example.com")

        when:
        binder.updateState(QueryParameters.simple([
                (binder.configurationParam): "byName",
                (binder.conditionParam)    : "property:name_equal_lane"
        ]))

        and: "the user changes the filter value"
        propertyFilterOn(byName, "name").setValue("mey")
        screen.ownersFilter.apply()

        then: "the loader filters by 'name' exactly once, and the application condition is kept"
        namePropertyConditions(screen.ownersDl.condition)*.parameterValue == ["mey"]
        allPropertyConditions(screen.ownersDl.condition).any { it.property == "email" }
    }

    def "workaround: activating the empty configuration in onInit makes the filter capture a clean base"() {
        given: "a view opened by a URL with filter parameters"
        def screen = navigateToView(GenericFilterBaseLeakTestView)
        def binder = getBinder(screen)
        Configuration byName = screen.ownersFilter.getConfiguration("byName")

        when: "the application forces the base condition capture before the URL is applied"
        screen.ownersFilter.setCurrentConfiguration(screen.ownersFilter.emptyConfiguration)

        and: "the URL parameters arrive and the user then changes the filter value"
        binder.updateState(QueryParameters.simple([
                (binder.configurationParam): "byName",
                (binder.conditionParam)    : "property:name_equal_lane"
        ]))
        propertyFilterOn(byName, "name").setValue("mey")
        screen.ownersFilter.apply()

        then: "the loader filters by 'name' exactly once, by the new value"
        namePropertyConditions(screen.ownersDl.condition)*.parameterValue == ["mey"]

        and: "the URL-selected configuration is still the current one"
        screen.ownersFilter.currentConfiguration.is(byName)
    }

    // --- helpers ---

    static GenericFilterUrlQueryParametersBinder getBinder(screen) {
        UrlQueryParametersFacet facet = screen.urlQueryParameters
        return facet.binders
                .findAll { it instanceof GenericFilterUrlQueryParametersBinder }
                .first() as GenericFilterUrlQueryParametersBinder
    }

    static PropertyFilter propertyFilterOn(Configuration configuration, String property) {
        return configuration.rootLogicalFilterComponent.filterComponents.find {
            it instanceof PropertyFilter && ((PropertyFilter) it).property == property
        } as PropertyFilter
    }

    static List<PropertyCondition> namePropertyConditions(Condition condition) {
        return allPropertyConditions(condition).findAll { it.property == "name" }
    }

    static List<PropertyCondition> allPropertyConditions(Condition condition) {
        List<PropertyCondition> result = []
        collectPropertyConditions(condition, result)
        return result
    }

    static void collectPropertyConditions(Condition condition, List<PropertyCondition> result) {
        if (condition instanceof LogicalCondition) {
            condition.conditions.each { collectPropertyConditions(it, result) }
        } else if (condition instanceof PropertyCondition) {
            result.add(condition)
        }
    }
}
