/*
 * Copyright (c) 2020 Haulmont.
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

package action_owner

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.action.BaseAction
import io.jmix.ui.component.Button
import io.jmix.ui.component.KeyCombination
import io.jmix.ui.icon.Icons
import io.jmix.ui.icon.JmixIcon
import io.jmix.ui.testassist.spec.ScreenSpecification
import io.jmix.ui.theme.ThemeClassNames
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class ActionOwnerTest extends ScreenSpecification {

    @Autowired
    Icons icons

    def "Action properties override owner properties"() {
        showTestMainScreen()

        when: 'Owner properties are initialized'

        def owner = uiComponents.create(Button)
        owner.caption = 'Owner caption'
        owner.description = 'Owner description'
        owner.shortcut = 'CTRL-ENTER'
        owner.enabled = true
        owner.visible = true
        owner.addStyleName(ThemeClassNames.PRIMARY_ACTION)
        owner.icon = icons.get(JmixIcon.SEARCH)

        and: 'Action properties are initialized'

        def action = new BaseAction('testAction')
        action.caption = 'Action caption'
        action.description = 'Action description'
        action.shortcut = 'CTRL-BACKSPACE'
        action.enabled = false
        action.visible = false
        action.primary = false
        action.icon = icons.get(JmixIcon.BAN)

        and: 'Action set to owner'

        owner.setAction(action, true)

        then: 'Action properties will override owner properties'

        owner.caption == 'Action caption'
        owner.description == 'Action description'
        owner.shortcutCombination == KeyCombination.create('CTRL-BACKSPACE')
        !owner.enabled
        !owner.visible
        owner.styleName == ''
        owner.icon == icons.get(JmixIcon.BAN)
    }

    def "Action properties do not override owner properties"() {
        showTestMainScreen()

        when: 'Owner properties are initialized'

        def owner = uiComponents.create(Button)
        owner.caption = 'Owner caption'
        owner.description = 'Owner description'
        owner.shortcut = 'CTRL-ENTER'
        owner.enabled = true
        owner.visible = true
        owner.addStyleName(ThemeClassNames.PRIMARY_ACTION)
        owner.icon = icons.get(JmixIcon.SEARCH)

        and: 'Action properties are initialized'

        def action = new BaseAction('testAction')
        action.caption = 'Action caption'
        action.description = 'Action description'
        action.shortcut = 'CTRL-BACKSPACE'
        action.enabled = false
        action.visible = false
        action.primary = false
        action.icon = icons.get(JmixIcon.BAN)

        and: 'Action set to owner'

        owner.setAction(action, false)

        then: 'Action properties will not override owner properties'

        owner.caption == 'Owner caption'
        owner.description == 'Owner description'
        owner.shortcutCombination == KeyCombination.create('CTRL-ENTER')
        owner.enabled
        owner.visible
        owner.styleName == ThemeClassNames.PRIMARY_ACTION
        owner.icon == icons.get(JmixIcon.SEARCH)
    }

    def "Action properties are applied if the owner's properties are null"(boolean overrideOwnerProperties) {
        showTestMainScreen()

        when: 'Owner component is created'

        def owner = uiComponents.create(Button)

        and: 'Action properties are initialized'

        def action = new BaseAction('testAction')
        action.caption = 'Action caption'
        action.description = 'Action description'
        action.shortcut = 'CTRL-BACKSPACE'
        action.enabled = false
        action.visible = false
        action.primary = false
        action.icon = icons.get(JmixIcon.BAN)

        and: 'Action set to owner'

        owner.setAction(action, overrideOwnerProperties)

        then: 'Action properties will applied'

        owner.caption == 'Action caption'
        owner.description == 'Action description'
        owner.shortcutCombination == KeyCombination.create('CTRL-BACKSPACE')
        !owner.enabled
        !owner.visible
        owner.styleName == ''
        owner.icon == icons.get(JmixIcon.BAN)

        where:

        overrideOwnerProperties << [
                true,
                false
        ]
    }
}
