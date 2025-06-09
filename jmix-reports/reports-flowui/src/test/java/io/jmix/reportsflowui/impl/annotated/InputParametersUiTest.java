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

import io.jmix.core.MetadataTools;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.testassist.notification.NotificationInfo;
import io.jmix.reportsflowui.test_support.OpenedDialogViewsTracker;
import io.jmix.reportsflowui.test_support.entity.TestDataInitializer;
import io.jmix.reportsflowui.test_support.report.PublishersAndGamesReport;
import io.jmix.reportsflowui.test_support.report.SampleDefaultValueReport;
import io.jmix.reportsflowui.view.run.InputParametersDialog;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class InputParametersUiTest extends BaseRunReportUiTest {

    @Autowired
    OpenedDialogViewsTracker openedDialogViewsTracker;

    @Autowired
    MetadataTools metadataTools;

    @BeforeAll
    public static void setup(@Autowired TestDataInitializer testDataInitializer) {
        testDataInitializer.init();
    }

    @Test
    public void testRequired() {
        // given
        String reportCode = PublishersAndGamesReport.CODE;

        // when
        launchReportFromRunView(reportCode);

        InputParametersDialog parametersDialog = (InputParametersDialog) openedDialogViewsTracker.getLastOpenedView();
        assertThat(parametersDialog).isNotNull();
        assertThat(parametersDialog.getPageTitle()).isEqualTo("Input parameters");

        // run report without specifying 2 required parameters
        JmixButton runButton = findComponent(parametersDialog, "printReportButton");
        runButton.click();

        // then
        NotificationInfo lastOpenedNotificationInfo = UiTestUtils.getLastOpenedNotification();
        assertThat(lastOpenedNotificationInfo).isNotNull();
        assertThat(lastOpenedNotificationInfo.getMessage()).contains("Fill in parameter");
        assertThat(lastOpenedNotificationInfo.getType()).isEqualTo(Notifications.Type.DEFAULT);
    }

    @Test
    public void testValidatorDelegate() {
        // given
        String reportCode = PublishersAndGamesReport.CODE;

        String startDateStr = "1999-06-01"; // bad value
        String endDateStr = "2025-03-01";

        // when
        launchReportFromRunView(reportCode);
        InputParametersDialog parametersDialog = (InputParametersDialog) openedDialogViewsTracker.getLastOpenedView();

        TypedDatePicker startDateField = findParameterField(parametersDialog, "param_startDate");
        startDateField.setValue(parseDate(startDateStr));
        TypedDatePicker endDateField = findParameterField(parametersDialog, "param_endDate");
        endDateField.setValue(parseDate(endDateStr));

        JmixButton runButton = findComponent(parametersDialog, "printReportButton");
        runButton.click();

        // then
        NotificationInfo lastOpenedNotificationInfo = UiTestUtils.getLastOpenedNotification();
        assertThat(lastOpenedNotificationInfo).isNotNull();
        assertThat(lastOpenedNotificationInfo.getMessage()).contains("Start date is too early");
        assertThat(lastOpenedNotificationInfo.getType()).isEqualTo(Notifications.Type.DEFAULT);
    }

    @Test
    public void testCrossValidationDelegate() {
        // given
        String reportCode = PublishersAndGamesReport.CODE;

        String startDateStr = "2025-03-02";
        String endDateStr = "2025-03-01";

        // when
        launchReportFromRunView(reportCode);
        InputParametersDialog parametersDialog = (InputParametersDialog) openedDialogViewsTracker.getLastOpenedView();

        TypedDatePicker startDateField = findParameterField(parametersDialog, "param_startDate");
        startDateField.setValue(parseDate(startDateStr));
        TypedDatePicker endDateField = findParameterField(parametersDialog, "param_endDate");
        endDateField.setValue(parseDate(endDateStr));

        JmixButton runButton = findComponent(parametersDialog, "printReportButton");
        runButton.click();

        // then
        NotificationInfo lastOpenedNotificationInfo = UiTestUtils.getLastOpenedNotification();
        assertThat(lastOpenedNotificationInfo).isNotNull();
        assertThat(lastOpenedNotificationInfo.getMessage()).contains("Start date must be earlier than end date");
        assertThat(lastOpenedNotificationInfo.getType()).isEqualTo(Notifications.Type.DEFAULT);
    }

    @Test
    public void testDefaultValueProvider() {
        // given
        String reportCode = SampleDefaultValueReport.CODE;
        String parameterAlias = SampleDefaultValueReport.PARAM_PUBLISHER;

        // when
        launchReportFromRunView(reportCode);
        InputParametersDialog parametersDialog = (InputParametersDialog) openedDialogViewsTracker.getLastOpenedView();

        // then
        EntityPicker field = findParameterField(parametersDialog, "param_" + parameterAlias);
        assertThat(field.getValue()).isNotNull();
        assertThat(metadataTools.getInstanceName(field.getValue())).isEqualTo("Ubisoft");
    }
}
