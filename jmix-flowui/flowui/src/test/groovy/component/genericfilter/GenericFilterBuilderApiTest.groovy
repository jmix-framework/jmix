/*
 * Copyright 2024 Haulmont.
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

import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.genericfilter.configuration.DesignTimeConfiguration
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
 * <p>Isolated unit tests for the builder classes are not practical: creating a
 * {@link GenericFilter} via {@code uiComponents.create()} triggers
 * {@code afterPropertiesSet()} which depends on several Spring beans being present.
 * Mocking that dependency graph would require more effort than the tests themselves
 * and would test the mock rather than the real behaviour.
 *
 * <h2>What is not covered here</h2>
 * <p>All tests use a {@link GenericFilter} created <em>without</em> a DataLoader.
 * This is sufficient to verify builder wiring, configuration registration, and the
 * modified-state auto-tracking introduced in {@link RunTimeConfiguration}.
 * Scenarios that require a DataLoader (e.g. full {@link PropertyFilter} value
 * binding, query execution) are covered by the XML-load integration tests in
 * {@code GenericFilterXmlLoadTest}.
 */
@SpringBootTest
class GenericFilterBuilderApiTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    // =========================================================================
    // FilterComponentBuilder — PropertyFilter
    // =========================================================================

    /**
     * Demonstrates building a {@link PropertyFilter} with {@code filter.componentBuilder()}.
     * The builder takes care of the mandatory initialisation order that the XML loader
     * performs automatically:
     * <ol>
     *   <li>{@code setConditionModificationDelegated(true)}</li>
     *   <li>{@code setDataLoader(…)} (skipped when the filter has no loader)</li>
     *   <li>{@code setProperty(…)}</li>
     *   <li>{@code setOperation(…)}</li>
     * </ol>
     */
    def "PropertyFilterBuilder creates PropertyFilter with correct property and operation"() {
        given: "A GenericFilter (DataLoader not required for this assertion)"
        GenericFilter filter = uiComponents.create(GenericFilter)

        when: "Building a PropertyFilter via the builder"
        def nameFilter = filter.componentBuilder()
                .propertyFilter()
                .property("name")
                .operation(PropertyFilter.Operation.CONTAINS)
                .build() as PropertyFilter

        then: "PropertyFilter has the expected property, operation, and delegated flag"
        nameFilter.property == "name"
        nameFilter.operation == PropertyFilter.Operation.CONTAINS
        nameFilter.conditionModificationDelegated
    }

    def "PropertyFilterBuilder.build() throws IllegalStateException when 'property' is not set"() {
        given:
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        filter.componentBuilder()
                .propertyFilter()
                .operation(PropertyFilter.Operation.EQUAL)
                .build()

        then:
        thrown(IllegalStateException)
    }

    def "PropertyFilterBuilder.build() throws IllegalStateException when 'operation' is not set"() {
        given:
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        filter.componentBuilder()
                .propertyFilter()
                .property("name")
                .build()

        then:
        thrown(IllegalStateException)
    }

    // =========================================================================
    // FilterComponentBuilder — JpqlFilter
    // =========================================================================

    /**
     * A <em>void</em> {@link JpqlFilter} has no query parameter — it is rendered
     * as a checkbox.  Use {@code filter.componentBuilder().jpqlFilter()} (no class
     * argument) to obtain this variant.
     */
    def "JpqlFilterBuilder creates a void JpqlFilter rendered as a checkbox"() {
        given: "A GenericFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)

        when: "Building a void JpqlFilter (no query parameter)"
        JpqlFilter<Boolean> activeFilter = filter.componentBuilder()
                .jpqlFilter()
                .where("{E}.status = 'ACTIVE'")
                .label("Active only")
                .build()

        then: "parameterClass is Void and the where clause is stored"
        activeFilter.parameterClass == Void.class
        activeFilter.where == "{E}.status = 'ACTIVE'"
        activeFilter.conditionModificationDelegated
        activeFilter.dataLoader == null
    }

    /**
     * A <em>typed</em> {@link JpqlFilter} takes a query parameter whose type is
     * specified via {@code jpqlFilter(Class)}.  A parameter name must also be given
     * so the generated JPQL condition can use a named bind parameter
     * ({@code :paramName}).
     */
    def "JpqlFilterBuilder creates a typed JpqlFilter with a named query parameter"() {
        given: "A GenericFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)

        when: "Building a typed JpqlFilter with a String parameter"
        JpqlFilter<String> codeFilter = filter.componentBuilder()
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
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        filter.componentBuilder()
                .jpqlFilter()
                .build()

        then:
        thrown(IllegalStateException)
    }

    // =========================================================================
    // FilterComponentBuilder — GroupFilter
    // =========================================================================

    /**
     * {@link GroupFilter} bundles several conditions under a single logical operator
     * (AND or OR).  Use {@code filter.componentBuilder().groupFilter()} to create one.
     */
    def "GroupFilterBuilder creates a GroupFilter with specified operation and child components"() {
        given: "A GenericFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)
        def builder = filter.componentBuilder()

        and: "Two void JpqlFilter conditions"
        def activeFilter = builder.jpqlFilter().where("{E}.status = 'ACTIVE'").build()
        def verifiedFilter = builder.jpqlFilter().where("{E}.verified = true").build()

        when: "Building an OR group that contains both conditions"
        GroupFilter group = builder.groupFilter()
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

    def "GroupFilterBuilder defaults to AND when no operation is specified"() {
        given:
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        GroupFilter group = filter.componentBuilder()
                .groupFilter()
                .build()

        then:
        group.operation == LogicalFilterComponent.Operation.AND
    }

    // =========================================================================
    // DesignTimeConfigurationBuilder
    // =========================================================================

    /**
     * {@link io.jmix.flowui.component.genericfilter.DesignTimeConfigurationBuilder}
     * creates a {@link DesignTimeConfiguration} — a fixed, developer-defined
     * configuration that is not user-editable at runtime.
     * <p>
     * Obtain one with {@code filter.configurationBuilder()}.
     */
    def "DesignTimeConfigurationBuilder registers a configuration with the filter"() {
        given: "A GenericFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)

        and: "A void JpqlFilter condition"
        JpqlFilter<Boolean> activeFilter = filter.componentBuilder()
                .jpqlFilter()
                .where("{E}.status = 'ACTIVE'")
                .build()

        when: "Creating and registering a DesignTimeConfiguration"
        DesignTimeConfiguration config = filter.configurationBuilder()
                .id("byStatus")
                .name("By Status")
                .add(activeFilter)
                .buildAndRegister()

        then: "Configuration is registered and contains the added condition"
        filter.configurations.any { it.id == "byStatus" }
        config.id == "byStatus"
        config.name == "By Status"
        config.rootLogicalFilterComponent.filterComponents.contains(activeFilter)
    }

    /**
     * Calling {@code .asDefault()} makes the newly created configuration the
     * current (active) configuration of the filter immediately after
     * {@code buildAndRegister()}.
     */
    def "DesignTimeConfigurationBuilder.asDefault() activates the configuration"() {
        given: "A GenericFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)

        and: "A JpqlFilter condition"
        def activeFilter = filter.componentBuilder()
                .jpqlFilter()
                .where("{E}.status = 'ACTIVE'")
                .build()

        when: "Creating a configuration and marking it as default"
        DesignTimeConfiguration config = filter.configurationBuilder()
                .id("main")
                .add(activeFilter)
                .asDefault()
                .buildAndRegister()

        then: "The configuration becomes the filter's current configuration"
        filter.currentConfiguration == config
    }

    /**
     * When a filter component is added with an explicit default value
     * ({@code .add(component, value)}), that value is stored in the configuration
     * so it can be restored when the user resets filters.
     * <p>
     * Note: setting the value on the component itself is best-effort — it may be
     * skipped when the filter has no DataLoader and the value UI component has not
     * yet been initialised.  The stored configuration default is always reliable.
     */
    def "DesignTimeConfigurationBuilder stores the default value for reset support"() {
        given: "A GenericFilter and a typed JpqlFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)
        JpqlFilter<String> codeFilter = filter.componentBuilder()
                .jpqlFilter(String)
                .parameterName("code")
                .where("{E}.code = ?")
                .build()

        when: "Registering the filter component with an explicit default value"
        DesignTimeConfiguration config = filter.configurationBuilder()
                .id("byCode")
                .add(codeFilter, "DEFAULT")
                .buildAndRegister()

        then: "The default value is retrievable from the configuration"
        config.getFilterComponentDefaultValue("code") == "DEFAULT"
    }

    def "DesignTimeConfigurationBuilder.buildAndRegister() throws when 'id' is not set"() {
        given:
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        filter.configurationBuilder()
                .name("no id")
                .buildAndRegister()

        then:
        thrown(IllegalStateException)
    }

    def "DesignTimeConfigurationBuilder respects the specified logical operation"() {
        given:
        GenericFilter filter = uiComponents.create(GenericFilter)

        when:
        DesignTimeConfiguration config = filter.configurationBuilder()
                .id("orConfig")
                .operation(LogicalFilterComponent.Operation.OR)
                .buildAndRegister()

        then:
        config.rootLogicalFilterComponent.operation == LogicalFilterComponent.Operation.OR
    }

    // =========================================================================
    // RunTimeConfigurationBuilder
    // =========================================================================

    /**
     * {@link io.jmix.flowui.component.genericfilter.RunTimeConfigurationBuilder}
     * creates a {@link RunTimeConfiguration} — a dynamic configuration whose
     * conditions can be added or removed by the user at runtime.
     * <p>
     * Obtain one with {@code filter.runtimeConfigurationBuilder()}.
     */
    def "RunTimeConfigurationBuilder creates and registers a RunTimeConfiguration"() {
        given: "A GenericFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)

        and: "A void JpqlFilter condition"
        def activeFilter = filter.componentBuilder()
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

    def "RunTimeConfigurationBuilder.asDefault() activates the configuration immediately"() {
        given: "A GenericFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)

        when: "Creating a RunTimeConfiguration and marking it as default"
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("active")
                .asDefault()
                .buildAndRegister()

        then: "The configuration is the filter's current configuration"
        filter.currentConfiguration == config
    }

    def "RunTimeConfigurationBuilder respects the specified logical operation"() {
        given:
        GenericFilter filter = uiComponents.create(GenericFilter)

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

    /**
     * {@link RunTimeConfiguration} stores the default value in the configuration map
     * even when the component's own {@code setValue} cannot be called
     * (best-effort semantics).
     */
    def "RunTimeConfigurationBuilder stores the default value for reset support"() {
        given: "A GenericFilter and a typed JpqlFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)
        JpqlFilter<String> codeFilter = filter.componentBuilder()
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

    // =========================================================================
    // RunTimeConfiguration — auto-tracking of modified state
    // =========================================================================

    /**
     * One of the key behavioural improvements in {@link RunTimeConfiguration}:
     * any component added to the root {@link LogicalFilterComponent} <em>after</em>
     * the configuration is constructed is automatically marked as modified, making
     * the per-condition remove button visible without extra boilerplate.
     */
    def "Adding a component to RunTimeConfiguration root auto-marks it as modified"() {
        given: "A RunTimeConfiguration with no pre-existing conditions"
        GenericFilter filter = uiComponents.create(GenericFilter)
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("tracker")
                .buildAndRegister()

        and: "A void JpqlFilter condition"
        def fc = filter.componentBuilder()
                .jpqlFilter()
                .where("{E}.active = true")
                .build()

        when: "The condition is added to the root after construction"
        config.rootLogicalFilterComponent.add(fc)

        then: "The condition is automatically marked as modified"
        config.isFilterComponentModified(fc)
        config.isModified()
    }

    /**
     * Removal is tracked symmetrically: when a component is removed from the root
     * the modified flag is cleared automatically.
     * <p>
     * A nested {@link GroupFilter} is used here because removing a
     * {@link io.jmix.flowui.component.filter.SingleFilterComponent} without a DataLoader
     * would throw (GroupFilter.remove calls DataLoader.removeParameter for single-filter
     * children).  Using a nested group is a perfectly valid real-world use case.
     */
    def "Removing a component from RunTimeConfiguration root auto-clears the modified flag"() {
        given: "A RunTimeConfiguration with one pre-added nested GroupFilter (via builder)"
        GenericFilter filter = uiComponents.create(GenericFilter)
        // A nested GroupFilter is not a SingleFilterComponent → remove works without a DataLoader
        def fc = filter.componentBuilder()
                .groupFilter()
                .build()
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("tracker")
                .add(fc)
                .buildAndRegister()

        expect: "Nested group is marked as modified after being added via the builder"
        config.isFilterComponentModified(fc)

        when: "The nested group is removed from the root"
        config.rootLogicalFilterComponent.remove(fc)

        then: "The modified flag is cleared automatically"
        !config.isFilterComponentModified(fc)
        !config.isModified()
    }

    /**
     * Components added through the builder (not manually to the root) are also
     * automatically marked as modified, because the builder adds them to the root
     * which triggers the internal change listener.
     */
    def "Components added via RunTimeConfigurationBuilder are auto-marked as modified"() {
        given: "A void JpqlFilter condition"
        GenericFilter filter = uiComponents.create(GenericFilter)
        def fc = filter.componentBuilder()
                .jpqlFilter()
                .where("{E}.active = true")
                .build()

        when: "Creating a RunTimeConfiguration with the condition via the builder"
        RunTimeConfiguration config = filter.runtimeConfigurationBuilder()
                .id("with-condition")
                .add(fc)
                .buildAndRegister()

        then: "The condition is automatically marked as modified (remove button visible)"
        config.isFilterComponentModified(fc)
        config.isModified()
    }

    // =========================================================================
    // GenericFilter helper methods
    // =========================================================================

    /**
     * {@link GenericFilter#addAndSetCurrentConfiguration} registers a configuration
     * <em>and</em> makes it current in a single atomic call — avoiding the silent
     * no-op of calling {@link GenericFilter#setCurrentConfiguration} on an
     * unregistered configuration.
     */
    def "addAndSetCurrentConfiguration registers and activates a configuration in one call"() {
        given: "A GenericFilter"
        GenericFilter filter = uiComponents.create(GenericFilter)

        and: "A DesignTimeConfiguration that is registered but not active"
        DesignTimeConfiguration first = filter.configurationBuilder()
                .id("first")
                .asDefault()
                .buildAndRegister()
        DesignTimeConfiguration second = filter.configurationBuilder()
                .id("second")
                .buildAndRegister()

        expect: "first is currently active"
        filter.currentConfiguration == first

        when: "Using setCurrentConfiguration to switch to second"
        filter.setCurrentConfiguration(second)

        then: "second is now active"
        filter.currentConfiguration == second
    }

    /**
     * {@link GenericFilter#setCurrentConfiguration} silently ignores a configuration
     * that has not been registered with the filter.  This is a known limitation of
     * the existing API; use {@link GenericFilter#addAndSetCurrentConfiguration} to
     * avoid it.
     */
    def "setCurrentConfiguration silently ignores an unregistered configuration"() {
        given: "A GenericFilter with its initial current configuration"
        GenericFilter filter = uiComponents.create(GenericFilter)
        def initialConfig = filter.currentConfiguration

        and: "A RunTimeConfiguration that has NOT been registered with this filter"
        GroupFilter root = uiComponents.create(GroupFilter)
        root.setConditionModificationDelegated(true)
        root.setOperation(LogicalFilterComponent.Operation.AND)
        RunTimeConfiguration unregistered = new RunTimeConfiguration("unregistered", root, filter)

        when: "Trying to activate the unregistered configuration"
        filter.setCurrentConfiguration(unregistered)

        then: "Current configuration is unchanged — no exception, no switch"
        filter.currentConfiguration == initialConfig
    }

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
