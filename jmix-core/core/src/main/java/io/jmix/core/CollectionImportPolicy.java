/*
 * Copyright 2019 Haulmont.
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
 * Enum describes the policy of processing collection members when importing the entity with the {@link
 * EntityImportPlan}
 */
public enum CollectionImportPolicy {

    /**
     * Absent collection items will be keep.
     */
    KEEP_ABSENT_ITEMS,

    /**
     * Absent collection items will be removed from the database or excluded from the collection.
     */
    REMOVE_ABSENT_ITEMS
}
