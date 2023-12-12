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
import io.jmix.quartz.model.TriggerModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;

import static io.jmix.quartz.model.ScheduleType.CRON_EXPRESSION;
import static io.jmix.quartz.model.ScheduleType.SIMPLE;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleDescriptionProviderTest {

    public static final String CRON_EXPRESSION_EXAMPLE = "0/5 * * * * ?";
    public static final String CRON_EXPRESSION_EXAMPLE_2 = "0 * * * * ?";
    public static final String EXECUTE_ONCE_MESSAGE = "Execute once";

    @Test
    void getScheduleDescription_null_schedule_type() {
        Messages messagesMock = mock(Messages.class);
        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(null);

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(messagesMock);
        assertNull(service.getScheduleDescription(triggerMock));
    }

    @Test
    void getScheduleDescription_cron_schedule_type() {
        Messages messagesMock = mock(Messages.class);
        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(CRON_EXPRESSION);
        String cronExpression = CRON_EXPRESSION_EXAMPLE;
        when(triggerMock.getCronExpression()).thenReturn(cronExpression);

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(messagesMock);
        assertEquals(cronExpression, service.getScheduleDescription(triggerMock));
    }

    @Test
    void getScheduleDescription_simple_schedule_type_null_repeat_count() {
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.formatMessage(ScheduleDescriptionProvider.class, ScheduleDescriptionProvider.EXECUTE_FOREVER_MESSAGE_KEY, "10"))
                .thenReturn("Execute forever every 10 seconds");

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(null);
        when(triggerMock.getRepeatInterval()).thenReturn(10000L);

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(messagesMock);
        assertEquals("Execute forever every 10 seconds", service.getScheduleDescription(triggerMock));
    }

    @Test
    void getScheduleDescription_simple_schedule_type_negative_repeat_count() {
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.formatMessage(ScheduleDescriptionProvider.class, ScheduleDescriptionProvider.EXECUTE_FOREVER_MESSAGE_KEY, "10"))
                .thenReturn("Execute forever every 10 seconds");

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(-1);
        when(triggerMock.getRepeatInterval()).thenReturn(10000L);

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(messagesMock);
        assertEquals("Execute forever every 10 seconds", service.getScheduleDescription(triggerMock));
    }


    @ParameterizedTest
    @CsvSource({"500, 0.5", "540, 0.5", "550, 0.6", "560, 0.6", "10540, 10.5", "10000, 10", "10000000, 10000"})
    void getScheduleDescription_simple_schedule_repeat_interval_formatting(long repeatInterval, String formattedRepeatInterval) {
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.formatMessage(eq(ScheduleDescriptionProvider.class), eq(ScheduleDescriptionProvider.EXECUTE_FOREVER_MESSAGE_KEY), anyString()))
                .thenReturn("Some message");

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(-1);
        when(triggerMock.getRepeatInterval()).thenReturn(repeatInterval);

        new ScheduleDescriptionProvider(messagesMock).getScheduleDescription(triggerMock);

        verify(messagesMock, only()).formatMessage(ScheduleDescriptionProvider.class, ScheduleDescriptionProvider.EXECUTE_FOREVER_MESSAGE_KEY, formattedRepeatInterval);
    }


    @Test
    void getScheduleDescription_simple_schedule_type_0_repeats() {
        Messages messagesMock = mock(Messages.class);
        when(messagesMock.getMessage(ScheduleDescriptionProvider.class, ScheduleDescriptionProvider.EXECUTE_ONCE_MESSAGE_KEY))
                .thenReturn(EXECUTE_ONCE_MESSAGE);

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(0);

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(messagesMock);
        assertEquals(EXECUTE_ONCE_MESSAGE, service.getScheduleDescription(triggerMock));
    }


    @ParameterizedTest
    @ValueSource(ints = {10, 20, 100})
    void getScheduleDescription_simple_schedule_type_several_repeats(int repeatsCount) {
        Messages messagesMock = mock(Messages.class);
        int incrementedRepeatsCount = repeatsCount + 1;
        String correctMessage = "Execute " + incrementedRepeatsCount + " times every 10 seconds";
        when(messagesMock.formatMessage(ScheduleDescriptionProvider.class, ScheduleDescriptionProvider.EXECUTE_SEVERAL_TIMES_MESSAGE_KEY, incrementedRepeatsCount, "10"))
                .thenReturn(correctMessage);

        TriggerModel triggerMock = mock(TriggerModel.class);
        when(triggerMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerMock.getRepeatCount()).thenReturn(repeatsCount);
        when(triggerMock.getRepeatInterval()).thenReturn(10000L);

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(messagesMock);
        assertEquals(correctMessage, service.getScheduleDescription(triggerMock));
    }

    @Test
    void getScheduleDescription_for_job_without_triggers() {
        JobModel jobModelMock = mock(JobModel.class);
        when(jobModelMock.getTriggers()).thenReturn(Collections.emptyList());

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(mock(Messages.class));
        assertNull(service.getScheduleDescription(jobModelMock));
    }


    @Test
    void getScheduleDescription_for_job_one_trigger() {
        JobModel jobModelMock = mock(JobModel.class);
        TriggerModel triggerModelMock = mock(TriggerModel.class);
        when(triggerModelMock.getScheduleType()).thenReturn(CRON_EXPRESSION);
        when(triggerModelMock.getCronExpression()).thenReturn(CRON_EXPRESSION_EXAMPLE);
        when(jobModelMock.getTriggers()).thenReturn(asList(triggerModelMock));

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(mock(Messages.class));
        assertEquals(CRON_EXPRESSION_EXAMPLE, service.getScheduleDescription(jobModelMock));
    }

    @Test
    void getScheduleDescription_for_job_some_trigger() {
        JobModel jobModelMock = mock(JobModel.class);
        TriggerModel triggerModelMock = mock(TriggerModel.class);
        when(triggerModelMock.getScheduleType()).thenReturn(CRON_EXPRESSION);
        when(triggerModelMock.getCronExpression()).thenReturn(CRON_EXPRESSION_EXAMPLE);

        TriggerModel triggerModelMock2 = mock(TriggerModel.class);
        when(triggerModelMock2.getScheduleType()).thenReturn(CRON_EXPRESSION);
        when(triggerModelMock2.getCronExpression()).thenReturn(CRON_EXPRESSION_EXAMPLE_2);

        when(jobModelMock.getTriggers()).thenReturn(asList(triggerModelMock, triggerModelMock2));

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(mock(Messages.class));
        assertEquals(CRON_EXPRESSION_EXAMPLE+", "+CRON_EXPRESSION_EXAMPLE_2, service.getScheduleDescription(jobModelMock));
    }

    @Test
    void getScheduleDescription_empty_repeat_interval_for_simple_trigger() {
        JobModel jobModelMock = mock(JobModel.class);
        TriggerModel triggerModelMock = mock(TriggerModel.class);
        when(triggerModelMock.getScheduleType()).thenReturn(SIMPLE);
        when(triggerModelMock.getRepeatInterval()).thenReturn(null);



        when(jobModelMock.getTriggers()).thenReturn(List.of(triggerModelMock));

        ScheduleDescriptionProvider service = new ScheduleDescriptionProvider(mock(Messages.class));
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> service.getScheduleDescription(jobModelMock));
        assertEquals("\"repeatInterval\" field for simple trigger shouldn't be null", exception.getMessage());
    }

}