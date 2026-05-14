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

package io.jmix.core.impl.metadata;

import org.jspecify.annotations.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Stores lazily created state objects keyed by metadata generation identifier.
 * <p>
 * Intended for singleton services that need separate caches or derived structures for each
 * published metadata generation.
 *
 * @param <T> type of generation-local state
 */
public class GenerationStateStore<T> {

    protected final ConcurrentHashMap<Long, T> states = new ConcurrentHashMap<>();

    /**
     * Returns state associated with the specified metadata generation, creating it lazily when absent.
     *
     * @param generationId metadata generation identifier
     * @param factory state factory used for the first access to the generation
     * @return generation-local state
     */
    public T getOrCreate(long generationId, Supplier<T> factory) {
        return states.computeIfAbsent(generationId, ignored -> factory.get());
    }

    /**
     * Returns cached state for the specified metadata generation.
     *
     * @param generationId metadata generation identifier
     * @return generation-local state or {@code null} if it has not been created
     */
    @Nullable
    public T get(long generationId) {
        return states.get(generationId);
    }

    /**
     * Removes cached state of the specified metadata generation.
     *
     * @param generationId metadata generation identifier
     */
    public void remove(long generationId) {
        states.remove(generationId);
    }

    /**
     * Removes cached state of all metadata generations.
     */
    public void clear() {
        states.clear();
    }
}
