/*
 * Copyright 2021 Haulmont.
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

package io.jmix.autoconfigure.search.job;

import io.jmix.search.SearchProperties;
import io.jmix.search.index.queue.IndexingQueueManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class EnqueueingSessionProcessingJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(EnqueueingSessionProcessingJob.class);

    @Autowired
    protected SearchProperties searchProperties;
    @Autowired
    private IndexingQueueManager indexingQueueManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (searchProperties.isEnabled()) {
            indexingQueueManager.processNextEnqueueingSession();
        }
    }
}
