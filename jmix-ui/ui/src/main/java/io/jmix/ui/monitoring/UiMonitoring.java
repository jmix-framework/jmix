/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.monitoring;

import io.jmix.ui.model.impl.ScreenDataXmlLoader;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * Logger class for UI performance monitoring.
 */
public final class UiMonitoring {
    private static final String NOT_AVAILABLE_TAG_VALUE = "N/A";
    private static final String SCREENS_BASE_NAME = "jmix.ui.screens";
    private static final String DATA_BASE_NAME = "jmix.ui.data";
    private static final String MENU_BASE_NAME = "jmix.ui.menu";
    private static final String SCREEN_TAG = "screen";
    private static final String MENU_TAG = "menuItem";
    private static final String DATA_LOADER_TAG = "dataLoader";
    private static final String LIFE_CYCLE_TAG = "lifeCycle";

    private UiMonitoring() {
    }

    public static Timer createScreenTimer(MeterRegistry meterRegistry, ScreenLifeCycle lifeCycle, String screen) {
        return meterRegistry.timer(SCREENS_BASE_NAME, SCREEN_TAG, screen, LIFE_CYCLE_TAG, lifeCycle.getName());
    }

    public static Timer createMenuTimer(MeterRegistry meterRegistry, String menuItemId) {
        return meterRegistry.timer(MENU_BASE_NAME, MENU_TAG, menuItemId);
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
        sample.stop(createDataLoaderTimer(
                        meterRegistry, lifeCycle, handleNullTag(info.screenId()), handleNullTag(info.loaderId())
                )
        );
    }


    protected static Timer createDataLoaderTimer(MeterRegistry meterRegistry,
                                                 DataLoaderLifeCycle lifeCycle,
                                                 String screenId, String loaderId) {
        return meterRegistry.timer(
                DATA_BASE_NAME, DATA_LOADER_TAG, loaderId, SCREEN_TAG, screenId, LIFE_CYCLE_TAG, lifeCycle.getName()
        );
    }

    protected static boolean canDataLoaderBeMonitored(@Nullable DataLoaderLifeCycle lifeCycle,
                                                      @Nullable DataLoaderMonitoringInfo monitoringInfo) {
        if (monitoringInfo == null || lifeCycle == null) {
            return false;
        }
        String loaderId = monitoringInfo.loaderId();
        return !StringUtils.isBlank(loaderId)
                && !loaderId.startsWith(ScreenDataXmlLoader.GENERATED_PREFIX);
    }

    /**
     * Prevents null from being tag value.
     * Actual sanity check should be performed before and prevent monitoring at all.
     */
    protected static String handleNullTag(@Nullable String tag) {
        return tag == null ? NOT_AVAILABLE_TAG_VALUE : tag;
    }
}