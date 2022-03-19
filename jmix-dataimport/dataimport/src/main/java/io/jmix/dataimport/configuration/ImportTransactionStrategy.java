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
 * The way how to import the entities:
 * <ul>
 *     <li>Single transaction: if any entity import fails, the import process fails</li>
 *     <li>Transaction per entity: if any entity import fails, the import process continues</li>
 *     <li>Transaction per batch: the entities are imported by batches. If any entity import in batch fails, the import process continues. All entities in the not imported batch are marked as failed.</li>
 * </ul>
 */
public enum ImportTransactionStrategy {
    SINGLE_TRANSACTION,
    TRANSACTION_PER_ENTITY,
    TRANSACTION_PER_BATCH
}