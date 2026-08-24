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

import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.reports.ReportsTestConfiguration;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;

import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.test_support.entity.GameTitle;
import io.jmix.reports.test_support.entity.Publisher;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import llm_data_set.test_support.DenyingLoadValuesConstraint;
import llm_data_set.test_support.LlmQueryExecutionTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

/**
 * The loader executing a stored query for real, against the database and through {@code DataManager}.
 * <p>
 * The sibling tests observe what the loader decides to run; this one observes what running it produces — that
 * the select aliases key the band rows, that values are bound as named JPQL parameters rather than inlined, and
 * that the permissions of the current user apply because the query goes through the constrained
 * {@code DataManager} like any other data set.
 */
@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
@ContextConfiguration(classes = {ReportsTestConfiguration.class, LlmQueryExecutionTestConfiguration.class})
class LlmQueryExecutionTest {

    /**
     * Prefixes the names of this test's own rows. The module's tests share one database, and other classes fill
     * these very tables, so every query here selects its own rows and the cleanup deletes only those.
     */
    protected static final String OWN = "LlmQueryExecution ";

    @Autowired
    protected ReportLoaderFactory loaderFactory;

    @Autowired
    protected LlmDataQuerySerializer serializer;

    @Autowired
    protected DenyingLoadValuesConstraint denyingConstraint;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() {
        denyingConstraint.reset();
        jdbcTemplate.update("delete from GAME_TITLE where NAME like ?", OWN + "%");
        jdbcTemplate.update("delete from PUBLISHER where NAME like ?", OWN + "%");
    }

    @Test
    void testStoredQueryIsExecutedAndItsAliasesKeyTheBandRows() {
        publisher("Nintendo");
        publisher("Ubisoft");
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select p.name as publisherName from Publisher p where p.name like :name order by p.name",
                List.of("publisherName"), List.of(new LlmQueryParameter("name", "java.lang.String")),
                null, List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of("name", OWN + "%"));

        assertThat(rows).containsExactly(
                Map.of("publisherName", OWN + "Nintendo"),
                Map.of("publisherName", OWN + "Ubisoft"));
    }

    @Test
    void testBandRowsFollowTheSelectClauseAndCanBeWrittenInto() {
        // What the report engine needs of a band row: the columns positioned as the select clause puts them,
        // because a cross-tab links its cells by the first matching one, and a row it can write into, because
        // merging several data sets of one band does exactly that.
        publisher("Nintendo");
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select p.name as publisherName, p.id as publisherId from Publisher p where p.name like :name",
                List.of("publisherName", "publisherId"),
                List.of(new LlmQueryParameter("name", "java.lang.String")), null, List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of("name", OWN + "%"));

        assertThat(rows.get(0).keySet()).containsExactly("publisherName", "publisherId");
        rows.get(0).put("addedByTheEngine", "value");
        rows.add(new LinkedHashMap<>());
    }

    @Test
    void testValueIsBoundAsANamedJpqlParameter() {
        // The query is written once and run with whatever the report parameters hold this time, so a value is
        // bound rather than inlined into the text.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        game("Destiny", new BigDecimal("25"), publisher);
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select g.name as gameName from GameTitle g where g.name like :name and g.price >= :minPrice "
                        + "order by g.name",
                List.of("gameName"), List.of(new LlmQueryParameter("name", "java.lang.String"),
                new LlmQueryParameter("minPrice", "java.math.BigDecimal")),
                null, List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null,
                Map.of("name", OWN + "%", "minPrice", new BigDecimal("10")));

        assertThat(rows).containsExactly(Map.of("gameName", OWN + "Destiny"));
    }

    @Test
    void testCollectionIsBoundWholeAndMatchedWithIn() {
        // What a cross-tab cell query does with the values of its axes, and what a "list of entities" report
        // parameter does: one name, every value, matched with IN.
        publisher("Nintendo");
        publisher("Ubisoft");
        publisher("Activision");
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select p.name as publisherName from Publisher p where p.name in :names order by p.name",
                List.of("publisherName"), List.of(new LlmQueryParameter("names", "java.lang.String")),
                null, List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null,
                Map.of("names", List.of(OWN + "Ubisoft", OWN + "Nintendo")));

        assertThat(rows).containsExactly(
                Map.of("publisherName", OWN + "Nintendo"),
                Map.of("publisherName", OWN + "Ubisoft"));
    }

    @Test
    void testAttributeTheUserMayNotReadComesBackAsNull() {
        // The band keeps the column, so a template printing it renders an empty value instead of failing on a
        // field the report says it has.
        Publisher publisher = publisher("Nintendo");
        GameTitle tetris = game("Tetris", new BigDecimal("5"), publisher);
        denyingConstraint.denySelectedPath("name");
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select g.name as gameName, g.price as price from GameTitle g where g.id = :id",
                List.of("gameName", "price"), List.of(new LlmQueryParameter("id", "java.util.UUID")),
                null, List.of()));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of("id", tetris.getId()));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0))
                .containsExactly(entry("gameName", null), entry("price", new BigDecimal("5.00")));
        // The value is withheld, not the row: the query itself was allowed to run.
    }

    @Test
    void testBeingRefusedTheDataFailsTheRunAsAccessDeniedRatherThanAsABadQuery() {
        // A query filtering by an attribute the user may not read is refused outright by the platform, and that
        // refusal passes through as what it is instead of being reported as a query to generate again.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        denyingConstraint.denyFilterPath("price");
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select g.name as gameName from GameTitle g where g.price >= :minPrice",
                List.of("gameName"), List.of(new LlmQueryParameter("minPrice", "java.math.BigDecimal")),
                null, List.of()));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("minPrice", new BigDecimal("1"))))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testQueryThatNoLongerFitsTheDataModelFailsNamingTheDataSetAndTheWayOut() {
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select p.title as publisherTitle from Publisher p",
                List.of("publisherTitle"), List.of(), null, List.of()));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Data", "failed", "Generate it again");
    }

    @Test
    void testAQueryReachingTheDatabaseDirectlyIsNeverHandedToIt() {
        // The promise of this data set type is that a run reads through DataManager, under the permissions of
        // the current user; a native escape would step around that, so it does not reach the database at all.
        publisher("Nintendo");
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select p.name as publisherName from Publisher p where sql('1 = 1')",
                List.of("publisherName"), List.of(), null, List.of()));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("sql", "not executed");
    }

    protected Publisher publisher(String name) {
        Publisher publisher = metadata.create(Publisher.class);
        publisher.setName(OWN + name);
        return dataManager.unconstrained().save(publisher);
    }

    protected GameTitle game(String name, BigDecimal price, Publisher publisher) {
        GameTitle game = metadata.create(GameTitle.class);
        game.setName(OWN + name);
        game.setPrice(price);
        game.setReleaseDate(LocalDate.of(2026, 1, 1));
        game.setPublisher(publisher);
        return dataManager.unconstrained().save(game);
    }

    protected DataSet llmDataSet(LlmDataQuery query) {
        DataSet dataSet = metadata.create(DataSet.class);
        dataSet.setName("Data");
        dataSet.setType(DataSetType.LLM);
        dataSet.setText("The prompt that produced the query");
        dataSet.setLlmGeneratedQuery(serializer.toJson(query));
        return dataSet;
    }

    protected ReportDataLoader loader() {
        return loaderFactory.createDataLoader(DataSetType.LLM.getCode());
    }
}
