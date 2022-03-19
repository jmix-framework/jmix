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
 *
 */
package com.haulmont.cuba.core;

import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.model.common.Permission;
import com.haulmont.cuba.core.model.common.PermissionType;
import com.haulmont.cuba.core.model.common.Role;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import io.jmix.core.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CoreTest
public class UpdateDetachedTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private EntityStates entityStates;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private TestSupport testSupport;

    private UUID roleId, role2Id, permissionId;

    @BeforeEach
    public void setUp() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Role role = new Role();
            roleId = role.getId();
            role.setName("testRole");
            em.persist(role);

            Role role2 = new Role();
            role2Id = role2.getId();
            role2.setName("testRole2");
            em.persist(role2);

            Permission permission = new Permission();
            permissionId = permission.getId();
            permission.setRole(role);
            permission.setType(PermissionType.SCREEN);
            permission.setTarget("testTarget");
            em.persist(permission);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query q;
            q = em.createNativeQuery("delete from TEST_PERMISSION where ID = ?");
            q.setParameter(1, permissionId.toString());
            q.executeUpdate();

            q = em.createNativeQuery("delete from TEST_ROLE where ID = ? or ID = ?");
            q.setParameter(1, roleId.toString());
            q.setParameter(2, role2Id.toString());
            q.executeUpdate();

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Test
    public void test() throws Exception {
        Permission p;
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(Permission.class)
                    .addProperty("target")
                    .addProperty("role",
                            new View(Role.class)
                                    .addProperty("name")
                    );

            p = em.find(Permission.class, permissionId, view);
            tx.commitRetaining();

            assertNotNull(p);
            p.setTarget("newTarget");

            em = persistence.getEntityManager();
            p = em.merge(p);

            tx.commit();
        } finally {
            tx.end();
        }
        p = testSupport.reserialize(p);
        assertTrue(entityStates.isDetached(p));
        assertNotNull(p.getRole());
        assertTrue(entityStates.isDetached(p.getRole()));
        assertTrue(entityStates.isLoaded(p, "role"));
    }

    @Test
    public void testRollback() {
        Permission p = null;
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            FetchPlan view = new View(Permission.class)
                    .addProperty("target")
                    .addProperty("role",
                            new View(Role.class)
                                    .addProperty("name")
                    );

            p = em.find(Permission.class, permissionId, view);
            tx.commitRetaining();

            p.setTarget("newTarget");

            em = persistence.getEntityManager();
            p = em.merge(p);

            throwException();
            tx.commit();
        } catch (RuntimeException e) {
            // ok
        } finally {
            tx.end();
            assertNotNull(p);
        }
    }

    private void throwException() {
        throw new RuntimeException();
    }

    @Test
    public void testDataManager() throws Exception {
        Permission p;

        LoadContext<Permission> ctx = new LoadContext<>(Permission.class);
        ctx.setId(permissionId);
        ctx.setView(new View(Permission.class)
                .addProperty("target")
                .addProperty("role",
                        new View(Role.class)
                                .addProperty("name")
                )
        );
        p = dataManager.load(ctx);

        assertNotNull(p);
        p.setTarget("newTarget");

        CommitContext commitCtx = new CommitContext(Collections.singleton(p));
        Set<Entity> entities = dataManager.commit(commitCtx);

        Permission result = null;
        for (Entity entity : entities) {
            if (entity.equals(p))
                result = (Permission) entity;
        }
        result = testSupport.reserialize(result);
        assertTrue(entityStates.isDetached(result));
        assertNotNull(result.getRole());
        assertTrue(entityStates.isDetached(result.getRole()));
        assertTrue(entityStates.isLoaded(result, "role"));
    }
}
