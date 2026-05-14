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
import io.jmix.flowui.monitoring.LegacyUiTimerSupport;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.support.DataComponentsLoaderSupport;
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

    /**
     * Sentinel for the low-cardinality {@code view.id} tag when the enclosing view cannot be resolved or
     * has no explicit id. Applied uniformly to all observation types so dashboards filtering by
     * {@code view.id} have a consistent value to query, and so Grafana variable dropdowns surface the
     * anonymous bucket alongside named views (use {@code view.class} / {@code fragment.class} to drill
     * down into which classes lack ids).
     */
    protected static final String MISSING_VIEW_ID = "N/A";

    /**
     * Sentinel for the low-cardinality {@code loader.id} tag when the loader carries an auto-generated id
     * (prefix {@code generated_}). Aggregates all anonymous loaders into a single Prometheus time-series
     * instead of producing one per instance. The original id is preserved as a high-cardinality
     * {@code full_loader_id} attribute on the span for trace-level identification.
     */
    protected static final String GENERATED_LOADER_ID_SENTINEL = "<generated>";

    @Autowired(required = false)
    protected ObservationRegistry observationRegistry;

    @Autowired
    protected LegacyUiTimerSupport legacyUiTimerSupport;

    protected boolean observationEnabled;

    public UiObservationSupport(UiProperties uiProperties) {
        this.observationEnabled = uiProperties.isUiObservationEnabled();
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
                .lowCardinalityKeyValue("view.id",
                        Strings.isNullOrEmpty(observationInfo.viewId()) ? MISSING_VIEW_ID : observationInfo.viewId())
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
                .lowCardinalityKeyValue("fragment.class", observationInfo.fragmentClass())
                .lowCardinalityKeyValue("view.id",
                        Strings.isNullOrEmpty(observationInfo.viewId()) ? MISSING_VIEW_ID : observationInfo.viewId());

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
     * path; legacy {@code jmix.ui.data} Timer recording is delegated to {@link LegacyUiTimerSupport}.
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
        Observation observation = createDataLoaderObservation(loader, phase);
        return legacyUiTimerSupport.recordDataLoaderTimer(loader, phase,
                () -> observation.observe(action));
    }

    /**
     * Records monitoring data for a void {@link View} lifecycle phase. Always invokes the modern Observation path;
     * legacy {@code jmix.ui.views} Timer recording is delegated to {@link LegacyUiTimerSupport}.
     */
    public void observeViewLifecycle(View<?> view, ViewLifecycle phase, Runnable action) {
        observeViewLifecycle(view, phase, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Records monitoring data for a value-returning {@link View} lifecycle phase.
     *
     * @see #observeViewLifecycle(View, ViewLifecycle, Runnable)
     */
    public <T> T observeViewLifecycle(View<?> view, ViewLifecycle phase, Supplier<T> action) {
        Observation observation = createViewLifecycleObservation(view, phase);
        return legacyUiTimerSupport.recordViewTimer(view, phase,
                () -> observation.observe(action));
    }

    /**
     * Records monitoring data for a void {@link Fragment} lifecycle phase via the modern Observation path.
     * <p>
     * Note: unlike data loaders and views, fragments never had a legacy {@link io.micrometer.core.instrument.Timer}-based metric, so
     * {@link UiProperties#isLegacyTimerEnabled()} has no effect here. No new legacy metric is introduced.
     */
    public void observeFragmentLifecycle(Fragment<?> fragment, FragmentLifecycle phase, Runnable action) {
        observeFragmentLifecycle(fragment, phase, () -> {
            action.run();
            return null;
        });
    }

    /**
     * Records monitoring data for a value-returning {@link Fragment} lifecycle phase.
     *
     * @see #observeFragmentLifecycle(Fragment, FragmentLifecycle, Runnable)
     */
    public <T> T observeFragmentLifecycle(Fragment<?> fragment, FragmentLifecycle phase, Supplier<T> action) {
        return createFragmentLifecycleObservation(fragment, phase).observe(action);
    }

    /**
     * @see #observeFragmentLifecycle(Fragment, FragmentLifecycle, Runnable)
     */
    public <T> T observeFragmentLifecycle(FragmentLifecycleObservationInfo info, FragmentLifecycle phase,
                                          Supplier<T> action) {
        return createFragmentLifecycleObservation(info, phase).observe(action);
    }

    public Observation createDataLoaderObservation(DataLoaderMonitoringInfo info, DataLoaderLifeCycle lifecycle) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }

        String loaderId = info.loaderId();
        if (StringUtils.isBlank(loaderId)) {
            return Observation.NOOP;
        }

        // Generated id → sentinel in the low-cardinality `loader.id` tag (keeps Prometheus
        // cardinality bounded), original preserved as high-cardinality attribute for trace search.
        String aggregatedLoaderId = loaderId.startsWith(DataComponentsLoaderSupport.GENERATED_PREFIX)
                ? GENERATED_LOADER_ID_SENTINEL
                : loaderId;

        String viewId = info.viewId();
        Observation observation = Observation.createNotStarted(DATA_LOADER_OBSERVATION_NAME, observationRegistry)
                .contextualName("data loader lifecycle")
                .lowCardinalityKeyValue("lifecycle.name", lifecycle.getName())
                .lowCardinalityKeyValue("loader.id", aggregatedLoaderId)
                .highCardinalityKeyValue("full_loader_id", loaderId)
                .lowCardinalityKeyValue("view.id", Strings.isNullOrEmpty(viewId) ? MISSING_VIEW_ID : viewId);

        if (!Strings.isNullOrEmpty(info.fragmentId())) {
            observation.lowCardinalityKeyValue("fragment.id", info.fragmentId());
        }

        return observation;
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

        String resolvedViewId = null;
        if (viewSource != null) {
            View<?> view = UiComponentUtils.findView(viewSource);
            if (view != null) {
                resolvedViewId = view.getId().orElse(null);
            }

            Fragment<?> fragment = UiComponentUtils.findFragment(viewSource);
            if (fragment != null) {
                fragment.getId().ifPresent(fragmentId ->
                        observation.lowCardinalityKeyValue("fragment.id", fragmentId));
            }
        }
        observation.lowCardinalityKeyValue("view.id",
                Strings.isNullOrEmpty(resolvedViewId) ? MISSING_VIEW_ID : resolvedViewId);

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
