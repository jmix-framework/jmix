/*
 * Copyright 2019 Haulmont.
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
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.model.Many2ManyA;
import com.haulmont.cuba.core.model.Many2ManyB;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.DeletePolicyException;
import io.jmix.core.Metadata;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@CoreTest
public class SoftDeleteMany2ManyTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    private Many2ManyA many2ManyA, many2ManyA2, a1, a2;
    private Many2ManyB many2ManyB, many2ManyB2, b1, b2, b3;

    @BeforeEach
    public void setUp() throws Exception {
        DataManager dataManager = AppBeans.get(DataManager.class);

        many2ManyA = metadata.create(Many2ManyA.class);
        many2ManyB = metadata.create(Many2ManyB.class);

        many2ManyA.setCollectionOfB(new HashSet<>());
        many2ManyA.getCollectionOfB().add(many2ManyB);

        dataManager.commit(new CommitContext(many2ManyA, many2ManyB));

        many2ManyA2 = metadata.create(Many2ManyA.class);
        many2ManyB2 = metadata.create(Many2ManyB.class);

        many2ManyA2.setCollectionOfB2(new HashSet<>());
        many2ManyA2.getCollectionOfB2().add(many2ManyB2);

        dataManager.commit(new CommitContext(many2ManyA2, many2ManyB2));

        a1 = metadata.create(Many2ManyA.class);
        a2 = metadata.create(Many2ManyA.class);
        b1 = metadata.create(Many2ManyB.class);
        b2 = metadata.create(Many2ManyB.class);
        b3 = metadata.create(Many2ManyB.class);

        a1.setCollectionOfB(new HashSet<>());
        a1.getCollectionOfB().add(b1);
        a1.getCollectionOfB().add(b2);

        a2.setCollectionOfB(new HashSet<>());
        a2.getCollectionOfB().add(b3);

        dataManager.commit(new CommitContext(a1, a2, b1, b2, b3));
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord("TEST_MANY2MANY_AB_LINK", "A_ID", many2ManyA.getId(), a1.getId(), a2.getId());
        testSupport.deleteRecord("TEST_MANY2MANY_AB_LINK2", "A_ID", many2ManyA2.getId());
        testSupport.deleteRecord(many2ManyA, many2ManyB, many2ManyA2, many2ManyB2, a1, a2, b1, b2, b3);
    }

    @Test
    public void testMany2ManyUnlink() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            Many2ManyA a = persistence.getEntityManager().find(Many2ManyA.class, this.many2ManyA.getId());
            assertNotNull(a);
            assertFalse(a.getCollectionOfB().isEmpty());
            assertEquals(many2ManyB, a.getCollectionOfB().iterator().next());

            tx.commit();
        }

        try (Transaction tx = persistence.createTransaction()) {
            Many2ManyB b = persistence.getEntityManager().find(Many2ManyB.class, this.many2ManyB.getId());
            persistence.getEntityManager().remove(b);

            tx.commit();
        }

        try (Transaction tx = persistence.createTransaction()) {
            Many2ManyA a = persistence.getEntityManager().find(Many2ManyA.class, this.many2ManyA.getId());
            assertNotNull(a);
            assertTrue(a.getCollectionOfB().isEmpty());

            tx.commit();
        }
    }

    @Test
    public void testMany2ManyDeny() throws Exception {
        try (Transaction tx = persistence.createTransaction()) {
            Many2ManyA a = persistence.getEntityManager().find(Many2ManyA.class, many2ManyA2.getId());
            assertNotNull(a);
            assertFalse(a.getCollectionOfB2().isEmpty());
            assertEquals(many2ManyB2, a.getCollectionOfB2().iterator().next());

            tx.commit();
        }

        try (Transaction tx = persistence.createTransaction()) {
            Many2ManyA a = persistence.getEntityManager().find(Many2ManyA.class, many2ManyA2.getId());
            persistence.getEntityManager().remove(a);

            tx.commit();
            fail();
        } catch (Exception e) {
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause == null) rootCause = e;
            if (!(rootCause instanceof DeletePolicyException))
                rootCause.printStackTrace();
            assertTrue(rootCause instanceof DeletePolicyException);
        }

        try (Transaction tx = persistence.createTransaction()) {
            Many2ManyA a = persistence.getEntityManager().find(Many2ManyA.class, many2ManyA2.getId());
            assertNotNull(a);
            assertFalse(a.getCollectionOfB2().isEmpty());
            assertEquals(many2ManyB2, a.getCollectionOfB2().iterator().next());

            tx.commit();
        }
    }

    /**
     * @see <a href="https://youtrack.haulmont.com/issue/PL-3452">PL-3452</a>
     */
    @Test
    public void test_PL_3452() throws Exception {
        Many2ManyA a1;
        Many2ManyA a2;
        Many2ManyB b1;

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            a1 = em.find(Many2ManyA.class, this.a1.getId());
            assertNotNull(a1);
            assertEquals(2, a1.getCollectionOfB().size());

            a2 = em.find(Many2ManyA.class, this.a2.getId());
            assertNotNull(a2);
            assertEquals(1, a2.getCollectionOfB().size());

            tx.commitRetaining();
            em = persistence.getEntityManager();

            b1 = em.find(Many2ManyB.class, this.b1.getId());
            em.remove(b1);

            tx.commitRetaining();
            em = persistence.getEntityManager();

            a1 = em.find(Many2ManyA.class, this.a1.getId());
            assertNotNull(a1);
            assertEquals(1, a1.getCollectionOfB().size());

            tx.commit();
        }
    }
}
