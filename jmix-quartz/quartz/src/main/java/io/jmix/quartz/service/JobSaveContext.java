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

package io.jmix.quartz.service;

import io.jmix.core.common.util.Preconditions;
import io.jmix.quartz.model.JobModel;
import org.jspecify.annotations.Nullable;
import org.quartz.JobKey;

/**
 * Parameters of the {@link QuartzService#saveJob(JobSaveContext)} operation.
 * Job data parameters and triggers are taken from the {@link JobModel} collections.
 */
public class JobSaveContext {

    protected final JobModel jobModel;

    protected JobKey originalJobKey;

    public JobSaveContext(JobModel jobModel) {
        Preconditions.checkNotNullArgument(jobModel, "jobModel is null");
        this.jobModel = jobModel;
    }

    public JobModel getJobModel() {
        return jobModel;
    }

    @Nullable
    public JobKey getOriginalJobKey() {
        return originalJobKey;
    }

    /**
     * Sets the key the job currently has in the Quartz engine, which defines what the save does:
     * {@code null} (the default) means creation of a new job, a key equal to the one built from the
     * {@link JobModel} name and group means an in-place update, a different key makes the save recreate
     * the job under the new key. See {@link QuartzService#saveJob(JobSaveContext)} for the complete
     * contract, including the failure cases.
     */
    public JobSaveContext setOriginalJobKey(@Nullable JobKey originalJobKey) {
        this.originalJobKey = originalJobKey;
        return this;
    }
}
