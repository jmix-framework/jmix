/*
 * Copyright 2020 Haulmont.
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

package facet.clipboard_trigger

import facet.clipboard_trigger.screen.ClipboardTriggerFacetTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.ClipboardTrigger
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class ClipboardTriggerFacetTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["facet.clipboard_trigger"])
    }

    def "open screen with ClipboardTrigger"() {
        showTestMainScreen()

        when:

        def screen = screens.create(ClipboardTriggerFacetTestScreen)
        screen.show()

        then:

        screen.window.getFacet('copyTrigger') instanceof ClipboardTrigger
        screen.window.facets.count() == 1

        screen.copyTrigger != null
        screen.copyTrigger.button != null
        screen.copyTrigger.input != null
    }
}
