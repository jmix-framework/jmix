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

package io.jmix.search.schedule;

import io.jmix.search.IndexProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//TODO Temporary solution. Scheduling should be performed via framework scheduling functionality
@Component
public class JobManager {

    private static final Logger log = LoggerFactory.getLogger(JobManager.class);

    @Autowired
    protected IndexProcessManager indexProcessManager;

    //TODO Property for timing.
    // Make internal monitoring of non-empty queue to reduce database requests?
    // (e.g. keep timestamps of last queuing and last start of processing queue to detect necessity of request to database)
    @Scheduled(fixedDelay = 5000L)
    public void scheduleQueueTrackingJob() {
        log.trace("Process Queue");
        indexProcessManager.processQueue();
    }

    @Scheduled(fixedDelay = 1000L)
    public void scheduleReindexJob() {
        log.trace("Check reindex entities");
        indexProcessManager.processNextReindexingEntity();
    }
}
