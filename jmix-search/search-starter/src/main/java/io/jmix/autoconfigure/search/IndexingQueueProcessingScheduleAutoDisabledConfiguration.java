/*
 * Copyright 2024 Haulmont.
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

package io.jmix.autoconfigure.search;

import io.jmix.search.SearchConfiguration;
import jakarta.annotation.PostConstruct;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(SearchConfiguration.class)
@ConditionalOnClass(Job.class)
@ConditionalOnMissingBean(IndexingQueueProcessingScheduleAutoConfiguration.class)
public class IndexingQueueProcessingScheduleAutoDisabledConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IndexingQueueProcessingScheduleAutoDisabledConfiguration.class);

    public static final String JOB_NAME = "IndexingQueueProcessing";

    public static final String JOB_GROUP = "DEFAULT";

    @Autowired
    JobCleaner jobCleaner;

    @PostConstruct
    void cleanJob() {
        jobCleaner.cleanJob(JOB_NAME, JOB_GROUP);
    }
}
