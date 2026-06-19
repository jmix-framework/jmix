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

package io.jmix.quartz.util;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("quartz_QuartzJobDetailsFinder")
public class QuartzJobDetailsFinder {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Returns {@link JobKey}'s of {@link JobDetail} which registered in codebase as Spring beans. Such {@link JobDetail}s considered
     * as "predefined" and cannot be removed from the Quartz engine via user interface.
     *
     * @see io.jmix.quartz.model.JobSource
     */
    public List<JobKey> getJobDetailBeanKeys() {
        return applicationContext.getBeansOfType(JobDetail.class)
                .values().stream()
                .map(JobDetail::getKey)
                .collect(Collectors.toList());
    }

}
