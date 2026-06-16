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
import io.jmix.flowui.Actions
import io.jmix.flowui.UiComponents
import io.jmix.flowui.action.genericfilter.GenericFilterRemoveAction
import io.jmix.flowui.component.genericfilter.MutableConfiguration
import io.jmix.flowui.component.genericfilter.configuration.RunTimeConfiguration
import io.jmix.flowui.component.logicalfilter.GroupFilter
import io.jmix.flowui.component.logicalfilter.LogicalFilterComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class GenericFilterApiTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    @Autowired
    Actions actions

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "addAndSetCurrentConfiguration() registers and activates in one call"() {
        given:
        def view = navigateToView(GenericFilterApiTestView)
        def filter = view.genericFilter
        def config = newRuntimeConfig("my-config", filter)

        when:
        filter.addAndSetCurrentConfiguration(config)

        then:
        filter.getConfigurations().contains(config)
        filter.getCurrentConfiguration() == config
    }

    def "refreshCurrentConfiguration() can be called on the public API without error"() {
        given:
        def view = navigateToView(GenericFilterApiTestView)

        when:
        view.genericFilter.refreshCurrentConfiguration()

        then:
        noExceptionThrown()
    }

    def "protectedFromUserDeletion defaults to false and can be set to true"() {
        given:
        def view = navigateToView(GenericFilterApiTestView)
        def config = newRuntimeConfig("config", view.genericFilter)

        expect:
        !config.isProtectedFromUserDeletion()

        when:
        config.setProtectedFromUserDeletion(true)

        then:
        config.isProtectedFromUserDeletion()
    }

    def "removeConfiguration() skips a configuration protected from user deletion"() {
        given:
        def view = navigateToView(GenericFilterApiTestView)
        def filter = view.genericFilter
        def config = newRuntimeConfig("protected", filter)
        config.setProtectedFromUserDeletion(true)
        filter.addConfiguration(config)

        when:
        filter.removeConfiguration(config)

        then:
        filter.getConfigurations().contains(config)
    }

    def "GenericFilterRemoveAction is not applicable when current configuration is protected from user deletion"() {
        given:
        def view = navigateToView(GenericFilterApiTestView)
        def filter = view.genericFilter
        def config = newRuntimeConfig("protected", filter)
        config.setProtectedFromUserDeletion(true)
        filter.addAndSetCurrentConfiguration(config)

        and:
        def removeAction = actions.create(GenericFilterRemoveAction.ID) as GenericFilterRemoveAction
        removeAction.setTarget(filter)

        expect:
        !removeAction.isApplicable()
    }

    def "RunTimeConfiguration implements MutableConfiguration; DesignTimeConfiguration does not"() {
        given:
        def view = navigateToView(GenericFilterApiTestView)
        def filter = view.genericFilter
        def runTimeConfig = newRuntimeConfig("rt", filter)
        def designTimeConfig = filter.addConfiguration("dt", "Design-time")

        expect:
        runTimeConfig instanceof MutableConfiguration
        !(designTimeConfig instanceof MutableConfiguration)
    }

    private RunTimeConfiguration newRuntimeConfig(String id, def filter) {
        def root = uiComponents.create(GroupFilter)
        root.setConditionModificationDelegated(true)
        root.setOperation(LogicalFilterComponent.Operation.AND)
        root.setOperationTextVisible(false)
        return new RunTimeConfiguration(id, root, filter)
    }
}
