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

import component.genericfilter.view.GenericFilterApiTestView
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration
import io.jmix.flowui.component.jpqlfilter.JpqlFilter
import io.jmix.flowui.component.logicalfilter.GroupFilter
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * Documents and verifies the programmatic API for GenericFilter builder classes.
 *
 * <p>These tests are intentionally written as usage examples so that reading them
 * gives a developer a quick and accurate picture of how to configure a
 * {@link GenericFilter} in Java/Groovy without relying on XML.
 *
 * <h2>Test classification: narrow integration tests</h2>
 * <p>Although individual test methods are small and focused (resembling unit tests),
 * this class is technically an <em>integration test suite</em>:
 * <ul>
 *   <li>{@code @SpringBootTest} brings up a full Spring application context
 *       ({@link io.jmix.flowui.FlowuiConfiguration}, EclipseLink, Data, Core, …).</li>
 *   <li>{@link test_support.spec.FlowuiTestSpecification#setup()} initialises a real
 *       Vaadin {@code UI} and {@code VaadinSession} for every test method.</li>
 *   <li>All Spring beans ({@link io.jmix.flowui.UiComponents}, {@code Messages},
 *       {@code Metadata}, …) are the real production implementations — no mocks.</li>
 * </ul>
 *
 * <h2>DataLoader requirement</h2>
 * <p>{@code FilterComponentBuilder} delegates to the framework converter, which requires the
 * owning {@link GenericFilter} to have a {@code DataLoader}. Tests therefore obtain the filter
 * from {@code GenericFilterApiTestView} (bound to an {@code ordersDl} loader) via
 * {@link #filterWithLoader()}. Tests that exercise only configuration registration — which does
 * not build filter components — use a bare {@code GenericFilter} created via
 * {@code uiComponents.create(...)}.
 */
@SpringBootTest
class GenericFilterBuilderApiTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    /**
     * Returns a {@link GenericFilter} bound to a {@code DataLoader} (the {@code ordersDl} loader
     * of {@code GenericFilterApiTestView}). Required for building filter components, since the
     * builder delegates to a converter that needs the loader's entity meta class.
     */
    protected GenericFilter filterWithLoader() {
        return navigateToView(GenericFilterApiTestView).genericFilter
    }

    // FilterComponentBuilder — PropertyFilter

    /**
     * Demonstrates building a {@link PropertyFilter} with {@code filter.filterComponentBuilder()}.
     * The builder assembles a condition model and delegates to the framework converter, so the
     * resulting component is initialised exactly like an XML-loaded one.
     */
    def "PropertyFilterBuilder creates PropertyFilter with correct property and operation"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        when: "Building a PropertyFilter via the builder"
        def numberFilter = filter.filterComponentBuilder()
                .propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.CONTAINS)
                .build() as PropertyFilter

        then: "PropertyFilter has the expected property, operation, and delegated flag"
        numberFilter.property == "number"
        numberFilter.operation == PropertyFilter.Operation.CONTAINS
        numberFilter.conditionModificationDelegated
    }

    def "PropertyFilterBuilder.build() throws IllegalStateException when 'property' is not set"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        filter.filterComponentBuilder()
                .propertyFilter()
                .operation(PropertyFilter.Operation.EQUAL)
                .build()

        then:
        thrown(IllegalStateException)
    }

    def "PropertyFilterBuilder.build() throws IllegalStateException when 'operation' is not set"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        filter.filterComponentBuilder()
                .propertyFilter()
                .property("number")
                .build()

        then:
        thrown(IllegalStateException)
    }

    def "PropertyFilterBuilder.build() generates a parameterName automatically"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        def pf = filter.filterComponentBuilder()
                .propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build() as PropertyFilter

        then: "parameterName is set automatically and derived from the property name"
        pf.parameterName != null
        pf.parameterName.startsWith("number")
    }

    def "PropertyFilterBuilder.defaultValue() sets the initial value"() {
        given: "A GenericFilter with a DataLoader"
        GenericFilter filter = filterWithLoader()

        when:
        PropertyFilter<String> pf = filter.filterComponentBuilder()
                .propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .defaultValue("ORD-001")
                .build()

        then:
        pf.getValue() == "ORD-001"
    }

    def "PropertyFilterBuilder.operationEditable(true) is reflected on the built PropertyFilter"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        PropertyFilter<String> pf = filter.filterComponentBuilder()
                .propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .operationEditable(true)
                .build()

        then:
        pf.isOperationEditable()
    }

    def "PropertyFilterBuilder.operationTextVisible(false) is reflected on the built PropertyFilter"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        PropertyFilter<String> pf = filter.filterComponentBuilder()
                .propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .operationTextVisible(false)
                .build()

        then:
        !pf.isOperationTextVisible()
    }

    def "PropertyFilterBuilder.build() throws when called twice on the same instance"() {
        given:
        GenericFilter filter = filterWithLoader()
        def builder = filter.filterComponentBuilder()
                .propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
        builder.build()

        when:
        builder.build()

        then:
        thrown(IllegalStateException)
    }

    /**
     * The one-shot guard is set only after a successful build, so a build that fails validation
     * does not "burn" the builder — the caller can fix the input and build again.
     */
    def "PropertyFilterBuilder.build() failing validation does not consume the builder"() {
        given:
        GenericFilter filter = filterWithLoader()
        def builder = filter.filterComponentBuilder()
                .propertyFilter()
                .operation(PropertyFilter.Operation.EQUAL)   // 'property' not set yet

        when: "first build fails validation"
        builder.build()

        then:
        thrown(IllegalStateException)

        when: "the missing property is set and build is retried on the same instance"
        PropertyFilter<String> pf = builder.property("number").build()

        then: "it builds successfully"
        pf.property == "number"
    }

    // FilterComponentBuilder — JpqlFilter

    /**
     * A <em>void</em> {@link JpqlFilter} has no query parameter — it is rendered as a checkbox
     * that toggles a fixed condition on and off. Use {@code filter.filterComponentBuilder().jpqlFilter()}
     * (no class argument) to obtain this variant; its value type is {@link Boolean}.
     */
    def "JpqlFilterBuilder creates a void JpqlFilter rendered as a checkbox"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        when: "Building a void JpqlFilter (no query parameter)"
        JpqlFilter<Boolean> activeFilter = filter.filterComponentBuilder()
                .jpqlFilter()
                .where("{E}.status = 'ACTIVE'")
                .label("Active only")
                .build()

        then: "parameterClass is Void, the where clause is stored, and a checkbox value component is generated"
        activeFilter.parameterClass == Void.class
        activeFilter.where == "{E}.status = 'ACTIVE'"
        activeFilter.conditionModificationDelegated
        activeFilter.dataLoader == filter.dataLoader
        activeFilter.valueComponent != null
    }

    /**
     * A void {@link JpqlFilter} applies its WHERE clause only while it is active (checked).
     * {@code defaultValue(true)} makes it active by default.
     */
    def "void JpqlFilter applies its WHERE clause to the query condition when active"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        when: "Building an active-by-default void JpqlFilter"
        JpqlFilter<Boolean> activeFilter = filter.filterComponentBuilder()
                .jpqlFilter()
                .where("{E}.number = 'ACTIVE'")
                .defaultValue(true)
                .build()

        then: "the value is true and the WHERE clause is applied to the query condition"
        activeFilter.value == Boolean.TRUE
        activeFilter.queryCondition.where == "{E}.number = 'ACTIVE'"
    }

    /**
     * Without {@code defaultValue(true)} a void {@link JpqlFilter} starts inactive (unchecked),
     * so its WHERE clause is not applied.
     */
    def "void JpqlFilter does not apply its WHERE clause when inactive"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        when: "Building a void JpqlFilter without a default value"
        JpqlFilter<Boolean> activeFilter = filter.filterComponentBuilder()
                .jpqlFilter()
                .where("{E}.number = 'ACTIVE'")
                .build()

        then: "the WHERE clause is not applied"
        activeFilter.value != Boolean.TRUE
        !activeFilter.queryCondition.where
    }

    /**
     * A <em>typed</em> {@link JpqlFilter} takes a query parameter whose type is specified via
     * {@code jpqlFilter(Class)}; the user-supplied value is bound to the parameter.
     */
    def "JpqlFilterBuilder creates a typed JpqlFilter with a named query parameter"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        when: "Building a typed JpqlFilter with a String parameter"
        JpqlFilter<String> codeFilter = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("code")
                .where("{E}.code = ?")
                .build()

        then: "JpqlFilter has the correct parameter class and name"
        codeFilter.parameterClass == String.class
        codeFilter.parameterName == "code"
        codeFilter.conditionModificationDelegated
    }

    def "JpqlFilterBuilder.build() throws IllegalStateException when 'where' is not set"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        filter.filterComponentBuilder()
                .jpqlFilter()
                .build()

        then:
        thrown(IllegalStateException)
    }

    def "JpqlFilterBuilder.build() generates a valueComponent"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        when: "Building a typed JpqlFilter"
        JpqlFilter<String> jf = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("number")
                .where("{E}.number = ?")
                .build()

        then: "valueComponent is non-null, mirroring what the XML loader produces"
        jf.valueComponent != null
    }

    def "JpqlFilterBuilder.hasInExpression(true) is reflected on the built JpqlFilter"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        JpqlFilter<String> jf = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("tags")
                .where("{E}.tags in ?")
                .hasInExpression(true)
                .build()

        then:
        jf.hasInExpression
    }

    /**
     * A {@code parameterName} is optional: when omitted it is generated automatically, matching
     * the behaviour of the XML loader. Set it explicitly only for a named bind parameter.
     */
    def "JpqlFilterBuilder typed without parameterName generates one automatically"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        JpqlFilter<String> jf = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .where("{E}.number = ?")
                .build()

        then:
        jf.parameterName != null
        !jf.parameterName.isEmpty()
    }

    def "JpqlFilterBuilder.build() throws when called twice on the same instance"() {
        given:
        GenericFilter filter = filterWithLoader()
        def builder = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("code")
                .where("{E}.code = ?")
        builder.build()

        when:
        builder.build()

        then:
        thrown(IllegalStateException)
    }

    // FilterComponentBuilder — GroupFilter

    /**
     * {@link GroupFilter} bundles several conditions under a single logical operator
     * (AND or OR).  Use {@code filter.filterComponentBuilder().groupFilter()} to create one.
     */
    def "GroupFilterBuilder creates a GroupFilter with specified operation and child components"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()
        def builder = filter.filterComponentBuilder()

        and: "Two void JpqlFilter conditions"
        def activeFilter = builder.jpqlFilter().where("{E}.status = 'ACTIVE'").build()
        def verifiedFilter = filter.filterComponentBuilder().jpqlFilter().where("{E}.verified = true").build()

        when: "Building an OR group that contains both conditions"
        GroupFilter group = filter.filterComponentBuilder().groupFilter()
                .operation(LogicalFilterComponent.Operation.OR)
                .add(activeFilter)
                .add(verifiedFilter)
                .build()

        then: "GroupFilter has the expected operation and children"
        group.operation == LogicalFilterComponent.Operation.OR
        group.filterComponents.size() == 2
        group.filterComponents.contains(activeFilter)
        group.filterComponents.contains(verifiedFilter)
        group.conditionModificationDelegated
    }

    def "GroupFilterBuilder.addAll() adds multiple components at once"() {
        given: "A GenericFilter bound to a DataLoader and two conditions"
        GenericFilter filter = filterWithLoader()
        def f1 = filter.filterComponentBuilder().jpqlFilter().where("{E}.status = 'A'").build()
        def f2 = filter.filterComponentBuilder().jpqlFilter().where("{E}.status = 'B'").build()

        when: "Adding both via the addAll vararg"
        GroupFilter group = filter.filterComponentBuilder().groupFilter()
                .addAll(f1, f2)
                .build()

        then:
        group.filterComponents.size() == 2
        group.filterComponents.contains(f1)
        group.filterComponents.contains(f2)
    }

    def "GroupFilterBuilder defaults to AND when no operation is specified"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        GroupFilter group = filter.filterComponentBuilder()
                .groupFilter()
                .build()

        then:
        group.operation == LogicalFilterComponent.Operation.AND
    }

    def "GroupFilterBuilder copies autoApply from the owning filter"() {
        given:
        GenericFilter filter = filterWithLoader()
        filter.setAutoApply(false)

        when:
        GroupFilter group = filter.filterComponentBuilder()
                .groupFilter()
                .build()

        then:
        !group.isAutoApply()
    }

    def "GroupFilterBuilder propagates the DataLoader to the built GroupFilter"() {
        given: "A GenericFilter with a DataLoader"
        GenericFilter filter = filterWithLoader()

        when:
        GroupFilter group = filter.filterComponentBuilder()
                .groupFilter()
                .build()

        then:
        group.dataLoader != null
        group.dataLoader == filter.dataLoader
    }

    def "GroupFilterBuilder.build() throws when called twice on the same instance"() {
        given:
        GenericFilter filter = filterWithLoader()
        def builder = filter.filterComponentBuilder().groupFilter()
        builder.build()

        when:
        builder.build()

        then:
        thrown(IllegalStateException)
    }

    // FilterComponentBuilder — DataLoader requirement

    /**
     * The builder delegates to the framework converter, which needs the owning filter's
     * {@code DataLoader}. Building a component without one fails fast.
     */
    def "FilterComponentBuilder requires the owning filter to have a DataLoader"() {
        given: "A GenericFilter created without a DataLoader"
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        filter.filterComponentBuilder()
                .propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .build()

        then:
        thrown(IllegalStateException)
    }

    // FilterConfigurationBuilder

    /**
     * {@link io.jmix.flowui.component.genericfilter.FilterConfigurationBuilder} creates a
     * {@link DesignTimeConfiguration}: the configuration belongs to the code that registers it,
     * as one declared in XML does.
     * <p>
     * Obtain one with {@code filter.filterConfigurationBuilder()}.
     */
    def "FilterConfigurationBuilder creates and registers a DesignTimeConfiguration"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        and: "A void JpqlFilter condition"
        def activeFilter = filter.filterComponentBuilder()
                .jpqlFilter()
                .where("{E}.status = 'ACTIVE'")
                .build()

        when: "Creating a configuration via the builder"
        DesignTimeConfiguration config = filter.filterConfigurationBuilder()
                .id("predefined")
                .name("Predefined Search")
                .add(activeFilter)
                .buildAndRegister()

        then: "The configuration is registered and contains the added condition"
        filter.getConfiguration("predefined").is(config)
        config.name == "Predefined Search"
        config.rootLogicalFilterComponent.filterComponents.contains(activeFilter)
    }

    def "FilterConfigurationBuilder.addAll() adds multiple components at once"() {
        given: "A GenericFilter bound to a DataLoader and two conditions"
        GenericFilter filter = filterWithLoader()
        def f1 = filter.filterComponentBuilder().jpqlFilter().where("{E}.status = 'A'").build()
        def f2 = filter.filterComponentBuilder().jpqlFilter().where("{E}.status = 'B'").build()

        when: "Adding both via the addAll vararg"
        DesignTimeConfiguration config = filter.filterConfigurationBuilder()
                .id("multi")
                .addAll(f1, f2)
                .buildAndRegister()

        then:
        config.rootLogicalFilterComponent.filterComponents.contains(f1)
        config.rootLogicalFilterComponent.filterComponents.contains(f2)
    }

    def "FilterConfigurationBuilder.makeCurrent() activates the configuration immediately"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        when: "Creating a configuration and making it current"
        DesignTimeConfiguration config = filter.filterConfigurationBuilder()
                .id("active")
                .makeCurrent()
                .buildAndRegister()

        then: "The configuration is the filter's current configuration"
        filter.currentConfiguration == config
    }

    def "FilterConfigurationBuilder respects the specified logical operation"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        DesignTimeConfiguration config = filter.filterConfigurationBuilder()
                .id("orConfiguration")
                .operation(LogicalFilterComponent.Operation.OR)
                .buildAndRegister()

        then:
        config.rootLogicalFilterComponent.operation == LogicalFilterComponent.Operation.OR
    }

    def "FilterConfigurationBuilder.buildAndRegister() throws when 'id' is not set"() {
        given:
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        filter.filterConfigurationBuilder()
                .buildAndRegister()

        then:
        thrown(IllegalStateException)
    }

    def "FilterConfigurationBuilder.buildAndRegister() throws when id is already registered"() {
        given: "A filter that already has a configuration with id 'dup'"
        GenericFilter filter = filterWithLoader()
        filter.filterConfigurationBuilder()
                .id("dup")
                .buildAndRegister()

        when: "Registering another configuration with the same id"
        filter.filterConfigurationBuilder()
                .id("dup")
                .buildAndRegister()

        then:
        thrown(IllegalStateException)
    }

    def "FilterConfigurationBuilder.buildAndRegister() throws when id is the reserved empty-configuration id"() {
        given: "A DataLoader-bound filter"
        GenericFilter filter = filterWithLoader()

        when: "Registering a configuration whose id equals the reserved empty-configuration id"
        filter.filterConfigurationBuilder()
                .id(filter.getEmptyConfiguration().getId())
                .name("With reserved id")
                .buildAndRegister()

        then:
        thrown(IllegalStateException)
    }

    def "FilterConfigurationBuilder.buildAndRegister() throws when called twice on the same instance"() {
        given: "A DataLoader-bound filter so the first build succeeds and sets the one-shot flag"
        GenericFilter filter = filterWithLoader()
        def builder = filter.filterConfigurationBuilder().id("once")
        builder.buildAndRegister()

        when:
        builder.buildAndRegister()

        then:
        thrown(IllegalStateException)
    }

    def "FilterConfigurationBuilder.buildAndRegister() throws when the filter has no DataLoader"() {
        given: "a GenericFilter without a DataLoader, with an id set so the DataLoader check is reached"
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        filter.filterConfigurationBuilder()
                .id("noLoader")
                .buildAndRegister()

        then:
        thrown(IllegalStateException)
    }

    /**
     * The builder records the default value in the configuration, so the value is restored when
     * the configuration is selected again or its values are cleared.
     */
    def "FilterConfigurationBuilder stores the default value of a condition"() {
        given: "A GenericFilter and a typed JpqlFilter"
        GenericFilter filter = filterWithLoader()
        JpqlFilter<String> codeFilter = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("code")
                .where("{E}.code = ?")
                .build()

        when: "Registering the filter component with an explicit default value"
        DesignTimeConfiguration config = filter.filterConfigurationBuilder()
                .id("byCode")
                .add(codeFilter, "DEFAULT")
                .buildAndRegister()

        then: "The default value is retrievable from the configuration"
        config.getFilterComponentDefaultValue("code") == "DEFAULT"
    }

    def "FilterConfigurationBuilder stores the default value of a condition nested in a group"() {
        given: "A GenericFilter and a group with a typed JpqlFilter that has a value"
        GenericFilter filter = filterWithLoader()
        JpqlFilter<String> codeFilter = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("code")
                .where("{E}.code = ?")
                .build()
        codeFilter.setValue("NESTED")
        GroupFilter group = filter.filterComponentBuilder()
                .groupFilter()
                .add(codeFilter)
                .build()

        when: "Registering the group"
        DesignTimeConfiguration config = filter.filterConfigurationBuilder()
                .id("withNestedValue")
                .add(group)
                .buildAndRegister()

        then: "The value of the nested condition is its default value"
        config.getFilterComponentDefaultValue("code") == "NESTED"
    }

    def "FilterConfigurationBuilder.add(fc, value) calls setValue on the component"() {
        given: "A GenericFilter with a DataLoader so the JpqlFilter gets a value component"
        GenericFilter filter = filterWithLoader()

        and: "A typed JpqlFilter built with a value component"
        JpqlFilter<String> jf = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("number")
                .where("{E}.number = ?")
                .build()

        when: "Adding the filter with an explicit default value"
        filter.filterConfigurationBuilder()
                .id("byNumber")
                .add(jf, "ORD-999")
                .buildAndRegister()

        then: "setValue was called on the component"
        jf.getValue() == "ORD-999"
    }

    def "FilterConfigurationBuilder.add() accepts a non-single filter component (GroupFilter)"() {
        given: "a GenericFilter with a DataLoader and a GroupFilter condition"
        GenericFilter filter = filterWithLoader()
        GroupFilter group = filter.filterComponentBuilder()
                .groupFilter()
                .add(filter.filterComponentBuilder().jpqlFilter().where("{E}.number = '1'").build())
                .build()

        when: "adding the GroupFilter (not a SingleFilterComponentBase) to a configuration"
        DesignTimeConfiguration config = filter.filterConfigurationBuilder()
                .id("withGroup")
                .add(group)
                .buildAndRegister()

        then: "the GroupFilter is part of the configuration"
        config.rootLogicalFilterComponent.filterComponents.contains(group)
    }

    def "FilterConfigurationBuilder copies autoApply from the filter"() {
        given: "A GenericFilter with autoApply explicitly set to false"
        GenericFilter filter = filterWithLoader()
        filter.setAutoApply(false)

        and: "A condition that will be added to the configuration"
        def fc = filter.filterComponentBuilder()
                .jpqlFilter()
                .where("{E}.active = true")
                .build()

        when: "Building a configuration"
        DesignTimeConfiguration config = filter.filterConfigurationBuilder()
                .id("noAutoApply")
                .add(fc)
                .buildAndRegister()

        then: "Root GroupFilter inherits autoApply=false from the filter"
        !config.rootLogicalFilterComponent.isAutoApply()
    }

    // GenericFilter helper methods

    /**
     * {@link GenericFilter#refreshCurrentConfiguration} forces the filter UI to
     * re-render the current configuration's conditions.  It is a public shorthand
     * for the otherwise protected {@code refreshCurrentConfigurationLayout()}.
     */
    def "refreshCurrentConfiguration does not throw for a filter without conditions"() {
        given:
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        filter.refreshCurrentConfiguration()

        then:
        noExceptionThrown()
    }

    def "PropertyFilterBuilder.label() sets the label on the built PropertyFilter"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        PropertyFilter<String> pf = filter.filterComponentBuilder()
                .<String> propertyFilter()
                .property("number")
                .operation(PropertyFilter.Operation.EQUAL)
                .label("Order number")
                .build()

        then:
        pf.label == "Order number"
    }

    def "JpqlFilterBuilder.join() sets the JOIN clause on the built JpqlFilter"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        JpqlFilter<String> jf = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("tag")
                .where("t.name = ?")
                .join("join {E}.tags t")
                .build()

        then:
        jf.queryCondition.join == "join {E}.tags t"
    }
}
