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

package view_initialized_event;

import io.jmix.flowui.event.view.ViewInitializedEvent;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import test_support.FlowuiTestConfiguration;
import view_initialized_event.view.ViewInitializedEventTestView;

import java.util.ArrayList;
import java.util.List;

@UiTest(viewBasePackages = "view_initialized_event.view")
@SpringBootTest(classes = {FlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class,
        ViewInitializedEventTest.TestConfig.class})
public class ViewInitializedEventTest {

    @Autowired
    ViewNavigationSupport navigationSupport;
    @Autowired
    TestViewInitializedEventListener eventListener;

    @TestConfiguration
    static class TestConfig {

        @Bean
        TestViewInitializedEventListener testViewInitializedEventListener() {
            return new TestViewInitializedEventListener();
        }
    }

    static class TestViewInitializedEventListener {

        List<View<?>> initializedViews = new ArrayList<>();

        @EventListener
        public void onViewInitialized(ViewInitializedEvent event) {
            initializedViews.add(event.getSource());
        }
    }

    @BeforeEach
    void setUp() {
        eventListener.initializedViews.clear();
    }

    @Test
    void testEventPublishedOnViewInit() {
        navigationSupport.navigate(ViewInitializedEventTestView.class);
        View<?> currentView = UiTestUtils.getCurrentView();

        Assertions.assertTrue(eventListener.initializedViews.contains(currentView));
    }
}
