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

import io.jmix.aitools.dataload.execution.JpqlAccessConstraintException;
import io.jmix.aitools.dataload.execution.JpqlLeftJoinSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.SecuredDataLoadTestConfiguration;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecuredDataLoadTestConfiguration.class)
@TestPropertySource(properties = {"eclipselink.ddl-generation=create-tables", "jmix.core.work-dir=build/test-home/work"})
class JpqlLeftJoinSupportTest {

    @Autowired
    JpqlLeftJoinSupport leftJoinSupport;

    String normalized(String jpql) {
        return jpql.replaceAll("\\s+", " ").replace("( ", "(").replace(" )", ")").trim().toLowerCase(Locale.ROOT);
    }

    @Test
    void addOnCondition_joinWithoutOn_addsOn() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number from aitls_Order o left join o.customer c where o.number is not null");

        rewrite.addOnCondition("c", "c.name <> 'HiddenCo'");

        assertEquals("select o.number from aitls_order o left join o.customer c on c.name <> 'hiddenco' "
                + "where o.number is not null", normalized(rewrite.getResult()));
    }

    @Test
    void addOnCondition_joinWithOn_andsBothConditions() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number from aitls_Order o left join o.customer c on c.name like 'H%'");

        rewrite.addOnCondition("c", "c.name <> 'HiddenCo'");
        rewrite.addOnCondition("c", "c.id > 0");

        assertEquals("select o.number from aitls_order o left join o.customer c "
                        + "on ((c.name like 'h%') and (c.name <> 'hiddenco')) and (c.id > 0)",
                normalized(rewrite.getResult()));
    }

    @Test
    void addOnCondition_joinWithOrInOn_keepsDisjunctionGrouped() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number from aitls_Order o left join o.customer c on c.name like 'A%' or c.name like 'B%'");

        rewrite.addOnCondition("c", "c.name <> 'HiddenCo'");

        // Without the parentheses, the first disjunct would let a hidden record through.
        assertEquals("select o.number from aitls_order o left join o.customer c "
                        + "on (c.name like 'a%' or c.name like 'b%') and (c.name <> 'hiddenco')",
                normalized(rewrite.getResult()));
    }

    @Test
    void addOnCondition_joinWithParameterInOn_keepsParameter() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number from aitls_Order o left join o.customer c on c.name = :name");

        rewrite.addOnCondition("c", "c.id <> :current_user_id");

        assertEquals("select o.number from aitls_order o left join o.customer c "
                + "on (c.name = :name) and (c.id <> :current_user_id)", normalized(rewrite.getResult()));
    }

    @Test
    void addOnCondition_subqueriesInBothConditions_keptAsTheyAre() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite("select o.number from aitls_Order o "
                + "left join o.customer c on c.id in (select c2.id from aitls_Customer c2 where c2.name like 'A%')");

        rewrite.addOnCondition("c", "c.id in (select c3.id from aitls_Customer c3 where c3.name <> 'HiddenCo')");

        assertEquals("select o.number from aitls_order o left join o.customer c "
                        + "on (c.id in (select c2.id from aitls_customer c2 where c2.name like 'a%')) "
                        + "and (c.id in (select c3.id from aitls_customer c3 where c3.name <> 'hiddenco'))",
                normalized(rewrite.getResult()));
    }

    @Test
    void addOnCondition_treatJoin_addsOn() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite("select d.title from aitls_Document d "
                + "left outer join treat(d.parent as aitls_SecretDocument) p");

        rewrite.addOnCondition("p", "p.title <> 'X'");

        assertEquals("select d.title from aitls_document d "
                        + "left outer join treat(d.parent as aitls_secretdocument) p on p.title <> 'x'",
                normalized(rewrite.getResult()));
    }

    @Test
    void addOnCondition_entityJoin_andsIntoItsOn() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number from aitls_Order o left join aitls_Customer c on c = o.customer");

        rewrite.addOnCondition("c", "c.name <> 'HiddenCo'");

        assertEquals("select o.number from aitls_order o left join aitls_customer c "
                + "on (c = o.customer) and (c.name <> 'hiddenco')", normalized(rewrite.getResult()));
    }

    @Test
    void addOnCondition_collectionJoin_addsOn() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number, count(l) from aitls_Order o left join o.lines l group by o.number");

        rewrite.addOnCondition("l", "l.quantity <> 2");

        assertEquals("select o.number, count(l) from aitls_order o left join o.lines l on l.quantity <> 2 "
                + "group by o.number", normalized(rewrite.getResult()));
    }

    @Test
    void joinIdPath_rewritesPathsInEveryClause() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number, o.customer.id from aitls_Order o where o.customer.id <> 5 order by o.customer.id");

        String alias = rewrite.joinIdPath("o.customer", "id").variable();
        rewrite.addOnCondition(alias, alias + ".name <> 'HiddenCo'");

        String a = alias.toLowerCase(Locale.ROOT);
        assertEquals("select o.number, " + a + ".id from aitls_order o left join o.customer " + a
                + " on " + a + ".name <> 'hiddenco' where " + a + ".id <> 5 order by " + a + ".id",
                normalized(rewrite.getResult()));
    }

    @Test
    void joinIdPath_groupByReference_becomesJoinedKey() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite("select o.customer.id, count(o) "
                + "from aitls_Order o where o.customer is not null group by o.customer order by o.customer");

        String alias = rewrite.joinIdPath("o.customer", "id").variable();

        String a = alias.toLowerCase(Locale.ROOT);
        assertEquals("select " + a + ".id, count(o) from aitls_order o left join o.customer " + a
                + " where o.customer is not null group by " + a + ".id order by o.customer",
                normalized(rewrite.getResult()));
    }

    @Test
    void joinIdPath_leavesOtherPathsAndOwnJoinAlone() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.customer.id, o.customer.name, c.id from aitls_Order o left join o.customer c");

        String alias = rewrite.joinIdPath("o.customer", "id").variable();

        String a = alias.toLowerCase(Locale.ROOT);
        assertEquals("select " + a + ".id, o.customer.name, c.id from aitls_order o left join o.customer " + a
                + " left join o.customer c", normalized(rewrite.getResult()));
    }

    @Test
    void joinIdPath_ownerIsFromVariable_newJoinPrecedesEarlierJoinReadingTheAlias() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number from aitls_Order o join aitls_Customer c on c.id = o.customer.id");

        String alias = rewrite.joinIdPath("o.customer", "id").variable();

        String a = alias.toLowerCase(Locale.ROOT);
        assertEquals("select o.number from aitls_order o left join o.customer " + a
                + " join aitls_customer c on c.id = " + a + ".id", normalized(rewrite.getResult()));
    }

    @Test
    void joinIdPath_ownerIsJoinVariable_newJoinRightAfterOwnerJoin() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select p.number from aitls_OrderLine l join l.order p "
                        + "join aitls_Customer c on c.id = p.customer.id");

        String alias = rewrite.joinIdPath("p.customer", "id").variable();

        String a = alias.toLowerCase(Locale.ROOT);
        assertEquals("select p.number from aitls_orderline l join l.order p left join p.customer " + a
                + " join aitls_customer c on c.id = " + a + ".id", normalized(rewrite.getResult()));
    }

    @Test
    void joinIdPath_pathInOnOfLeftJoinDeclaringOwner_refusedAndLeavesQueryUntouched() {
        String jpql = "select o.number from aitls_Order o left join o.lines l on l.order.id = :p";
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(jpql);

        JpqlAccessConstraintException e = assertThrows(JpqlAccessConstraintException.class,
                () -> rewrite.joinIdPath("l.order", "id"));

        assertTrue(e.getMessage().contains("on clause"), e.getMessage());
        assertEquals(leftJoinSupport.rewrite(jpql).getResult(), rewrite.getResult());
    }

    @Test
    void joinIdPath_pathOnlyInOnOfInnerJoinDeclaringOwner_leftInPlaceWithoutJoin() {
        String jpql = "select o.number from aitls_Order o join aitls_OrderLine l on l.order.id = o.id";
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(jpql);

        JpqlLeftJoinSupport.Rewrite.JoinedIdPath joined = rewrite.joinIdPath("l.order", "id");

        assertNull(joined.variable());
        assertTrue(joined.leftInInnerJoinOn());
        assertEquals(leftJoinSupport.rewrite(jpql).getResult(), rewrite.getResult());
    }

    @Test
    void joinIdPath_pathInOnOfInnerJoinDeclaringOwner_rewritesOnlyOtherOccurrences() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select l.order.id from aitls_Order o join aitls_OrderLine l on l.order.id = o.id");

        JpqlLeftJoinSupport.Rewrite.JoinedIdPath joined = rewrite.joinIdPath("l.order", "id");

        assertTrue(joined.leftInInnerJoinOn());
        String a = String.valueOf(joined.variable()).toLowerCase(Locale.ROOT);
        assertEquals("select " + a + ".id from aitls_order o join aitls_orderline l on l.order.id = o.id "
                + "left join l.order " + a, normalized(rewrite.getResult()));
    }

    @Test
    void joinIdPath_notAReferenceOfVariable_throws() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number, o.customer.id from aitls_Order o");

        assertThrows(IllegalArgumentException.class, () -> rewrite.joinIdPath("customer", "id"));
        assertThrows(IllegalArgumentException.class, () -> rewrite.joinIdPath("o.customer.parent", "id"));
    }

    @Test
    void joinIdPath_noSuchPath_throwsAndLeavesQueryUntouched() {
        String jpql = "select o.number, o.customer.name from aitls_Order o";
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(jpql);

        assertThrows(IllegalStateException.class, () -> rewrite.joinIdPath("o.customer", "id"));

        assertEquals(leftJoinSupport.rewrite(jpql).getResult(), rewrite.getResult());
    }

    @Test
    void newVariable_avoidsDeclaredNamesAndRepeats() {
        JpqlLeftJoinSupport.Rewrite rewrite = leftJoinSupport.rewrite(
                "select o.number from aitls_Order o left join o.customer c");

        String first = rewrite.newVariable();
        String second = rewrite.newVariable();

        assertNotEquals(first, second);
        assertTrue(!first.equalsIgnoreCase("o") && !first.equalsIgnoreCase("c"));
    }
}
