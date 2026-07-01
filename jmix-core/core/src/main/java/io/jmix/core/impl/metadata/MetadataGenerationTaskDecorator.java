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

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.task.TaskDecorator;

/**
 * Spring task decorator that propagates the submitting thread's metadata generation to async work.
 * <p>
 * Intended for executor-backed tasks that must keep the same metadata snapshot as the request or
 * operation that scheduled them.
 */
public class MetadataGenerationTaskDecorator implements TaskDecorator {

    protected final ObjectProvider<MetadataGenerationManager> metadataGenerationManagerProvider;

    /**
     * Creates a task decorator that propagates metadata generation visibility to asynchronous execution.
     *
     * @param metadataGenerationManagerProvider provider that resolves the manager lazily when a task is submitted
     */
    public MetadataGenerationTaskDecorator(
            ObjectProvider<MetadataGenerationManager> metadataGenerationManagerProvider) {
        this.metadataGenerationManagerProvider = metadataGenerationManagerProvider;
    }

    /**
     * Wraps a task so it runs with the metadata generation visible at submission time.
     *
     * @param runnable task to decorate
     * @return generation-aware runnable
     */
    @Override
    public Runnable decorate(Runnable runnable) {
        return metadataGenerationManagerProvider.getObject().wrap(runnable);
    }
}
