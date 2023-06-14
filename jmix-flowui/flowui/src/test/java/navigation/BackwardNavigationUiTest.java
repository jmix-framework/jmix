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

package navigation;

import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.StandardOutcome;
import navigation.view.BackwardNavigationDetailView;
import navigation.view.BackwardNavigationListView;
import navigation.view.BackwardNavigationStandardView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;
import test_support.entity.sales.Customer;

@UiTest(viewBasePackages = "navigation.view")
@SpringBootTest(classes = {FlowuiTestAssistConfiguration.class, FlowuiTestConfiguration.class})
public class BackwardNavigationUiTest {

    @Autowired
    ViewNavigators viewNavigators;

    @Test
    @DisplayName("Back to detail-view")
    public void backToDetailView() {
        // Navigate to detail-view
        viewNavigators.detailView(Customer.class)
                .newEntity()
                .navigate();

        BackwardNavigationDetailView currentView = UiTestUtils.getCurrentView();
        Assertions.assertNotNull(currentView);

        // Open another view with backward navigation
        currentView.navigateToViewBtn.click();
        BackwardNavigationStandardView secondView = UiTestUtils.getCurrentView();

        // View should be opened
        Assertions.assertEquals(secondView.getClass(), BackwardNavigationStandardView.class);

        // Close view
        secondView.close(StandardOutcome.CLOSE);
        currentView = UiTestUtils.getCurrentView();

        // Detail-view should be opened
        Assertions.assertEquals(currentView.getClass(), BackwardNavigationDetailView.class);
    }

    @Test
    @DisplayName("Back to standard-view")
    public void backToStandardView() {
        // Navigate to standard-view
        viewNavigators.view(BackwardNavigationStandardView.class)
                .navigate();

        BackwardNavigationStandardView currentView = UiTestUtils.getCurrentView();
        Assertions.assertNotNull(currentView);

        // Open another view with backward navigation
        currentView.navigateToViewBtn.click();
        BackwardNavigationListView secondView = UiTestUtils.getCurrentView();

        // Another view should be opened
        Assertions.assertEquals(secondView.getClass(), BackwardNavigationListView.class);

        // Close view
        secondView.close(StandardOutcome.CLOSE);
        currentView = UiTestUtils.getCurrentView();

        // Standard-view should be opened
        Assertions.assertEquals(currentView.getClass(), BackwardNavigationStandardView.class);
    }

    @Test
    @DisplayName("Create new entity instance using CreateAction and back to list-view")
    public void createNewEntityInstanceUsingCreateActionAndBackToListView() {
        // Navigate to list-view
        viewNavigators.view(BackwardNavigationListView.class)
                .navigate();

        BackwardNavigationListView currentView = UiTestUtils.getCurrentView();
        Assertions.assertNotNull(currentView);

        // Create new entity instance by clicking create button
        currentView.createBtn.click();

        // Current opened view should be detail-view
        BackwardNavigationDetailView detailView = UiTestUtils.getCurrentView();
        Assertions.assertEquals(detailView.getClass(), BackwardNavigationDetailView.class);

        // Close detail view
        detailView.closeBtn.click();

        // List-view should be opened
        currentView = UiTestUtils.getCurrentView();
        Assertions.assertEquals(currentView.getClass(), BackwardNavigationListView.class);
    }
}
