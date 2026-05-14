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

package io.jmix.core.impl;

import io.jmix.core.CoreProperties;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import test_support.TestCoreProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;

class TriggerFileProcessorScheduleConfigurerTest {

    @Test
    void testDoesNotScheduleTriggerProcessingWhenFeatureIsDisabled() {
        TrackingTaskScheduler taskScheduler = new TrackingTaskScheduler();
        CoreProperties properties = TestCoreProperties.builder()
                .setUnsafeRuntimeFeaturesEnabled(false)
                .setTriggerFilesEnabled(true)
                .build();

        TriggerFileProcessorScheduleConfigurer configurer =
                new TriggerFileProcessorScheduleConfigurer(taskScheduler, properties, new TriggerFilesProcessor());

        configurer.onContextRefreshedEvent(null);

        assertThat(taskScheduler.scheduleWithFixedDelayCallCount).isZero();
    }

    @Test
    void testSchedulesTriggerProcessingWhenFeatureIsEnabled() {
        TrackingTaskScheduler taskScheduler = new TrackingTaskScheduler();
        CoreProperties properties = TestCoreProperties.builder()
                .setUnsafeRuntimeFeaturesEnabled(true)
                .setTriggerFilesEnabled(true)
                .setTriggerFilesProcessInterval(Duration.ofSeconds(7))
                .build();

        TriggerFileProcessorScheduleConfigurer configurer =
                new TriggerFileProcessorScheduleConfigurer(taskScheduler, properties, new TriggerFilesProcessor());

        configurer.onContextRefreshedEvent(null);

        assertThat(taskScheduler.scheduleWithFixedDelayCallCount).isEqualTo(1);
        assertThat(taskScheduler.lastDelay).isEqualTo(Duration.ofSeconds(7));
    }

    private static class TrackingTaskScheduler implements TaskScheduler {

        private int scheduleWithFixedDelayCallCount;
        private Duration lastDelay;

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Trigger trigger) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) {
            scheduleWithFixedDelayCallCount++;
            lastDelay = delay;
            return null;
        }
    }
}
