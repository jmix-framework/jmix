/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core;

/**
 * Class that is used for serialization and deserialization of fetch plan to JSON.
 */
public interface FetchPlanSerialization {

    /**
     * Deserializes a JSON object to fetch plan. The method automatically identifies whether the fetch plan was serialized with the
     * {@link FetchPlanSerializationOption#COMPACT_FORMAT} option.
     *
     * @param json JSON objects that represents the fetch plan
     * @return a fetchPlan
     */
    FetchPlan fromJson(String json);

    /**
     * Serializes a fetch plan to JSON object
     *
     * @param fetchPlan a fetchPlan
     * @param options   options specifying how a JSON object graph should be serialized
     * @return a string that represents a JSON object
     */
    String toJson(FetchPlan fetchPlan, FetchPlanSerializationOption... options);
}
