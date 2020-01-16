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

import com.haulmont.cuba.core.model.CascadeDeletionPolicyEntity;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestContainer;
import io.jmix.core.AppBeans;
import io.jmix.core.Metadata;
import io.jmix.data.EntityManager;
import io.jmix.data.Transaction;
import io.jmix.data.impl.EntityListenerManager;
import io.jmix.data.listener.BeforeDeleteEntityListener;
import io.jmix.data.listener.BeforeUpdateEntityListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CoreTest
public class CascadeDeletionPolicyTest {
    public static TestContainer cont = TestContainer.Common.INSTANCE;

    protected CascadeDeletionPolicyEntity root, first, second, third;

    @BeforeEach
    public void setUp() {
        Transaction tx = cont.persistence().createTransaction();
        try {
            EntityManager em = cont.persistence().getEntityManager();
            Metadata metadata = cont.metadata();

            root = metadata.create(CascadeDeletionPolicyEntity.class);
            root.setName("root");
            em.persist(root);

            first = metadata.create(CascadeDeletionPolicyEntity.class);
            first.setName("first");
            first.setFather(root);
            em.persist(first);

            second = metadata.create(CascadeDeletionPolicyEntity.class);
            second.setName("second");
            second.setFather(first);
            em.persist(second);

            third = metadata.create(CascadeDeletionPolicyEntity.class);
            third.setName("third");
            third.setFather(second);
            em.persist(third);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @AfterEach
    public void tearDown() {
        cont.deleteRecord(third, second, first, root);
    }

    @Test
    public void testRemoveCascade() {
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.persistence().getEntityManager();
            CascadeDeletionPolicyEntity loadedRoot = em.find(CascadeDeletionPolicyEntity.class, root.getId());
            em.find(CascadeDeletionPolicyEntity.class, first.getId());
            em.remove(loadedRoot);
            tx.commit();
        }

        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.persistence().getEntityManager();
            List r = em.createQuery("select e from test$CascadeDeletionPolicyEntity e where e.id in ?1")
                    .setParameter(1, Arrays.asList(root, first, second, third))
                    .getResultList();
            assertEquals(0, r.size());
            tx.commit();
        }
    }

    @Test
    public void testEntityListenerOnCascadeDelete() {
        EntityListenerManager entityListenerManager = AppBeans.get(EntityListenerManager.class);
        entityListenerManager.addListener(CascadeDeletionPolicyEntity.class, DeleteCascadeDeletionPolicyEntityListener.class);
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.persistence().getEntityManager();
            CascadeDeletionPolicyEntity loadedSecond = em.find(CascadeDeletionPolicyEntity.class, second.getId());
            em.find(CascadeDeletionPolicyEntity.class, third.getId());
            em.remove(loadedSecond);
            tx.commit();
        }
        entityListenerManager.removeListener(CascadeDeletionPolicyEntity.class, DeleteCascadeDeletionPolicyEntityListener.class);
        assertEquals(2, DeleteCascadeDeletionPolicyEntityListener.deletedEvents.size());
        assertTrue(DeleteCascadeDeletionPolicyEntityListener.deletedEvents.contains(second.getId().toString()));
        assertTrue(DeleteCascadeDeletionPolicyEntityListener.deletedEvents.contains(third.getId().toString()));
    }

    @Test
    public void testEntityListenerOnUpdate() {
        EntityListenerManager entityListenerManager = AppBeans.get(EntityListenerManager.class);
        entityListenerManager.addListener(CascadeDeletionPolicyEntity.class, UpdateCascadeDeletionPolicyEntityListener.class);
        try (Transaction tx = cont.persistence().createTransaction()) {
            EntityManager em = cont.persistence().getEntityManager();
            CascadeDeletionPolicyEntity loadedThird = em.find(CascadeDeletionPolicyEntity.class, third.getId());
            CascadeDeletionPolicyEntity loadedSecond = em.find(CascadeDeletionPolicyEntity.class, second.getId());
            loadedThird.setName("third#1");
            tx.commit();
        }
        entityListenerManager.removeListener(CascadeDeletionPolicyEntity.class, UpdateCascadeDeletionPolicyEntityListener.class);
        assertEquals(2, UpdateCascadeDeletionPolicyEntityListener.updatedEvents.size());
        assertTrue(UpdateCascadeDeletionPolicyEntityListener.updatedEvents.contains(second.getId().toString()));
        assertTrue(UpdateCascadeDeletionPolicyEntityListener.updatedEvents.contains(third.getId().toString()));
    }

    public static class DeleteCascadeDeletionPolicyEntityListener implements BeforeDeleteEntityListener<CascadeDeletionPolicyEntity> {
        public static final List<String> deletedEvents = new ArrayList<>();

        @Override
        public void onBeforeDelete(CascadeDeletionPolicyEntity entity, EntityManager entityManager) {
            deletedEvents.add(entity.getId().toString());
        }
    }

    public static class UpdateCascadeDeletionPolicyEntityListener implements BeforeUpdateEntityListener<CascadeDeletionPolicyEntity> {
        public static final List<String> updatedEvents = new ArrayList<>();

        @Override
        public void onBeforeUpdate(CascadeDeletionPolicyEntity entity, EntityManager entityManager) {
            updatedEvents.add(entity.getId().toString());
            if (entity.getName().contains("third")) {
                entity.getFather().setName("second#1");
            }
        }
    }
}
