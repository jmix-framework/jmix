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

/**
 * Auto-closeable scope that keeps one metadata generation pinned to the current thread.
 * <p>
 * Intended for request, data-operation, or async-task boundaries that need stable metadata
 * visibility while work is executing.
 */
public class MetadataGenerationScope implements AutoCloseable {

    protected final MetadataGenerationManager metadataGenerationManager;
    protected final MetadataGeneration generation;
    protected boolean closed;

    /**
     * Creates a scope that keeps the given generation pinned to the current thread.
     *
     * @param metadataGenerationManager manager that owns the scope lifecycle
     * @param generation generation pinned by the scope
     */
    public MetadataGenerationScope(MetadataGenerationManager metadataGenerationManager, MetadataGeneration generation) {
        this.metadataGenerationManager = metadataGenerationManager;
        this.generation = generation;
    }

    /**
     * Returns the metadata generation pinned by this scope.
     */
    public MetadataGeneration getGeneration() {
        return generation;
    }

    /**
     * Releases the pinned generation if the scope has not been closed yet.
     */
    @Override
    public void close() {
        if (!closed) {
            metadataGenerationManager.leave(generation);
            closed = true;
        }
    }
}
