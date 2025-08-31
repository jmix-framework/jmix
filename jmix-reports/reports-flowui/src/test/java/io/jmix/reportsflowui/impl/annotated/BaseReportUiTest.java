/*
 * Copyright 2025 Haulmont.
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

package io.jmix.reportsflowui.impl.annotated;

import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.view.View;
import io.jmix.reportsflowui.ReportsFlowuiTestConfiguration;
import io.jmix.reportsflowui.test_support.AuthenticatedAsAdmin;
import io.jmix.reportsflowui.test_support.entity.TestDataInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@UiTest
@SpringBootTest(classes = {ReportsFlowuiTestConfiguration.class})
@ExtendWith({AuthenticatedAsAdmin.class})
public abstract class BaseReportUiTest {
    @Autowired
    protected ViewNavigators viewNavigators;

    @BeforeAll
    public static void setup(@Autowired TestDataInitializer testDataInitializer) {
        testDataInitializer.init();
    }

    @SuppressWarnings("unchecked")
    protected static <T> T findComponent(View<?> view, String componentId) {
        return (T) UiComponentUtils.getComponent(view, componentId);
    }

    protected LocalDate parseDate(String isoDateString) {
        return LocalDate.parse(isoDateString, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
