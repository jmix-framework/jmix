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
import io.jmix.reports.entity.BandDefinition;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;
import io.jmix.reports.libintegration.LlmCrossTabAxes;
import io.jmix.reports.libintegration.LlmDataLoader;
import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.reports.yarg.structure.BandData;
import io.jmix.reports.yarg.structure.BandOrientation;
import llm_data_set.test_support.LlmDataSetTestConfiguration;
import llm_data_set.test_support.TestLlmDataLoader;
import llm_data_set.test_support.TestLlmDataQueryService;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    protected TestLlmDataLoader dataLoader;

    @Autowired
    protected LlmDataQuerySerializer serializer;

    @Autowired
    protected Metadata metadata;

    @BeforeEach
    void setUp() {
        queryService.reset();
        dataLoader.reset();
    }

    @Test
    void testStoredQueryIsExecutedWithoutGeneration() {
        dataLoader.setRows(List.of(Map.of("orderNumber", "A-1")));
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of());

        assertThat(rows).containsExactly(Map.of("orderNumber", "A-1"));
        assertThat(dataLoader.getLastExecution().jpql()).isEqualTo(CACHED_JPQL);
        // Nothing was asked of the service: a run neither generates nor validates.
        assertThat(queryService.getGenerationRequests()).isEmpty();
        assertThat(queryService.getValidatedQueries()).isEmpty();
    }

    @Test
    void testQueryThatIsNotASelectIsRefused() {
        // A report arrives by import as well, carrying whatever text the file holds, and this loader binds
        // values into that text and runs it.
        LlmDataQuery notASelect = new LlmDataQuery("delete from sales_Order o where o.number = :number",
                List.of("orderNumber"), List.of(parameter("number", "java.lang.String")), null, List.of());
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(notASelect));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("number", "A-1")))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Data", "not a select");

        assertThat(dataLoader.getExecutions()).isEmpty();
    }

    /**
     * Every EclipseLink construct with which a select reaches past JPQL: {@code SQL} inlines database SQL,
     * {@code FUNCTION}, {@code FUNC} and {@code OPERATOR} call a database function, and {@code COLUMN} and
     * {@code TABLE} read what the entity model does not map — so none of them can be executed by a data set
     * whose promise is reading the model under the permissions of the current user.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "select o.number as orderNumber from sales_Order o where sql('1 = 1')",
            "select FUNCTION('YEAR', o.date) as orderNumber from sales_Order o",
            "select FUNC('YEAR', o.date) as orderNumber from sales_Order o",
            "select OPERATOR('Trim', o.number) as orderNumber from sales_Order o",
            "select COLUMN('SECRET_CODE', o) as orderNumber from sales_Order o",
            "select o.number as orderNumber from TABLE('SALES_ORDER') o"})
    void testQueryReachingPastJpqlIsRefused(String jpql) {
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(
                new LlmDataQuery(jpql, List.of("orderNumber"), List.of(), null, List.of())));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Data", "not executed");

        assertThat(dataLoader.getExecutions()).isEmpty();
    }

    /**
     * The rest of what the EclipseLink grammar adds stays within the entity model, so a query using it is
     * executed like any other.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "select CAST(o.number as CHAR) as orderNumber from sales_Order o",
            "select EXTRACT(YEAR from o.date) as orderNumber from sales_Order o",
            "select TREAT(o.customer as sales_VipCustomer).name as orderNumber from sales_Order o"})
    void testQueryStayingWithinTheModelIsExecuted(String jpql) {
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(
                new LlmDataQuery(jpql, List.of("orderNumber"), List.of(), null, List.of())));

        loader().loadData(dataSet, null, Map.of());

        assertThat(dataLoader.getExecutions()).hasSize(1);
    }

    @Test
    void testSuchACallSpelledInsideAStringLiteralIsJustText() {
        LlmDataQuery withLiteral = new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o where o.note = 'sql(1 = 1)'",
                List.of("orderNumber"), List.of(), null, List.of());
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(withLiteral));

        loader().loadData(dataSet, null, Map.of());

        assertThat(dataLoader.getLastExecution().jpql()).contains("'sql(1 = 1)'");
    }

    @Test
    void testANameThatMerelyEndsWithSuchACallIsNotTakenForOne() {
        // The call has to stand on its own: a word ending in one is a name, and a name is not an escape.
        LlmDataQuery ordinary = new LlmDataQuery(
                "select o.number as orderNumber from sales_Order o where o.total = mysql(o.amount)",
                List.of("orderNumber"), List.of(), null, List.of());
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(ordinary));

        loader().loadData(dataSet, null, Map.of());

        assertThat(dataLoader.getExecutions()).hasSize(1);
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

        assertThat(dataLoader.getLastExecution().arguments()).containsExactly(entry("dateFrom", dateFrom));
    }

    @Test
    void testValueIsBoundAsTheRunHoldsItWhateverTheQueryDeclares() {
        // The type in the stored query was written to tell a model what the value would be; binding uses the
        // value itself, so a type the model guessed wrong changes nothing here.
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.lang.String"))));
        LocalDate dateFrom = LocalDate.of(2026, 8, 1);

        loader().loadData(dataSet, null, Map.of("dateFrom", dateFrom));

        assertThat(dataLoader.getLastExecution().arguments()).containsExactly(entry("dateFrom", dateFrom));
    }

    @Test
    void testParametersTheQueryDoesNotReferenceAreNotBound() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"))));

        loader().loadData(dataSet, null, Map.of("dateFrom", LocalDate.of(2026, 8, 1), "unusedParam", "value"));

        assertThat(dataLoader.getLastExecution().arguments())
                .containsOnlyKeys("dateFrom");
    }

    @Test
    void testEmptyStringParameterIsBoundLikeAnyOtherValue() {
        // An empty string is a value the run provides, unlike a parameter left unfilled, which arrives as null.
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("dateFrom", "java.time.LocalDate"),
                parameter("blank", "java.lang.String"))));

        loader().loadData(dataSet, null, Map.of("dateFrom", LocalDate.of(2026, 8, 1), "blank", ""));

        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("dateFrom", LocalDate.of(2026, 8, 1)), entry("blank", ""));
    }

    @Test
    void testParentBandFieldIsOfferedAndBoundUnderItsFlattenedName() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("Orders_number", "java.lang.String"))));
        BandData ordersBand = band("Orders", null, Map.of("number", "A-1"));

        loader().loadData(dataSet, ordersBand, Map.of());

        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("Orders_number", "A-1"));
    }

    @Test
    void testGrandparentBandFieldIsBoundUnderItsFlattenedName() {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of(parameter("Orders_number", "java.lang.String"),
                parameter("Customers_customerId", "java.lang.Long"))));
        BandData customersBand = band("Customers", null, Map.of("customerId", 42L));
        BandData ordersBand = band("Orders", customersBand, Map.of("number", "A-1"));

        loader().loadData(dataSet, ordersBand, Map.of());

        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("Orders_number", "A-1"), entry("Customers_customerId", 42L));
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
        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("customerName", "Acme"));

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

        assertThat(dataLoader.getLastExecution().arguments())
                .containsOnlyKeys("Orders_number")
                .containsValue("from run parameters");
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
        assertThat(dataLoader.getLastExecution().arguments())
                .containsOnlyKeys("Orders_number");

        DataSet withNull = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("Orders_comment", "java.lang.String"))));
        assertThatThrownBy(() -> loader().loadData(withNull, ordersBand, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("Orders_comment");
    }

    @Test
    void testCrossTabAxisFieldsAreBoundAsListsInsteadOfTheAxesThemselves() {
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(List.of(
                parameter("revenue_dynamic_header_year", "java.lang.String"),
                parameter("revenue_master_data_publisherId", "java.lang.String")))));

        loader().loadData(dataSet, null, crossTabParams());

        // Every value the axis has for that field, as the list a query matches with IN.
        assertThat(dataLoader.getLastExecution().arguments()).containsExactly(
                entry("revenue_dynamic_header_year", List.of(2025, 2025)),
                entry("revenue_master_data_publisherId", List.of("Nintendo", "Ubisoft")));

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

        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("revenue_dynamic_header_year", List.of(2025)));
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
        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("revenue_dynamic_header_year", List.of(2025)));
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
        assertThat(dataLoader.getLastExecution().arguments())
                .containsOnlyKeys("revenue_dynamic_header_year");
    }

    @Test
    void testCrossTabAxisListIsBoundAsAWholeAtExecution() {
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header_year", "java.lang.String")))));

        loader().loadData(dataSet, null, crossTabParams());

        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("revenue_dynamic_header_year", List.of(2025, 2025)));
    }

    @Test
    void testParameterNamedLikeAnAxisButHoldingPlainValuesStaysAParameter() {
        // Bound under its own name rather than broken into <axis>_<field> parameters, and — holding several
        // values — as one a query matches with IN.
        DataSet dataSet = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("selected_master_data", "java.lang.String"))));

        loader().loadData(dataSet, null, Map.of("selected_master_data", List.of("A-1", "A-2")));

        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("selected_master_data", List.of("A-1", "A-2")));
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
        assertThat(dataLoader.getExecutions()).isEmpty();
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
    void testAxisOfAnotherBandIsNotTakenForOwn() {
        // The params of a run are one map shared by every band of it, and a cross-tab band leaves its axes in
        // them, so an ordinary band extracted afterwards sees axes that are none of its business.
        dataLoader.setRows(List.of(Map.of("orderNumber", "A-1")));
        DataSet dataSet = llmDataSetOfBand("Orders", storedQuery(List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, crossTabParams());

        assertThat(rows).containsExactly(Map.of("orderNumber", "A-1"));
        assertThat(dataLoader.getLastExecution().arguments()).isEmpty();
    }

    @Test
    void testEmptyAxisOfAnotherBandDoesNotEmptyThisBand() {
        // The worst of the two: an empty foreign axis used to make the band render empty with a debug line.
        dataLoader.setRows(List.of(Map.of("orderNumber", "A-1")));
        DataSet dataSet = llmDataSetOfBand("Orders", storedQuery(List.of()));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of());

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, params);

        assertThat(rows).containsExactly(Map.of("orderNumber", "A-1"));
    }

    @Test
    void testAxisOfABandWhoseNameStartsLikeThisOneIsNotOwn() {
        // Band names share prefixes: "revenue" must not claim the axis of "revenue_extra".
        dataLoader.setRows(List.of(Map.of("orderNumber", "A-1")));
        DataSet dataSet = llmDataSetOfBand("revenue", storedQuery(List.of()));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_extra_dynamic_header", List.of(axisRow("year", 2025)));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, params);

        assertThat(rows).containsExactly(Map.of("orderNumber", "A-1"));
        assertThat(dataLoader.getLastExecution().arguments()).isEmpty();
    }

    @Test
    void testOwnAxisIsStillTheBandsBusiness() {
        DataSet dataSet = llmDataSetOfBand("revenue", serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header_year", "java.lang.String")))));

        loader().loadData(dataSet, null, crossTabParams());

        assertThat(dataLoader.getLastExecution().arguments())
                .containsEntry("revenue_dynamic_header_year", List.of(2025, 2025));
    }

    @Test
    void testAxisWhoseEveryValueIsNullStillHasToBeLinkable() {
        // The axis has rows, so the matrix has that column; a cell query that cannot be linked to it would
        // otherwise render an empty matrix with nothing said.
        DataSet dataSet = llmDataSetOfBand("revenue", storedQuery(List.of()));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of(axisRow("year", null), axisRow("year", null)));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, params))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("revenue_dynamic_header");

        assertThat(dataLoader.getExecutions()).isEmpty();
    }

    @Test
    void testAxisWhoseFieldsHaveNoOrderRequiresNoParticularColumn() {
        // A JPQL or SQL axis hands out its rows as plain HashMaps, where "the first field" is a hash order that
        // changes between runs of the JVM: a stored query accepted today would be refused tomorrow.
        Map<String, Object> unordered = new HashMap<>();
        unordered.put("year", 2025);
        unordered.put("caption", "2025");
        LlmDataQuery byTheSecondField = new LlmDataQuery(CACHED_JPQL,
                List.of("revenue_dynamic_header_caption", "revenue_master_data_publisherId", "amount"),
                List.of(), null, List.of());
        DataSet dataSet = llmDataSetOfBand("revenue", serializer.toJson(byTheSecondField));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of(unordered));
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo")));

        loader().loadData(dataSet, null, params);

        assertThat(dataLoader.getExecutions()).hasSize(1);
    }

    @Test
    void testAxisWhoseFieldsAreOrderedStillRequiresItsFirstColumn() {
        LlmDataQuery byTheSecondField = new LlmDataQuery(CACHED_JPQL,
                List.of("revenue_dynamic_header_year_caption", "revenue_dynamic_header_year",
                        "revenue_master_data_publisherId", "amount"),
                List.of(), null, List.of());
        DataSet dataSet = llmDataSetOfBand("revenue", serializer.toJson(byTheSecondField));
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("revenue_dynamic_header", List.of(axisRow("year", 2025, "year_caption", "2025")));
        params.put("revenue_master_data", List.of(axisRow("publisherId", "Nintendo")));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, params))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("revenue_dynamic_header_year");
    }

    @Test
    void testPromptIsNotReadAtRunTime() {
        // A run executes the query stored with the report, so the prompt that produced it is of no consequence
        // here — it matters in the designer, where the query is generated.
        DataSet dataSet = llmDataSet("  ", storedQuery(List.of()));

        loader().loadData(dataSet, null, Map.of());

        assertThat(dataLoader.getLastExecution().jpql()).isEqualTo(CACHED_JPQL);
    }

    @Test
    void testUnreadableStoredQueryFails() {
        DataSet dataSet = llmDataSet(PROMPT, "{\"jpql\": ");

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Data", "cannot be read");
    }

    @Test
    void testQueryTheDatabaseRefusesFailsNamingTheDataSet() {
        // What an imported report meets when its stored query no longer matches the data model.
        dataLoader.setFailure(new IllegalStateException("Unknown attribute [o.number] of entity [sales_Order]"));
        DataSet dataSet = llmDataSet(PROMPT, storedQuery(List.of()));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Data", "Unknown attribute [o.number]", "Generate it again");
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
    void testNullAndEmptyStringResultValuesRemainDistinct() {
        Map<String, Object> executionRow = new HashMap<>();
        executionRow.put("missingNumber", null);
        executionRow.put("emptyNumber", "");
        dataLoader.setRows(List.of(executionRow));
        DataSet dataSet = llmDataSet(PROMPT,
                storedQuery(List.of("missingNumber", "emptyNumber"), List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of());

        assertThat(rows.get(0))
                .containsEntry("missingNumber", null)
                .containsEntry("emptyNumber", "");

        DataSet childDataSet = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("Orders_emptyNumber", "java.lang.String"))));
        loader().loadData(childDataSet, band("Orders", null, rows.get(0)), Map.of());

        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("Orders_emptyNumber", ""));
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
    void testAxisFieldShadowedByARunParameterIsSaidOnceAndTheColumnStillRequired() {
        // The run parameter wins the name, so the axis values are not offered — but the column named after the
        // axis is still required back, or the cross-tab would have nothing to link its cells by.
        DataSet dataSet = llmDataSet(PROMPT, serializer.toJson(linkableCrossTabQuery(
                List.of(parameter("revenue_dynamic_header_year", "java.lang.String")))));
        Map<String, Object> params = new LinkedHashMap<>(crossTabParams());
        params.put("revenue_dynamic_header_year", 2024);
        // One run, told by the band hierarchy it is rooted at, so that "once per run" is what is observed.
        BandData cells = band("Cells", band("Root", null, Map.of()), Map.of());
        ListAppender<ILoggingEvent> logged = captureCrossTabLog();

        try {
            loader().loadData(dataSet, cells, params);
            loader().loadData(dataSet, cells, params);
        } finally {
            releaseCrossTabLog(logged);
        }

        assertThat(dataLoader.getLastExecution().arguments())
                .containsEntry("revenue_dynamic_header_year", 2024);
        assertThat(logged.list)
                .filteredOn(event -> event.getFormattedMessage().contains("cross-tab axis"))
                .hasSize(1);
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
        assertThat(dataLoader.getExecutions()).hasSize(2);
    }

    @Test
    void testListParameterIsBoundAsTheWholeList() {
        DataSet dataSet = llmDataSet(PROMPT,
                storedQuery(List.of(parameter("orderNumbers", "java.lang.String"))));

        loader().loadData(dataSet, null, Map.of("orderNumbers", List.of("A-1", "A-2")));

        assertThat(dataLoader.getLastExecution().arguments())
                .containsExactly(entry("orderNumbers", List.of("A-1", "A-2")));
    }

    @Test
    void testEmptyListParameterCannotBeBound() {
        // A collection with nothing to match is not a value, so the parameter has none and the query says so.
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

    /**
     * Collects what the cross-tab rules log, which is a logger of their own.
     */
    protected ListAppender<ILoggingEvent> captureCrossTabLog() {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        ((Logger) LoggerFactory.getLogger(LlmCrossTabAxes.class)).addAppender(appender);
        return appender;
    }

    protected void releaseCrossTabLog(ListAppender<ILoggingEvent> appender) {
        ((Logger) LoggerFactory.getLogger(LlmCrossTabAxes.class)).detachAppender(appender);
        appender.stop();
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

    /**
     * A data set of a named band, which is how a run tells the axes of one cross-tab band from those of another.
     */
    protected DataSet llmDataSetOfBand(String bandName, @Nullable String storedQuery) {
        DataSet dataSet = llmDataSet(PROMPT, storedQuery);
        BandDefinition band = metadata.create(BandDefinition.class);
        band.setName(bandName);
        dataSet.setBandDefinition(band);
        return dataSet;
    }
}
