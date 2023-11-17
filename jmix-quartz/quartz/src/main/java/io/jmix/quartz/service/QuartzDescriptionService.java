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

package io.jmix.quartz.service;

import io.jmix.core.Messages;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.ScheduleType;
import io.jmix.quartz.model.TriggerModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.stream.Collectors;

@Service("quartz_QuartzDescriptionService")
public class QuartzDescriptionService {

    protected Messages messages;
    @Autowired
    public QuartzDescriptionService(Messages messages) {
        this.messages = messages;
    }

    public String getScheduleDescription(TriggerModel triggerModel) {
        ScheduleType scheduleType = triggerModel.getScheduleType();
        if (scheduleType == null) {
            return null;
        }

        if (scheduleType == ScheduleType.CRON_EXPRESSION) {
            return triggerModel.getCronExpression();
        }

        Integer repeatCount = triggerModel.getRepeatCount();
        Long repeatInterval = triggerModel.getRepeatInterval();
        if (Objects.isNull(repeatCount) || repeatCount < 0) {
            return messages.formatMessage(QuartzDescriptionService.class, "ExecuteForeverMessage", repeatInterval/1000);
        } else if (repeatCount == 0) {
            return messages.getMessage(QuartzDescriptionService.class, "ExecuteOnceMessage");
        } else {
            return messages.formatMessage(QuartzDescriptionService.class, "ExecuteSeveralTimesMessage", repeatCount + 1, repeatInterval/1000);
        }
    }

    public String getTriggerDescription(JobModel jobModel) {
        if (CollectionUtils.isEmpty(jobModel.getTriggers())) {
            return null;
        }

        return jobModel.getTriggers().stream()
                .map(this::getScheduleDescription)
                .collect(Collectors.joining(", "));
    }
}

