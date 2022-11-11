/*
 * Copyright 2022 Haulmont.
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

package data_manager;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.DataTestConfiguration;
import test_support.TestContextInititalizer;
import test_support.entity.sales.Order;

import javax.persistence.TemporalType;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, DataTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
public class NullParamTest {

    private static final BigDecimal MAGIC_NUM = new BigDecimal("7482683");

    @Autowired
    UnconstrainedDataManager dataManager;

    @Autowired
    JdbcTemplate jdbc;

    private Order orderWithNulls;
    private Order orderWithoutNulls;

    @BeforeEach
    void setUp() {
        orderWithNulls = dataManager.create(Order.class);
        orderWithNulls.setAmount(MAGIC_NUM);
        dataManager.save(orderWithNulls);

        orderWithoutNulls = dataManager.create(Order.class);
        orderWithoutNulls.setAmount(MAGIC_NUM);
        orderWithoutNulls.setNumber("1");
        orderWithoutNulls.setDate(new Date());
        dataManager.save(orderWithoutNulls);
    }

    @AfterEach
    void tearDown() {
        jdbc.update("delete from SALES_ORDER");
    }

    @Test
    void testQuery() {
        // SELECT ... FROM SALES_ORDER WHERE (((AMOUNT = ?) AND (NUM IS NULL)) AND (DELETE_TS IS NULL))
        List<Order> orders = dataManager.load(Order.class)
                .query("select e from sales_Order e where e.amount = :amount and e.number = :number")
                .parameter("amount", MAGIC_NUM)
                .parameter("number", null)
                .list();
        assertEquals(1, orders.size());
        assertEquals(this.orderWithNulls, orders.get(0));

        // SELECT ... FROM SALES_ORDER WHERE (((AMOUNT = ?) AND (DATE_ IS NULL)) AND (DELETE_TS IS NULL))
        orders = dataManager.load(Order.class)
                .query("select e from sales_Order e where e.amount = :amount and e.date = :date")
                .parameter("amount", MAGIC_NUM)
                .parameter("date", null, TemporalType.DATE)
                .list();
        assertEquals(1, orders.size());
        assertEquals(this.orderWithNulls, orders.get(0));
    }

    @Test
    void testCondition() {
        // SELECT ... FROM SALES_ORDER WHERE ((AMOUNT = ?) AND (DELETE_TS IS NULL))
        List<Order> orders = dataManager.load(Order.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.createWithParameterName("amount", PropertyCondition.Operation.EQUAL, "amount"),
                        PropertyCondition.createWithParameterName("number", PropertyCondition.Operation.EQUAL, "number")))
                .parameter("amount", MAGIC_NUM)
                .parameter("number", null)
                .list();
        assertEquals(2, orders.size());

        // SELECT ... FROM SALES_ORDER WHERE ((AMOUNT = ?) AND (DELETE_TS IS NULL))
        orders = dataManager.load(Order.class)
                .condition(LogicalCondition.and(
                        PropertyCondition.createWithParameterName("amount", PropertyCondition.Operation.EQUAL, "amount"),
                        PropertyCondition.createWithParameterName("date", PropertyCondition.Operation.EQUAL, "date")))
                .parameter("amount", MAGIC_NUM)
                .parameter("date", null, TemporalType.DATE)
                .list();
        assertEquals(2, orders.size());
    }
}
