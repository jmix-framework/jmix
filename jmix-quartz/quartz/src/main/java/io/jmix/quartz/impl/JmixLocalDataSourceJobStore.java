/*
 * Copyright 2026 Haulmont.
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

package io.jmix.quartz.impl;

import org.jspecify.annotations.NullMarked;
import org.quartz.SchedulerConfigException;
import org.quartz.impl.jdbcjobstore.StdRowLockSemaphore;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerSignaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;

/**
 * A {@link LocalDataSourceJobStore} that honors {@code org.quartz.jobStore.useDBLocks=true} on HSQLDB.
 * <p>
 * The parent store unconditionally downgrades HSQLDB to the in-JVM {@code SimpleSemaphore} — a leftover from
 * HSQLDB 1.x which had no {@code SELECT ... FOR UPDATE}. The in-JVM semaphore is released after every job store
 * operation while transactional job store writes stay uncommitted, so a saving transaction and the scheduler
 * polling thread deadlock on databases where writers block readers. Modern HSQLDB supports
 * {@code SELECT ... FOR UPDATE}, so when database locking is explicitly requested it is restored here.
 */
@NullMarked
public class JmixLocalDataSourceJobStore extends LocalDataSourceJobStore {

    private static final Logger log = LoggerFactory.getLogger(JmixLocalDataSourceJobStore.class);

    @Override
    public void initialize(ClassLoadHelper loadHelper, SchedulerSignaler signaler) throws SchedulerConfigException {
        boolean dbLocksRequested = getUseDBLocks();
        super.initialize(loadHelper, signaler);
        if (dbLocksRequested && !getUseDBLocks()) {
            log.info("Restoring database-based data access locking that the parent job store has downgraded"
                    + " to thread monitor-based locking");
            setUseDBLocks(true);
            setLockHandler(new StdRowLockSemaphore(getTablePrefix(), getInstanceName(), getSelectWithLockSQL()));
        }
    }
}
