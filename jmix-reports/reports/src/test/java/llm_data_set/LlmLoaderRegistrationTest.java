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

package llm_data_set;

import io.jmix.core.Metadata;
import io.jmix.reports.ReportsSerialization;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.entity.Orientation;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.exception.ReportingException;
import io.jmix.reports.libintegration.LlmDataLoader;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.yarg.exception.UnsupportedLoaderException;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import llm_data_set.test_support.LlmDataSetTestConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The LLM loader reaches the loader factory only when the AI Tools add-on provides the beans it needs.
 */
class LlmLoaderRegistrationTest {

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = {ReportsTestConfiguration.class, LlmDataSetTestConfiguration.class})
    class WithAddOnBeans {

        @Autowired
        protected ReportLoaderFactory loaderFactory;

        @Autowired
        protected LlmDataLoader llmDataLoader;

        @Test
        void testLlmLoaderTypeIsServedByTheLlmLoader() {
            assertThat(loaderFactory.createDataLoader(DataSetType.LLM.getCode())).isSameAs(llmDataLoader);
        }
    }

    @Nested
    @ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
    @ContextConfiguration(classes = {ReportsTestConfiguration.class})
    class WithoutAddOnBeans {

        @Autowired
        protected ReportLoaderFactory loaderFactory;

        @Autowired
        protected ReportRunner reportRunner;

        @Autowired
        protected ReportsSerialization reportsSerialization;

        @Autowired
        protected Metadata metadata;

        @Test
        void testLlmLoaderTypeIsUnsupported() {
            assertThatThrownBy(() -> loaderFactory.createDataLoader(DataSetType.LLM.getCode()))
                    .isInstanceOf(UnsupportedLoaderException.class);
        }

        @Test
        void testBuiltInLoaderTypesKeepWorking() {
            assertThat(loaderFactory.createDataLoader("jpql")).isNotNull();
            assertThat(loaderFactory.createDataLoader("sql")).isNotNull();
            assertThat(loaderFactory.createDataLoader("groovy")).isNotNull();
            assertThat(loaderFactory.createDataLoader("json")).isNotNull();
        }

        @Test
        void testRunningAReportWithAnLlmBandFailsNamingTheTypeAndTheBand() {
            // A report authored elsewhere keeps its data set type: the designer can only hide the type from the
            // combo, so the run is where an application without the add-on finds out.
            // The runner folds the loader's failure into the message of a ReportingException instead of keeping
            // it as a cause, so what an application shows is that message.
            assertThatThrownBy(() -> reportRunner.byReportEntity(reportWithLlmBand()).run())
                    .isInstanceOf(ReportingException.class)
                    .hasMessageContainingAll("Orders", "Unsupported loader type", DataSetType.LLM.getCode());
        }

        protected Report reportWithLlmBand() {
            Report report = metadata.create(Report.class);
            report.setName("LLM band report");

            BandDefinition rootBand = metadata.create(BandDefinition.class);
            rootBand.setReport(report);
            rootBand.setName("Root");
            rootBand.setOrientation(Orientation.HORIZONTAL);
            rootBand.setPosition(0);

            BandDefinition ordersBand = metadata.create(BandDefinition.class);
            ordersBand.setReport(report);
            ordersBand.setName("Orders");
            ordersBand.setOrientation(Orientation.HORIZONTAL);
            ordersBand.setPosition(0);
            ordersBand.setParentBandDefinition(rootBand);
            rootBand.getChildrenBandDefinitions().add(ordersBand);

            DataSet dataSet = metadata.create(DataSet.class);
            dataSet.setName("Orders");
            dataSet.setBandDefinition(ordersBand);
            dataSet.setType(DataSetType.LLM);
            dataSet.setText("Order numbers of this month");
            dataSet.setLlmGeneratedQuery("""
                    {"jpql":"select o.number as orderNumber from sales_Order o",\
                    "resultProperties":["orderNumber"]}""");
            ordersBand.setDataSets(List.of(dataSet));
            report.setBands(Set.of(rootBand, ordersBand));

            ReportTemplate template = metadata.create(ReportTemplate.class);
            template.setReport(report);
            template.setCode("default");
            template.setReportOutputType(ReportOutputType.CSV);
            template.setName("LlmReport.csv");
            template.setContent("Number\n${orderNumber}\n".getBytes(StandardCharsets.UTF_8));
            report.setTemplates(List.of(template));
            report.setDefaultTemplate(template);

            report.setXml(reportsSerialization.convertToString(report));
            return report;
        }
    }
}
