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
import facet.url_query_parameters.view.GenericFilterEditableOpConfigTestView
import facet.url_query_parameters.view.GenericFilterUrlQueryParamsTestView
import io.jmix.core.querycondition.Condition
import io.jmix.core.querycondition.LogicalCondition
import io.jmix.core.querycondition.PropertyCondition
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.UrlQueryParametersFacet
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder
import io.jmix.flowui.model.CollectionLoader
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * The URL restore parses conditions into plain models first, validates them there, and creates a
 * filter component (through the converter route) only for the conditions that survive. This spec
 * pins the behaviors that the model-first flow guarantees: an unknown attribute degrades to a
 * skipped condition instead of a failed navigation, a restored condition never leaks into the data
 * loader condition outside the filter's own composition, and applying the URL state costs a single
 * data load.
 */
@SpringBootTest
class GenericFilterUrlConditionModelTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")
    }

    def "a URL condition on an unknown attribute is skipped and does not fail the restore"() {
        given: "a view with a generic filter"
        def view = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(view.urlQueryParameters)

        when: "the URL carries a condition on an attribute that does not exist"
        binder.updateState(QueryParameters.simple(
                [(binder.conditionParam): "property:nonexistent_contains_x"]))

        then: "the restore succeeds and the condition is not applied"
        noExceptionThrown()
        view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents.isEmpty()
    }

    def "a restored condition appears in the loader condition exactly once"() {
        given:
        def view = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(view.urlQueryParameters)

        when: "the URL carries a condition that is added to the current configuration"
        binder.updateState(QueryParameters.simple(
                [(binder.conditionParam): "property:name_contains_John"]))

        then: "the filter shows the condition"
        def components = view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents
        components.size() == 1
        (components.first() as PropertyFilter<?>).value == "John"

        and: "the loader condition contains it exactly once - only through the filter's composition"
        countPropertyConditions(view.ownersFilter.dataLoader.condition, "name") == 1
    }

    def "a condition rejected by the property filters predicate leaves no trace in the loader condition"() {
        given: "a filter that does not allow filtering by 'name'"
        def view = navigateToView(GenericFilterUrlQueryParamsTestView)
        view.ownersFilter.setPropertyFiltersPredicate { mpp -> !"name".equals(mpp.toPathString()) }
        def binder = getBinder(view.urlQueryParameters)

        when:
        binder.updateState(QueryParameters.simple(
                [(binder.conditionParam): "property:name_contains_John"]))

        then: "no component is added and nothing on 'name' reaches the loader condition"
        view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents.isEmpty()
        countPropertyConditions(view.ownersFilter.dataLoader.condition, "name") == 0
    }

    def "restoring a configuration with a changed operation fires no load of its own"() {
        given: "a view with a design-time configuration whose condition operation is editable"
        def view = navigateToView(GenericFilterEditableOpConfigTestView)
        def binder = getBinder(view.urlQueryParameters)
        int loads = 0
        (view.ownersFilter.dataLoader as CollectionLoader).addPostLoadListener { loads++ }

        when: "the URL selects the configuration and carries a different operation for its condition"
        binder.updateState(new QueryParameters([
                (binder.configurationParam): List.of("byName"),
                (binder.conditionParam)    : List.of("property:name_not-equal_Bob")]))

        then: "the operation and the value are applied to the configuration's own condition"
        def component = view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents
                .first() as PropertyFilter<?>
        component.operation == PropertyFilter.Operation.NOT_EQUAL
        component.value == "Bob"

        and: "the restore composed the loader condition without loading - the load belongs to the navigation itself"
        loads == 0
        countPropertyConditions(view.ownersFilter.dataLoader.condition, "name") == 1
    }

    def "a malformed condition string is skipped and does not fail the restore"() {
        given:
        def view = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(view.urlQueryParameters)

        when: "the URL carries a hand-edited condition without separators and one with an unknown operation"
        binder.updateState(QueryParameters.simple([(binder.conditionParam): conditionString]))

        then: "the restore succeeds and the condition is not applied"
        noExceptionThrown()
        view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents.isEmpty()

        where:
        conditionString << ["property:name", "property:name_garbage_x", "garbage",
                            "property:_contains_x", "property:__", "property:a.b.c_equal_x",
                            "property:name..weird_equal_x"]
    }

    def "a LIST condition value from the URL is restored as a collection"() {
        given:
        def view = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(view.urlQueryParameters)

        when:
        binder.updateState(QueryParameters.simple(
                [(binder.conditionParam): "property:name_in-list_John,Jane"]))

        then:
        def component = view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents
                .first() as PropertyFilter<?>
        component.operation == PropertyFilter.Operation.IN_LIST
        component.value == ["John", "Jane"]
    }

    def "a runtime configuration takes a matched operation change and a new condition from the URL without loading"() {
        given: "a view with a runtime configuration built by the programmatic API"
        def view = navigateToView(GenericFilterConfigsTestView)
        def binder = getBinder(view.urlQueryParameters)
        int loads = 0
        (view.ownersFilter.dataLoader as CollectionLoader).addPostLoadListener { loads++ }

        when: "the URL selects it, changes the operation of its own condition and adds a new one"
        binder.updateState(new QueryParameters([
                (binder.configurationParam): List.of("active"),
                (binder.conditionParam)    : List.of(
                        "property:name_not-equal_Bob",
                        "property:email_contains_gmail")]))

        then: "the existing condition is updated in place and the new one is added as modified"
        def components = view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents
        components.size() == 2
        with(components.find { (it as PropertyFilter<?>).property == "name" } as PropertyFilter<?>) {
            operation == PropertyFilter.Operation.NOT_EQUAL
            value == "Bob"
        }
        with(components.find { (it as PropertyFilter<?>).property == "email" } as PropertyFilter<?>) {
            operation == PropertyFilter.Operation.CONTAINS
            value == "gmail"
        }
        view.ownersFilter.currentConfiguration.isFilterComponentModified(
                components.find { (it as PropertyFilter<?>).property == "email" })

        and: "the restore fired no load of its own"
        loads == 0
    }

    def "an unparsable condition value degrades to a condition without a value"() {
        given:
        def view = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(view.urlQueryParameters)

        when: "the URL carries a resolvable attribute with a value that cannot be deserialized"
        binder.updateState(QueryParameters.simple([(binder.conditionParam): conditionString]))

        then: "the condition is present but empty, and the restore succeeds"
        noExceptionThrown()
        def components = view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents
        components.size() == 1
        (components.first() as PropertyFilter<?>).value == null

        where: "a malformed UUID and an unparsable value of an embedded attribute"
        conditionString << ["property:id_equal_not-a-uuid", "property:address_equal_x"]
    }

    def "a condition with an operation not available for the attribute is skipped"() {
        given:
        def view = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(view.urlQueryParameters)

        when: "the URL carries a string operation on a UUID attribute"
        binder.updateState(QueryParameters.simple(
                [(binder.conditionParam): "property:id_contains_x"]))

        then: "the restore succeeds and the condition is not applied"
        noExceptionThrown()
        view.ownersFilter.currentConfiguration.rootLogicalFilterComponent.filterComponents.isEmpty()
    }

    def "an operation outside the component's operations list is not applied and does not fail the restore"() {
        given: "a design-time configuration whose condition allows only EQUAL"
        def view = navigateToView(GenericFilterEditableOpConfigTestView)
        def binder = getBinder(view.urlQueryParameters)
        def component = view.ownersFilter.getConfiguration("byName").rootLogicalFilterComponent.filterComponents
                .first() as PropertyFilter<?>
        component.setOperationsList(List.of(PropertyFilter.Operation.EQUAL))

        when: "the URL carries an operation outside that list"
        binder.updateState(new QueryParameters([
                (binder.configurationParam): List.of("byName"),
                (binder.conditionParam)    : List.of("property:name_not-equal_Bob")]))

        then: "the restore succeeds and the component keeps its own operation"
        noExceptionThrown()
        component.operation == PropertyFilter.Operation.EQUAL
    }

    @SuppressWarnings('GrDeprecatedAPIUsage')
    def "deprecated component-based methods delegate to the model flow"() {
        given:
        def view = navigateToView(GenericFilterUrlQueryParamsTestView)
        def binder = getBinder(view.urlQueryParameters)
        def dataLoader = view.ownersFilter.dataLoader

        when: "the deprecated parser is called directly"
        def component = binder.parsePropertyCondition("name_contains_a_b", dataLoader) as PropertyFilter<?>

        then: "it returns a component built by the converter route, with delegated condition modification"
        component.property == "name"
        component.operation == PropertyFilter.Operation.CONTAINS
        component.value == "a_b"
        component.conditionModificationDelegated

        when: "the deprecated deserialization gets a valid condition and one on an unknown attribute"
        def components = binder.deserializeConditions(
                List.of("property:name_contains_x", "property:nonexistent_contains_x"), dataLoader)

        then: "only the valid condition yields a component"
        components.size() == 1
        (components.first() as PropertyFilter<?>).property == "name"

        and: "the deprecated permission check accepts the built component"
        binder.isPermitted(dataLoader, components.first())
    }

    private static int countPropertyConditions(Condition condition, String property) {
        if (condition instanceof LogicalCondition) {
            int count = 0
            for (Condition nested : condition.conditions) {
                count += countPropertyConditions(nested, property)
            }
            return count
        }
        if (condition instanceof PropertyCondition) {
            return property == condition.property ? 1 : 0
        }
        return 0
    }

    private static GenericFilterUrlQueryParametersBinder getBinder(UrlQueryParametersFacet facet) {
        return facet.binders
                .findAll { it instanceof GenericFilterUrlQueryParametersBinder }
                .first() as GenericFilterUrlQueryParametersBinder
    }
}
