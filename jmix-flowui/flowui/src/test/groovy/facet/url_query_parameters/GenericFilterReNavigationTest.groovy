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
import facet.url_query_parameters.view.GenericFilterConfigsTestView
import facet.url_query_parameters.view.GenericFilterDesignTimeConfigTestView
import facet.url_query_parameters.view.GenericFilterNestedGroupTestView
import facet.url_query_parameters.view.GenericFilterUrlQueryParamsTestView
import io.jmix.flowui.component.genericfilter.Configuration
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration
import io.jmix.flowui.component.logicalfilter.GroupFilter
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.UrlQueryParametersFacet
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder
import io.jmix.flowui.view.View
import io.jmix.flowui.view.ViewControllerUtils
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

import java.util.concurrent.atomic.AtomicInteger

/**
 * Verifies how a {@link io.jmix.flowui.component.genericfilter.GenericFilter} bound via the
 * {@code urlQueryParameters} facet behaves on a same-view re-navigation. When Vaadin reuses the
 * view instance, {@code onInit} is not called again: the facet fires
 * {@code RestoreComponentsStateEvent} (-&gt; {@code applyInitialState()}) and then a
 * {@code QueryParametersChangeEvent} (-&gt; {@code updateState(...)}). These two calls on the same
 * binder instance reproduce that lifecycle without a real browser.
 *
 * <ul>
 *     <li><b>Bug/T4</b> — a programmatic configuration activated in {@code onInit} must survive a
 *     clean re-navigation instead of being wiped by {@code applyInitialState()}.</li>
 *     <li><b>T1/T2</b> — the common empty-configuration + URL-derived conditions flow must be
 *     unchanged (byte-for-byte with the old {@code removeAll()} behaviour).</li>
 *     <li><b>T3</b> — a configuration switched to via the URL is reset to the initial configuration
 *     on a clean re-navigation.</li>
 *     <li><b>T6/T7</b> — a baseline value changed via the URL is reset, and a condition added via
 *     the URL on top of a baseline is removed, while the baseline itself is kept.</li>
 * </ul>
 */
@SpringBootTest
class GenericFilterReNavigationTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")
    }

    // --- Bug / T4: programmatic baseline must survive re-navigation ---

    def "programmatic configuration activated in onInit survives a clean re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")

        expect: "the programmatic 'name' condition is present after init"
        hasPropertyConditionOn(active, "name")

        when: "the same view is reopened: initial state restored, then a clean URL applied"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the programmatic condition is NOT wiped and the configuration stays current"
        hasPropertyConditionOn(active, "name")
        screen.ownersFilter.currentConfiguration.is(active)
    }

    // --- T1: empty configuration + URL condition, cleared on clean re-navigation ---

    def "T1: a URL-derived condition on the empty configuration is cleared on clean re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)

        when: "a condition is applied from the URL"
        binder.updateState(QueryParameters.simple([(binder.conditionParam): "property:name_equal_Bob"]))

        then: "the empty configuration now carries the condition"
        !currentComponents(screen).isEmpty()

        when: "clean re-navigation"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the condition is removed (back to the empty baseline)"
        currentComponents(screen).isEmpty()
    }

    // --- T2: switching between URLs does not accumulate conditions ---

    def "T2: re-navigating to a different URL condition does not accumulate conditions"() {
        given:
        def screen = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)

        when: "first URL condition"
        binder.updateState(QueryParameters.simple([(binder.conditionParam): "property:name_equal_Bob"]))

        and: "re-navigation to a different URL condition"
        binder.applyInitialState()
        binder.updateState(QueryParameters.simple([(binder.conditionParam): "property:name_equal_Alice"]))

        then: "exactly one condition, reflecting only the latest URL"
        def components = currentComponents(screen)
        components.size() == 1
        (components.first() as PropertyFilter).value == "Alice"
    }

    // --- T3: a configuration selected via URL is reset to the initial configuration ---

    def "T3: a configuration selected via URL after init is reset to the initial one on clean re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")
        Configuration other = screen.ownersFilter.getConfiguration("other")

        when: "the user switches to the 'other' configuration via URL"
        binder.updateState(QueryParameters.simple([(binder.configurationParam): "other"]))

        then:
        screen.ownersFilter.currentConfiguration.is(other)

        when: "clean re-navigation"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the filter is restored to the initial configuration, still carrying its baseline"
        screen.ownersFilter.currentConfiguration.is(active)
        hasPropertyConditionOn(active, "name")
    }

    // --- T6: a baseline value changed via URL is reset on re-navigation ---

    def "T6: a baseline value changed via URL is reset on clean re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")

        when: "URL selects the active configuration and changes the 'name' value"
        binder.updateState(QueryParameters.simple([
                (binder.configurationParam): "active",
                (binder.conditionParam)    : "property:name_equal_Zoe"
        ]))

        then: "the name filter value reflects the URL"
        propertyFilterOn(active, "name").value == "Zoe"

        when: "clean re-navigation"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the value is restored to the initial baseline value"
        propertyFilterOn(active, "name").value == "John"
    }

    // --- T7: a condition added via URL is removed while the baseline is kept ---

    def "T7: a condition added via URL on top of a baseline is removed on clean re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")

        when: "URL adds an extra 'email' condition on top of the 'name' baseline"
        binder.updateState(new QueryParameters([
                (binder.configurationParam): ["active"],
                (binder.conditionParam)    : ["property:name_equal_John", "property:email_equal_extra"]
        ]))

        then: "both conditions are present"
        hasPropertyConditionOn(active, "name")
        hasPropertyConditionOn(active, "email")

        when: "clean re-navigation"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the URL-added condition is gone, the baseline remains"
        hasPropertyConditionOn(active, "name")
        !hasPropertyConditionOn(active, "email")
    }

    // --- T8: an operation changed via URL on a baseline is reset ---

    def "T8: an operation changed via URL on a baseline is reset on clean re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")

        when: "URL changes the operation of the baseline 'name' condition"
        binder.updateState(QueryParameters.simple([
                (binder.configurationParam): "active",
                (binder.conditionParam)    : "property:name_contains_John"
        ]))

        then: "the operation reflects the URL"
        propertyFilterOn(active, "name").operation == PropertyFilter.Operation.CONTAINS

        when: "clean re-navigation"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the operation is restored to the initial baseline operation"
        propertyFilterOn(active, "name").operation == PropertyFilter.Operation.EQUAL
    }

    // --- T9: operation AND value changed via URL are both reset (ordering) ---

    def "T9: operation and value changed via URL are both reset on clean re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")

        when: "URL changes both the operation and the value of the baseline 'name' condition"
        binder.updateState(QueryParameters.simple([
                (binder.configurationParam): "active",
                (binder.conditionParam)    : "property:name_not-equal_Zoe"
        ]))

        then:
        propertyFilterOn(active, "name").operation == PropertyFilter.Operation.NOT_EQUAL
        propertyFilterOn(active, "name").value == "Zoe"

        when: "clean re-navigation"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "both operation and value are restored to the initial baseline"
        propertyFilterOn(active, "name").operation == PropertyFilter.Operation.EQUAL
        propertyFilterOn(active, "name").value == "John"
    }

    // --- a design-time configuration value changed via URL is reset on re-navigation ---

    def "T10: a design-time configuration value changed via URL is reset on clean re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterDesignTimeConfigTestView)
        def binder = getBinder(screen)
        Configuration byName = screen.ownersFilter.getConfiguration("byName")

        expect: "the design-time configuration is current with its default value"
        screen.ownersFilter.currentConfiguration.is(byName)
        propertyFilterOn(byName, "name").value == "John"

        when: "URL selects the design-time configuration and changes the 'name' value"
        binder.updateState(QueryParameters.simple([
                (binder.configurationParam): "byName",
                (binder.conditionParam)    : "property:name_equal_Zoe"
        ]))

        then: "the name filter value reflects the URL"
        propertyFilterOn(byName, "name").value == "Zoe"

        when: "clean re-navigation"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the value is restored to the design-time default"
        propertyFilterOn(byName, "name").value == "John"
    }

    // --- Facet-level: drive the REAL event chain (RestoreComponentsStateEvent /
    //     QueryParametersChangeEvent) through UrlQueryParametersFacetImpl, not the binder directly ---

    def "F1: programmatic configuration survives re-navigation through the real facet event chain"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        Configuration active = screen.ownersFilter.getConfiguration("active")

        expect: "the programmatic 'name' condition is present after init"
        hasPropertyConditionOn(active, "name")

        when: "the framework restores the initial state and applies a clean URL via real view events"
        ViewControllerUtils.fireEvent(screen, new View.RestoreComponentsStateEvent(screen))
        ViewControllerUtils.fireEvent(screen, new View.QueryParametersChangeEvent(screen, QueryParameters.empty()))

        then: "the facet routed the events to the binder and the baseline condition survived"
        hasPropertyConditionOn(active, "name")
        screen.ownersFilter.currentConfiguration.is(active)
    }

    def "F2: URL condition on the empty configuration is cleared on re-navigation through the real facet event chain"() {
        given:
        def screen = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)

        when: "a condition arrives via a real QueryParametersChangeEvent"
        ViewControllerUtils.fireEvent(screen, new View.QueryParametersChangeEvent(screen,
                QueryParameters.simple([(binder.conditionParam): "property:name_equal_Bob"])))

        then: "the empty configuration now carries the condition"
        !currentComponents(screen).isEmpty()

        when: "clean re-navigation: RestoreComponentsStateEvent then a clean QueryParametersChangeEvent"
        ViewControllerUtils.fireEvent(screen, new View.RestoreComponentsStateEvent(screen))
        ViewControllerUtils.fireEvent(screen, new View.QueryParametersChangeEvent(screen, QueryParameters.empty()))

        then: "the condition is removed (back to the empty baseline)"
        currentComponents(screen).isEmpty()
    }

    // --- Pure user actions (no URL): scope of the re-navigation restore across several configurations ---

    def "a value edit on a configuration is reset when switching away and back (no URL)"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        Configuration active = screen.ownersFilter.getConfiguration("active")
        Configuration other = screen.ownersFilter.getConfiguration("other")

        when: "the user edits the name value on the entry config, then switches to another config and back"
        propertyFilterOn(active, "name").setValue("Zoe")
        screen.ownersFilter.setCurrentConfiguration(other)
        screen.ownersFilter.setCurrentConfiguration(active)

        then: "the transient value is gone: it was reset to the configuration default on switch, not kept"
        propertyFilterOn(active, "name").value == "John"
    }

    def "a condition added to a non-entry configuration is also restored on re-navigation (no URL)"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")
        Configuration other = screen.ownersFilter.getConfiguration("other")

        when: "the user switches to 'other' and adds a condition to it (pure UI, no URL), then switches back"
        screen.ownersFilter.setCurrentConfiguration(other)
        def added = screen.ownersFilter.filterComponentBuilder()
                .propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.EQUAL)
                .build()
        other.rootLogicalFilterComponent.add(added)
        ((RunTimeConfiguration) other).setFilterComponentModified(added, true)
        screen.ownersFilter.setCurrentConfiguration(active)

        and: "a clean re-navigation happens"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the entry config is restored to its baseline"
        screen.ownersFilter.currentConfiguration.is(active)
        hasPropertyConditionOn(active, "name")

        and: "the non-entry config is ALSO restored: the user's addition is gone, its baseline remains"
        !hasPropertyConditionOn(other, "name")
        hasPropertyConditionOn(other, "email")
    }

    def "a condition removed from a non-entry configuration is restored on re-navigation (no URL)"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")
        Configuration other = screen.ownersFilter.getConfiguration("other")

        when: "the user switches to 'other' and removes its baseline 'email' condition, then switches back"
        screen.ownersFilter.setCurrentConfiguration(other)
        def emailFilter = propertyFilterOn(other, "email")
        other.rootLogicalFilterComponent.remove(emailFilter)
        ((RunTimeConfiguration) other).setFilterComponentModified(emailFilter, false)
        screen.ownersFilter.setCurrentConfiguration(active)

        then: "the condition is gone from 'other'"
        !hasPropertyConditionOn(other, "email")

        when: "a clean re-navigation happens"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the entry config is restored, and 'other' is restored too: its baseline condition is back"
        screen.ownersFilter.currentConfiguration.is(active)
        hasPropertyConditionOn(active, "name")
        hasPropertyConditionOn(other, "email")
    }

    // --- a baseline condition removed via the remove button is fully restored on re-navigation ---

    def "a baseline condition removed via the remove button is restored on re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(screen)
        Configuration active = screen.ownersFilter.getConfiguration("active")
        def nameFilter = propertyFilterOn(active, "name")
        def paramName = nameFilter.parameterName

        expect: "the baseline is present, modified (so it has a remove button) and its default value is registered"
        hasPropertyConditionOn(active, "name")
        active.isFilterComponentModified(nameFilter)
        active.getFilterComponentDefaultValue(paramName) == "John"

        when: "the user removes the baseline condition via the remove button (same effect as GenericFilter.removeFilterComponent)"
        active.resetFilterComponentDefaultValue(paramName)
        active.rootLogicalFilterComponent.remove(nameFilter)
        active.setFilterComponentModified(nameFilter, false)

        then: "it is gone from the configuration"
        !hasPropertyConditionOn(active, "name")

        when: "a clean re-navigation happens"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the baseline condition is fully restored: present again, modified (remove button reappears), default value re-registered and value reset"
        hasPropertyConditionOn(active, "name")
        def restored = propertyFilterOn(active, "name")
        active.isFilterComponentModified(restored)
        active.getFilterComponentDefaultValue(paramName) == "John"
        restored.value == "John"
    }

    // --- Nested group: a condition removed from a nested group is restored (recursive structure) ---

    def "a condition removed from a nested group is restored on re-navigation"() {
        given:
        def screen = navigateToView(GenericFilterNestedGroupTestView)
        def binder = getBinder(screen)
        Configuration grouped = screen.ownersFilter.getConfiguration("grouped")
        GroupFilter group = grouped.rootLogicalFilterComponent.ownFilterComponents
                .find { it instanceof GroupFilter } as GroupFilter

        expect: "the nested group initially holds both conditions, in order"
        group != null
        nestedProperties(group) == ["name", "email"]

        when: "a condition is removed directly from the nested group (simulating a fixed nested removal)"
        def nameInner = group.ownFilterComponents
                .find { it instanceof PropertyFilter && ((PropertyFilter) it).property == "name" }
        group.remove(nameInner)

        then: "only the other condition remains in the group"
        nestedProperties(group) == ["email"]

        when: "a clean re-navigation happens"
        binder.applyInitialState()
        binder.updateState(QueryParameters.empty())

        then: "the nested group is fully restored, in the original order"
        GroupFilter restoredGroup = grouped.rootLogicalFilterComponent.ownFilterComponents
                .find { it instanceof GroupFilter } as GroupFilter
        nestedProperties(restoredGroup) == ["name", "email"]
    }

    // --- Event storm: restore must not push URL parameters once per add/remove ---

    def "restore does not push URL parameters once per add/remove (event storm suppressed)"() {
        given:
        def screen = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(screen)

        and: "the empty configuration currently holds several URL-added conditions"
        binder.updateState(new QueryParameters([
                (binder.conditionParam): ["property:name_equal_A", "property:name_equal_B", "property:name_equal_C"]
        ]))
        assert currentComponents(screen).size() == 3

        and: "a listener counts how many times the binder pushes URL parameters"
        def pushCount = new AtomicInteger(0)
        def registration = binder.addUrlQueryParametersChangeListener({ event -> pushCount.incrementAndGet() })

        when: "the initial (empty) state is restored — this removes all three conditions"
        binder.applyInitialState()

        then: "all conditions are gone, but the whole reconciliation pushed at most once (not once per removed condition)"
        currentComponents(screen).isEmpty()
        pushCount.get() <= 1

        cleanup:
        registration.remove()
    }

    // --- helpers ---

    static GenericFilterUrlQueryParametersBinder getBinder(screen) {
        UrlQueryParametersFacet facet = screen.urlQueryParameters
        return facet.binders
                .findAll { it instanceof GenericFilterUrlQueryParametersBinder }
                .first() as GenericFilterUrlQueryParametersBinder
    }

    static List currentComponents(screen) {
        return screen.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents
    }

    static boolean hasPropertyConditionOn(Configuration configuration, String property) {
        return configuration.rootLogicalFilterComponent.filterComponents.any {
            it instanceof PropertyFilter && ((PropertyFilter) it).property == property
        }
    }

    static PropertyFilter propertyFilterOn(Configuration configuration, String property) {
        return configuration.rootLogicalFilterComponent.filterComponents.find {
            it instanceof PropertyFilter && ((PropertyFilter) it).property == property
        } as PropertyFilter
    }

    static List<String> nestedProperties(GroupFilter group) {
        return group.ownFilterComponents
                .findAll { it instanceof PropertyFilter }
                .collect { ((PropertyFilter) it).property }
    }
}
