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

import io.jmix.core.DataLoadContextQuery;
import io.jmix.core.LoadContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

@Internal
public final class DataObservationUtils {

    private static final String DATA_BASE_NAME = "jmix.data.load";

    private DataObservationUtils() {
    }

    public static Observation createEntityLoadObservation(MetaClass metaClass, @Nullable LoadContext.Query query,
                                                          ObservationRegistry observationRegistry) {
        Observation observation = Observation.createNotStarted(DATA_BASE_NAME, observationRegistry)
                .contextualName("load entity")
                .lowCardinalityKeyValue("method", "load")
                .lowCardinalityKeyValue("entity.name", metaClass.getName());

        addQueryHighCardinalityIfExist(observation, query, () -> Objects.requireNonNull(query).getQueryString());

        return observation;
    }

    public static Observation createEntityListLoadObservation(MetaClass metaClass, @Nullable LoadContext.Query query,
                                                              ObservationRegistry observationRegistry) {
        Observation observation = Observation.createNotStarted(DATA_BASE_NAME, observationRegistry)
                .contextualName("load list of entities")
                .lowCardinalityKeyValue("method", "loadList")
                .lowCardinalityKeyValue("entity.name", metaClass.getName());

        addQueryHighCardinalityIfExist(observation, query, () -> Objects.requireNonNull(query).getQueryString());

        return observation;
    }

    public static Observation createEntityCountObservation(MetaClass metaClass, @Nullable LoadContext.Query query,
                                                           ObservationRegistry observationRegistry) {
        Observation observation = Observation.createNotStarted(DATA_BASE_NAME, observationRegistry)
                .contextualName("count entities")
                .lowCardinalityKeyValue("method", "getCount")
                .lowCardinalityKeyValue("entity.name", metaClass.getName());

        addQueryHighCardinalityIfExist(observation, query, () -> Objects.requireNonNull(query).getQueryString());

        return observation;
    }

    public static Observation createValuesLoadObservation(ValueLoadContext context,
                                                          ObservationRegistry observationRegistry) {
        Observation observation = Observation.createNotStarted(DATA_BASE_NAME, observationRegistry)
                .contextualName("load values")
                .lowCardinalityKeyValue("method", "loadValues")
                .highCardinalityKeyValue("properties", context.getProperties().toString());

        addQueryHighCardinalityIfExist(observation, context.getQuery(), () -> context.getQuery().getQueryString());

        return observation;
    }

    public static Observation createValuesCountObservation(ValueLoadContext context,
                                                           ObservationRegistry observationRegistry) {
        Observation observation = Observation.createNotStarted(DATA_BASE_NAME, observationRegistry)
                .contextualName("load values")
                .lowCardinalityKeyValue("method", "getCount")
                .highCardinalityKeyValue("properties", context.getProperties().toString());

        addQueryHighCardinalityIfExist(observation, context.getQuery(), () -> context.getQuery().getQueryString());

        return observation;
    }

    private static void addQueryHighCardinalityIfExist(Observation observation,
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
