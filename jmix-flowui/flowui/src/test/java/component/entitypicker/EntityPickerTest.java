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

package component.entitypicker;

import component.entitypicker.view.EntityPickerTestView;
import component.standarddetailview.view.BlankTestView;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

@UiTest(viewBasePackages = {"component.entitypicker.view", "component.standarddetailview.view"})
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class EntityPickerTest {

    @Autowired
    private ViewNavigators viewNavigators;

    @Autowired
    private ViewNavigationSupport navigationSupport;

    @Test
    @DisplayName("Set empty value from client should not cause unparseable validation error")
    public void setEmptyValueFromClientShouldNotCauseUnparseableValidationError() {
        var origin = navigateTo(BlankTestView.class);

        viewNavigators.view(origin, EntityPickerTestView.class)
                .navigate();

        EntityPickerTestView entityPickerTestView = UiTestUtils.getCurrentView();

        // Simulate user action that sets an empty value
        entityPickerTestView.productField.setValueFromClient(null);

        // This should not have unparseable validation error
        Assertions.assertFalse(entityPickerTestView.productField.isInvalid());
        Assertions.assertNull(entityPickerTestView.productField.getErrorMessage());
    }

    protected <T extends View<?>> T navigateTo(Class<T> viewClass) {
        navigationSupport.navigate(viewClass);
        return UiTestUtils.getCurrentView();
    }
}
