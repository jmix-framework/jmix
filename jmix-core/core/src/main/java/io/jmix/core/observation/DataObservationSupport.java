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

package io.jmix.core.observation;

import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Support class for observing data operations such as entity loading, counting,
 * value loading, and entity saving.
 * <p>
 * This class uses an {@link ObservationRegistry} if observation functionality
 * is enabled in the application configuration.
 * <p>
 * Observation functionality can be turned on or off using the {@code jmix.core.data-observation-enabled}
 * property in the application's configuration.
 *
 * @see CoreProperties#isDataObservationEnabled()
 */
@Component("core_DataObservationSupport")
public class DataObservationSupport {

    @Autowired
    protected Metadata metadata;
    @Autowired(required = false)
    protected ObservationRegistry observationRegistry;

    @Value("${jmix.core.data-observation-enabled:false}")
    protected Boolean observationEnabled;

    private static final String DATA_BASE_LOAD_NAME = "jmix.data.load";
    private static final String DATA_BASE_SAVE_NAME = "jmix.data.save";

    public Observation createEntityLoadObservation(MetaClass metaClass, @Nullable LoadContext.Query query) {
        if (!observationEnabled) {
            return Observation.NOOP;
        }

        Observation observation = Observation.createNotStarted(DATA_BASE_LOAD_NAME, observationRegistry)
                .contextualName("load entity")
                .lowCardinalityKeyValue("method", "load")
                .lowCardinalityKeyValue("entity.name", metaClass.getName());

        addQueryHighCardinalityIfExist(observation, query, () -> Objects.requireNonNull(query).getQueryString());

        return observation;
    }

    public Observation createEntityListLoadObservation(MetaClass metaClass, @Nullable LoadContext.Query query) {
        if (!observationEnabled) {
            return Observation.NOOP;
        }

        Observation observation = Observation.createNotStarted(DATA_BASE_LOAD_NAME, observationRegistry)
                .contextualName("load list of entities")
                .lowCardinalityKeyValue("method", "loadList")
                .lowCardinalityKeyValue("entity.name", metaClass.getName());

        addQueryHighCardinalityIfExist(observation, query, () -> Objects.requireNonNull(query).getQueryString());

        return observation;
    }

    public Observation createEntityCountObservation(MetaClass metaClass, @Nullable LoadContext.Query query) {
        if (!observationEnabled) {
            return Observation.NOOP;
        }

        Observation observation = Observation.createNotStarted(DATA_BASE_LOAD_NAME, observationRegistry)
                .contextualName("count entities")
                .lowCardinalityKeyValue("method", "getCount")
                .lowCardinalityKeyValue("entity.name", metaClass.getName());

        addQueryHighCardinalityIfExist(observation, query, () -> Objects.requireNonNull(query).getQueryString());

        return observation;
    }

    public Observation createValuesLoadObservation(ValueLoadContext context) {
        if (!observationEnabled) {
            return Observation.NOOP;
        }

        Observation observation = Observation.createNotStarted(DATA_BASE_LOAD_NAME, observationRegistry)
                .contextualName("load values")
                .lowCardinalityKeyValue("method", "loadValues")
                .highCardinalityKeyValue("properties", context.getProperties().toString());

        addQueryHighCardinalityIfExist(observation, context.getQuery(), () -> context.getQuery().getQueryString());

        return observation;
    }

    public Observation createValuesCountObservation(ValueLoadContext context) {
        if (!observationEnabled) {
            return Observation.NOOP;
        }

        Observation observation = Observation.createNotStarted(DATA_BASE_LOAD_NAME, observationRegistry)
                .contextualName("load values")
                .lowCardinalityKeyValue("method", "getCount")
                .highCardinalityKeyValue("properties", context.getProperties().toString());

        addQueryHighCardinalityIfExist(observation, context.getQuery(), () -> context.getQuery().getQueryString());

        return observation;
    }

    public Observation createEntitiesSaveObservation(SaveContext context, String store) {
        if (!observationEnabled) {
            return Observation.NOOP;
        }

        Observation observation = Observation.createNotStarted(DATA_BASE_SAVE_NAME, observationRegistry)
                .contextualName("save entities")
                .lowCardinalityKeyValue("method", "saveContextToStore")
                .lowCardinalityKeyValue("dataStore", store);

        Map<String, Long> entitiesToSaveCount = context.getEntitiesToSave().stream()
                .collect(Collectors.groupingBy(e -> metadata.getClass(e).getName(), Collectors.counting()));
        if (!entitiesToSaveCount.isEmpty()) {
            observation.highCardinalityKeyValue("entitiesToSave.count", entitiesToSaveCount.toString());
        }

        Map<String, Long> entitiesToRemoveCount = context.getEntitiesToRemove().stream()
                .collect(Collectors.groupingBy(e -> metadata.getClass(e).getName(), Collectors.counting()));
        if (!entitiesToRemoveCount.isEmpty()) {
            observation.highCardinalityKeyValue("entitiesToRemove.count", entitiesToRemoveCount.toString());
        }

        return observation;
    }

    private void addQueryHighCardinalityIfExist(Observation observation,
                                                @Nullable DataLoadContextQuery query,
                                                Supplier<String> queryGetter) {
        if (query != null && queryGetter.get() != null) {
            observation.highCardinalityKeyValue("jpql.query", queryGetter.get());

            if (!query.getParameters().isEmpty()) {
                observation.highCardinalityKeyValue("jpql.parameters", query.getParameters().toString());
            }
        }
    }
}
