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

package llm_designer;

import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Report;
import io.jmix.reportsflowui.ReportsFlowuiTestConfiguration;
import io.jmix.reportsflowui.test_support.AuthenticatedAsAdmin;
import io.jmix.reportsflowui.view.report.ReportDetailView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Without the query service — that is, in an application that did not add the AI Tools add-on — the designer does
 * not offer the LLM data set type at all, while every other type keeps working.
 */
@UiTest
@SpringBootTest(classes = {ReportsFlowuiTestConfiguration.class})
@ExtendWith({AuthenticatedAsAdmin.class})
public class LlmDataSetTypeUnavailableUiTest {

    @Autowired
    protected ViewNavigators viewNavigators;

    @Test
    @SuppressWarnings("unchecked")
    public void testLlmTypeIsNotOfferedAmongDataSetTypes() {
        viewNavigators.detailView(UiTestUtils.getCurrentView(), Report.class)
                .withViewClass(ReportDetailView.class)
                .newEntity()
                .navigate();
        View<?> reportDetailView = UiTestUtils.getCurrentView();

        JmixSelect<DataSetType> typeField =
                (JmixSelect<DataSetType>) UiComponentUtils.getComponent(reportDetailView, "singleDataSetTypeField");
        List<DataSetType> types = typeField.getListDataView().getItems().toList();

        assertThat(types).doesNotContain(DataSetType.LLM);
        assertThat(types).contains(DataSetType.JPQL, DataSetType.SQL, DataSetType.GROOVY, DataSetType.JSON);
    }
}
