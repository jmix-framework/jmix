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

package navigation

import com.vaadin.flow.component.UI
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.view.StandardOutcome
import navigation.view.BackwardNavigationDetailView
import navigation.view.BackwardNavigationListView
import navigation.view.BackwardNavigationStandardView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Customer
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class BackwardNavigationTest extends FlowuiTestSpecification {

    @Autowired
    ViewNavigators viewNavigators

    void setup() {
        registerViewBasePackages("navigation.view")
    }

    def "Back to detail-view"() {
        when: "Navigate to detail-view"

        viewNavigators.detailView(Customer)
                .newEntity()
                .navigate()

        then: "Detail-view is opened"

        currentView.getClass() == BackwardNavigationDetailView

        when: "Open another view with backward navigation"
        ((BackwardNavigationDetailView) currentView).navigateToViewBtn.click()

        then: "View should be opened"

        currentView.getClass() == BackwardNavigationStandardView

        when: "Close view"

        ((BackwardNavigationStandardView) currentView).close(StandardOutcome.CLOSE)

        then: "Detail-view should be opened"

        currentView.getClass() == BackwardNavigationDetailView
    }

    def "Back to standard-view"() {
        when: "Navigate to standard-view"

        def view = navigateToView(BackwardNavigationStandardView)

        then: "Standard-view should be opened"

        view.getClass() == BackwardNavigationStandardView

        when: "Open another view with backward navigation"

        view.navigateToViewBtn.click()

        then: "Another view should be opened"

        currentView.getClass() == BackwardNavigationListView

        when: "Close view"

        ((BackwardNavigationListView) currentView).close(StandardOutcome.CLOSE)

        then: "Standard-view should be opened"

        currentView.getClass() == BackwardNavigationStandardView
    }

    def "Create new entity instance using CreateAction and back to list-view"() {
        when: "Navigate to list view"
        def view = navigateToView(BackwardNavigationListView)

        then: "List view is opened"

        view.getClass() == BackwardNavigationListView

        when: "Create new entity instance by clicking create button"

        view.createBtn.click()

        then: "Current opened view should be detail-view"

        currentView.getClass() == BackwardNavigationDetailView

        when: "Close detail view"

        ((BackwardNavigationDetailView) currentView).closeBtn.click()

        then: "List-view should be opened"

        currentView.getClass() == BackwardNavigationListView
    }

    <T> T getCurrentView() {
        return UI.getCurrent().getInternals().getActiveRouterTargetsChain()[0] as T
    }
}
