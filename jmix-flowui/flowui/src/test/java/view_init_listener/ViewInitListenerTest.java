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

package view_init_listener;

import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInitListener;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import test_support.FlowuiTestConfiguration;
import view_init_listener.view.ViewInitListenerTestView;

import java.util.ArrayList;
import java.util.List;

@UiTest(viewBasePackages = "view_init_listener.view")
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        ViewInitListenerTest.TestConfig.class})
public class ViewInitListenerTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    TestViewInitListener testViewInitListener;

    @TestConfiguration
    static class TestConfig {
        @Bean
        TestViewInitListener testViewInitListener() {
            return new TestViewInitListener();
        }
    }

    static class TestViewInitListener implements ViewInitListener {
        List<View<?>> initializedViews = new ArrayList<>();

        @Override
        public void onViewInit(View<?> view) {
            initializedViews.add(view);
        }
    }

    @BeforeEach
    void setUp() {
        testViewInitListener.initializedViews.clear();
    }

    @Test
    void testListenerInvokedOnViewInit() {
        navigationSupport.navigate(ViewInitListenerTestView.class);
        View<?> currentView = UiTestUtils.getCurrentView();
        Assertions.assertTrue(testViewInitListener.initializedViews.contains(currentView));
    }
}
