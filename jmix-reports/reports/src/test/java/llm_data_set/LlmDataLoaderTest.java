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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.jmix.core.Metadata;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.libintegration.LlmDataLoader;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmDataQueryException;
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
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of());

        assertThat(rows).containsExactly(Map.of("orderNumber", "A-1"));
        assertThat(queryService.getGenerationRequests()).isEmpty();
        assertThat(queryService.getLastExecutionRequest().getQuery().getJpql()).isEqualTo(CACHED_JPQL);
    }

    @Test
    void testMissingStoredQueryFails() {
        // A run never generates a query: it is generated in the report designer and stored with the report, so a
        // data set without one has nothing to execute.
        DataSet dataSet = llmDataSet(PROMPT, null);

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Data", "no generated query stored");
    }

    @Test
    void testReportParameterValueIsBoundToTheQueryParameter() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"))));
        LocalDate dateFrom = LocalDate.of(2026, 8, 1);

        loader().loadData(dataSet, null, Map.of("dateFrom", dateFrom));

        List<LlmQueryParameter> arguments = queryService.getLastExecutionRequest().getArguments();
        assertThat(arguments).hasSize(1);
        assertThat(arguments.get(0).getName()).isEqualTo("dateFrom");
        assertThat(arguments.get(0).getValue()).isEqualTo(dateFrom);
    }

    @Test
    void testArgumentTypeComesFromTheReportParameterNotFromTheStoredQuery() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.lang.String"))));

        loader().loadData(dataSet, null, Map.of("dateFrom", LocalDate.of(2026, 8, 1)));

        assertThat(queryService.getLastExecutionRequest().getArguments().get(0).getJavaType())
                .isEqualTo("java.time.LocalDate");
    }

    @Test
    void testParametersTheQueryDoesNotReferenceAreNotBound() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"))));

        loader().loadData(dataSet, null, Map.of("dateFrom", LocalDate.of(2026, 8, 1), "unusedParam", "value"));

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName)
                .containsExactly("dateFrom");
    }

    @Test
    void testEmptyStringParameterIsBoundLikeAnyOtherValue() {
        // An empty string is a value the run provides, unlike a parameter left unfilled, which arrives as null.
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"),
                parameter("blank", "java.lang.String"))));

        loader().loadData(dataSet, null, Map.of("dateFrom", LocalDate.of(2026, 8, 1), "blank", ""));

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("dateFrom", LocalDate.of(2026, 8, 1)), tuple("blank", ""));
    }

    @Test
    void testParentBandFieldIsOfferedAndBoundUnderItsFlattenedName() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("Orders_number", "java.lang.String"))));
        BandData ordersBand = band("Orders", null, Map.of("number", "A-1"));

        loader().loadData(dataSet, ordersBand, Map.of());

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("Orders_number", "A-1"));
    }

    @Test
    void testGrandparentBandFieldIsBoundUnderItsFlattenedName() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("Orders_number", "java.lang.String"),
                parameter("Customers_customerId", "java.lang.Long"))));
        BandData customersBand = band("Customers", null, Map.of("customerId", 42L));
        BandData ordersBand = band("Orders", customersBand, Map.of("number", "A-1"));

        loader().loadData(dataSet, ordersBand, Map.of());

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("Orders_number", "A-1"), tuple("Customers_customerId", 42L));
    }

    @Test
    void testRunParameterIsNotBoundASecondTimeUnderTheRootBand() {
        // The root band's data is the run parameters themselves, so a query binds them under their own names and
        // there is no Root_<name> to bind them by a second time.
        BandData rootBand = band("Root", null, Map.of("customerName", "Acme"));
        BandData ordersBand = band("Orders", rootBand, Map.of("number", "A-1"));

        DataSet byOwnName = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("customerName", "java.lang.String"))));
        loader().loadData(byOwnName, ordersBand, Map.of("customerName", "Acme"));
        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("customerName", "Acme"));

        DataSet underRoot = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("Root_customerName", "java.lang.String"))));
        assertThatThrownBy(() -> loader().loadData(underRoot, ordersBand, Map.of("customerName", "Acme")))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("Root_customerName");
    }

    @Test
    void testRunParameterWinsOverAParentBandFieldOfTheSameName() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("Orders_number", "java.lang.String"))));
        BandData ordersBand = band("Orders", null, Map.of("number", "from band"));

        loader().loadData(dataSet, ordersBand, Map.of("Orders_number", "from run parameters"));

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getValue)
                .containsExactly("from run parameters");
    }

    @Test
    void testFieldOfABandWhoseNameIsNotAnIdentifierCannotBeBound() {
        // A JPQL parameter name is an identifier, so a band named with a space contributes nothing — and its
        // fields are not offered under a name made up by replacing that space either.
        DataSet dataSet = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("Order_Details_number", "java.lang.String"))));
        BandData ordersBand = band("Order Details", null, Map.of("number", "A-1"));

        assertThatThrownBy(() -> loader().loadData(dataSet, ordersBand, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("Order_Details_number");
    }

    @Test
    void testNullValuedParentBandFieldCannotBeBound() {
        Map<String, Object> bandRow = new HashMap<>();
        bandRow.put("number", "A-1");
        bandRow.put("comment", null);
        BandData ordersBand = band("Orders", null, bandRow);

        DataSet withValue = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("Orders_number", "java.lang.String"))));
        loader().loadData(withValue, ordersBand, Map.of());
        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName)
                .containsExactly("Orders_number");

        DataSet withNull = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("Orders_comment", "java.lang.String"))));
        assertThatThrownBy(() -> loader().loadData(withNull, ordersBand, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("Orders_comment");
    }

    @Test
    void testCrossTabAxisFieldsAreBoundAsTypedListsInsteadOfTheAxesThemselves() {
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(List.of(
                parameter("revenue_dynamic_header_year", "java.lang.String"),
                parameter("revenue_master_data_publisherId", "java.lang.String")))));

        loader().loadData(dataSet, null, crossTabParams());

        // The value is the whole list — the add-on converts a collection element by element — and the type is
        // the element's, not the list's.
        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue,
                        LlmQueryParameter::getJavaType, LlmQueryParameter::isMultiValued)
                .containsExactly(
                        tuple("revenue_dynamic_header_year", List.of(2025, 2025), "java.lang.Integer", true),
                        tuple("revenue_master_data_publisherId", List.of("Nintendo", "Ubisoft"),
                                "java.lang.String", true));

        // The axis itself is a list of rows, not a value a query can be filtered by.
        DataSet byTheAxisItself = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header", "java.lang.String")))));
        assertThatThrownBy(() -> loader().loadData(byTheAxisItself, null, crossTabParams()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("revenue_dynamic_header");
    }

    @Test
    void testNullValuesInsideACrossTabAxisAreDropped() {
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header_year", "java.lang.String")))));
        Map<String, Object> rowWithNull = new HashMap<>();
        rowWithNull.put("year", null);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of(Map.of("year", 2025), rowWithNull));
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo")));

        loader().loadData(dataSet, null, params);

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("revenue_dynamic_header_year", List.of(2025)));
    }

    @Test
    void testAxisIsLinkedByItsFirstFieldEvenWhenTheFirstRowLeavesItEmpty() {
        // Which field links the matrix follows from the axis's own shape. Deciding it by the first field that
        // happens to hold a value would move the required column between runs, and a stored query would be
        // refused on the run where the shape came out differently.
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header_year", "java.lang.String")))));
        // Ordered the way a data set's own rows are: the axis describes year first, whatever this row holds.
        Map<String, Object> firstRow = new LinkedHashMap<>();
        firstRow.put("year", null);
        firstRow.put("month", 3);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", Arrays.asList(firstRow, axisRow("year", 2025, "month", 4)));
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo")));

        loader().loadData(dataSet, null, params);

        // The query links by year — the axis's first field — and binds the values that field does have.
        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("revenue_dynamic_header_year", List.of(2025)));
    }

    @Test
    void testAxisFieldWithoutValuesIsStillRequiredBack() {
        // Nothing can be said about a field this run left empty, but the axis still describes it first, and the
        // column that links the matrix is the axis's first field whatever this run's rows hold. A query linking
        // by the field that does have values would place the cells under the wrong column.
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of(axisRow("year", null, "month", 3),
                axisRow("year", null, "month", 4)));
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo")));

        DataSet byTheFirstField = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery()));
        loader().loadData(byTheFirstField, null, params);

        LlmDataQuery byTheFieldWithValues = new LlmDataQuery(CACHED_JPQL,
                List.of("revenue_dynamic_header_month", "revenue_master_data_publisherId", "amount"),
                List.of(), "Revenue per publisher and month", List.of());
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(byTheFieldWithValues));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, params))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("revenue_dynamic_header_month", "revenue_dynamic_header_year");
    }

    @Test
    void testCrossTabAxisFieldEmptyInEveryRowCannotBeBound() {
        // The field is required back all the same, but there is nothing to bind under it and no type to state.
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header_year", "java.lang.String")))));
        Map<String, Object> rowWithNull = new HashMap<>();
        rowWithNull.put("year", null);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of(rowWithNull));
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo")));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, params))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("revenue_dynamic_header_year");
    }

    @Test
    void testCrossTabAxisFieldWhoseNameIsNotAnIdentifierIsSkipped() {
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header_year", "java.lang.String")))));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of(axisRow("year of sale", 2025, "year", 2025)));
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo")));

        loader().loadData(dataSet, null, params);

        // The field named with spaces contributes nothing, while the one that is an identifier binds.
        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName)
                .containsExactly("revenue_dynamic_header_year");
    }

    @Test
    void testCrossTabAxisListIsBoundAsAWholeAtExecution() {
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header_year", "java.lang.String")))));

        loader().loadData(dataSet, null, crossTabParams());

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("revenue_dynamic_header_year", List.of(2025, 2025)));
    }

    @Test
    void testParameterNamedLikeAnAxisButHoldingPlainValuesStaysAParameter() {
        // Bound under its own name rather than broken into <axis>_<field> parameters, and — holding several
        // values — as one a query matches with IN.
        DataSet dataSet = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("selected_master_data", "java.lang.String"))));

        loader().loadData(dataSet, null, Map.of("selected_master_data", List.of("A-1", "A-2")));

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::isMultiValued,
                        LlmQueryParameter::getValue)
                .containsExactly(tuple("selected_master_data", true, List.of("A-1", "A-2")));
    }

    @Test
    void testQueryThatLinksACrossTabAxisByAnotherFieldFails() {
        LlmDataQuery query = new LlmDataQuery(CACHED_JPQL,
                List.of("revenue_dynamic_header_year_caption", "revenue_dynamic_header_year",
                        "revenue_master_data_publisherId", "amount"),
                List.of(), "Revenue per publisher and year", List.of());
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(query));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of(axisRow("year", 2025, "year_caption", "2025")));
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo")));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, params))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("revenue_dynamic_header_year_caption")
                .hasMessageContaining("revenue_dynamic_header_year");
    }

    @Test
    void testRowsCutShortByTheLimitStillReachTheBand() {
        // The run says so in the log rather than failing: a band of the first rows is what the limit asked for.
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()));
        queryService.setRows(List.of(Map.of("orderNumber", "A-1")));
        queryService.setTruncated(true);

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of());

        assertThat(rows).containsExactly(Map.of("orderNumber", "A-1"));
    }

    @Test
    void testEmptyCrossTabAxisProducesNoRowsInsteadOfFailing() {
        // A period with no data leaves an axis empty; the matrix then has no cells, and the stored query that
        // filters by that axis has nothing to bind.
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header_year", "java.lang.Integer")))));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of());
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo")));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, params);

        assertThat(rows).isEmpty();
        assertThat(queryService.getExecutionRequests()).isEmpty();
    }

    @Test
    void testQueryThatCannotBeLinkedToACrossTabAxisFails() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()));
        Map<String, Object> params = Map.of("revenue_dynamic_header", List.of(Map.of("year", 2025)));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, params))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("revenue_dynamic_header")
                .hasMessageContaining("orderNumber");
    }

    @Test
    void testBlankPromptFails() {
        DataSet dataSet = llmDataSet("  ", storedQuery(List.of()));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("Data");
    }

    @Test
    void testUnreadableStoredQueryFails() {
        DataSet dataSet = llmDataSet(PROMPT, "{\"jpql\": ");

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class);
    }

    @Test
    void testStoredQueryTheAddOnRejectsFailsWithItsValidationIssues() {
        // What an imported report meets when its stored query no longer matches the domain model.
        queryService.setExecutionFailure(new LlmDataQueryException(
                "The query is invalid: Unknown attribute [o.number] of entity [sales_Order]"));
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("Data")
                .cause()
                .hasMessageContaining("Unknown attribute [o.number]");
    }

    @Test
    void testQueryParameterWithoutAValueFails() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"))));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("dateFrom");
    }

    @Test
    void testFailureNamesAnEmptyReportParameterAsEmptyRatherThanAsUnknown() {
        // An optional report parameter left unfilled arrives with a null value. It is the common way a run meets
        // an unbindable parameter, and the author has to hear which one it is and that filling it in is the fix.
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"))));
        Map<String, Object> params = new HashMap<>();
        params.put("dateFrom", null);

        assertThatThrownBy(() -> loader().loadData(dataSet, null, params))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("dateFrom", "left empty", "fill the parameter in");

        // A name the run never heard of stays a different message: nothing to fill in there.
        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("provides no value for it");
    }

    @Test
    void testRowsAreMutableAndFollowTheSelectClause() {
        // The add-on hands out immutable maps in an order of its own; a band row is written into by the engine
        // and read positionally by a cross-tab.
        queryService.setRows(List.of(Map.copyOf(Map.of("amount", 10, "orderNumber", "A-1"))));
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of("orderNumber", "amount"), List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of());

        assertThat(rows.get(0).keySet()).containsExactly("orderNumber", "amount");
        rows.get(0).put("addedByTheEngine", "value");
        rows.add(new HashMap<>());
    }

    @Test
    void testNullAndEmptyStringResultValuesRemainDistinct() {
        Map<String, Object> executionRow = new HashMap<>();
        executionRow.put("missingNumber", null);
        executionRow.put("emptyNumber", "");
        queryService.setRows(List.of(executionRow));
        DataSet dataSet = llmDataSet(PROMPT,
                storedQuery(List.of("missingNumber", "emptyNumber"), List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of());

        assertThat(rows.get(0))
                .containsEntry("missingNumber", null)
                .containsEntry("emptyNumber", "");

        DataSet childDataSet = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("Orders_emptyNumber", "java.lang.String"))));
        loader().loadData(childDataSet, band("Orders", null, rows.get(0)), Map.of());

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getValue)
                .containsExactly(tuple("Orders_emptyNumber", ""));
    }

    @Test
    void testInvalidQueryFailsTheDataSetBeforeItIsExecuted() {
        // Execution answers an invalid query by asking a model to repair it, which would spend tokens on a run,
        // send this run's arguments to the model and bind the values the model replies with.
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()));
        queryService.setProblems(List.of("DTO parameter is not used in JPQL: dateFrom"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("Data")
                .hasRootCauseMessage("The query was rejected as invalid: DTO parameter is not used in JPQL: dateFrom");

        assertThat(queryService.getExecutionRequests()).isEmpty();
    }

    @Test
    void testQueryIsCheckedOnceForTheWholeRun() {
        // A check parses the query and resolves it against the data model, and the query does not change while
        // the run executes it, so a band loaded once per parent row must not pay for the same verdict twice.
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()));
        BandData rootBand = band("Root", null, Map.of());

        loader().loadData(dataSet, band("Orders", rootBand, Map.of("orderNumber", "A-1")), Map.of());
        loader().loadData(dataSet, band("Orders", rootBand, Map.of("orderNumber", "A-2")), Map.of());

        assertThat(queryService.getValidatedQueries()).hasSize(1);
        assertThat(queryService.getExecutionRequests()).hasSize(2);
    }

    @Test
    void testWarningOfOneDataSetDoesNotSilenceAnotherOfTheSameName() {
        // Data set names are unique within a band, so every band's first data set is called the same by default.
        // A warning remembered for one of them must not silence the other.
        DataSet first = llmDataSet(PROMPT, storedQuery(List.of(parameter("Orders_number", "java.lang.String"))));
        DataSet second = llmDataSet(PROMPT, storedQuery(List.of(parameter("Orders_number", "java.lang.String"))));
        assertThat(first.getName()).isEqualTo(second.getName());
        BandData rootBand = band("Root", null, Map.of());
        ListAppender<ILoggingEvent> logged = captureLoaderLog();

        try {
            // A run parameter named like the flattened band field shadows that field, which is what the loader
            // warns about — once per data set, so twice here.
            loader().loadData(first, band("Orders", rootBand, Map.of("number", "A-1")),
                    Map.of("Orders_number", "P-1"));
            loader().loadData(second, band("Orders", rootBand, Map.of("number", "A-2")),
                    Map.of("Orders_number", "P-1"));
        } finally {
            releaseLoaderLog(logged);
        }

        assertThat(logged.list)
                .filteredOn(event -> event.getFormattedMessage().contains("is not offered to the query"))
                .hasSize(2);
    }

    @Test
    void testStoredQueryIsReadOnceForTheWholeRun() {
        // The document does not change while the run reads it, so a parent row does not pay for reading it again.
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()));
        BandData rootBand = band("Root", null, Map.of());
        ReportDataLoader loader = loader();
        CountingSerializer counting = new CountingSerializer();
        ReflectionTestUtils.setField(loader, "llmDataQuerySerializer", counting);

        try {
            loader.loadData(dataSet, band("Orders", rootBand, Map.of("orderNumber", "A-1")), Map.of());
            loader.loadData(dataSet, band("Orders", rootBand, Map.of("orderNumber", "A-2")), Map.of());
        } finally {
            ReflectionTestUtils.setField(loader, "llmDataQuerySerializer", serializer);
        }

        assertThat(counting.reads).isEqualTo(1);
        assertThat(queryService.getExecutionRequests()).hasSize(2);
    }

    @Test
    void testListParameterIsBoundAsMultiValuedOfItsElementType() {
        DataSet dataSet = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("orderNumbers", "java.lang.String"))));

        loader().loadData(dataSet, null, Map.of("orderNumbers", List.of("A-1", "A-2")));

        assertThat(queryService.getLastExecutionRequest().getArguments())
                .extracting(LlmQueryParameter::getName, LlmQueryParameter::getJavaType,
                        LlmQueryParameter::isMultiValued)
                .containsExactly(tuple("orderNumbers", "java.lang.String", true));
    }

    @Test
    void testEmptyListParameterCannotBeBound() {
        // Nothing to match with IN and no type to state.
        DataSet dataSet = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("orderNumbers", "java.lang.String"))));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("orderNumbers", List.of())))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("orderNumbers");
    }

    protected ReportDataLoader loader() {
        return loaderFactory.createDataLoader(DataSetType.LLM.getCode());
    }

    /**
     * Collects what the loader logs. A warning written once per run is only observable there.
     */
    protected ListAppender<ILoggingEvent> captureLoaderLog() {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        ((Logger) LoggerFactory.getLogger(LlmDataLoader.class)).addAppender(appender);
        return appender;
    }

    protected void releaseLoaderLog(ListAppender<ILoggingEvent> appender) {
        ((Logger) LoggerFactory.getLogger(LlmDataLoader.class)).detachAppender(appender);
        appender.stop();
    }

    /**
     * A query a cross-tab band can place into its matrix: it returns one column per axis, named after that axis.
     */
    protected LlmDataQuery linkableCrossTabQuery() {
        return linkableCrossTabQuery(List.of());
    }

    protected LlmDataQuery linkableCrossTabQuery(List<LlmQueryParameter> parameters) {
        return new LlmDataQuery(CACHED_JPQL,
                List.of("revenue_dynamic_header_year", "revenue_master_data_publisherId", "amount"),
                parameters, "Revenue per publisher and year", List.of());
    }

    /**
     * The params a cross-tab band puts in place before the cell data set runs: one entry per axis, each holding
     * the rows that axis produced.
     */
    protected Map<String, Object> crossTabParams() {
        // The rows of an axis keep the order of the query that produced them, and the loader requires the first
        // field back as the column a cross-tab links by, so the fixture states that order.
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of(axisRow("year", 2025, "month", 3),
                axisRow("year", 2025, "month", 4)));
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo"),
                axisRow("publisherId", "Ubisoft")));
        return params;
    }

    protected Map<String, Object> axisRow(String name, Object value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put(name, value);
        return row;
    }

    protected Map<String, Object> axisRow(String firstName, Object firstValue, String secondName, Object secondValue) {
        Map<String, Object> row = axisRow(firstName, firstValue);
        row.put(secondName, secondValue);
        return row;
    }

    /**
     * Counts how often a stored document is read back.
     */
    static class CountingSerializer extends LlmDataQuerySerializer {

        int reads;

        @Nullable
        @Override
        public LlmDataQuery fromJson(@Nullable String json) {
            reads++;
            return super.fromJson(json);
        }
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
        return storedQuery(List.of("orderNumber"), parameters);
    }

    protected String storedQuery(List<String> resultProperties, List<LlmQueryParameter> parameters) {
        return serializer.toJson(new LlmDataQuery(CACHED_JPQL, resultProperties, parameters,
                "Orders since the given date", List.of()));
    }

    protected DataSet llmDataSet(String prompt, @Nullable String storedQuery) {
        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName("Data");
        dataSet.setType(DataSetType.LLM);
        dataSet.setText(prompt);
        dataSet.setLlmGeneratedQuery(storedQuery);
        return dataSet;
    }
}
