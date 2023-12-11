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

import io.jmix.core.Messages;
import io.jmix.quartz.model.JobModel;
import io.jmix.quartz.model.ScheduleType;
import io.jmix.quartz.model.TriggerModel;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for calculating description for quartz jobs and triggers based on their data and localized messages.
 */
@Component("quartz_ScheduleDescriptionProvider")
public class ScheduleDescriptionProvider {

    static final String EXECUTE_FOREVER_MESSAGE_KEY = "executeForeverMessage";
    static final String EXECUTE_ONCE_MESSAGE_KEY = "executeOnceMessage";
    static final String EXECUTE_SEVERAL_TIMES_MESSAGE_KEY = "executeSeveralTimesMessage";

    protected Messages messages;

    @Autowired
    public ScheduleDescriptionProvider(Messages messages) {
        this.messages = messages;
    }

    /**
     * Calculating trigger description, based on schedule type, repeat count and repeat interval.
     * Text messages is taken from correspondent localized properties files.
     *
     * @param triggerModel trigger data to calculate trigger description
     * @return localized description
     */
    @Nullable
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
            return messages.formatMessage(ScheduleDescriptionProvider.class, EXECUTE_FOREVER_MESSAGE_KEY, calculateInterval(repeatInterval));
        } else if (repeatCount == 0) {
            return messages.getMessage(ScheduleDescriptionProvider.class, EXECUTE_ONCE_MESSAGE_KEY);
        } else {
            return messages.formatMessage(ScheduleDescriptionProvider.class, EXECUTE_SEVERAL_TIMES_MESSAGE_KEY, repeatCount + 1, calculateInterval(repeatInterval));
        }
    }

    private static String calculateInterval(Long repeatInterval) {
        double doubleValue = (double) repeatInterval / 1000;
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(LocaleContextHolder.getLocale());
        decimalFormatSymbols.setDecimalSeparator('.');

        DecimalFormat format = new DecimalFormat("0.#", decimalFormatSymbols);
        return format.format(doubleValue);
    }

    /**
     * Calculating schedule description for quartz Job, based on schedule description of associated triggers
     * as comma-separated string.
     *
     * @param jobModel to calculate description
     * @return calculated description as comma-separated string
     */
    @Nullable
    public String getScheduleDescription(JobModel jobModel) {
        if (CollectionUtils.isEmpty(jobModel.getTriggers())) {
            return null;
        }

        return jobModel.getTriggers().stream()
                .map(this::getScheduleDescription)
                .collect(Collectors.joining(", "));
    }
}

