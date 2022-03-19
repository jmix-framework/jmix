/*
 * Copyright 2021 Haulmont.
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

package io.jmix.dataimport.configuration;

/**
 * Available policies to process a found duplicate:
 * <ol>
 *     <li>Update: an extracted entity not imported, existing one is updated.
 *     Properties of an existing entity are populated by values from input data as for extracted entity.
 *     </li>
 *     <li>Skip: an extracted entity not imported, existing one is not changed</li>
 *     <li>Abort: the import process is aborted immediately.
 *     <b>Note:</b> depending on the ImportTransactionStrategy the result of the import process differs:
 *     no one entity imported (single transaction) or entities, before the entity for which the duplicate found, are imported (transaction per entity)</li>
 * </ol>
 */
public enum DuplicateEntityPolicy {
    SKIP,
    UPDATE,
    ABORT
}