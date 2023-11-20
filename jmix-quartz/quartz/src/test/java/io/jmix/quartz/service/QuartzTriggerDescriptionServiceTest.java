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
import io.jmix.quartz.model.TriggerModel;
import org.junit.jupiter.api.Test;

import static io.jmix.quartz.model.ScheduleType.CRON_EXPRESSION;
import static io.jmix.quartz.model.ScheduleType.SIMPLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuartzTriggerDescriptionServiceTest {

    @Test
    void getScheduleDescription_null_shedule_type() {
        Messages messagesMock = mock(Messages.class);
        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(null);

        QuartzTriggerDescriptionService service = new QuartzTriggerDescriptionService(messagesMock);
        assertNull(service.getScheduleDescription(triggerMock));
    }

    @Test
    void getScheduleDescription_cron_shedule_type() {
        Messages messagesMock = mock(Messages.class);
        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(CRON_EXPRESSION);
        String cronExpression = "Some CRON expression";
        when(triggerMock.getCronExpression()).thenReturn(cronExpression);

        QuartzTriggerDescriptionService service = new QuartzTriggerDescriptionService(messagesMock);
        assertEquals(cronExpression, service.getScheduleDescription(triggerMock));
    }

    @Test
    void getScheduleDescription_simiple_shedule_type() {
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.formatMessage(QuartzTriggerDescriptionService.class, QuartzTriggerDescriptionService.EXECUTE_FOREVER_MESSAGE_KEY, 10L)).thenReturn();

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(null);
        when(triggerMock.getRepeatInterval()).thenReturn(10000L);

        QuartzTriggerDescriptionService service = new QuartzTriggerDescriptionService(messagesMock);
        assertEquals("", service.getScheduleDescription(triggerMock));
    }


}