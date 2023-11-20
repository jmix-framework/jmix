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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static io.jmix.quartz.model.ScheduleType.CRON_EXPRESSION;
import static io.jmix.quartz.model.ScheduleType.SIMPLE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuartzDescriptionServiceTest {

    @Test
    void getScheduleDescription_null_schedule_type() {
        Messages messagesMock = mock(Messages.class);
        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(null);

        QuartzDescriptionService service = new QuartzDescriptionService(messagesMock);
        assertNull(service.getScheduleDescription(triggerMock));
    }

    @Test
    void getScheduleDescription_cron_schedule_type() {
        Messages messagesMock = mock(Messages.class);
        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(CRON_EXPRESSION);
        String cronExpression = "Some CRON expression";
        when(triggerMock.getCronExpression()).thenReturn(cronExpression);

        QuartzDescriptionService service = new QuartzDescriptionService(messagesMock);
        assertEquals(cronExpression, service.getScheduleDescription(triggerMock));
    }

    @Test
    void getScheduleDescription_simple_schedule_type_null_repeat_count() {
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.formatMessage(QuartzDescriptionService.class, QuartzDescriptionService.EXECUTE_FOREVER_MESSAGE_KEY, "10"))
                .thenReturn("Execute forever every 10 seconds");

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(null);
        when(triggerMock.getRepeatInterval()).thenReturn(10000L);

        QuartzDescriptionService service = new QuartzDescriptionService(messagesMock);
        assertEquals("Execute forever every 10 seconds", service.getScheduleDescription(triggerMock));
    }

    @ParameterizedTest
    @ValueSource(ints = {-100, -3, -1})
    void getScheduleDescription_simple_schedule_type_negative_repeat_count(int repeatCount) {
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.formatMessage(QuartzDescriptionService.class, QuartzDescriptionService.EXECUTE_FOREVER_MESSAGE_KEY, "10"))
                .thenReturn("Execute forever every 10 seconds");

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(repeatCount);
        when(triggerMock.getRepeatInterval()).thenReturn(10000L);

        QuartzDescriptionService service = new QuartzDescriptionService(messagesMock);
        assertEquals("Execute forever every 10 seconds", service.getScheduleDescription(triggerMock));
    }


    @ParameterizedTest
    @CsvSource({"500, 0.5", "540, 0.5", "550, 0.6",  "560, 0.6", "10540, 10.5", "10000, 10", "10000000, 10000"})
    void getScheduleDescription_simple_schedule_type_less_1_second_interval(long interval, String formattedInterval) {
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.formatMessage(eq(QuartzDescriptionService.class), eq(QuartzDescriptionService.EXECUTE_FOREVER_MESSAGE_KEY), anyString()))
                .thenReturn("Some message");

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(-1);
        when(triggerMock.getRepeatInterval()).thenReturn(interval);

        new QuartzDescriptionService(messagesMock).getScheduleDescription(triggerMock);

        verify(messagesMock, only()).formatMessage(QuartzDescriptionService.class, QuartzDescriptionService.EXECUTE_FOREVER_MESSAGE_KEY, formattedInterval);
    }




    @Test
    void getScheduleDescription_simple_schedule_type_0_repeats() {
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.getMessage(QuartzDescriptionService.class, QuartzDescriptionService.EXECUTE_ONCE_MESSAGE_KEY))
                .thenReturn("Execute once message");

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(0);

        QuartzDescriptionService service = new QuartzDescriptionService(messagesMock);
        assertEquals("Execute once message", service.getScheduleDescription(triggerMock));
    }


    @Test
    void getScheduleDescription_simple_schedule_type_several_repeats() {

        int repeatsCount = 10;
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.formatMessage(QuartzDescriptionService.class, QuartzDescriptionService.EXECUTE_SEVERAL_TIMES_MESSAGE_KEY, repeatsCount +1, "10"))
                .thenReturn("Execute once message");

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(repeatsCount);
        when(triggerMock.getRepeatInterval()).thenReturn(10000L);

        QuartzDescriptionService service = new QuartzDescriptionService(messagesMock);
        assertEquals("Execute once message", service.getScheduleDescription(triggerMock));
    }


}