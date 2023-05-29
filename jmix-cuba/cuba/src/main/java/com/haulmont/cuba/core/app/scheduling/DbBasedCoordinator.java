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

package com.haulmont.cuba.core.app.scheduling;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.ScheduledTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.LockModeType;
import javax.persistence.PessimisticLockException;
import java.util.Date;
import java.util.List;

/**
 * Implementation of {@link Coordinator} interface, performing synchronization of singleton schedulers on the main
 * database.
 * <p>This implementation should not be used if the database is overloaded.</p>
 */
@Component(Coordinator.NAME)
public class DbBasedCoordinator implements Coordinator {

    private static final Logger log = LoggerFactory.getLogger(DbBasedCoordinator.class);

    protected static class ContextImpl implements Context {

        protected List<ScheduledTask> tasks;
        protected Transaction transaction;

        protected ContextImpl(List<ScheduledTask> tasks, Transaction transaction) {
            this.tasks = tasks;
            this.transaction = transaction;
        }

        @Override
        public List<ScheduledTask> getTasks() {
            return tasks;
        }

        public Transaction getTransaction() {
            return transaction;
        }
    }

    @Inject
    protected Persistence persistence;

    @Override
    public Context begin() {
        Transaction tx = persistence.createTransaction();
        try {
            List<ScheduledTask> tasks = getTasks();
            return new ContextImpl(tasks, tx);
        } catch (SchedulingLockException e) {
            //noinspection IncorrectClosingTransaction
            tx.end();
            throw e;
        } catch (Exception e) {
            //noinspection IncorrectClosingTransaction
            tx.end();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void end(Context context) {
        log.trace("Commit transaction thereby unlock active tasks");
        ((ContextImpl) context).transaction.commit();
        ((ContextImpl) context).transaction.end();
    }

    @Override
    public boolean isLastExecutionFinished(ScheduledTask task, long now) {
        EntityManager em = persistence.getEntityManager();
        Query query = em.createQuery(
                "select e.finishTime from sys$ScheduledExecution e where e.task.id = ?1 and e.startTime = ?2");
        query.setParameter(1, task.getId());
        query.setParameter(2, task.getLastStartTime());
        List list = query.getResultList();
        if (list.isEmpty() || list.get(0) == null) {
            // Execution finish was not registered for some reason, so using timeout value or just return false
            boolean result = task.getTimeout() != null
                    && (task.getLastStart() + task.getTimeout() * 1000) <= now;
            if (result)
                log.trace(task + ": considered finished because of timeout");
            else
                log.trace(task + ": not finished and not timed out");
            return result;
        }
        Date date = (Date) list.get(0);
        log.trace("{} : finished at {}", task, date.getTime());
        return true;
    }

    @Override
    public long getLastFinished(ScheduledTask task) {
        EntityManager em = persistence.getEntityManager();
        Query query = em.createQuery(
                "select max(e.finishTime) from sys$ScheduledExecution e where e.task.id = ?1")
                .setParameter(1, task.getId());
        Date date = (Date) query.getFirstResult();
        return date == null ? 0 : date.getTime();
    }

    protected synchronized List<ScheduledTask> getTasks() {
        log.trace("Read all active tasks from DB and lock them");
        EntityManager em = persistence.getEntityManager();
        try {
            //noinspection unchecked
            return em.createQuery("select t from sys$ScheduledTask t where t.active = true")
                    .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                    .getResultList();
        } catch (PessimisticLockException e) {
            log.info("Unable to acquire lock on tasks");
            if (log.isTraceEnabled()) {
                log.trace("Unable to acquire lock on tasks. Error:", e);
            }
            throw new SchedulingLockException("Lock exception while acquiring tasks");
        }
    }
}