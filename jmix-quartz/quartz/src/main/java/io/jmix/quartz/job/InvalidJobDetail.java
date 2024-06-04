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

    protected String originClassName;

    protected String description;

    public InvalidJobDetail(JobKey jobKey, String originClassName, String description) {
        this.jobKey = jobKey;
        this.originClassName = originClassName;
        this.description = description;
    }

    @Override
    public JobKey getKey() {
        return jobKey;
    }

    public String getOriginClassName() {
        return originClassName;
    }

    public void setOriginClassName(String originClassName) {
        this.originClassName = originClassName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Class<? extends Job> getJobClass() {
        return null;
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
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Job clone failed", e);
        }
    }
}
