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

package io.jmix.flowui.monitoring;

import io.jmix.core.annotation.Internal;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.observation.ViewLifecycle;
import io.jmix.flowui.view.View;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Brackets a piece of work with the legacy {@link Timer}-based metrics
 * ({@code jmix.ui.data}, {@code jmix.ui.views}) when
 * {@link UiProperties#isLegacyMonitoringEnabled()} is on.
 * <p>
 * Exists solely for back-compat with dashboards built on the legacy tag schema. The whole bean
 * is expected to be removed in a future release once consumers migrate to the modern Observation
 * tag schema; at that point this class — together with the property — can be deleted with no
 * impact on call sites in {@code UiObservationSupport}.
 */
@Internal
@Deprecated(since = "3.0", forRemoval = true)
@Component("flowui_LegacyUiTimerSupport")
public class LegacyUiTimerSupport {

    @Autowired
    protected MeterRegistry meterRegistry;

    @Autowired
    protected UiProperties uiProperties;

    protected boolean isEnabled() {
        return uiProperties.isLegacyMonitoringEnabled();
    }

    public <T> T recordDataLoaderTimer(DataLoader loader, DataLoaderLifeCycle phase, Supplier<T> action) {
        if (!isEnabled()) {
            return action.get();
        }
        Timer.Sample sample = UiMonitoring.startTimerSample(meterRegistry);
        try {
            return action.get();
        } finally {
            DataLoaderMonitoringInfo info = loader.getMonitoringInfoProvider().apply(loader);
            UiMonitoring.stopDataLoaderTimerSample(sample, meterRegistry, phase, info);
        }
    }

    public <T> T recordViewTimer(View<?> view, ViewLifecycle phase, Supplier<T> action) {
        if (!isEnabled()) {
            return action.get();
        }
        Timer.Sample sample = UiMonitoring.startTimerSample(meterRegistry);
        try {
            return action.get();
        } finally {
            UiMonitoring.stopViewTimerSample(sample, meterRegistry,
                    ViewLifeCycle.valueOf(phase.name()), view.getId().orElse(null));
        }
    }
}
