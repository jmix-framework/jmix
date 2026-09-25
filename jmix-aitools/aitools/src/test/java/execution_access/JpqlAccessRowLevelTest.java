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

package execution_access;

import io.jmix.aitools.dataload.execution.JpqlAccessSupport;
import io.jmix.aitools.dataload.execution.JpqlAccessConstraintException;
import io.jmix.aitools.dataload.execution.JpqlExecutionParameter;
import io.jmix.aitools.dataload.execution.JpqlExecutionResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.SecuredDataLoadTestConfiguration;
import test_support.SecuredDataLoadTestSupport;
import test_support.role.SecCustomerJoinPolicyRole;
import test_support.role.SecCustomerReadRole;
import test_support.role.SecCustomerRowLevelRole;
import test_support.role.SecOrderLineRowLevelRole;
import test_support.role.SecOrderReadRole;
import test_support.role.SecOrderRowLevelRole;
import test_support.role.SecOrderShipmentRowLevelRole;

import java.util.List;

import static io.jmix.aitools.dataload.validation.validator.ParametersValidator.PARAMETER_RESERVED_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecuredDataLoadTestConfiguration.class)
@TestPropertySource(properties = {"eclipselink.ddl-generation=create-tables", "jmix.core.work-dir=build/test-home/work"})
class JpqlAccessRowLevelTest {

    @Autowired
    SecuredDataLoadTestSupport support;
    @Autowired
    JpqlAccessSupport accessSupport;

    @BeforeEach
    void setUp() {
        support.createData();
    }

    @AfterEach
    void tearDown() {
        support.deleteData();
    }

    void assertCannotBeDetermined(JpqlExecutionResult result) {
        assertFalse(result.isExecuted());
        String error = String.valueOf(result.getExecutionError());
        assertTrue(error.contains("cannot be determined"), error);
    }

    void assertDeclaredMoreThanOnce(JpqlExecutionResult result, String variable) {
        assertFalse(result.isExecuted());
        String error = String.valueOf(result.getExecutionError());
        assertTrue(error.contains("Variable " + variable + " is declared more than once"), error);
    }

    void assertRefusedOver(JpqlExecutionResult result, String entityName) {
        assertFalse(result.isExecuted());
        String error = String.valueOf(result.getExecutionError());
        assertTrue(error.contains("cannot be applied") && error.contains(entityName), error);
    }

    List<Object> column(JpqlExecutionResult result, String property) {
        assertTrue(result.isExecuted(), () -> "not executed: " + result.getExecutionError());
        return result.getRows().stream().map(row -> row.get(property)).toList();
    }

    @Test
    void execute_joinedEntityPolicy_hidesRows() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select o.number as number from aitls_Order o join o.customer c order by o.number", "number");

        assertEquals(List.of("ORD-OtherCo", "ORD-VisibleCo"), column(result, "number"));
    }

    @Test
    void execute_pathReachedEntityPolicy_hidesRows() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select o.customer.name as name from aitls_Order o order by o.customer.name", "name");

        assertEquals(List.of("OtherCo", "VisibleCo"), column(result, "name"));
    }

    @Test
    void execute_aliasAndPathToSameEntity_hidesRows() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select c.name as name from aitls_Order o join o.customer c where o.customer.name like '%Co' "
                        + "order by c.name", "name");

        assertEquals(List.of("OtherCo", "VisibleCo"), column(result, "name"));
    }

    @Test
    void execute_rootPolicy_stillAppliedByPlatform() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select o.number as number from aitls_Order o order by o.number", "number");

        assertEquals(List.of("ORD-HiddenCo", "ORD-VisibleCo"), column(result, "number"));
    }

    @Test
    void execute_rootEntityReachedByPathWithPolicy_hidesRows() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select l.order.number as number from aitls_OrderLine l order by l.order.number", "number");

        assertEquals(List.of("ORD-HiddenCo", "ORD-VisibleCo"), column(result, "number"));
    }

    @Test
    void execute_policiedEntityInSubquery_refused() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number from aitls_Order o "
                + "where exists (select c.id from aitls_Customer c where c = o.customer)", "number");

        assertFalse(result.isExecuted());
        assertTrue(result.getRows().isEmpty());
        String error = String.valueOf(result.getExecutionError());
        assertTrue(error.contains("aitls_Customer"), error);
    }

    @Test
    void execute_entityUnderTwoAliases_narrowsBoth() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderRowLevelRole.CODE);

        JpqlExecutionResult pairs = support.execute(
                "select o1.number as number from aitls_Order o1, aitls_Order o2 where o1.customer = o2.customer "
                        + "order by o1.number", "number");
        JpqlExecutionResult count = support.execute(
                "select count(o2) as cnt from aitls_Order o1, aitls_Order o2", "cnt");

        assertEquals(List.of("ORD-HiddenCo", "ORD-VisibleCo"), column(pairs, "number"));
        // Two visible orders on each side, not two times three.
        assertEquals(4L, ((Number) column(count, "cnt").get(0)).longValue());
    }

    @Test
    void execute_policiedEntityOnlyAsBareVariableInSubquery_refused() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select c.name as name from aitls_Customer c "
                + "where (select count(c2) from aitls_Customer c2) > 0", "name");

        assertRefusedOver(result, "aitls_Customer");
    }

    @Test
    void execute_collectionReadBesideItsAlias_refused() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderLineRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select o.number as number from aitls_Order o join o.lines l where size(o.lines) > 0", "number");

        assertRefusedOver(result, "aitls_OrderLine");
    }

    @Test
    void execute_collectionJoinedWithAlias_hidesRows() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderLineRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select o.number as number from aitls_Order o join o.lines l", "number");

        assertEquals(List.of(), column(result, "number"));
    }

    @Test
    void execute_subqueryVariableShadowingOuterOne_notExecuted() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select c.name as name from aitls_Customer c "
                + "where exists (select 1 from aitls_Customer c where c.name = 'HiddenCo')", "name");

        assertDeclaredMoreThanOnce(result, "c");
    }

    @Test
    void execute_sameVariableInSiblingSubqueries_notExecuted() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        // Validation lets this through (both `x` range over Order), but the parser resolves `x` to its first
        // declaration wherever it is used, so the second subquery's paths cannot be trusted: refused, by name.
        JpqlExecutionResult result = support.execute("select c.name as name from aitls_Customer c "
                + "where exists (select 1 from aitls_Order x where x.customer = c) "
                + "and exists (select 1 from aitls_Order x where x.number is not null)", "name");

        assertDeclaredMoreThanOnce(result, "x");
    }

    @Test
    void execute_subqueryDeclaringFromOuterPath_refused() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderLineRowLevelRole.CODE);

        // The grammar keeps `from o.lines l` as bare tokens, so its variable is not a declaration in the tree.
        JpqlExecutionResult result = support.execute("select o.number as number from aitls_Order o "
                + "where exists (select l.id from o.lines l where l.quantity > 1)", "number");

        assertCannotBeDetermined(result);
    }

    @Test
    void execute_entityJoinWithOnClause_hidesRows() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select c.name as name from aitls_Order o "
                + "join aitls_Customer c on c.id = o.customer.id order by c.name", "name");

        assertEquals(List.of("OtherCo", "VisibleCo"), column(result, "name"));
    }

    @Test
    void applyAccessConstraints_leftJoinedOwningReference_letsAbsentRecordThroughByForeignKey() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        String constrained = accessSupport.applyAccessConstraints(
                "select o.number as number from aitls_Order o left join o.customer c", List.of());

        assertTrue(constrained.contains("(o.customer is null or (c.name <> 'HiddenCo'))"), constrained);
    }

    @Test
    void execute_leftJoinOnClause_doesNotProbeHiddenRecord() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult matching = support.execute("select o.number as number from aitls_Order o "
                + "left join o.customer c on c.name like 'H%' order by o.number", "number");
        JpqlExecutionResult notMatching = support.execute("select o.number as number from aitls_Order o "
                + "left join o.customer c on c.name like 'Z%' order by o.number", "number");

        // Whether the hidden customer matches the `on` clause must not show in the rows. A row whose `on` clause
        // found no record refers to a customer all the same, so the condition drops it: such a query returns
        // only the rows whose `on` clause matched a visible customer, here none.
        assertEquals(List.of(), column(matching, "number"));
        assertEquals(List.of(), column(notMatching, "number"));
    }

    @Test
    void execute_leftJoinedInverseReference_narrowsWithoutNullCheck() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderShipmentRowLevelRole.CODE);

        // The shipment refers to the order, so an order without one cannot be told from one whose shipment is
        // hidden: the condition narrows the row either way.
        JpqlExecutionResult result = support.execute(
                "select o.number as number from aitls_Order o left join o.shipment s order by o.number", "number");

        assertEquals(List.of(), column(result, "number"));
    }

    @Test
    void execute_leftJoinedCollectionAllHidden_hidesRows() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderLineRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select o.number as number from aitls_Order o left join o.lines l", "number");

        assertEquals(List.of(), column(result, "number"));
    }

    @Test
    void execute_reservedParameterPassed_notExecuted() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select c.name as name from aitls_Order o join o.customer c where o.number <> :current_user_username",
                List.of(new JpqlExecutionParameter("current_user_username", "String", "admin")), "name");

        assertFalse(result.isExecuted());
        assertTrue(result.getValidationResult().getIssues().stream()
                .anyMatch(issue -> PARAMETER_RESERVED_CODE.equals(issue.getCode())));
        assertThrows(JpqlAccessConstraintException.class, () -> accessSupport.applyAccessConstraints(
                "select c.name as name from aitls_Order o join o.customer c where o.number <> :current_user_username",
                List.of("current_user_username")));
    }

    @Test
    void execute_unpoliciedEntityInSubquery_executes() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select c.name as name from aitls_Customer c "
                + "where exists (select o.id from aitls_Order o where o.customer = c) order by c.name", "name");

        assertEquals(List.of("OtherCo", "VisibleCo"), column(result, "name"));
    }

    @Test
    void execute_joinedEntityPolicyWithJoinClause_refused() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerJoinPolicyRole.CODE);

        JpqlExecutionResult result = support.execute(
                "select c.name as name from aitls_Order o join o.customer c", "name");

        assertFalse(result.isExecuted());
    }

    @Test
    void execute_rootPolicyWithJoinClause_executes() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerJoinPolicyRole.CODE);

        JpqlExecutionResult result = support.execute("select c.name as name from aitls_Customer c order by c.name", "name");

        assertEquals(List.of("HiddenCo", "OtherCo", "VisibleCo"), column(result, "name"));
    }

    @Test
    void applyAccessConstraints_noPolicies_returnsTextUnchanged() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE);

        String jpql = "select c.name as name from aitls_Order o join o.customer c";
        assertEquals(jpql, accessSupport.applyAccessConstraints(jpql, List.of()));
    }

    @Test
    void execute_wovenCondition_notReturnedToCaller() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);
        String jpql = "select o.number as number from aitls_Order o join o.customer c";

        JpqlExecutionResult result = support.execute(jpql, "number");

        assertEquals(jpql, result.getGeneratedJpqlResult().getJpql());
    }
}
