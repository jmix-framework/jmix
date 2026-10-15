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

package io.jmix.aitools.tool;

import org.jspecify.annotations.NullMarked;
import org.springframework.context.ApplicationEvent;

/**
 * Status update of an AI tool, published while the tool runs inside a model call whose tool context carries
 * {@link AiToolStatusPublisher#CORRELATION_ID}. The correlation id lets a listener attribute the update to the
 * call that started it.
 * <p>
 * Published synchronously on the thread that runs the tool, so a listener must return quickly.
 */
@NullMarked
public class AiToolStatusUpdateEvent extends ApplicationEvent {

    protected final String correlationId;
    protected final AiToolStatusUpdate update;

    public AiToolStatusUpdateEvent(Object source, String correlationId, AiToolStatusUpdate update) {
        super(source);
        this.correlationId = correlationId;
        this.update = update;
    }

    /**
     * Returns the correlation id the caller put into the tool context.
     *
     * @return the correlation id of the model call
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the status update reported by the tool.
     *
     * @return the status update
     */
    public AiToolStatusUpdate getUpdate() {
        return update;
    }
}
