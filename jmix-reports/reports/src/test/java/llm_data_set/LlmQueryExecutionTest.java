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
import io.jmix.core.impl.session.ThreadLocalSessionData;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.reports.entity.DataSet;
import io.jmix.reports.entity.DataSetType;

import io.jmix.reports.llm.LlmDataQuery;
import io.jmix.reports.llm.LlmQueryParameter;
import io.jmix.reports.llm.impl.LlmDataQuerySerializer;
import io.jmix.reports.test_support.AuthenticatedAsSystem;
import io.jmix.reports.test_support.entity.GameTitle;
import io.jmix.reports.test_support.entity.Publisher;
import io.jmix.reports.yarg.exception.DataLoadingException;
import io.jmix.security.model.RowLevelPolicy;
import io.jmix.security.model.RowLevelPolicyAction;
import io.jmix.reports.yarg.loaders.ReportDataLoader;
import io.jmix.reports.yarg.loaders.factory.ReportLoaderFactory;
import io.jmix.security.role.RoleGrantedAuthorityUtils;
import io.jmix.reports.test_support.role.FullAccessRole;
import io.jmix.reports.test_support.role.TestPublisherRowLevelRole;
import llm_data_set.test_support.DenyingLoadValuesConstraint;
import llm_data_set.test_support.TestRowLevelPolicies;
import llm_data_set.test_support.LlmQueryExecutionTestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
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

    protected static final String ROW_LEVEL_USER = "llm-row-level-user";

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

    @Autowired
    protected TestRowLevelPolicies rowLevelPolicies;

    @Autowired
    protected SystemAuthenticator systemAuthenticator;

    /**
     * Declared by the shared test configuration as a {@code UserRepository}; adding and removing a user is
     * {@link InMemoryUserRepository}'s own API, hence {@link #users()}.
     */
    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RoleGrantedAuthorityUtils roleGrantedAuthorityUtils;

    @AfterEach
    void cleanup() {
        denyingConstraint.reset();
        rowLevelPolicies.reset();
        // By username rather than by an equal-looking User: what remove() matches on is the repository's own
        // business, and a user left behind would carry a row-level role into every later test of this class.
        users().getByUsernameLike(ROW_LEVEL_USER).forEach(users()::removeUser);
        jdbcTemplate.update("delete from GAME_TITLE where NAME like ?", OWN + "%");
        jdbcTemplate.update("delete from PUBLISHER where NAME like ?", OWN + "%");
    }

    @Test
    void testStoredQueryIsExecutedAndItsAliasesKeyTheBandRows() {
        publisher("Nintendo");
        publisher("Ubisoft");
        DataSet dataSet = llmDataSet(
                "select p.name as publisherName from Publisher p where p.name like :name order by p.name",
                List.of("publisherName"), text("name"));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of("name", OWN + "%"));

        assertThat(rows).containsExactly(
                Map.of("publisherName", OWN + "Nintendo"),
                Map.of("publisherName", OWN + "Ubisoft"));
    }


    @Test
    void testRowCountOfThePromptLimitsWhatTheBandGets() {
        // "The top 2 games" is a count the add-on's contract puts beside the query rather than inside it, JPQL
        // having no `limit`. A run that ignored it would print every row and say nothing.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        game("Rayman", new BigDecimal("7"), publisher);
        game("Doom", new BigDecimal("9"), publisher);
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select g.name as gameName from GameTitle g where g.name like :name order by g.price desc",
                List.of("gameName"), List.of(new LlmQueryParameter("name", "java.lang.String")),
                null, List.of(), 2, null));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of("name", OWN + "%"));

        assertThat(rows).extracting(row -> row.get("gameName"))
                .containsExactly(OWN + "Doom", OWN + "Rayman");
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
    void testGuardedConditionSwitchesOffWhenAnOptionalParameterIsEmpty() {
        // What the whole optional-parameter contract rests on: the guard the model is told to write really does
        // neutralise the condition once null is bound, against a real database.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        game("Destiny", new BigDecimal("25"), publisher);
        LlmDataQuery guarded = new LlmDataQuery(
                "select g.name as gameName from GameTitle g where g.name like :name "
                        + "and (:minPrice is null or g.price >= :minPrice) order by g.name",
                List.of("gameName"), List.of(new LlmQueryParameter("name", "java.lang.String"),
                new LlmQueryParameter("minPrice", "java.math.BigDecimal")),
                null, List.of());
        DataSet dataSet = llmDataSet(guarded);

        Map<String, Object> empty = new HashMap<>();
        empty.put("name", OWN + "%");
        empty.put("minPrice", null);
        List<Map<String, Object>> everything = loader().loadData(dataSet, null, empty);

        // Empty: the condition is off, so both rows come back.
        assertThat(everything).containsExactly(
                Map.of("gameName", OWN + "Destiny"), Map.of("gameName", OWN + "Tetris"));

        // Filled: the very same stored query narrows.
        List<Map<String, Object>> narrowed = loader().loadData(dataSet, null,
                Map.of("name", OWN + "%", "minPrice", new BigDecimal("10")));
        assertThat(narrowed).containsExactly(Map.of("gameName", OWN + "Destiny"));
    }

    @Test
    void testAnEmptyValueMatchedWithInFailsInsteadOfEmptyingTheBand() {
        // A collection parameter left empty reaches a run as null — Reporting puts every declared parameter into
        // the map — and an IN cannot match it, guard or no guard: (:names is null or … in :names) still matches
        // nothing, which measured here is why the run refuses such a value rather than binding it.
        publisher("Nintendo");
        publisher("Ubisoft");
        LlmDataQuery guarded = new LlmDataQuery(
                "select p.name as publisherName from Publisher p where p.name like :name "
                        + "and (:names is null or p.name in :names) order by p.name",
                List.of("publisherName"), List.of(new LlmQueryParameter("name", "java.lang.String"),
                new LlmQueryParameter("names", "java.lang.String")),
                null, List.of());
        DataSet dataSet = llmDataSet(guarded);

        Map<String, Object> params = new HashMap<>();
        params.put("name", OWN + "%");
        params.put("names", null);

        assertThatThrownBy(() -> loader().loadData(dataSet, null, params))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("names", "IN");
    }

    @Test
    void testCollectionIsBoundWholeAndMatchedWithIn() {
        // What a cross-tab cell query does with the values of its axes, and what a "list of entities" report
        // parameter does: one name, every value, matched with IN.
        publisher("Nintendo");
        publisher("Ubisoft");
        publisher("Activision");
        DataSet dataSet = llmDataSet(
                "select p.name as publisherName from Publisher p where p.name in :names order by p.name",
                List.of("publisherName"), text("names"));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null,
                Map.of("names", List.of(OWN + "Ubisoft", OWN + "Nintendo")));

        assertThat(rows).containsExactly(
                Map.of("publisherName", OWN + "Nintendo"),
                Map.of("publisherName", OWN + "Ubisoft"));
    }

    @Test
    void testEntityTheUserMayNotReadStopsTheRun() {
        // The platform does not act on a denied entity in a value load — DataStoreCrudValuesListener consumes
        // only the denied columns — so a user without READ would otherwise still get every attribute the query
        // selects. This type promises a run reads no more than its user may, so it checks that itself.
        publisher("Nintendo");
        denyingConstraint.denyEntity("Publisher");
        DataSet dataSet = llmDataSet(
                "select p.name as publisherName from Publisher p where p.name like :name",
                List.of("publisherName"), text("name"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testEntityTheUserMayReadIsNotStopped() {
        // The control: nothing is denied, so the same query runs.
        publisher("Nintendo");
        DataSet dataSet = llmDataSet(
                "select p.name as publisherName from Publisher p where p.name like :name",
                List.of("publisherName"), text("name"));

        assertThat(loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .containsExactly(Map.of("publisherName", OWN + "Nintendo"));
    }



    @Test
    void testDeniedEntityInsideASubqueryIsCaughtToo() {
        // The query graph reaches into subqueries, and so must the check: a denied entity read only there would
        // otherwise pass, the same way a denied root did while judging by the selected paths.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        denyingConstraint.denyEntity("GameTitle");
        DataSet dataSet = llmDataSet(
                "select p.name as publisherName from Publisher p where p.name like :name "
                        + "and exists (select g from GameTitle g where g.publisher = p)",
                List.of("publisherName"), text("name"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void testSelectingAnEntityValuedAssociationIsRefused() {
        // Measured before it was refused: this really did hand back a whole Publisher, and a denied `name` read
        // fine off it — masking is applied to the selected column, and the column here is the entity. No denial
        // is set up here, because the refusal comes before any constraint does: the path names a property rather
        // than an alias, which is what the alias rule alone did not catch.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        DataSet dataSet = llmDataSet(
                "select g.publisher as publisher from GameTitle g where g.name like :name",
                List.of("publisher"), text("name"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("selects the entities", "g.publisher", "Publisher");
    }

    @Test
    void testSelectingAnAttributeThroughAnAssociationIsAllowed() {
        // The control for the refusal above: the same association read one step further is a value, and a band
        // prints it.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        DataSet dataSet = llmDataSet(
                "select g.publisher.name as publisherName from GameTitle g where g.name like :name",
                List.of("publisherName"), text("name"));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of("name", OWN + "%"));

        assertThat(rows).extracting(row -> row.get("publisherName")).containsExactly(OWN + "Nintendo");
    }


    @Test
    void testSelectingAnEntityItselfIsRefusedBecauseMaskingCannotReachIt() {
        // Measured before it was refused: attribute permissions are applied by masking selected columns, so an
        // entity handed back whole carried the very attribute masking would have hidden — `select p` returned a
        // Publisher whose denied name was readable. A band prints values, so this is refused outright.
        publisher("Nintendo");
        denyingConstraint.denySelectedPath("name");
        DataSet dataSet = llmDataSet(
                "select p as publisher from Publisher p where p.id is not null",
                List.of("publisher"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("selects the entities", "Publisher");
    }

    @Test
    void testRowLevelPolicyOfAJoinedEntityNarrowsTheRows() {
        // The platform weaves a row-level policy into a value load for the query's own entity only, and a report
        // query joins as a matter of course, so the policy of a joined entity is this loader's to apply.
        Publisher allowed = publisher("Nintendo");
        Publisher hidden = publisher("Ubisoft");
        game("Tetris", new BigDecimal("5"), allowed);
        game("Rayman", new BigDecimal("7"), hidden);
        rowLevelPolicies.add("Publisher",
                new RowLevelPolicy("Publisher", "{E}.name = '" + OWN + "Nintendo'", null));
        DataSet dataSet = llmDataSet(
                "select g.name as gameName from GameTitle g join g.publisher p where g.name like :name",
                List.of("gameName"), text("name"));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of("name", OWN + "%"));

        assertThat(rows).extracting(row -> row.get("gameName")).containsExactly(OWN + "Tetris");
    }

    @Test
    void testPolicySortingAndRowCountHoldTogether() {
        // The ordinary shape of a report query — a join, a sort, and "the top few" — with a policy woven into it.
        // The three meet in one text: the condition has to land before the `order by` rather than after it, and
        // the count has to apply to what the policy left.
        Publisher visible = publisher("Nintendo");
        Publisher hidden = publisher("Ubisoft");
        game("Tetris", new BigDecimal("5"), visible);
        game("Doom", new BigDecimal("9"), visible);
        game("Rayman", new BigDecimal("7"), hidden);
        rowLevelPolicies.add("Publisher",
                new RowLevelPolicy("Publisher", "{E}.name = '" + OWN + "Nintendo'", null));
        DataSet dataSet = llmDataSet(new LlmDataQuery(
                "select g.name as gameName from GameTitle g join g.publisher p where g.name like :name "
                        + "order by g.price desc",
                List.of("gameName"), List.of(new LlmQueryParameter("name", "java.lang.String")),
                null, List.of(), 1, null));

        List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of("name", OWN + "%"));

        // Rayman is the most expensive of the three, and the policy hides it: the top row is Doom.
        assertThat(rows).extracting(row -> row.get("gameName")).containsExactly(OWN + "Doom");
    }

    @Test
    void testRowLevelPolicyOfARoleNarrowsTheRowsForTheUserItIsAssignedTo() {
        // The whole chain, not the loader's half of it: a role assigned to a real user, the policies it puts into
        // PolicyStore, and what a run of the report then returns. The other row-level tests here set the policies
        // up directly, which says nothing about a role reaching them.
        Publisher visible = publisher("Nintendo");
        Publisher hidden = publisher("Ubisoft");
        game("Tetris", new BigDecimal("5"), visible);
        game("Rayman", new BigDecimal("7"), hidden);
        users().addUser(User.builder()
                .username(ROW_LEVEL_USER)
                .password("{noop}")
                .authorities(List.of(
                        roleGrantedAuthorityUtils.createResourceRoleGrantedAuthority(FullAccessRole.NAME),
                        roleGrantedAuthorityUtils.createRowLevelRoleGrantedAuthority(TestPublisherRowLevelRole.CODE)))
                .build());
        DataSet dataSet = llmDataSet(
                "select g.name as gameName from GameTitle g join g.publisher p where g.name like :name",
                List.of("gameName"), text("name"));

        List<Map<String, Object>> rows = systemAuthenticator.withUser(ROW_LEVEL_USER,
                () -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")));

        assertThat(rows).extracting(row -> row.get("gameName")).containsExactly(OWN + "Tetris");
    }

    @Test
    void testWovenPolicyBindsItsOwnSessionParameter() {
        // A policy may filter by a session attribute — the usual shape in a multi-tenant application. Nothing
        // binds such a parameter here: the platform's own QueryParamValuesManager resolves `session_*` when the
        // query executes. Read off the platform first, then measured, because a woven condition whose parameter
        // nobody sets would fail every run.
        Publisher visible = publisher("Nintendo");
        Publisher hidden = publisher("Ubisoft");
        game("Tetris", new BigDecimal("5"), visible);
        game("Rayman", new BigDecimal("7"), hidden);
        rowLevelPolicies.add("Publisher",
                new RowLevelPolicy("Publisher", "{E}.name = :session_visiblePublisher", null));
        DataSet dataSet = llmDataSet(
                "select g.name as gameName from GameTitle g join g.publisher p where g.name like :name",
                List.of("gameName"), text("name"));

        ThreadLocalSessionData.setAttribute("visiblePublisher", OWN + "Nintendo");
        try {
            List<Map<String, Object>> rows = loader().loadData(dataSet, null, Map.of("name", OWN + "%"));

            assertThat(rows).extracting(row -> row.get("gameName")).containsExactly(OWN + "Tetris");
        } finally {
            ThreadLocalSessionData.clear();
        }
    }


    @Test
    void testEntityReadOnlyInsideASubqueryWithAPolicyIsRefused() {
        // Its alias is not in scope where the condition would have to go, so the rows it may show cannot be
        // narrowed — and running the query unfiltered would show the ones the policy exists to hide.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        rowLevelPolicies.add("Publisher",
                new RowLevelPolicy("Publisher", "{E}.name = '" + OWN + "Nintendo'", null));
        DataSet dataSet = llmDataSet(
                "select g.name as gameName from GameTitle g where g.name like :name and exists "
                        + "(select p from Publisher p where p = g.publisher)",
                List.of("gameName"), text("name"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Publisher", "row-level policies cannot be applied", "subquery");
    }


    @Test
    void testAQueryThatCannotRunIsSaidToBeSoBeforeItsPoliciesAreWeighed() {
        // Order matters between the two refusals: a text that is not a select is not a text to weave a policy
        // into, and "this is not a select" is what its author needs to hear first.
        publisher("Nintendo");
        rowLevelPolicies.add("Publisher", new RowLevelPolicy("Publisher", RowLevelPolicyAction.READ,
                (entity, context) -> true, Collections.emptyMap()));
        DataSet dataSet = llmDataSet(
                "delete from Publisher p where p.name like :name",
                List.of("publisherName"), text("name"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("is not a select");
    }

    @Test
    void testPredicateRowLevelPolicyOfTheQueryOwnEntityIsRefused() {
        // Not only a joined entity: the platform applies no predicate policy to a value load at all, the entity
        // the query selects from included, so leaving that one to the platform would run it unfiltered.
        publisher("Nintendo");
        publisher("Ubisoft");
        rowLevelPolicies.add("Publisher", new RowLevelPolicy("Publisher", RowLevelPolicyAction.READ,
                (entity, context) -> true, Collections.emptyMap()));
        DataSet dataSet = llmDataSet(
                "select p.name as publisherName from Publisher p where p.name like :name",
                List.of("publisherName"), text("name"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Publisher", "predicate row-level policy");
    }

    @Test
    void testRowLevelPolicyJoiningAnotherEntityIsRefusedForAJoinedEntity() {
        // Measured: the transformer re-bases an added join onto the query's root alias, so a policy joining from
        // `p` would come out joining from `g` — filtering another entity, or naming a path that does not exist.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        rowLevelPolicies.add("Publisher",
                new RowLevelPolicy("Publisher", "t.name like '" + OWN + "%'", "join {E}.titles t"));
        DataSet dataSet = llmDataSet(
                "select g.name as gameName from GameTitle g join g.publisher p where g.name like :name",
                List.of("gameName"), text("name"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Publisher", "joins another entity");
    }



    @Test
    void testPermissionIsAnsweredBeforeTheQueryIsJudgedAsAQuery() {
        // This query is faulty *and* forbidden: it selects the entity whole, which the barrier refuses naming the
        // entity, and the user may not read that entity either. The permission answers first — what the stored
        // text says is not something to tell a user who is refused its data.
        publisher("Nintendo");
        denyingConstraint.denyEntity("Publisher");
        DataSet dataSet = llmDataSet(
                "select p as publisher from Publisher p where p.name like :name",
                List.of("publisher"), text("name"));

        // AccessDeniedException, not the DataLoadingException the barrier would have raised.
        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .isInstanceOf(AccessDeniedException.class);
    }


    @Test
    void testQueryThePlatformParserCannotReadNeverRunsAtAll() {
        // Such a text is what the checks that parse it — entity READ, the row-level policies — step aside for,
        // so it matters that it cannot execute either: measured, `loadValues` fails on the very same parser
        // before reaching the database. There is no way past the checks through a query the parser dislikes.
        Publisher publisher = publisher("Nintendo");
        game("Tetris", new BigDecimal("5"), publisher);
        denyingConstraint.denyEntity("Publisher");
        DataSet dataSet = llmDataSet(
                "select CAST(p.name as CHAR) as publisherName from Publisher p where p.name like :name",
                List.of("publisherName"), text("name"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of("name", OWN + "%")))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContaining("failed");
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
        DataSet dataSet = llmDataSet(
                "select p.title as publisherTitle from Publisher p",
                List.of("publisherTitle"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("Data", "failed", "Generate it again");
    }

    @Test
    void testAQueryReachingTheDatabaseDirectlyIsNeverHandedToIt() {
        // The promise of this data set type is that a run reads through DataManager, under the permissions of
        // the current user; a native escape would step around that, so it does not reach the database at all.
        publisher("Nintendo");
        DataSet dataSet = llmDataSet(
                "select p.name as publisherName from Publisher p where sql('1 = 1')",
                List.of("publisherName"));

        assertThatThrownBy(() -> loader().loadData(dataSet, null, Map.of()))
                .isInstanceOf(DataLoadingException.class)
                .hasMessageContainingAll("sql", "not executed");
    }

    protected InMemoryUserRepository users() {
        return (InMemoryUserRepository) userRepository;
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

    /**
     * A data set carrying the given query, with the columns it names and the parameters it references.
     */
    protected DataSet llmDataSet(String jpql, List<String> columns, LlmQueryParameter... parameters) {
        return llmDataSet(new LlmDataQuery(jpql, columns, List.of(parameters), null, List.of()));
    }

    /**
     * The same, limited to a row count as a prompt asking for "the top few" would be.
     */
    protected DataSet llmDataSet(String jpql, List<String> columns, Integer maxResults,
                                 Integer firstResult, LlmQueryParameter... parameters) {
        return llmDataSet(new LlmDataQuery(jpql, columns, List.of(parameters), null, List.of(),
                maxResults, firstResult));
    }

    protected LlmQueryParameter text(String name) {
        return new LlmQueryParameter(name, "java.lang.String");
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
