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

package io.jmix.flowui.model;

import io.jmix.core.querycondition.Condition;

import io.jmix.flowui.monitoring.DataLoaderMonitoringInfo;
import io.jmix.flowui.observation.DataLoaderObservationInfo;
import org.jspecify.annotations.Nullable;
import java.io.Serializable;
import java.util.Map;
import java.util.function.Function;

/**
 * The root interface in the <i>data loaders</i> hierarchy. Data loaders are designed to load entity instances and
 * collections from DataManager or custom services to data containers.
 *
 * @see InstanceContainer
 * @see CollectionContainer
 */
public interface DataLoader {

    /**
     * Loads data to the connected container.
     */
    void load();

    /**
     * Returns connected container.
     */
    InstanceContainer<?> getContainer();

    /**
     * Returns data context. If the data context is set, all loaded instance will be merged into it.
     */
    @Nullable
    DataContext getDataContext();

    /**
     * Sets the data context for the loader. If the data context is set, all loaded instance will be merged into it.
     */
    void setDataContext(@Nullable DataContext dataContext);

    /**
     * Returns the query which is used for loading entities.
     */
    String getQuery();

    /**
     * Sets a query which will be used for loading entities.
     */
    void setQuery(String query);

    /**
     * Returns the root condition which is used together with the query when loading entities.
     */
    @Nullable
    Condition getCondition();

    /**
     * Sets the root condition which will be used together with the query when loading entities.
     */
    void setCondition(@Nullable Condition condition);

    /**
     * Returns the map of query parameters.
     */
    Map<String, Object> getParameters();

    /**
     * Sets the map of query parameters.
     */
    void setParameters(Map<String, Object> parameters);

    /**
     * Returns a query parameter by its name.
     */
    Object getParameter(String name);

    /**
     * Sets a query parameter.
     */
    void setParameter(String name, @Nullable Object value);

    /**
     * Removes a query parameter.
     */
    void removeParameter(String name);

    /**
     * Sets custom hint that should be used by the query for loading data.
     */
    void setHint(String hintName, Serializable value);

    /**
     * @return custom hints which are used by the query for loading data.
     */
    Map<String, Serializable> getHints();

    /**
     * No-op retained for binary compatibility. The legacy monitoring info is now derived from
     * {@link #setObservationInfoProvider(Function)}; setting a legacy provider has no effect.
     *
     * @deprecated Use {@link #setObservationInfoProvider(Function)} instead.
     */
    @Deprecated(since = "3.0", forRemoval = true)
    default void setMonitoringInfoProvider(Function<DataLoader, DataLoaderMonitoringInfo> monitoringInfoProvider) {
    }

    /**
     * Returns function that provides monitoring info about this data loader for the deprecated
     * legacy Timer path. The legacy 2-tuple is derived from {@link #getObservationInfoProvider()}:
     * for fragment-owned loaders the fragment id folds into the single legacy {@code viewId} slot,
     * preserving the pre-3.0 {@code view} tag of {@code jmix_ui_data} dashboards.
     *
     * @deprecated Use {@link #getObservationInfoProvider()} instead.
     */
    @Deprecated(since = "3.0", forRemoval = true)
    default Function<DataLoader, DataLoaderMonitoringInfo> getMonitoringInfoProvider() {
        return dl -> {
            DataLoaderObservationInfo info = getObservationInfoProvider().apply(dl);
            String legacyOwner = info.fragmentId() != null ? info.fragmentId() : info.viewId();
            return new DataLoaderMonitoringInfo(legacyOwner, info.loaderId());
        };
    }

    /**
     * Sets function that provides observation info about this data loader for the modern
     * {@code jmix.ui.data} Observation metric.
     */
    default void setObservationInfoProvider(Function<DataLoader, DataLoaderObservationInfo> observationInfoProvider) {
    }

    /**
     * Returns function that provides observation info about this data loader for the modern
     * {@code jmix.ui.data} Observation metric.
     */
    default Function<DataLoader, DataLoaderObservationInfo> getObservationInfoProvider() {
        return __ -> DataLoaderObservationInfo.empty();
    }
}
