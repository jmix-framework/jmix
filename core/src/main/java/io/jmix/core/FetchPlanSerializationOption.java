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
 * An object that configures how to serialize a fetch plan to JSON. Objects of this type are used by methods of the {@link
 * FetchPlanSerialization}. This object may be also used during the deserialization.
 */
public enum FetchPlanSerializationOption {

    /**
     * Specifies that fetch plan is serialized in the compact format. In compact format repeated nested fetch plans are replaced
     * with just a fetch plan name. For example fetch plan for {@code customerGroup2} is defined with a string:
     * <pre>
     * {
     *  "name": "customer-fp",
     *  "entity": "sample_Customer",
     *  "properties": [
     *      "name",
     *      {
     *          "name": "customerGroup",
     *          "fetchPlan": {
     *              "name": "customerGroup-fp",
     *              "properties": [
     *                  "id",
     *                  "createdBy",
     *                  "createTs",
     *                  "name"
     *              ]
     *          }
     *      },
     *      {
     *          "name": "customerGroup2",
     *          "fetchPlan": "customerGroup-fp"
     *      }
     *  ]
     * }
     * </pre>
     */
    COMPACT_FORMAT,

    /**
     * Specifies that an information about properties fetch mode will be included to the JSON. Default fetchMode value
     * (AUTO) will not be included.
     */
    INCLUDE_FETCH_MODE
}
