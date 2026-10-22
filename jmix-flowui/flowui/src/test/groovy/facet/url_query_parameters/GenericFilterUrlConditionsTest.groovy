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

import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.router.QueryParameters
import facet.url_query_parameters.view.GenericFilterUrlConditionsTestView
import facet.url_query_parameters.view.GenericFilterUrlQueryParamsTestView
import io.jmix.core.DataManager
import io.jmix.core.MetadataTools
import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.flowui.component.factory.PropertyFilterComponentGenerationStrategy
import io.jmix.flowui.component.filter.FilterComponent
import io.jmix.flowui.component.genericfilter.Configuration
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.facet.urlqueryparameters.FilterUrlQueryParametersSupport
import io.jmix.flowui.facet.urlqueryparameters.GenericFilterUrlQueryParametersBinder
import io.jmix.flowui.view.navigation.UrlParamSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.petclinic.OwnerCategory
import test_support.spec.FlowuiTestSpecification

import java.util.function.Predicate

import static facet.url_query_parameters.TestGenericFilterUrlBinders.getBinder
import static facet.url_query_parameters.TestGenericFilterUrlBinders.propertyFilterOn

/**
 * Pins the values {@link GenericFilterUrlQueryParametersBinder} gives to the conditions from the URL and
 * the rows it matches them onto: the value of a reference and a date-time attribute on a new row and on
 * a row of a configuration, and what happens to a condition whose operation differs from the one of a
 * row that does not allow changing it. Also checks that the value of a condition that is not used is not
 * parsed, that an operation the attribute does not support is skipped, and that a row added from the URL
 * keeps the classes of its generated value component.
 */
@SpringBootTest
class GenericFilterUrlConditionsTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    UrlParamSerializer urlParamSerializer

    @Autowired
    MetadataTools metadataTools

    OwnerCategory cat1
    OwnerCategory cat2

    @Override
    void setup() {
        registerViewBasePackages("facet.url_query_parameters", "io.jmix.flowui.app")

        cat1 = dataManager.save(newCategory("cat1"))
        cat2 = dataManager.save(newCategory("cat2"))
    }

    // The base fixture methods run on their own, so there is no super.cleanup() here.
    @Override
    void cleanup() {
        dataManager.remove(cat1, cat2)
    }

    def "reference EQUAL value from the URL is set on a new row"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)

        when:
        applyConditions(view, "property:category_equal_${urlParamSerializer.serialize(cat1.id)}")

        then:
        def rows = rowsOf(view.ownersFilter.emptyConfiguration)
        rows.size() == 1
        (rows.first() as PropertyFilter).value == cat1
    }

    def "reference EQUAL value from the URL is set on the matched row"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)
        Configuration byCategory = view.ownersFilter.getConfiguration("byCategory")

        when:
        applyConfiguration(view, "byCategory", "property:category_equal_${urlParamSerializer.serialize(cat1.id)}")

        then:
        def rows = rowsOf(byCategory)
        rows.size() == 1
        (rows.first() as PropertyFilter).value == cat1

        and:
        view.ownersFilter.currentConfiguration.is(byCategory)
    }

    def "reference IN_LIST value from the URL is set on a new row"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)

        when:
        applyConditions(view, inListCondition())

        then:
        def rows = rowsOf(view.ownersFilter.emptyConfiguration)
        rows.size() == 1
        ((rows.first() as PropertyFilter).value as List) == [cat1, cat2]
    }

    def "reference IN_LIST value from the URL is set on the matched row"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)
        Configuration byCategories = view.ownersFilter.getConfiguration("byCategories")

        when:
        applyConfiguration(view, "byCategories", inListCondition())

        then:
        def rows = rowsOf(byCategories)
        rows.size() == 1
        ((rows.first() as PropertyFilter).value as List) == [cat1, cat2]
    }

    def "DATE_EQUALS value from the URL is the same on a new row and on the matched row"() {
        given: "a date with a time of day, so the value cannot be told from a start of the day by accident"
        Date date = new GregorianCalendar(2026, Calendar.OCTOBER, 22, 13, 45, 10).time
        String condition = "property:createTs_date-equals_${urlParamSerializer.serialize(date)}"

        when: "the condition is applied on a new row of the empty configuration"
        def newRowView = openFreshView()
        applyConditions(newRowView, condition)
        def newRowValue = (rowsOf(newRowView.ownersFilter.emptyConfiguration).first() as PropertyFilter).value

        and: "the same condition is applied on the matched row of a configuration in a fresh view"
        def matchedRowView = openFreshView()
        applyConfiguration(matchedRowView, "byCreateTs", condition)
        def matchedRowValue = propertyFilterOn(matchedRowView.ownersFilter.getConfiguration("byCreateTs"), "createTs")
                .value

        then:
        !newRowView.is(matchedRowView)
        newRowValue != null
        matchedRowValue != null
        newRowValue == matchedRowValue
    }

    def "URL operation differing from a non-editable runtime row adds a new row"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)
        Configuration runtimeByName = view.ownersFilter.getConfiguration("runtimeByName")
        def originalValue = propertyFilterOn(runtimeByName, "name").value

        when:
        applyConfiguration(view, "runtimeByName", "property:name_not-equal_x")

        then:
        def rows = rowsOf(runtimeByName)
        rows.size() == 2

        and: "the original row is left as it was"
        def original = rows.find { (it as PropertyFilter).operation == PropertyFilter.Operation.EQUAL } as PropertyFilter
        original.value == originalValue

        and: "the URL condition is a new row"
        def added = rows.find { (it as PropertyFilter).operation == PropertyFilter.Operation.NOT_EQUAL } as PropertyFilter
        added.value == "x"
    }

    def "URL operation differing from a non-editable design-time row is dropped"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)
        Configuration byName = view.ownersFilter.getConfiguration("byName")
        def originalValue = propertyFilterOn(byName, "name").value

        when:
        applyConfiguration(view, "byName", "property:name_not-equal_x")

        then:
        def rows = rowsOf(byName)
        rows.size() == 1

        and:
        def row = rows.first() as PropertyFilter
        row.operation == PropertyFilter.Operation.EQUAL
        row.value == originalValue
    }

    def "a condition rejected by the property predicate is not value-parsed"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)
        view.ownersFilter.propertyFiltersPredicate = { it.toPathString() != "category" } as Predicate<MetaPropertyPath>
        def support = spyUrlParamsSupport(view)

        when:
        applyConditions(view, "property:category_equal_${urlParamSerializer.serialize(cat1.id)}")

        then:
        0 * support.parseValue(*_)

        and:
        rowsOf(view.ownersFilter.emptyConfiguration).isEmpty()
    }

    def "a condition dropped on a design-time configuration is not value-parsed"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)
        Configuration byName = view.ownersFilter.getConfiguration("byName")
        def support = spyUrlParamsSupport(view)

        when:
        applyConfiguration(view, "byName", "property:category_equal_${urlParamSerializer.serialize(cat1.id)}")

        then:
        0 * support.parseValue(*_)

        and:
        rowsOf(byName).size() == 1
    }

    def "an unsupported operation in the URL is skipped on a new row"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)

        when:
        applyConditions(view, "property:name_greater_x")

        then:
        noExceptionThrown()

        and:
        rowsOf(view.ownersFilter.emptyConfiguration).isEmpty()
    }

    def "an unsupported operation in the URL is skipped on an editable matched row"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)
        Configuration byNameEditable = view.ownersFilter.getConfiguration("byNameEditable")

        when:
        applyConfiguration(view, "byNameEditable", "property:name_greater_x")

        then:
        noExceptionThrown()

        and:
        def rows = rowsOf(byNameEditable)
        rows.size() == 1
        (rows.first() as PropertyFilter).operation == PropertyFilter.Operation.EQUAL
    }

    def "an IS_SET row added from the URL keeps the unary-field class"() {
        given:
        def view = navigateToView(GenericFilterUrlConditionsTestView)

        when:
        applyConditions(view, "property:name_is-set_true")

        then:
        def rows = rowsOf(view.ownersFilter.emptyConfiguration)
        rows.size() == 1

        and:
        def row = rows.first() as PropertyFilter
        row.operation == PropertyFilter.Operation.IS_SET
        (row.valueComponent as HasStyle).classNames
                .contains(PropertyFilterComponentGenerationStrategy.UNARY_FIELD_CLASS_NAME)
    }

    // Spock 2.4 needs a no-arg constructor to spy on an existing instance, so the spy is a new instance.
    FilterUrlQueryParametersSupport spyUrlParamsSupport(GenericFilterUrlConditionsTestView view) {
        def support = Spy(FilterUrlQueryParametersSupport,
                constructorArgs: [dataManager, metadataTools, urlParamSerializer])
        getBinder(view).filterUrlQueryParametersSupport = support
        return support
    }

    // Navigating to the view that is already open reuses its instance, so another view is opened first.
    GenericFilterUrlConditionsTestView openFreshView() {
        navigateToView(GenericFilterUrlQueryParamsTestView)
        return navigateToView(GenericFilterUrlConditionsTestView)
    }

    String inListCondition() {
        return "property:category_in-list_${urlParamSerializer.serialize([cat1.id, cat2.id])}"
    }

    OwnerCategory newCategory(String name) {
        OwnerCategory category = dataManager.create(OwnerCategory)
        category.name = name
        return category
    }

    void applyConditions(GenericFilterUrlConditionsTestView view, String... conditions) {
        def binder = getBinder(view)
        binder.updateState(new QueryParameters([(binder.conditionParam): conditions.toList()]))
    }

    void applyConfiguration(GenericFilterUrlConditionsTestView view, String configurationId,
                            String... conditions) {
        def binder = getBinder(view)
        binder.updateState(new QueryParameters([
                (binder.configurationParam): [configurationId],
                (binder.conditionParam)    : conditions.toList()
        ]))
    }

    List<FilterComponent> rowsOf(Configuration configuration) {
        return configuration.rootLogicalFilterComponent.filterComponents
    }
}
