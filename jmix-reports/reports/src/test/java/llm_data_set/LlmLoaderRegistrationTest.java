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

import io.jmix.core.DataManager;
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
import io.jmix.reports.libintegration.JmixGroovyDataLoader;
import io.jmix.reports.libintegration.JmixJsonDataLoader;
import io.jmix.reports.libintegration.JmixSqlDataLoader;
import io.jmix.reports.libintegration.JpqlDataLoader;
import io.jmix.reports.libintegration.LlmDataLoader;
import io.jmix.reports.libintegration.MultiEntityDataLoader;
import io.jmix.reports.libintegration.SingleEntityDataLoader;
import io.jmix.reports.runner.ReportRunner;
import io.jmix.reports.yarg.reporting.ReportOutputDocument;
import io.jmix.reports.test_support.entity.Publisher;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import llm_data_set.test_support.LlmDataSetTestConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The loader of the LLM data set type is an ordinary Reports bean: a run executes the query stored with the
 * report and asks nothing of the AI Tools add-on, so such a report runs whether the add-on is there or not.
 */
class LlmLoaderRegistrationTest {

    /**
     * Names this test's own row. The module's tests share one database and other classes fill this table too,
     * so the report selects its own row and the cleanup deletes only that one.
     */
    protected static final String OWN_PUBLISHER = "LlmLoaderRegistration publisher";

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

        @Autowired
        protected DataManager dataManager;

        @Autowired
        protected JdbcTemplate jdbcTemplate;

        @AfterEach
        void cleanup() {
            jdbcTemplate.update("delete from PUBLISHER where NAME = ?", OWN_PUBLISHER);
        }

        @Test
        void testLlmLoaderTypeIsServedWithoutTheAddOn() {
            assertThat(loaderFactory.createDataLoader(DataSetType.LLM.getCode()))
                    .isInstanceOf(LlmDataLoader.class);
        }

        @Test
        void testBuiltInLoaderTypesKeepWorking() {
            // Registering the LLM loader rebuilt the factory's map, so each built-in type must still reach its
            // own loader rather than merely resolve to something.
            assertThat(loaderFactory.createDataLoader("jpql")).isInstanceOf(JpqlDataLoader.class);
            assertThat(loaderFactory.createDataLoader("sql")).isInstanceOf(JmixSqlDataLoader.class);
            assertThat(loaderFactory.createDataLoader("groovy")).isInstanceOf(JmixGroovyDataLoader.class);
            assertThat(loaderFactory.createDataLoader("json")).isInstanceOf(JmixJsonDataLoader.class);
            assertThat(loaderFactory.createDataLoader("single")).isInstanceOf(SingleEntityDataLoader.class);
            assertThat(loaderFactory.createDataLoader("multi")).isInstanceOf(MultiEntityDataLoader.class);
        }

        @Test
        void testReportWithAnLlmBandRunsWithoutTheAddOn() {
            // The whole point of executing the stored query here: a report authored where the add-on is present
            // runs where it is absent, because nothing on this path needs a model or the add-on's services.
            Publisher publisher = metadata.create(Publisher.class);
            publisher.setName(OWN_PUBLISHER);
            dataManager.unconstrained().save(publisher);

            ReportOutputDocument document = reportRunner.byReportEntity(reportWithLlmBand()).run();

            assertThat(new String(document.getContent(), StandardCharsets.UTF_8)).contains(OWN_PUBLISHER);
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
            ordersBand.setName("Publishers");
            ordersBand.setOrientation(Orientation.HORIZONTAL);
            ordersBand.setPosition(0);
            ordersBand.setParentBandDefinition(rootBand);
            rootBand.getChildrenBandDefinitions().add(ordersBand);

            DataSet dataSet = metadata.create(DataSet.class);
            dataSet.setName("Publishers");
            dataSet.setBandDefinition(ordersBand);
            dataSet.setType(DataSetType.LLM);
            dataSet.setText("Names of the publishers");
            dataSet.setLlmGeneratedQuery("""
                    {"jpql":"select p.name as publisherName from Publisher p \
                    where p.name = 'LlmLoaderRegistration publisher'",\
                    "resultProperties":["publisherName"]}""");
            ordersBand.setDataSets(List.of(dataSet));
            report.setBands(Set.of(rootBand, ordersBand));

            ReportTemplate template = metadata.create(ReportTemplate.class);
            template.setReport(report);
            template.setCode("default");
            template.setReportOutputType(ReportOutputType.CSV);
            template.setName("LlmReport.csv");
            template.setContent("Publisher\n${publisherName}\n".getBytes(StandardCharsets.UTF_8));
            report.setTemplates(List.of(template));
            report.setDefaultTemplate(template);

            report.setXml(reportsSerialization.convertToString(report));
            return report;
        }
    }
}
