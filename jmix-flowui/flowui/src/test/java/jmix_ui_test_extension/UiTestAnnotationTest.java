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

package jmix_ui_test_extension;

import component.image.view.JmixImageTestView;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.UiTest;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import test_support.FlowuiTestConfiguration;

@UiTest(viewBasePackages = "component.image")
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class UiTestAnnotationTest {

    @Autowired
    private ViewNavigators viewNavigators;

    @DisplayName("Load JmixImage with dataContainer")
    @Test
    public void loadJmixImageWithDataContainer() {
        viewNavigators.view(JmixImageTestView.class)
                .navigate();

        JmixImageTestView view = UiTestUtils.getCurrentView();

        Assertions.assertTrue(Strings.isNotEmpty(view.imageByteArray.getSrc()));
    }
}
