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
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.app.SchedulingService;
import com.haulmont.cuba.core.app.scheduled.MethodInfo;
import com.haulmont.cuba.core.entity.ScheduledTask;
import com.haulmont.cuba.core.global.DataManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service(SchedulingService.NAME)
public class SchedulingServiceBean implements SchedulingService {

    @Inject
    protected Persistence persistence;

    @Inject
    private SchedulingAPI scheduling;

    @Inject
    protected SchedulingBeansMetadata schedulingBeansMetadata;

    @Inject
    protected DataManager dataManager;

    @Override
    public Map<String, List<MethodInfo>> getAvailableBeans() {
        return schedulingBeansMetadata.getAvailableBeans();
    }

    @Override
    public void setActive(boolean active) {
        scheduling.setActive(active);
    }

    @Override
    public void setActive(ScheduledTask task, boolean active) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            ScheduledTask t = em.find(ScheduledTask.class, task.getId());
            t.setActive(active);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Override
    public void setActive(Set<ScheduledTask> tasks, boolean active) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            for (ScheduledTask task : tasks) {
                ScheduledTask t = em.find(ScheduledTask.class, task.getId());
                t.setActive(active);
            }
            tx.commit();
        }
    }

    @Override
    public void runOnce(ScheduledTask task) {
        scheduling.runOnce(task);
    }

    @Override
    public List<ScheduledTask> getRunningTasks() {
        return scheduling.getRunningTasks();
    }

    public static class SetSchedulingActiveMsg implements Serializable {
        private static final long serialVersionUID = 6934530919733469448L;

        public final boolean active;

        public SetSchedulingActiveMsg(boolean active) {
            this.active = active;
        }

        @Override
        public String toString() {
            return "SetSchedulingActiveMsg{" +
                    "active=" + active +
                    '}';
        }
    }
}