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

import io.jmix.core.Messages
import io.jmix.flowui.sys.ViewSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import page_title.view.AnnotatedPageTitleHardcodedView
import page_title.view.AnnotatedPageTitleView
import page_title.view.BothPageTitleView
import page_title.view.DeclarativePageTitleHardcodedView
import page_title.view.DeclarativePageTitleView
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class PageTitleTest extends FlowuiTestSpecification {

    @Autowired
    ViewSupport viewSupport

    @Autowired
    Messages messages

    void setup() {
        registerScreenBasePackages("page_title.view")
    }

    def "load page title from view descriptor"() {
        when: "Open view with title defined as message key in the descriptor"

        def view = openScreen(DeclarativePageTitleView)

        then: "View should use loaded page title"

        viewSupport.getLocalizedTitle(view) ==
                messages.getMessage("page_title.view/declarativePageTitleView.title")

        when: "Open view with hardcoded title defined in the descriptor"

        view = openScreen(DeclarativePageTitleHardcodedView)

        then: "View should use loaded page title"

        viewSupport.getLocalizedTitle(view) == "Declarative Page Title Hardcoded"
    }

    def "load page title from annotation"() {
        when: "Open view that has @PageTitle with message key"

        def view = openScreen(AnnotatedPageTitleView)

        then: "View should use loaded page title from @PageTitle"

        viewSupport.getLocalizedTitle(view) ==
                messages.getMessage("page_title.view/annotatedPageTitleView.title")

        when: "Open view that has @PageTitle with hardcoded value"

        view = openScreen(AnnotatedPageTitleHardcodedView)

        then: "View should use loaded page title from @PageTitle"

        viewSupport.getLocalizedTitle(view) == "Annotated Page Title Hardcoded"
    }

    def "annotated page title should override declarative definition"() {
        when: "Open view that has @PageTitle and 'title' attribute in the descriptor"

        def view = openScreen(BothPageTitleView)

        then: "View should use page title from @PageTitle"

        viewSupport.getLocalizedTitle(view) ==
                messages.getMessage("page_title.view/bothPageTitleView.title")
    }
}
