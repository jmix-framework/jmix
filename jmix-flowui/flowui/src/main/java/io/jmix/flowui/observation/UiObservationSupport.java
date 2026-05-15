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
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.UiProperties;
import io.jmix.flowui.action.TargetAction;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.monitoring.DataLoaderLifeCycle;
import io.jmix.flowui.monitoring.LegacyUiTimerSupport;
import io.jmix.flowui.view.View;
import io.jmix.flowui.xml.layout.support.DataComponentsLoaderSupport;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import jakarta.annotation.PostConstruct;
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
@Experimental
@org.springframework.stereotype.Component("flowui_UiObservationSupport")
public class UiObservationSupport {

    public static final String VIEW_OBSERVATION_NAME = "jmix.ui.views";
    public static final String FRAGMENT_OBSERVATION_NAME = "jmix.ui.fragments";
    public static final String ACTION_OBSERVATION_NAME = "jmix.ui.actions";
    public static final String DATA_LOADER_OBSERVATION_NAME = "jmix.ui.data";

    /**
     * Sentinel for the low-cardinality {@code view.id} tag when the enclosing view cannot be resolved
     * or has no explicit id.
     */
    protected static final String MISSING_VIEW_ID = "N/A";

    /**
     * Sentinel for the low-cardinality {@code fragment.id} tag when there is no enclosing fragment or
     * the fragment has no explicit id. The tag is added unconditionally so the Prometheus meter for a
     * given metric name always carries the same set of tag keys — otherwise conditionally-added tags
     * make {@link io.micrometer.prometheusmetrics.PrometheusMeterRegistry} reject one of the
     * registrations and silently drop a slice of the data.
     */
    protected static final String MISSING_FRAGMENT_ID = "N/A";

    /** Same rationale as {@link #MISSING_FRAGMENT_ID}, for {@code target.id} of action observations. */
    protected static final String MISSING_TARGET_ID = "N/A";

    /**
     * Sentinel for the low-cardinality {@code loader.id} tag when the loader carries an auto-generated
     * id (prefix {@code generated_}). Aggregates all anonymous loaders into a single time-series; the
     * original id is preserved as a high-cardinality {@code full_loader_id} attribute on the span.
     */
    protected static final String GENERATED_LOADER_ID_SENTINEL = "<generated>";

    @Autowired(required = false)
    protected ObservationRegistry observationRegistry;

    @Autowired(required = false)
    protected MeterRegistry meterRegistry;

    @Autowired
    protected LegacyUiTimerSupport legacyUiTimerSupport;

    protected boolean observationEnabled;
    protected boolean legacyMonitoringEnabled;

    public UiObservationSupport(UiProperties uiProperties) {
        this.observationEnabled = uiProperties.isUiObservationEnabled();
        this.legacyMonitoringEnabled = uiProperties.isLegacyMonitoringEnabled();
    }

    /**
     * In legacy mode the {@code jmix.ui.*} metrics must look exactly like Jmix 2.x — only the legacy
     * Timer with the old tag schema. The installed {@link MeterFilter} denies any registration under
     * {@code jmix.ui.*} that carries the modern-schema marker tag {@code lifecycle.name}, filtering
     * out the Timer and LongTaskTimer that {@code DefaultMeterObservationHandler} would otherwise add.
     * Tracing spans are unaffected — they don't go through the {@link MeterRegistry}.
     */
    @PostConstruct
    protected void suppressObservationMetersInLegacyMode() {
        if (meterRegistry == null || !legacyMonitoringEnabled) {
            return;
        }
        meterRegistry.config().meterFilter(MeterFilter.deny(id -> {
            String name = id.getName();
            return name != null
                    && name.startsWith("jmix.ui.")
                    && id.getTags().stream().anyMatch(t -> "lifecycle.name".equals(t.getKey()));
        }));
    }

    /**
     * Records monitoring data for a void {@link DataLoader} lifecycle phase.
     */
    public void observeDataLoader(DataLoader loader, DataLoaderLifeCycle phase, Runnable action) {
        observeDataLoader(loader, phase, () -> {
            action.run();
            return null;
        });
    }

    /**
     * @see #observeDataLoader(DataLoader, DataLoaderLifeCycle, Runnable)
     */
    public <T> T observeDataLoader(DataLoader loader, DataLoaderLifeCycle phase, Supplier<T> action) {
        Observation observation = buildDataLoaderObservation(loader, phase);
        return observation.observe(() ->
                legacyUiTimerSupport.recordDataLoaderTimer(loader, phase, action));
    }

    /**
     * Builds the data loader Observation lazily — observation availability is checked before invoking
     * {@link DataLoader#getObservationInfoProvider()}, since that provider can walk the UI tree to
     * resolve fragment/view context (see R8) and should not run when observation is disabled.
     */
    private Observation buildDataLoaderObservation(DataLoader loader, DataLoaderLifeCycle lifecycle) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }
        DataLoaderObservationInfo info = loader.getObservationInfoProvider().apply(loader);
        String loaderId = info.loaderId();
        if (StringUtils.isBlank(loaderId)) {
            return Observation.NOOP;
        }

        String aggregatedLoaderId = loaderId.startsWith(DataComponentsLoaderSupport.GENERATED_PREFIX)
                ? GENERATED_LOADER_ID_SENTINEL
                : loaderId;

        String viewId = info.viewId();
        return Observation.createNotStarted(DATA_LOADER_OBSERVATION_NAME, observationRegistry)
                .contextualName("data loader lifecycle")
                .lowCardinalityKeyValue("lifecycle.name", lifecycle.getName())
                .lowCardinalityKeyValue("loader.id", aggregatedLoaderId)
                .highCardinalityKeyValue("full_loader_id", loaderId)
                .lowCardinalityKeyValue("view.id", Strings.isNullOrEmpty(viewId) ? MISSING_VIEW_ID : viewId)
                .lowCardinalityKeyValue("fragment.id",
                        Strings.isNullOrEmpty(info.fragmentId()) ? MISSING_FRAGMENT_ID : info.fragmentId());
    }

    /**
     * Records monitoring data for a void {@link View} lifecycle phase.
     */
    public void observeViewLifecycle(View<?> view, ViewLifecycle phase, Runnable action) {
        observeViewLifecycle(view, phase, () -> {
            action.run();
            return null;
        });
    }

    /**
     * @see #observeViewLifecycle(View, ViewLifecycle, Runnable)
     */
    public <T> T observeViewLifecycle(View<?> view, ViewLifecycle phase, Supplier<T> action) {
        Observation observation = buildViewLifecycleObservation(view, phase);
        return observation.observe(() ->
                legacyUiTimerSupport.recordViewTimer(view, phase, action));
    }

    private Observation buildViewLifecycleObservation(View<?> view, ViewLifecycle lifecycle) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }
        String viewId = view.getId().orElse(null);
        return Observation.createNotStarted(VIEW_OBSERVATION_NAME, observationRegistry)
                .contextualName("view lifecycle")
                .lowCardinalityKeyValue("lifecycle.name", lifecycle.getName())
                .lowCardinalityKeyValue("view.id",
                        Strings.isNullOrEmpty(viewId) ? MISSING_VIEW_ID : viewId)
                .lowCardinalityKeyValue("view.class", view.getClass().getName());
    }

    /**
     * Records monitoring data for a void {@link Fragment} lifecycle phase.
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
        return observeFragmentLifecycle(new FragmentObservationInfo(fragment), phase, action);
    }

    /**
     * @see #observeFragmentLifecycle(Fragment, FragmentLifecycle, Runnable)
     */
    public <T> T observeFragmentLifecycle(FragmentObservationInfo info, FragmentLifecycle phase,
                                          Supplier<T> action) {
        return buildFragmentLifecycleObservation(info, phase).observe(action);
    }

    private Observation buildFragmentLifecycleObservation(FragmentObservationInfo info,
                                                          FragmentLifecycle lifecycle) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }
        return Observation.createNotStarted(FRAGMENT_OBSERVATION_NAME, observationRegistry)
                .contextualName("fragment lifecycle")
                .lowCardinalityKeyValue("lifecycle.name", lifecycle.getName())
                .lowCardinalityKeyValue("fragment.class", info.fragmentClass())
                .lowCardinalityKeyValue("view.id",
                        Strings.isNullOrEmpty(info.viewId()) ? MISSING_VIEW_ID : info.viewId())
                .lowCardinalityKeyValue("fragment.id",
                        Strings.isNullOrEmpty(info.fragmentId()) ? MISSING_FRAGMENT_ID : info.fragmentId());
    }

    public Observation createActionExecutionObservation(Action action, @Nullable Component triggerComponent) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }

        Component viewSource = triggerComponent;
        String resolvedTargetId = null;
        if (action instanceof TargetAction<?> targetAction
                && targetAction.getTarget() instanceof Component component) {
            resolvedTargetId = UiComponentUtils.getComponentId(component).orElse(null);
            viewSource = component;
        }

        String resolvedViewId = null;
        String resolvedFragmentId = null;
        if (viewSource != null) {
            View<?> view = UiComponentUtils.findView(viewSource);
            if (view != null) {
                resolvedViewId = view.getId().orElse(null);
            }

            Fragment<?> fragment = UiComponentUtils.findFragment(viewSource);
            if (fragment != null) {
                resolvedFragmentId = fragment.getId().orElse(null);
            }
        }

        return Observation.createNotStarted(ACTION_OBSERVATION_NAME, observationRegistry)
                .contextualName("execute action")
                .lowCardinalityKeyValue("action.id", action.getId())
                .lowCardinalityKeyValue("target.id",
                        Strings.isNullOrEmpty(resolvedTargetId) ? MISSING_TARGET_ID : resolvedTargetId)
                .lowCardinalityKeyValue("view.id",
                        Strings.isNullOrEmpty(resolvedViewId) ? MISSING_VIEW_ID : resolvedViewId)
                .lowCardinalityKeyValue("fragment.id",
                        Strings.isNullOrEmpty(resolvedFragmentId) ? MISSING_FRAGMENT_ID : resolvedFragmentId);
    }

    protected boolean isObservationAvailable() {
        return observationEnabled && observationRegistry != null;
    }
}
