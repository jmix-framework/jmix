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

import org.springframework.context.ApplicationEvent;

/**
 * Published after a retired metadata generation completes all deferred cleanup.
 * <p>
 * Intended for services that want to drop generation-local state only after no running work can
 * still legitimately reference that generation.
 */
public class MetadataGenerationRetiredEvent extends ApplicationEvent {

    protected final long generationId;

    /**
     * Creates an event published after a retired metadata generation finishes all deferred cleanup.
     *
     * @param source event publisher
     * @param generationId identifier of the retired generation
     */
    public MetadataGenerationRetiredEvent(Object source, long generationId) {
        super(source);
        this.generationId = generationId;
    }

    /**
     * Returns the identifier of the retired metadata generation.
     */
    public long getGenerationId() {
        return generationId;
    }
}
