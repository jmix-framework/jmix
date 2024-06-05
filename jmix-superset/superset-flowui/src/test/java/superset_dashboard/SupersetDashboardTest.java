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

package superset_dashboard;

import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.navigation.ViewNavigationSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import superset_dashboard.view.SupersetDashboardTestView;
import test_support.SupersetFlowuiTestConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@UiTest(viewBasePackages = "superset_dashboard.view")
@SpringBootTest(classes = {SupersetFlowuiTestConfiguration.class, FlowuiTestAssistConfiguration.class})
public class SupersetDashboardTest {

    @Autowired
    ViewNavigationSupport navigationSupport;

    @Test
    @DisplayName("Load SupersetDashboard component from XML")
    public void loadSupersetDashboardFromXml() {
        navigationSupport.navigate(SupersetDashboardTestView.class);

        var view = (SupersetDashboardTestView) UiTestUtils.getCurrentView();
        var dashboard1 = view.dashboard1;

        assertNotNull(view.dashboard1);

        assertAll(
                () -> assertTrue(dashboard1.isChartControlsVisible()),
                () -> assertEquals(dashboard1.getClassName(), "classNames"),
                () -> assertEquals("green", dashboard1.getStyle().get("color")),
                () -> assertNotNull(dashboard1.getDatasetConstraintsProvider()),
                () -> assertEquals("172f1241-f8c8-4203-88a6-0771753da7b3", dashboard1.getEmbeddedId()),
                () -> assertTrue(dashboard1.isFiltersExpanded()),
                () -> assertEquals("30px", dashboard1.getHeight()),
                () -> assertEquals("40px", dashboard1.getMaxHeight()),
                () -> assertEquals("40px", dashboard1.getMaxWidth()),
                () -> assertEquals("20px", dashboard1.getMinHeight()),
                () -> assertEquals("20px", dashboard1.getMinWidth()),
                () -> assertTrue(dashboard1.isTitleVisible()),
                () -> assertFalse(dashboard1.isVisible()),
                () -> assertEquals("30px", dashboard1.getWidth())
        );

        // Check dataset constraints are loaded from XML
        var constraintsProvider = view.dashboard2.getDatasetConstraintsProvider();
        assertNotNull(constraintsProvider);
        assertEquals(1, constraintsProvider.getConstraints().size());
    }
}
