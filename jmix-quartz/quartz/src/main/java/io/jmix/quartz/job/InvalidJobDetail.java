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

package io.jmix.quartz.job;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;

public class InvalidJobDetail implements JobDetail {

    protected JobKey jobKey;
    protected String description;

    public InvalidJobDetail(JobKey jobKey, String description) {
        this.jobKey = jobKey;
        this.description = description;
    }

    @Override
    public JobKey getKey() {
        return jobKey;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return InvalidJob.class;
    }

    @Override
    public JobDataMap getJobDataMap() {
        return null;
    }

    @Override
    public boolean isDurable() {
        return false;
    }

    @Override
    public boolean isPersistJobDataAfterExecution() {
        return false;
    }

    @Override
    public boolean isConcurrentExectionDisallowed() {
        return false;
    }

    @Override
    public boolean requestsRecovery() {
        return false;
    }

    @Override
    public JobBuilder getJobBuilder() {
        return null;
    }

    @Override
    public Object clone() {
        return null;
    }
}