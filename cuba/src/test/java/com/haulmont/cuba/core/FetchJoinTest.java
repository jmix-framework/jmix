/*
 * Copyright (c) 2008-2016 Haulmont.
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

package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.model.fetchjoin.*;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.FetchMode;
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@CoreTest
public class FetchJoinTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    protected JoinC joinC;
    protected JoinD joinD;
    protected JoinE joinE;
    protected JoinF joinF;
    protected JoinB joinB;
    protected JoinA joinA;

    protected Party partyCustomer, partyPerson;
    protected Customer customer;
    protected SalesPerson salesPerson;
    protected Product product;
    protected Order order;
    protected OrderLine orderLine;

    protected JoinUser user;
    protected JoinType type;
    protected JoinClassType classType;

    @BeforeEach
    public void setUp() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            joinF = metadata.create(JoinF.class);
            joinF.setName("joinF");
            em.persist(joinF);

            joinD = metadata.create(JoinD.class);
            joinD.setName("joinD");
            em.persist(joinD);

            joinE = metadata.create(JoinE.class);
            joinE.setName("joinE");
            joinE.setF(joinF);
            em.persist(joinE);

            joinC = metadata.create(JoinC.class);
            joinC.setName("joinC");
            joinC.setD(joinD);
            joinC.setE(joinE);
            em.persist(joinC);

            joinB = metadata.create(JoinB.class);
            joinB.setName("joinB");
            joinB.setC(joinC);
            em.persist(joinB);

            joinA = metadata.create(JoinA.class);
            joinA.setName("joinA");
            joinA.setB(joinB);
            em.persist(joinA);

            product = metadata.create(Product.class);
            product.setName("Product");
            em.persist(product);

            partyCustomer = metadata.create(Party.class);
            partyCustomer.setName("Customer");
            em.persist(partyCustomer);

            partyPerson = metadata.create(Party.class);
            partyPerson.setName("Person");
            em.persist(partyPerson);

            customer = metadata.create(Customer.class);
            customer.setParty(partyCustomer);
            customer.setCustomerNumber(1);
            em.persist(customer);

            salesPerson = metadata.create(SalesPerson.class);
            salesPerson.setParty(partyPerson);
            salesPerson.setSalespersonNumber(1);
            em.persist(salesPerson);

            order = metadata.create(Order.class);
            order.setOrderNumber(1);
            order.setOrderAmount(BigDecimal.ONE);
            order.setCustomer(customer);
            order.setSalesPerson(salesPerson);
            em.persist(order);

            orderLine = metadata.create(OrderLine.class);
            orderLine.setOrder(order);
            orderLine.setProduct(product);
            orderLine.setQuantity(1);
            em.persist(orderLine);

            classType = metadata.create(JoinClassType.class);
            classType.setName("classType");
            em.persist(classType);

            type = metadata.create(JoinType.class);
            type.setName("type");
            type.setClassType(classType);
            em.persist(type);

            user = metadata.create(JoinUser.class);
            user.setName("user");
            user.setType(type);
            em.persist(user);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord(joinA, joinB, joinC, joinD, joinE, joinF);
        testSupport.deleteRecord(orderLine, order, product, salesPerson, customer, partyCustomer, partyPerson);
        testSupport.deleteRecord(classType, type, user);
    }

    @Test
    public void testNotLoadingJoinB() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            FetchPlan fView = new View(JoinF.class).addProperty("name");
            FetchPlan eView = new View(JoinE.class).addProperty("name").addProperty("f", fView, FetchMode.JOIN);
            FetchPlan dView = new View(JoinD.class).addProperty("name");
            FetchPlan cView = new View(JoinC.class).addProperty("name")
                    .addProperty("d", dView, FetchMode.JOIN)
                    .addProperty("e", eView, FetchMode.JOIN);
            FetchPlan bView = new View(JoinB.class).addProperty("name")
                    .addProperty("c", cView, FetchMode.JOIN);
            FetchPlan aView = new View(JoinA.class).addProperty("name")
                    .addProperty("b", bView, FetchMode.JOIN);

            JoinA loadedA = em.find(JoinA.class, joinA.getId(), aView);
            assertNotNull(loadedA);
            assertNotNull(loadedA.getB().getC().getD());
            assertNotNull(loadedA.getB().getC().getE());
            assertNotNull(loadedA.getB().getC().getE().getF());
            tx.commit();
        }
    }

    @Test
    public void testNotLoadingCustomer() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            FetchPlan partyView = new View(Party.class).addProperty("name");
            FetchPlan productView = new View(Product.class).addProperty("name");
            FetchPlan customerView = new View(Customer.class)
                    .addProperty("customerNumber")
                    .addProperty("party", partyView);
            FetchPlan salesPersonView = new View(SalesPerson.class)
                    .addProperty("salespersonNumber")
                    .addProperty("party", partyView);
            FetchPlan orderView = new View(Order.class)
                    .addProperty("orderNumber")
                    .addProperty("customer", customerView)
                    .addProperty("salesPerson", salesPersonView);
            FetchPlan orderLineView = new View(OrderLine.class)
                    .addProperty("order", orderView)
                    .addProperty("product", productView);

            OrderLine reloadedOrderLine = em.find(OrderLine.class, orderLine.getId(), orderLineView);
            assertNotNull(reloadedOrderLine);
            assertNotNull(reloadedOrderLine.getOrder().getCustomer());
            assertEquals(partyCustomer, reloadedOrderLine.getOrder().getCustomer().getParty());
            assertNotNull(reloadedOrderLine.getOrder().getSalesPerson());
            assertEquals(partyPerson, reloadedOrderLine.getOrder().getSalesPerson().getParty());
            tx.commit();
        }
    }

    @Test
    public void testLoadingJoinedInheritance() throws Exception {
        FetchPlan typeLocalView = new View(JoinType.class).addProperty("name");
        FetchPlan classTypeView = new View(JoinClassType.class)
                .addProperty("name")
                .addProperty("types", typeLocalView);
        FetchPlan typeView = new View(JoinType.class)
                .addProperty("name")
                .addProperty("classType", classTypeView);
        FetchPlan userView = new View(JoinUser.class)
                .addProperty("name")
                .addProperty("type", typeView);

        DataManager dataManager = AppBeans.get(DataManager.class);
        JoinUser reloadedUser = dataManager.load(JoinUser.class)
                .id(user.getId())
                .view(userView).one();

        assertNotNull(reloadedUser);
        assertEquals(type, reloadedUser.getType());
        assertEquals(classType, reloadedUser.getType().getClassType());
    }
}
