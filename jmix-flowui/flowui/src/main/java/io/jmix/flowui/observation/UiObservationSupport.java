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

package io.jmix.flowui.observation;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.action.TargetAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.monitoring.DataLoaderLifeCycle;
import io.jmix.flowui.monitoring.DataLoaderMonitoringInfo;
import io.jmix.flowui.monitoring.UiMonitoring;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.support.DataComponentsLoaderSupport;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Support class for observing UI events such as view lifecycle events and action executions.
 * <p>
 * This class uses an {@link ObservationRegistry} if observation functionality
 * is enabled in the application configuration.
 * <p>
 * Observation functionality can be turned on or off using the {@code jmix.ui.ui-observation-enabled}
 * property in the application's configuration.
 *
 * @see UiProperties#isUiObservationEnabled()
 */
@org.springframework.stereotype.Component("flowui_UiObservationSupport")
public class UiObservationSupport {

    public static final String VIEW_OBSERVATION_NAME = "jmix.ui.views";
    public static final String FRAGMENT_OBSERVATION_NAME = "jmix.ui.fragments";
    public static final String ACTION_OBSERVATION_NAME = "jmix.ui.actions";
    public static final String DATA_LOADER_OBSERVATION_NAME = "jmix.ui.data";

    protected static final String NOT_AVAILABLE_TAG_VALUE = "N/A";

    @Autowired(required = false)
    protected ObservationRegistry observationRegistry;

    @Autowired
    protected MeterRegistry meterRegistry;

    protected boolean observationEnabled;
    protected boolean dataLoaderLegacyTimerEnabled;

    public UiObservationSupport(UiProperties uiProperties) {
        this.observationEnabled = uiProperties.isUiObservationEnabled();
        this.dataLoaderLegacyTimerEnabled = uiProperties.isDataLoaderLegacyTimerEnabled();
    }

    public Observation createViewLifecycleObservation(View<?> view, ComponentEvent<?> viewEvent) {
        ViewLifecycle viewLifecycle = eventToLifecycle(viewEvent);
        return viewLifecycle != null
                ? createViewLifecycleObservation(view, viewLifecycle)
                : Observation.NOOP;
    }

    public Observation createViewLifecycleObservation(View<?> view, ViewLifecycle lifecycle) {
        return createViewLifecycleObservation(new ViewLifecycleObservationInfo(view), lifecycle);
    }

    public Observation createViewLifecycleObservation(ViewLifecycleObservationInfo observationInfo,
                                                      ViewLifecycle lifecycle) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }

        return Observation.createNotStarted(VIEW_OBSERVATION_NAME, observationRegistry)
                .contextualName("view lifecycle")
                .lowCardinalityKeyValue("lifecycle.name", lifecycle.getName())
                .lowCardinalityKeyValue("view.id", observationInfo.viewId())
                .lowCardinalityKeyValue("view.class", observationInfo.viewClass());
    }

    public Observation createFragmentLifecycleObservation(Fragment<?> fragment, FragmentLifecycle lifecycle) {
        return createFragmentLifecycleObservation(new FragmentLifecycleObservationInfo(fragment), lifecycle);
    }

    public Observation createFragmentLifecycleObservation(FragmentLifecycleObservationInfo observationInfo,
                                                          FragmentLifecycle lifecycle) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }

        Observation observation = Observation.createNotStarted(FRAGMENT_OBSERVATION_NAME, observationRegistry)
                .contextualName("fragment lifecycle")
                .lowCardinalityKeyValue("lifecycle.name", lifecycle.getName())
                .lowCardinalityKeyValue("fragment.class", observationInfo.fragmentClass());

        if (!Strings.isNullOrEmpty(observationInfo.fragmentId())) {
            observation.lowCardinalityKeyValue("fragment.id", observationInfo.fragmentId());
        }

        return observation;
    }

    public Observation createDataLoaderObservation(DataLoader loader, DataLoaderLifeCycle lifecycle) {
        DataLoaderMonitoringInfo info = loader.getMonitoringInfoProvider().apply(loader);
        return createDataLoaderObservation(info, lifecycle);
    }

    /**
     * Records monitoring data for a void {@link DataLoader} lifecycle phase. Always invokes the modern Observation
     * path; additionally writes the legacy {@code jmix.ui.data} {@link Timer} when
     * {@link UiProperties#isDataLoaderLegacyTimerEnabled()} is on (default).
     */
    public void observeDataLoader(DataLoader loader, DataLoaderLifeCycle phase, Runnable action) {
        observeDataLoader(loader, phase, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Records monitoring data for a value-returning {@link DataLoader} lifecycle phase.
     *
     * @see #observeDataLoader(DataLoader, DataLoaderLifeCycle, Runnable)
     */
    public <T> T observeDataLoader(DataLoader loader, DataLoaderLifeCycle phase, Supplier<T> action) {
        DataLoaderMonitoringInfo info = loader.getMonitoringInfoProvider().apply(loader);
        Observation observation = createDataLoaderObservation(info, phase);

        if (!dataLoaderLegacyTimerEnabled) {
            return observation.observe(action);
        }
        Timer.Sample sample = UiMonitoring.startTimerSample(meterRegistry);
        try {
            return observation.observe(action);
        } finally {
            UiMonitoring.stopDataLoaderTimerSample(sample, meterRegistry, phase, info);
        }
    }

    public Observation createDataLoaderObservation(DataLoaderMonitoringInfo info, DataLoaderLifeCycle lifecycle) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }

        String loaderId = info.loaderId();
        if (StringUtils.isBlank(loaderId)
                || loaderId.startsWith(DataComponentsLoaderSupport.GENERATED_PREFIX)) {
            return Observation.NOOP;
        }

        String viewId = info.viewId();
        return Observation.createNotStarted(DATA_LOADER_OBSERVATION_NAME, observationRegistry)
                .contextualName("data loader lifecycle")
                .lowCardinalityKeyValue("lifecycle.name", lifecycle.getName())
                .lowCardinalityKeyValue("loader.id", loaderId)
                .lowCardinalityKeyValue("view.id", Strings.isNullOrEmpty(viewId) ? NOT_AVAILABLE_TAG_VALUE : viewId);
    }

    public Observation createActionExecutionObservation(Action action) {
        return createActionExecutionObservation(action, null);
    }

    public Observation createActionExecutionObservation(Action action, @Nullable Component triggerComponent) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }

        Observation observation = Observation.createNotStarted(ACTION_OBSERVATION_NAME, observationRegistry)
                .contextualName("execute action")
                .lowCardinalityKeyValue("action.id", action.getId());

        Component viewSource = triggerComponent;
        if (action instanceof TargetAction<?> targetAction
                && targetAction.getTarget() instanceof Component component) {
            UiComponentUtils.getComponentId(component)
                    .ifPresent(componentId -> observation.lowCardinalityKeyValue("target.id", componentId));
            viewSource = component;
        }

        if (viewSource != null) {
            View<?> view = UiComponentUtils.findView(viewSource);
            if (view != null) {
                view.getId().ifPresent(viewId -> observation.lowCardinalityKeyValue("view.id", viewId));
            }
        }

        return observation;
    }

    @Nullable
    protected ViewLifecycle eventToLifecycle(ComponentEvent<?> event) {
        if (event instanceof View.InitEvent) {
            return ViewLifecycle.INIT;
        }

        if (event instanceof View.BeforeShowEvent) {
            return ViewLifecycle.BEFORE_SHOW;
        }

        if (event instanceof View.ReadyEvent) {
            return ViewLifecycle.READY;
        }

        if (event instanceof View.BeforeCloseEvent) {
            return ViewLifecycle.BEFORE_CLOSE;
        }

        if (event instanceof View.AfterCloseEvent) {
            return ViewLifecycle.AFTER_CLOSE;
        }

        return null;
    }

    protected boolean isObservationAvailable() {
        return observationEnabled && observationRegistry != null;
    }
}
