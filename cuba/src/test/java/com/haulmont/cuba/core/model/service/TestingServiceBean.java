/*
 * Copyright 2020 Haulmont.
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

package com.haulmont.cuba.core.model.service;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(TestingService.NAME)
public class TestingServiceBean implements TestingService {

    private final Logger log = LoggerFactory.getLogger(TestingServiceBean.class);

    @Inject
    private Persistence persistence;

    @Override
    public String executeFor(int timeMillis) {
        log.debug("executeFor {} started", timeMillis);
        try {
            Thread.sleep(timeMillis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.debug("executeFor {} finished", timeMillis);
        return "Done";
    }

    @Override
    public String execute() {
        log.debug("execute started");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Done: com.haulmont.cuba.core.app.TestingServiceBean.execute";
    }

    @Override
    public void clearScheduledTasks() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            Query query = em.createNativeQuery("delete from SYS_SCHEDULED_EXECUTION");
            query.executeUpdate();

            query = em.createNativeQuery("delete from SYS_SCHEDULED_TASK");
            query.executeUpdate();

            tx.commit();
        } finally {
            tx.end();
        }
    }
}
