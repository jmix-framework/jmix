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
import io.jmix.reportsflowui.test_support.AuthenticatedAsAdmin;
import io.jmix.reportsflowui.view.report.ReportDetailView;
import llm_designer.test_support.LlmDesignerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * With the query service in the context, the report designer offers the LLM data set type.
 */
@UiTest
@SpringBootTest(classes = {LlmDesignerTestConfiguration.class})
@ExtendWith({AuthenticatedAsAdmin.class})
public class LlmDataSetTypeAvailabilityUiTest {

    @Autowired
    protected ViewNavigators viewNavigators;

    @Test
    public void testLlmTypeIsOfferedAmongDataSetTypes() {
        List<DataSetType> types = dataSetTypeOptions();

        assertThat(types).contains(DataSetType.LLM);
        assertThat(types).contains(DataSetType.JPQL, DataSetType.SQL, DataSetType.GROOVY, DataSetType.JSON);
        // DELEGATE cannot be set up in the runtime editor, and offering LLM did not bring it back.
        assertThat(types).doesNotContain(DataSetType.DELEGATE);
    }

    @SuppressWarnings("unchecked")
    protected List<DataSetType> dataSetTypeOptions() {
        viewNavigators.detailView(UiTestUtils.getCurrentView(), Report.class)
                .withViewClass(ReportDetailView.class)
                .newEntity()
                .navigate();
        View<?> reportDetailView = UiTestUtils.getCurrentView();

        JmixSelect<DataSetType> typeField =
                (JmixSelect<DataSetType>) UiComponentUtils.getComponent(reportDetailView, "singleDataSetTypeField");
        return typeField.getListDataView().getItems().toList();
    }
}
