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

package io.jmix.aitoolsflowui.view.chat.timeline;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.annotation.JmixEntity;

import java.util.UUID;

/**
 * A single status line shown under an assistant "thinking" timeline item: a message describing a
 * step and an optional short result snippet once the step has finished.
 */
@JmixEntity(name = "aitls_TimelineItemStatus")
public class TimelineItemStatus {

    @JmixId
    @JmixGeneratedValue
    private UUID id;

    private String message;

    private String resultSnippet;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResultSnippet() {
        return resultSnippet;
    }

    public void setResultSnippet(String resultSnippet) {
        this.resultSnippet = resultSnippet;
    }

    /**
     * Returns whether this status represents a finished step, i.e. carries a non-empty result snippet.
     *
     * @return {@code true} if the step is completed
     */
    public boolean isCompleted() {
        return resultSnippet != null && !resultSnippet.isEmpty();
    }
}
