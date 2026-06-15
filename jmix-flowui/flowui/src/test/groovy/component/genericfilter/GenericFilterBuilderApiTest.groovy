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
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration
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

    // RunTimeConfigurationBuilder

    /**
     * {@link io.jmix.flowui.component.genericfilter.RunTimeConfigurationBuilder}
     * creates a {@link RunTimeConfiguration} — a dynamic configuration whose
     * conditions can be added or removed by the user at runtime.
     * <p>
     * Obtain one with {@code filter.runtimeConfigurationBuilder()}.
     */
    def "RunTimeConfigurationBuilder creates and registers a RunTimeConfiguration"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        and: "A void JpqlFilter condition"
        def activeFilter = filter.filterComponentBuilder()
                .jpqlFilter()
                .where("{E}.status = 'ACTIVE'")
                .build()

        when: "Creating a RunTimeConfiguration via the builder"
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("dynamic")
                .name("Dynamic Search")
                .add(activeFilter)
                .buildAndRegister()

        then: "Configuration is registered and contains the added condition"
        filter.configurations.any { it.id == "dynamic" }
        config instanceof RunTimeConfiguration
        config.id == "dynamic"
        config.name == "Dynamic Search"
        config.rootLogicalFilterComponent.filterComponents.contains(activeFilter)
    }

    def "RunTimeConfigurationBuilder.addAll() adds multiple components at once"() {
        given: "A GenericFilter bound to a DataLoader and two conditions"
        GenericFilter filter = filterWithLoader()
        def f1 = filter.filterComponentBuilder().jpqlFilter().where("{E}.status = 'A'").build()
        def f2 = filter.filterComponentBuilder().jpqlFilter().where("{E}.status = 'B'").build()

        when: "Adding both via the addAll vararg"
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("multi")
                .addAll(f1, f2)
                .buildAndRegister()

        then:
        config.rootLogicalFilterComponent.filterComponents.contains(f1)
        config.rootLogicalFilterComponent.filterComponents.contains(f2)
    }

    def "RunTimeConfigurationBuilder.makeCurrent() activates the configuration immediately"() {
        given: "A GenericFilter bound to a DataLoader"
        GenericFilter filter = filterWithLoader()

        when: "Creating a RunTimeConfiguration and making it current"
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("active")
                .makeCurrent()
                .buildAndRegister()

        then: "The configuration is the filter's current configuration"
        filter.currentConfiguration == config
    }

    def "RunTimeConfigurationBuilder respects the specified logical operation"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("orRuntime")
                .operation(LogicalFilterComponent.Operation.OR)
                .buildAndRegister()

        then:
        config.rootLogicalFilterComponent.operation == LogicalFilterComponent.Operation.OR
    }

    def "RunTimeConfigurationBuilder.buildAndRegister() throws when 'id' is not set"() {
        given:
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        filter.runtimeConfigurationBuilder()
                .buildAndRegister()

        then:
        thrown(IllegalStateException)
    }

    def "RunTimeConfigurationBuilder.buildAndRegister() throws when id is already registered"() {
        given: "A filter that already has a configuration with id 'dup'"
        GenericFilter filter = filterWithLoader()
        filter.runtimeConfigurationBuilder()
                .id("dup")
                .buildAndRegister()

        when: "Registering another configuration with the same id"
        filter.runtimeConfigurationBuilder()
                .id("dup")
                .buildAndRegister()

        then:
        thrown(IllegalStateException)
    }

    /**
     * {@link RunTimeConfiguration} stores the default value in the configuration map so the
     * value can be restored on reset.
     */
    def "RunTimeConfigurationBuilder stores the default value for reset support"() {
        given: "A GenericFilter and a typed JpqlFilter"
        GenericFilter filter = filterWithLoader()
        JpqlFilter<String> codeFilter = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("code")
                .where("{E}.code = ?")
                .build()

        when: "Registering the filter component with an explicit default value"
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("byCode")
                .add(codeFilter, "DEFAULT")
                .buildAndRegister()

        then: "The default value is retrievable from the configuration"
        config.getFilterComponentDefaultValue("code") == "DEFAULT"
    }

    def "RunTimeConfigurationBuilder.add(fc, value) calls setValue on the component"() {
        given: "A GenericFilter with a DataLoader so the JpqlFilter gets a value component"
        GenericFilter filter = filterWithLoader()

        and: "A typed JpqlFilter built with a value component"
        JpqlFilter<String> jf = filter.filterComponentBuilder()
                .jpqlFilter(String)
                .parameterName("number")
                .where("{E}.number = ?")
                .build()

        when: "Adding the filter with an explicit default value"
        filter.runtimeConfigurationBuilder()
                .id("byNumber")
                .add(jf, "ORD-999")
                .buildAndRegister()

        then: "setValue was called on the component"
        jf.getValue() == "ORD-999"
    }

    // RunTimeConfiguration — modified state

    /**
     * Components added through the builder are marked as modified by the builder
     * itself (via {@code config.setModified(true)}) so the per-condition remove
     * button is visible immediately — no extra boilerplate needed.
     */
    def "Components added via RunTimeConfigurationBuilder are marked as modified"() {
        given: "A void JpqlFilter condition"
        GenericFilter filter = filterWithLoader()
        def fc = filter.filterComponentBuilder()
                .jpqlFilter()
                .where("{E}.active = true")
                .build()

        when: "Creating a RunTimeConfiguration with the condition via the builder"
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("with-condition")
                .add(fc)
                .buildAndRegister()

        then: "The condition is marked as modified (remove button visible)"
        config.isFilterComponentModified(fc)
        config.isModified()
    }

    def "RunTimeConfigurationBuilder copies autoApply from the filter"() {
        given: "A GenericFilter with autoApply explicitly set to false"
        GenericFilter filter = filterWithLoader()
        filter.setAutoApply(false)

        and: "A condition that will be added to the configuration"
        def fc = filter.filterComponentBuilder()
                .jpqlFilter()
                .where("{E}.active = true")
                .build()

        when: "Building a RunTimeConfiguration"
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("noAutoApply")
                .add(fc)
                .buildAndRegister()

        then: "Root GroupFilter inherits autoApply=false from the filter"
        !config.rootLogicalFilterComponent.isAutoApply()

        and: "Child filter component also has autoApply=false (GroupFilter.add() propagates it)"
        !fc.isAutoApply()
    }

    def "Empty RunTimeConfiguration built without components is not modified"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("empty")
                .buildAndRegister()

        then:
        !config.isModified()
    }

    // RunTimeConfiguration — protection from user deletion

    def "RunTimeConfigurationBuilder creates configuration protected from user deletion by default"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("protected")
                .buildAndRegister()

        then:
        config.isProtectedFromUserDeletion()
    }

    def "RunTimeConfigurationBuilder.allowDeletion() disables user-deletion protection"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("deletable")
                .allowDeletion()
                .buildAndRegister()

        then:
        !config.isProtectedFromUserDeletion()
    }

    def "RunTimeConfigurationBuilder.allowDeletion(true) disables protection"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("deletable2")
                .allowDeletion(true)
                .buildAndRegister()

        then:
        !config.isProtectedFromUserDeletion()
    }

    def "RunTimeConfigurationBuilder.allowDeletion(false) keeps protection enabled"() {
        given:
        GenericFilter filter = filterWithLoader()

        when:
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("protected2")
                .allowDeletion(false)
                .buildAndRegister()

        then:
        config.isProtectedFromUserDeletion()
    }

    /**
     * {@code protectedFromUserDeletion} only hides the Remove action in the UI
     * ({@code GenericFilterRemoveAction#isApplicable}). Programmatic removal via
     * {@code removeConfiguration()} must still work — it is used internally,
     * e.g. by the configuration renaming flow.
     */
    def "removeConfiguration() removes a configuration protected from user deletion"() {
        given:
        GenericFilter filter = filterWithLoader()
        filter.runtimeConfigurationBuilder()
                .id("protected")
                .buildAndRegister()

        when:
        filter.removeConfiguration(filter.getConfigurations().find { it.id == "protected" })

        then:
        !filter.getConfigurations().any { it.id == "protected" }
    }

    def "removeConfiguration() removes a configuration created with allowDeletion()"() {
        given:
        GenericFilter filter = filterWithLoader()
        filter.runtimeConfigurationBuilder()
                .id("deletable")
                .allowDeletion()
                .buildAndRegister()

        when:
        filter.removeConfiguration(filter.getConfigurations().find { it.id == "deletable" })

        then:
        !filter.getConfigurations().any { it.id == "deletable" }
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
}
