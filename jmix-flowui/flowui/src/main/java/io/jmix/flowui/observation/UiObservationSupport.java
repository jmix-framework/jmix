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
@Experimental
@org.springframework.stereotype.Component("flowui_UiObservationSupport")
public class UiObservationSupport {

    /**
     * Shared prefix of every {@code jmix.ui.*} observation/metric name produced by this support class.
     */
    public static final String OBSERVATION_NAME_PREFIX = "jmix.ui.";

    public static final String VIEW_OBSERVATION_NAME = OBSERVATION_NAME_PREFIX + "views";
    public static final String FRAGMENT_OBSERVATION_NAME = OBSERVATION_NAME_PREFIX + "fragments";
    public static final String ACTION_OBSERVATION_NAME = OBSERVATION_NAME_PREFIX + "actions";
    public static final String DATA_LOADER_OBSERVATION_NAME = OBSERVATION_NAME_PREFIX + "data";

    /**
     * Human-readable contextual name passed to the {@link Observation} of view lifecycle phases.
     */
    public static final String VIEW_CONTEXTUAL_NAME = "view lifecycle";
    /**
     * Human-readable contextual name passed to the {@link Observation} of fragment lifecycle phases.
     */
    public static final String FRAGMENT_CONTEXTUAL_NAME = "fragment lifecycle";
    /**
     * Human-readable contextual name passed to the {@link Observation} of data loader lifecycle phases.
     */
    public static final String DATA_LOADER_CONTEXTUAL_NAME = "data loader lifecycle";
    /**
     * Human-readable contextual name passed to the {@link Observation} of action executions.
     */
    public static final String ACTION_CONTEXTUAL_NAME = "execute action";

    /**
     * Tag key carrying the modern-schema lifecycle phase name on view / fragment / data-loader
     * observations. Acts as the marker that distinguishes modern Observation meters from the
     * legacy {@code Timer}-based schema; the legacy stack uses it to suppress modern Timer
     * registrations under the same {@code jmix.ui.*} names (see {@code LegacyUiTimerSupport}).
     */
    public static final String LIFECYCLE_NAME_TAG = "lifecycle.name";

    /**
     * Low-cardinality tag key carrying the enclosing view's id.
     */
    public static final String VIEW_ID_TAG = "view.id";
    /**
     * Low-cardinality tag key carrying the FQN of the view's class.
     */
    public static final String VIEW_CLASS_TAG = "view.class";
    /**
     * Low-cardinality tag key carrying the enclosing fragment's id.
     */
    public static final String FRAGMENT_ID_TAG = "fragment.id";
    /**
     * Low-cardinality tag key carrying the FQN of the fragment's class.
     */
    public static final String FRAGMENT_CLASS_TAG = "fragment.class";
    /**
     * Low-cardinality tag key carrying the data loader's id (or {@link #GENERATED_LOADER_ID_SENTINEL}).
     */
    public static final String LOADER_ID_TAG = "loader.id";
    /**
     * High-cardinality span attribute key preserving the original loader id when {@link #LOADER_ID_TAG} collapses to the sentinel.
     */
    public static final String FULL_LOADER_ID_TAG = "full_loader_id";
    /**
     * Low-cardinality tag key carrying the action's id on action-execution observations.
     */
    public static final String ACTION_ID_TAG = "action.id";
    /**
     * Low-cardinality tag key carrying the target component's id on action-execution observations.
     */
    public static final String TARGET_ID_TAG = "target.id";

    /**
     * Sentinel value placed into low-cardinality tags when the underlying id cannot be resolved
     * (view/fragment/target absent or without an explicit id). The tag is added unconditionally so
     * the Prometheus meter for a given metric name always carries the same set of tag keys —
     * otherwise conditionally-added tags make
     * {@code PrometheusMeterRegistry} reject one of the registrations and silently drop a slice
     * of the data.
     */
    private static final String NOT_AVAILABLE = "N/A";

    /**
     * Sentinel for the low-cardinality {@code view.id} tag — see {@link #NOT_AVAILABLE}.
     */
    protected static final String MISSING_VIEW_ID = NOT_AVAILABLE;

    /**
     * Sentinel for the low-cardinality {@code fragment.id} tag — see {@link #NOT_AVAILABLE}.
     */
    protected static final String MISSING_FRAGMENT_ID = NOT_AVAILABLE;

    /**
     * Sentinel for the low-cardinality {@code target.id} tag of action observations — see {@link #NOT_AVAILABLE}.
     */
    protected static final String MISSING_TARGET_ID = NOT_AVAILABLE;

    /**
     * Sentinel for the low-cardinality {@code loader.id} tag when the loader carries an auto-generated
     * id (prefix {@code generated_}). Aggregates all anonymous loaders into a single time-series; the
     * original id is preserved as a high-cardinality {@code full_loader_id} attribute on the span.
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
    protected Observation buildDataLoaderObservation(DataLoader loader, DataLoaderLifeCycle lifecycle) {
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
                .contextualName(DATA_LOADER_CONTEXTUAL_NAME)
                .lowCardinalityKeyValue(LIFECYCLE_NAME_TAG, lifecycle.getName())
                .lowCardinalityKeyValue(LOADER_ID_TAG, aggregatedLoaderId)
                .highCardinalityKeyValue(FULL_LOADER_ID_TAG, loaderId)
                .lowCardinalityKeyValue(VIEW_ID_TAG, Strings.isNullOrEmpty(viewId) ? MISSING_VIEW_ID : viewId)
                .lowCardinalityKeyValue(FRAGMENT_ID_TAG,
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

    protected Observation buildViewLifecycleObservation(View<?> view, ViewLifecycle lifecycle) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }
        String viewId = view.getId().orElse(null);
        return Observation.createNotStarted(VIEW_OBSERVATION_NAME, observationRegistry)
                .contextualName(VIEW_CONTEXTUAL_NAME)
                .lowCardinalityKeyValue(LIFECYCLE_NAME_TAG, lifecycle.getName())
                .lowCardinalityKeyValue(VIEW_ID_TAG,
                        Strings.isNullOrEmpty(viewId) ? MISSING_VIEW_ID : viewId)
                .lowCardinalityKeyValue(VIEW_CLASS_TAG, view.getClass().getName());
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

    protected Observation buildFragmentLifecycleObservation(FragmentObservationInfo info,
                                                            FragmentLifecycle lifecycle) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }
        return Observation.createNotStarted(FRAGMENT_OBSERVATION_NAME, observationRegistry)
                .contextualName(FRAGMENT_CONTEXTUAL_NAME)
                .lowCardinalityKeyValue(LIFECYCLE_NAME_TAG, lifecycle.getName())
                .lowCardinalityKeyValue(FRAGMENT_CLASS_TAG, info.fragmentClass())
                .lowCardinalityKeyValue(VIEW_ID_TAG,
                        Strings.isNullOrEmpty(info.viewId()) ? MISSING_VIEW_ID : info.viewId())
                .lowCardinalityKeyValue(FRAGMENT_ID_TAG,
                        Strings.isNullOrEmpty(info.fragmentId()) ? MISSING_FRAGMENT_ID : info.fragmentId());
    }

    public Observation createActionExecutionObservation(Action action, @Nullable Component invocationSource) {
        if (!isObservationAvailable()) {
            return Observation.NOOP;
        }

        Component viewSource = invocationSource;
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
                .contextualName(ACTION_CONTEXTUAL_NAME)
                .lowCardinalityKeyValue(ACTION_ID_TAG, action.getId())
                .lowCardinalityKeyValue(TARGET_ID_TAG,
                        Strings.isNullOrEmpty(resolvedTargetId) ? MISSING_TARGET_ID : resolvedTargetId)
                .lowCardinalityKeyValue(VIEW_ID_TAG,
                        Strings.isNullOrEmpty(resolvedViewId) ? MISSING_VIEW_ID : resolvedViewId)
                .lowCardinalityKeyValue(FRAGMENT_ID_TAG,
                        Strings.isNullOrEmpty(resolvedFragmentId) ? MISSING_FRAGMENT_ID : resolvedFragmentId);
    }

    protected boolean isObservationAvailable() {
        return observationEnabled && observationRegistry != null;
    }
}
