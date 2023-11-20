/*
 * Copyright 2022 Haulmont.
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

import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.service.QuartzTriggerDescriptionService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service("quartz_QuartzJobDescriptionService")
public class QuartzJobDescriptionService {

    @Autowired
    QuartzTriggerDescriptionService triggerDescriptionService;

    public String getTriggerDescription(JobModel jobModel) {
        if (CollectionUtils.isEmpty(jobModel.getTriggers())) {
            return null;
        }

        return jobModel.getTriggers().stream()
                .map(trigger -> triggerDescriptionService.getScheduleDescription(trigger))
                .collect(Collectors.joining(", "));
    }

}