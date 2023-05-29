/*
 * Copyright (c) 2008-2018 Haulmont.
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
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.model.Many2Many_FetchSame1;
import com.haulmont.cuba.core.model.Many2Many_FetchSame2;
import com.haulmont.cuba.core.model.Many2Many_FetchSame3;
import com.haulmont.cuba.core.testsupport.CoreTest;
import com.haulmont.cuba.core.testsupport.TestSupport;
import io.jmix.core.Metadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

@CoreTest
public class ManyToManyFetchSameEntityTest {
    @Autowired
    private Persistence persistence;
    @Autowired
    private Metadata metadata;
    @Autowired
    private TestSupport testSupport;

    protected Many2Many_FetchSame1 same1_1, same1_2, same1_3;
    protected Many2Many_FetchSame2 same2_1, same2_2, same2_3, same2_4;
    protected Many2Many_FetchSame3 same3_1, same3_2;

    @BeforeEach
    public void setUp() throws Exception {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            same1_1 = metadata.create(Many2Many_FetchSame1.class);
            same1_1.setName("same1_1");
            em.persist(same1_1);

            same1_2 = metadata.create(Many2Many_FetchSame1.class);
            same1_2.setName("same1_2");
            em.persist(same1_2);

            same1_3 = metadata.create(Many2Many_FetchSame1.class);
            same1_3.setName("same1_3");
            em.persist(same1_3);

            same2_1 = metadata.create(Many2Many_FetchSame2.class);
            same2_1.setName("same2_1");
            em.persist(same2_1);

            same2_2 = metadata.create(Many2Many_FetchSame2.class);
            same2_2.setName("same2_2");
            em.persist(same2_2);

            same2_3 = metadata.create(Many2Many_FetchSame2.class);
            same2_3.setName("same2_3");
            same2_3.setManyToOne1(same1_3);
            em.persist(same2_3);

            same2_4 = metadata.create(Many2Many_FetchSame2.class);
            same2_4.setName("same2_4");
            same2_4.setManyToOne1(same1_3);
            em.persist(same2_4);

            same3_1 = metadata.create(Many2Many_FetchSame3.class);
            same3_1.setName("same3_1");
            same3_1 = metadata.create(Many2Many_FetchSame3.class);
            em.persist(same3_1);

            same3_2 = metadata.create(Many2Many_FetchSame3.class);
            same3_2.setName("same3_2");
            same3_2 = metadata.create(Many2Many_FetchSame3.class);
            em.persist(same3_2);

            same1_1.setMany2(Collections.singletonList(same2_1));
            same1_2.setMany2(Collections.singletonList(same2_2));

            same2_3.setMany3(same3_1);
            same2_4.setMany3(same3_2);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        testSupport.deleteRecord("TEST_MANY2_MANY_FETCH_SAME1_MANY2_MANY_FETCH_SAME2_LINK", "MANY2_MANY__FETCH_SAME1_ID", same1_1.getId(), same1_2.getId(), same1_3.getId());
        testSupport.deleteRecord(same2_3, same2_4, same1_1, same1_2, same1_3, same2_1, same2_2, same3_1, same3_2);
    }

    @Test
    public void testManyToMany_emptyCollection() {
        DataManager dataManager = AppBeans.get(DataManager.class);

        LoadContext<Many2Many_FetchSame1> loadContext = new LoadContext<>(Many2Many_FetchSame1.class).setFetchPlan("Many2Many_FetchSame1-emptyCollection");
        loadContext.setQueryString("select e from test$Many2Many_FetchSame1 e where e.name <> 'same1_3'");

        List<Many2Many_FetchSame1> result = dataManager.loadList(loadContext);
        for (Many2Many_FetchSame1 e : result) {
            Assertions.assertTrue(!e.getMany2().isEmpty());
        }
    }

    @Test
    public void testManyToMany_sameEntityTwice() {
        DataManager dataManager = AppBeans.get(DataManager.class);

        LoadContext<Many2Many_FetchSame1> loadContext = new LoadContext<>(Many2Many_FetchSame1.class).setFetchPlan("Many2Many_FetchSame1-sameEntityTwice").setId(same1_3);

        Many2Many_FetchSame1 result = dataManager.load(loadContext);
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getOneToMany2());
        Assertions.assertNotNull(result.getOneToMany2().get(0).getMany3());
        Assertions.assertNotNull(result.getOneToMany2().get(1).getMany3());
    }
}
