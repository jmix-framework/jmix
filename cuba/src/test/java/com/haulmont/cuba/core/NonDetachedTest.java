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
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.model.CascadeDeletionPolicyEntity;
import com.haulmont.cuba.core.model.common.Group;
import com.haulmont.cuba.core.model.common.Server;
import com.haulmont.cuba.core.model.common.User;
import com.haulmont.cuba.core.model.sales.Customer;
import com.haulmont.cuba.core.model.sales.Order;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import io.jmix.core.Entity;
import io.jmix.core.UuidProvider;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class NonDetachedTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TestSupport testSupport;

    private User user;
    private Group companyGroup;
    private Customer customer;
    private Order order;
    private FetchPlan orderView;
    private CascadeDeletionPolicyEntity cascadeEntity1;
    private CascadeDeletionPolicyEntity cascadeEntity2;

    @BeforeEach
    public void setUp() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            user = metadata.create(User.class);
            user.setName("testUser");
            user.setLogin("testLogin");
            companyGroup = metadata.create(Group.class);
            companyGroup.setName("Company");
            em.persist(companyGroup);
            user.setGroup(companyGroup);
            em.persist(user);

            customer = metadata.create(Customer.class);
            customer.setName("test customer");
            em.persist(customer);

            order = metadata.create(Order.class);
            order.setDate(new Date());
            order.setAmount(BigDecimal.TEN);
            order.setCustomer(customer);
            order.setUser(user);
            em.persist(order);

            cascadeEntity1 = metadata.create(CascadeDeletionPolicyEntity.class);
            cascadeEntity1.setName("cascadeEntity1");
            em.persist(cascadeEntity1);

            cascadeEntity2 = metadata.create(CascadeDeletionPolicyEntity.class);
            cascadeEntity2.setName("cascadeEntity2");
            cascadeEntity2.setFather(cascadeEntity1);
            em.persist(cascadeEntity2);

            cascadeEntity1.setFirstChild(cascadeEntity2);

            tx.commit();
        }

        orderView = new View(Order.class)
                .addProperty("date")
                .addProperty("amount")
                .addProperty("customer", new View(Customer.class).addProperty("name"))
                .addProperty("user", new View(User.class).addProperty("login").addProperty("name"));
    }

    @AfterEach
    public void tearDown() {
        testSupport.deleteRecord(order, customer, user, companyGroup);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(persistence.getDataSource());
        jdbcTemplate.update("delete from TEST_CASCADE_DELETION_POLICY_ENTITY");
    }

    @Test
    public void testSaveNotDetached_DM() throws Exception {
        Group companyGroupCopy = metadata.getTools().copy(companyGroup);
        assertTrue(entityStates.isNew(companyGroupCopy));
        entityStates.makeDetached(companyGroupCopy);

        User user = persistence.callInTransaction((em) -> em.find(User.class, this.user.getId()));
        User userCopy = metadata.getTools().copy(user);
        assertNull(userCopy.getGroup());

        assertTrue(entityStates.isNew(userCopy));
        entityStates.makeDetached(userCopy);

        userCopy.setGroup(companyGroupCopy);
        userCopy.setName("new name");
        AppBeans.get(DataManager.class).commit(userCopy);

        user = persistence.callInTransaction((em) -> em.find(User.class, this.user.getId()));
        assertEquals("new name", user.getName());
    }

    @Test
    public void testCreateNew_DM() throws Exception {
        // check versioned entity
        Group companyGroupCopy = metadata.getTools().copy(companyGroup);
        assertTrue(entityStates.isNew(companyGroupCopy));
        entityStates.makeDetached(companyGroupCopy);

        User user = persistence.callInTransaction((em) -> em.find(User.class, this.user.getId()));
        User userCopy = metadata.getTools().copy(user);
        assertNull(userCopy.getGroup());

        assertTrue(entityStates.isNew(userCopy));
        entityStates.makeDetached(userCopy);

        userCopy.setId(UuidProvider.createUuid());
        userCopy.setVersion(null);
        userCopy.setGroup(companyGroupCopy);
        userCopy.setName("new name");
        try {
            AppBeans.get(DataManager.class).commit(userCopy);

            user = persistence.callInTransaction((em) -> em.find(User.class, userCopy.getId()));
            assertEquals("new name", user.getName());
        } finally {
            testSupport.deleteRecord(userCopy);
        }

        // check non-versioned entity
        Server server = metadata.create(Server.class);
        server.setName("server-" + server.getId());
        assertTrue(entityStates.isNew(server));
        entityStates.makeDetached(server);
        try {
            AppBeans.get(DataManager.class).commit(server);
            Server loaded = persistence.callInTransaction(em -> em.find(Server.class, server.getId()));
            assertNotNull(loaded);
        } finally {
            testSupport.deleteRecord(server);
        }
    }

    interface Saver {
        void save(Entity entity);
    }

    private Order loadChangeAndSave(Saver saver) {
        Order order = persistence.callInTransaction(em -> em.find(Order.class, this.order.getId()));
        Order orderCopy = metadata.getTools().copy(order);

        Customer customerCopy = metadata.getTools().copy(this.customer);

        Date date = DateUtils.addDays(orderCopy.getDate(), 1);
        orderCopy.setDate(date);
        orderCopy.setAmount(null);
        orderCopy.setCustomer(customerCopy);
        assertNull(orderCopy.getUser());

        assertTrue(entityStates.isNew(orderCopy));
        saver.save(orderCopy);

        order = persistence.callInTransaction(em -> em.find(Order.class, this.order.getId(), orderView));
        assertEquals(date, order.getDate());

        return order;
    }

    @Test
    public void testSaveNulls_DM() throws Exception {
        Order order = loadChangeAndSave(entity -> {
            entityStates.makeDetached(entity);
            AppBeans.get(DataManager.class).commit(entity);
        });
        assertNull(order.getAmount());
        assertNotNull(order.getCustomer());
        assertNull(order.getUser());
    }

    @Test
    public void testSaveNulls_EM() throws Exception {
        Order order = loadChangeAndSave(entity -> {
            entityStates.makeDetached(entity);
            persistence.runInTransaction(em -> em.merge(entity));
        });
        assertNull(order.getAmount());
        assertNotNull(order.getCustomer());
        assertNull(order.getUser());
    }

    @Test
    public void testDoNotSaveNulls_DM() throws Exception {
        Order order = loadChangeAndSave(entity -> {
            entityStates.makePatch(entity);
            dataManager.commit(entity);
        });
        assertNotNull(order.getAmount());
        assertNotNull(order.getCustomer());
        assertNotNull(order.getUser());
    }

    @Test
    public void testDoNotSaveNulls_EM() throws Exception {
        Order order = loadChangeAndSave(entity -> {
            entityStates.makePatch(entity);
            persistence.runInTransaction(em -> em.merge(entity));
        });
        assertNotNull(order.getAmount());
        assertNotNull(order.getCustomer());
        assertNotNull(order.getUser());
    }

    @Test
    public void testDoNotSaveNulls_EM_new() throws Exception {
        Order order = loadChangeAndSave(entity -> {
            persistence.runInTransaction(em -> em.merge(entity));
        });
        assertNotNull(order.getAmount());
        assertNotNull(order.getCustomer());
        assertNotNull(order.getUser());
    }

    @Test
    public void testRecursiveObjects() throws Exception {
        CascadeDeletionPolicyEntity e1 = metadata.create(CascadeDeletionPolicyEntity.class);
        e1.setName("cascadeEntity1");

        CascadeDeletionPolicyEntity e2 = metadata.create(CascadeDeletionPolicyEntity.class);
        e2.setName("cascadeEntity2");
        e2.setFather(cascadeEntity1);

        e1.setChildren(new HashSet<>());
        e1.getChildren().add(e2);
        e1.setFirstChild(e2);

        entityStates.makePatch(e1);

        CascadeDeletionPolicyEntity mergedEntity = persistence.callInTransaction(em -> {
            return em.merge(e1);
        });

        assertEquals(cascadeEntity1.getName(), mergedEntity.getName());
    }
}
