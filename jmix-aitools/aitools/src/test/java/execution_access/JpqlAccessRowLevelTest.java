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
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.SystemAuthenticator;
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
import test_support.entity.sales.Order;
import test_support.entity.sales.OrderShipment;
import test_support.role.SecCustomerJoinPolicyRole;
import test_support.role.SecCustomerReadRole;
import test_support.role.SecCustomerRowLevelRole;
import test_support.role.SecOrderLineNavigatingRowLevelRole;
import test_support.role.SecOrderLineRowLevelRole;
import test_support.role.SecOrderReadRole;
import test_support.role.SecOrderRowLevelRole;
import test_support.role.SecOrderShipmentRowLevelRole;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

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
    @Autowired
    UnconstrainedDataManager dataManager;
    @Autowired
    Metadata metadata;
    @Autowired
    SystemAuthenticator systemAuthenticator;

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

    void ship(String orderNumber, String trackingNumber) {
        systemAuthenticator.runWithSystem(() -> {
            Order order = dataManager.load(Order.class)
                    .query("select o from aitls_Order o where o.number = :number")
                    .parameter("number", orderNumber)
                    .one();
            OrderShipment shipment = metadata.create(OrderShipment.class);
            shipment.setTrackingNumber(trackingNumber);
            shipment.setOrder(order);
            dataManager.saveWithoutReload(shipment);
        });
    }

    String normalized(String jpql) {
        return jpql.replaceAll("\\s+", " ").replace("( ", "(").replace(" )", ")").trim().toLowerCase(Locale.ROOT);
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
    void execute_idPathInGroupBy_groupsHiddenAsEmpty() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.customer.id as customerId, count(o) as cnt "
                + "from aitls_Order o group by o.customer.id order by o.customer.id", "customerId", "cnt");

        List<Object> customerIds = column(result, "customerId");
        assertEquals(3, customerIds.size());
        assertTrue(customerIds.containsAll(Arrays.asList(null, 1L, 3L)), customerIds.toString());
        for (Object count : column(result, "cnt")) {
            assertEquals(1L, ((Number) count).longValue());
        }
    }

    @Test
    void execute_idPathSelectedWithGroupByReference_groupsByJoinedKey() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        // `group by o.customer` groups by the owner's foreign key; with `o.customer.id` moved to the added join's
        // key, the group by has to follow it, or the selected key is no longer a grouped expression.
        JpqlExecutionResult result = support.execute("select o.customer.id as customerId, count(o) as cnt "
                + "from aitls_Order o group by o.customer", "customerId", "cnt");

        List<Object> customerIds = column(result, "customerId");
        assertEquals(3, customerIds.size());
        assertTrue(customerIds.containsAll(Arrays.asList(null, 1L, 3L)), customerIds.toString());
        for (Object count : column(result, "cnt")) {
            assertEquals(1L, ((Number) count).longValue());
        }
    }

    @Test
    void applyAccessConstraints_leftJoin_narrowsInOnClause() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        String constrained = accessSupport.applyAccessConstraints(
                "select o.number as number from aitls_Order o left join o.customer c", List.of());

        String text = constrained.replaceAll("\\s+", " ");
        assertTrue(text.matches("(?i).*left join o\\.customer c on .*c\\.name <> 'HiddenCo'.*"), constrained);
        assertFalse(text.toLowerCase().contains(" where "), constrained);
    }

    @Test
    void applyAccessConstraints_idOfReference_becomesLeftJoinOfItsOwn() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        String constrained = accessSupport.applyAccessConstraints(
                "select o.number, o.customer.id from aitls_Order o", List.of());

        assertEquals("select o.number, aitlsjoin1.id from aitls_order o "
                + "left join o.customer aitlsjoin1 on aitlsjoin1.name <> 'hiddenco'", normalized(constrained));
    }

    @Test
    void execute_idPathInOnOfLeftJoinDeclaringItsOwner_refused() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number "
                + "from aitls_OrderLine l left join l.order o on o.customer.id = 1", "number");

        assertFalse(result.isExecuted());
        String error = String.valueOf(result.getExecutionError());
        assertTrue(error.contains("on clause"), error);
    }

    @Test
    void execute_idPathInOnOfInnerJoinDeclaringItsOwner_narrowsInWhere() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderRowLevelRole.CODE);
        String jpql = "select o.number as number from aitls_Order o "
                + "join aitls_OrderLine l on l.order.id = o.id order by o.number";

        String constrained = normalized(accessSupport.applyAccessConstraints(jpql, List.of()));
        JpqlExecutionResult result = support.execute(jpql, "number");

        // An inner join drops a row whose order is hidden either way: the path stays in the `on`, and the order is
        // narrowed in the `where` as a path prefix.
        assertTrue(constrained.contains("on l.order.id = o.id"), constrained);
        assertTrue(constrained.contains("l.order is null or"), constrained);
        assertFalse(constrained.contains("aitlsjoin"), constrained);
        assertEquals(List.of("ORD-HiddenCo", "ORD-VisibleCo"), column(result, "number"));
    }

    @Test
    void execute_idPathOfJoinedOwnerInWhere_executes() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number "
                + "from aitls_OrderLine l left join l.order o where o.customer.id = 1", "number");

        assertTrue(result.isExecuted(), () -> "not executed: " + result.getExecutionError());
    }

    @Test
    void execute_leftJoinToHiddenReference_keepsRowWithEmptySide() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number, c.name as name "
                + "from aitls_Order o left join o.customer c order by o.number", "number", "name");

        assertEquals(List.of("ORD-HiddenCo", "ORD-OtherCo", "ORD-VisibleCo"), column(result, "number"));
        assertEquals(Arrays.asList(null, "OtherCo", "VisibleCo"), column(result, "name"));
    }

    @Test
    void execute_leftJoinOnClause_doesNotProbeHiddenRecord() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult matchingHidden = support.execute("select o.number as number, c.name as name "
                + "from aitls_Order o left join o.customer c on c.name like 'H%' order by o.number", "number", "name");
        JpqlExecutionResult matchingNone = support.execute("select o.number as number, c.name as name "
                + "from aitls_Order o left join o.customer c on c.name like 'Z%' order by o.number", "number", "name");
        JpqlExecutionResult matchingVisible = support.execute("select o.number as number, c.name as name "
                + "from aitls_Order o left join o.customer c on c.name like 'V%' order by o.number", "number", "name");

        List<String> allOrders = List.of("ORD-HiddenCo", "ORD-OtherCo", "ORD-VisibleCo");
        // A hidden record matching the query's own `on` looks exactly like no record matching it.
        assertEquals(allOrders, column(matchingHidden, "number"));
        assertEquals(Arrays.asList(null, null, null), column(matchingHidden, "name"));
        assertEquals(allOrders, column(matchingNone, "number"));
        assertEquals(Arrays.asList(null, null, null), column(matchingNone, "name"));
        assertEquals(allOrders, column(matchingVisible, "number"));
        assertEquals(Arrays.asList(null, null, "VisibleCo"), column(matchingVisible, "name"));
    }

    @Test
    void execute_idOfHiddenReference_keepsRowWithEmptyId() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number, o.customer.id as customerId "
                + "from aitls_Order o order by o.number", "number", "customerId");

        assertEquals(List.of("ORD-HiddenCo", "ORD-OtherCo", "ORD-VisibleCo"), column(result, "number"));
        assertEquals(Arrays.asList(null, 3L, 1L), column(result, "customerId").stream()
                .map(value -> value == null ? null : ((Number) value).longValue()).toList());
    }

    @Test
    void execute_ownLeftJoinAndIdPathOverSameReference_bothKeepRowWithEmptySide() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number, c.name as name, "
                        + "o.customer.id as customerId from aitls_Order o left join o.customer c order by o.number",
                "number", "name", "customerId");

        assertEquals(List.of("ORD-HiddenCo", "ORD-OtherCo", "ORD-VisibleCo"), column(result, "number"));
        assertEquals(Arrays.asList(null, "OtherCo", "VisibleCo"), column(result, "name"));
        assertEquals(Arrays.asList(null, 3L, 1L), column(result, "customerId").stream()
                .map(value -> value == null ? null : ((Number) value).longValue()).toList());
    }

    @Test
    void execute_leftJoinTargetAlsoInSubquery_refused() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number from aitls_Order o "
                + "left join o.customer c "
                + "where exists (select c2.id from aitls_Customer c2 where c2.name = 'X')", "number");

        assertFalse(result.isExecuted());
        String error = String.valueOf(result.getExecutionError());
        assertTrue(error.contains("aitls_Customer"), error);
    }

    @Test
    void execute_idOfHiddenReferenceInWhere_doesNotMatchHiddenRecord() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult hidden = support.execute(
                "select o.number as number from aitls_Order o where o.customer.id = 2", "number");
        JpqlExecutionResult visible = support.execute(
                "select o.number as number from aitls_Order o where o.customer.id = 1", "number");

        assertEquals(List.of(), column(hidden, "number"));
        assertEquals(List.of("ORD-VisibleCo"), column(visible, "number"));
    }

    @Test
    void execute_idAndAttributeOfSameReference_attributeKeepsPathSemantics() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number, o.customer.id as customerId, "
                + "o.customer.name as name from aitls_Order o order by o.number", "number", "customerId", "name");

        // `o.customer.name` is a path: a row without a visible customer goes, as for an inner join.
        assertEquals(List.of("ORD-OtherCo", "ORD-VisibleCo"), column(result, "number"));
    }

    @Test
    void execute_leftJoinedInverseReference_keepsRowsWithoutVisibleRecord() {
        ship("ORD-VisibleCo", "HIDDEN");
        ship("ORD-OtherCo", "TRK-1");
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderShipmentRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number, s.trackingNumber as tracking "
                + "from aitls_Order o left join o.shipment s order by o.number", "number", "tracking");

        assertEquals(List.of("ORD-HiddenCo", "ORD-OtherCo", "ORD-VisibleCo"), column(result, "number"));
        assertEquals(Arrays.asList(null, "TRK-1", null), column(result, "tracking"));
    }

    @Test
    void execute_leftJoinedCollectionAllHidden_keepsOneRowPerOwner() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderLineRowLevelRole.CODE);

        JpqlExecutionResult rows = support.execute(
                "select o.number as number from aitls_Order o left join o.lines l order by o.number", "number");
        JpqlExecutionResult counts = support.execute("select o.number as number, count(l) as cnt "
                + "from aitls_Order o left join o.lines l group by o.number order by o.number", "number", "cnt");

        assertEquals(List.of("ORD-HiddenCo", "ORD-OtherCo", "ORD-VisibleCo"), column(rows, "number"));
        assertEquals(List.of(0L, 0L, 0L), column(counts, "cnt").stream()
                .map(value -> ((Number) value).longValue()).toList());
    }

    @Test
    void execute_leftEntityJoin_keepsRowWithEmptySide() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select o.number as number, c.name as name "
                + "from aitls_Order o left join aitls_Customer c on c = o.customer order by o.number", "number", "name");

        assertEquals(List.of("ORD-HiddenCo", "ORD-OtherCo", "ORD-VisibleCo"), column(result, "number"));
        assertEquals(Arrays.asList(null, "OtherCo", "VisibleCo"), column(result, "name"));
    }

    @Test
    void execute_aggregateOverLeftJoin_countsVisibleRows() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecCustomerRowLevelRole.CODE);

        JpqlExecutionResult result = support.execute("select count(o) as orders, count(c) as customers "
                + "from aitls_Order o left join o.customer c", "orders", "customers");

        assertTrue(result.isExecuted(), () -> "not executed: " + result.getExecutionError());
        assertEquals(3L, ((Number) result.getRows().get(0).get("orders")).longValue());
        assertEquals(2L, ((Number) result.getRows().get(0).get("customers")).longValue());
    }

    @Test
    void execute_leftJoinWithNavigatingPolicy_wrapsIntoSubquery() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderLineNavigatingRowLevelRole.CODE);
        String jpql = "select o.number as number, l.quantity as quantity "
                + "from aitls_Order o left join o.lines l order by o.number";

        String constrained = accessSupport.applyAccessConstraints(jpql, List.of());
        JpqlExecutionResult result = support.execute(jpql, "number", "quantity");

        assertTrue(constrained.toLowerCase().indexOf("select", 1) > 0, constrained); // a subquery was added
        assertEquals(List.of("ORD-HiddenCo", "ORD-OtherCo", "ORD-VisibleCo"), column(result, "number"));
        assertEquals(Arrays.asList(null, 2, 2), column(result, "quantity").stream()
                .map(value -> value == null ? null : ((Number) value).intValue()).toList());
    }

    @Test
    void applyAccessConstraints_leftJoinWithSimplePolicy_addsNoSubquery() {
        support.loginAs(SecOrderReadRole.CODE, SecCustomerReadRole.CODE, SecOrderLineRowLevelRole.CODE);

        String constrained = accessSupport.applyAccessConstraints(
                "select o.number as number from aitls_Order o left join o.lines l", List.of());

        assertEquals(-1, constrained.toLowerCase().indexOf("select", 1), constrained);
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
