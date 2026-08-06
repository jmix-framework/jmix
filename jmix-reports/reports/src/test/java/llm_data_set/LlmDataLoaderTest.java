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
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmQueryExecutionRequest;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.BandOrientation;
import llm_data_set.test_support.LlmDataSetTestConfiguration;
import llm_data_set.test_support.TestLlmDataQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportsTestConfiguration.class, LlmDataSetTestConfiguration.class})
class LlmDataLoaderTest {

    protected static final String PROMPT = "Orders placed since the given date";
    protected static final String CACHED_JPQL =
            "select o.number as orderNumber from sales_Order o where o.date >= :dateFrom";

    @Autowired
    protected ReportLoaderFactory loaderFactory;

    @Autowired
    protected TestLlmDataQueryService queryService;

    @Autowired
    protected LlmDataQuerySerializer serializer;

    @Autowired
    protected Metadata metadata;

    @BeforeEach
    void setUp() {
        queryService.reset();
    }

    @Test
    void testStoredQueryIsExecutedWithoutGeneration() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()), false, null);

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of());

        assertThat(rows).containsExactly(Map.of("orderNumber", "A-1"));
        assertThat(queryService.getGenerationRequests()).isEmpty();
        assertThat(queryService.getLastExecutionRequest().getQuery().getJpql()).isEqualTo(CACHED_JPQL);
    }

    @Test
    void testRegenerateOnRunGeneratesQueryInsteadOfUsingStoredOne() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()), true, null);

        loader().loadData(dataSet, null, Map.of());

        assertThat(queryService.getGenerationRequests()).hasSize(1);
        assertThat(queryService.getLastGenerationRequest().getPrompt()).isEqualTo(PROMPT);
        assertThat(queryService.getLastExecutionRequest().getQuery().getJpql())
                .isEqualTo(TestLlmDataQueryService.GENERATED_JPQL);
    }

    @Test
    void testMissingStoredQueryIsGeneratedOnTheFly() {
        DataSet dataSet = llmDataSet(PROMPT, null, false, null);

        loader().loadData(dataSet, null, Map.of());

        assertThat(queryService.getGenerationRequests()).hasSize(1);
        assertThat(queryService.getLastExecutionRequest().getQuery().getJpql())
                .isEqualTo(TestLlmDataQueryService.GENERATED_JPQL);
    }

    @Test
    void testReportParameterValueIsBoundToTheQueryParameter() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"))),
                false, null);
        LocalDate dateFrom = LocalDate.of(2026, 8, 1);

        loader().loadData(dataSet, null, Map.of("dateFrom", dateFrom));

        List<LlmQueryParameter> arguments = queryService.getLastExecutionRequest().getArguments();
        assertThat(arguments).hasSize(1);
        assertThat(arguments.get(0).getName()).isEqualTo("dateFrom");
        assertThat(arguments.get(0).getValue()).isEqualTo(dateFrom);
    }

    @Test
    void testArgumentTypeComesFromTheReportParameterNotFromTheStoredQuery() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.lang.String"))),
                false, null);

        loader().loadData(dataSet, null, Map.of("dateFrom", LocalDate.of(2026, 8, 1)));

        assertThat(queryService.getLastExecutionRequest().getArguments().get(0).getJavaType())
                .isEqualTo("java.time.LocalDate");
    }

    @Test
    void testParametersTheQueryDoesNotReferenceAreNotBound() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"))),
                false, null);

        loader().loadData(dataSet, null, Map.of("dateFrom", LocalDate.of(2026, 8, 1), "unusedParam", "value"));

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName)
                .containsExactly("dateFrom");
    }

    @Test
    void testAvailableParametersAreOfferedToGeneration() {
        DataSet dataSet = llmDataSet(PROMPT, null, false, null);

        loader().loadData(dataSet, null, Map.of("dateFrom", LocalDate.of(2026, 8, 1), "blank", ""));

        assertThat(queryService.getLastGenerationRequest().getAvailableParameters())
                .extracting(LlmQueryParameter::getName)
                .contains("dateFrom", "blank");
    }

    @Test
    void testNullValuedParametersAreNotOfferedToGeneration() {
        DataSet dataSet = llmDataSet(PROMPT, null, false, null);
        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", LocalDate.of(2026, 8, 1));
        params.put("emptyParam", null);

        loader().loadData(dataSet, null, params);

        assertThat(queryService.getLastGenerationRequest().getAvailableParameters())
                .extracting(LlmQueryParameter::getName)
                .containsExactly("dateFrom");
    }

    @Test
    void testParentBandFieldIsOfferedAndBoundUnderItsFlattenedName() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("Orders_number", "java.lang.String"))),
                false, null);
        BandData ordersBand = band("Orders", null, Map.of("number", "A-1"));

        loader().loadData(dataSet, ordersBand, Map.of());

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("Orders_number", "A-1"));
    }

    @Test
    void testGrandparentBandFieldIsOfferedToGeneration() {
        DataSet dataSet = llmDataSet(PROMPT, null, false, null);
        BandData customersBand = band("Customers", null, Map.of("customerId", 42L));
        BandData ordersBand = band("Orders", customersBand, Map.of("number", "A-1"));

        loader().loadData(dataSet, ordersBand, Map.of());

        assertThat(queryService.getLastGenerationRequest().getAvailableParameters())
                .extracting(LlmQueryParameter::getName)
                .contains("Orders_number", "Customers_customerId");
    }

    @Test
    void testRunParameterWinsOverAParentBandFieldOfTheSameName() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("Orders_number", "java.lang.String"))),
                false, null);
        BandData ordersBand = band("Orders", null, Map.of("number", "from band"));

        loader().loadData(dataSet, ordersBand, Map.of("Orders_number", "from run parameters"));

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getValue)
                .containsExactly("from run parameters");
    }

    @Test
    void testBandWhoseNameIsNotAnIdentifierContributesNothing() {
        DataSet dataSet = llmDataSet(PROMPT, null, false, null);
        BandData ordersBand = band("Order Details", null, Map.of("number", "A-1"));

        loader().loadData(dataSet, ordersBand, Map.of());

        assertThat(queryService.getLastGenerationRequest().getAvailableParameters()).isEmpty();
    }

    @Test
    void testNullValuedParentBandFieldIsSkipped() {
        DataSet dataSet = llmDataSet(PROMPT, null, false, null);
        Map<String, Object> bandRow = new HashMap<>();
        bandRow.put("number", "A-1");
        bandRow.put("comment", null);
        BandData ordersBand = band("Orders", null, bandRow);

        loader().loadData(dataSet, ordersBand, Map.of());

        assertThat(queryService.getLastGenerationRequest().getAvailableParameters())
                .extracting(LlmQueryParameter::getName)
                .containsExactly("Orders_number");
    }

    @Test
    void testDataSetRowLimitReachesExecution() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()), false, 700);

        loader().loadData(dataSet, null, Map.of());

        LlmQueryExecutionRequest request = queryService.getLastExecutionRequest();
        assertThat(request.getMaxResults()).isEqualTo(700);
    }

    @Test
    void testBlankPromptFails() {
        DataSet dataSet = llmDataSet("  ", storedQuery(List.of()), false, null);

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("Data");
    }

    @Test
    void testUnreadableStoredQueryFails() {
        DataSet dataSet = llmDataSet(PROMPT, "{\"jpql\": ", false, null);

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class);
    }

    @Test
    void testQueryParameterWithoutAValueFails() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"))),
                false, null);

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("dateFrom");
    }

    protected ReportDataLoader loader() {
        return loaderFactory.createDataLoader(DataSetType.LLM.getCode());
    }

    protected BandData band(String name, @Nullable BandData parentBand, Map<String, Object> row) {
        BandData band = new BandData(name, parentBand, BandOrientation.HORIZONTAL);
        band.setData(row);
        return band;
    }

    protected LlmQueryParameter parameter(String name, String javaType) {
        return new LlmQueryParameter(name, javaType, null);
    }

    protected String storedQuery(List<LlmQueryParameter> parameters) {
        return serializer.toJson(new LlmDataQuery(CACHED_JPQL, List.of("orderNumber"), parameters,
                "Orders since the given date", List.of(), null));
    }

    protected DataSet llmDataSet(String prompt,
                                 @Nullable String storedQuery,
                                 boolean regenerateOnRun,
                                 @Nullable Integer maxResults) {
        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName("Data");
        dataSet.setType(DataSetType.LLM);
        dataSet.setText(prompt);
        dataSet.setLlmGeneratedQuery(storedQuery);
        dataSet.setLlmRegenerateOnRun(regenerateOnRun);
        dataSet.setLlmMaxResults(maxResults);
        return dataSet;
    }
}
