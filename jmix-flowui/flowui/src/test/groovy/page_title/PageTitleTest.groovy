/*
 * Copyright 2022 Haulmont.
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

package page_title

import com.vaadin.flow.component.UI
import io.jmix.core.Messages
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.sys.ViewSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import page_title.screen.AnnotatedPageTitleScreen
import page_title.screen.DeclarativePageTitleScreen
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class PageTitleTest extends FlowuiTestSpecification {

    @Autowired
    ViewNavigators screenNavigators

    @Autowired
    ViewSupport screenSupport

    @Autowired
    Messages messages

    void setup() {
        registerScreenBasePackages("page_title.screen")
    }

    def "load page title from screen descriptor"() {
        when: "Open screen with defined title in the descriptor"
        screenNavigators.view(DeclarativePageTitleScreen)
                .navigate()

        then: "Screen should use loaded page title"
        def screen = (DeclarativePageTitleScreen) UI.getCurrent()
                .getInternals().getActiveRouterTargetsChain().get(0)

        screenSupport.getLocalizedPageTitle(screen) ==
                messages.getMessage("page_title.screen/declarativePageTitleScreen.title")
    }

    def "annotated page title should override declarative definition"() {
        when: "Open screen that has @PageTitle and 'title' attribute in the descriptor"

        screenNavigators.view(AnnotatedPageTitleScreen)
                .navigate()

        then: "Screen should use loaded page title from @PageTitle"

        def screen = (AnnotatedPageTitleScreen) UI.getCurrent()
                .getInternals().getActiveRouterTargetsChain().get(0)

        screenSupport.getLocalizedPageTitle(screen) ==
                messages.getMessage("page_title.screen/annotatedPageTitleScreen.annotatedTitle")
    }
}
