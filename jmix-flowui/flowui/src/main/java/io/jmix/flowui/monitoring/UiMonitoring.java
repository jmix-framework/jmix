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

package io.jmix.flowui.monitoring;

import io.jmix.flowui.model.impl.ViewDataXmlLoader;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

import java.util.Objects;

public class UiMonitoring {

    private static final String NOT_AVAILABLE_TAG_VALUE = "N/A";
    private static final String VIEWS_BASE_NAME = "jmix.ui.views";
    private static final String DATA_BASE_NAME = "jmix.ui.data";
    private static final String VIEW_TAG = "view";
    private static final String DATA_LOADER_TAG = "dataLoader";
    private static final String LIFE_CYCLE_TAG = "lifeCycle";

    private UiMonitoring() {
    }

    public static Timer.Sample startTimerSample(MeterRegistry meterRegistry) {
        return Timer.start(meterRegistry);
    }

    public static void stopDataLoaderTimerSample(Timer.Sample sample,
                                                 MeterRegistry meterRegistry,
                                                 DataLoaderLifeCycle lifeCycle,
                                                 DataLoaderMonitoringInfo info) {
        if (!canDataLoaderBeMonitored(lifeCycle, info)) {
            return;
        }
        sample.stop(createDataLoaderTimer(meterRegistry, lifeCycle, handleNullTag(info.viewId()), info.loaderId()));
    }

    public static void stopViewTimerSample(Timer.Sample sample,
                                           MeterRegistry meterRegistry,
                                           ViewLifeCycle lifeCycle,
                                           @Nullable String viewId) {
        if (!canViewBeMonitored(lifeCycle, viewId)) {
            return;
        }

        sample.stop(createViewTimer(meterRegistry, lifeCycle, Objects.requireNonNull(viewId)));
    }

    protected static Timer createViewTimer(MeterRegistry meterRegistry, ViewLifeCycle lifeCycle, String viewId) {
        return meterRegistry.timer(VIEWS_BASE_NAME, VIEW_TAG, viewId, LIFE_CYCLE_TAG, lifeCycle.getName());
    }

    protected static Timer createDataLoaderTimer(MeterRegistry meterRegistry,
                                                 DataLoaderLifeCycle lifeCycle,
                                                 String viewId, String loaderId) {
        return meterRegistry.timer(
                DATA_BASE_NAME, DATA_LOADER_TAG, loaderId, VIEW_TAG, viewId, LIFE_CYCLE_TAG, lifeCycle.getName()
        );
    }

    protected static boolean canDataLoaderBeMonitored(@Nullable DataLoaderLifeCycle lifeCycle,
                                                      @Nullable DataLoaderMonitoringInfo monitoringInfo) {
        if (monitoringInfo == null || lifeCycle == null) {
            return false;
        }
        String loaderId = monitoringInfo.loaderId();
        return !StringUtils.isBlank(loaderId)
                && !loaderId.startsWith(ViewDataXmlLoader.GENERATED_PREFIX);
    }

    protected static boolean canViewBeMonitored(@Nullable ViewLifeCycle lifeCycle, @Nullable String viewId) {
        return lifeCycle != null && !StringUtils.isBlank(viewId);
    }

    protected static String handleNullTag(@Nullable String tag) {
        return tag == null ? NOT_AVAILABLE_TAG_VALUE : tag;
    }
}
