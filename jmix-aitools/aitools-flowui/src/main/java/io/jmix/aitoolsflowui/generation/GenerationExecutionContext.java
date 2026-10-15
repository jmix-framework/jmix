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

package io.jmix.aitoolsflowui.generation;

import io.jmix.core.impl.logging.LogMdc;
import io.jmix.core.impl.session.ThreadLocalSessionData;
import io.jmix.core.security.SecurityContextHelper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

/**
 * The caller's execution context captured when a generation starts and installed around it on a worker thread: the
 * authentication and the HTTP session attributes that {@code session_*} query parameters of row-level policies read.
 */
@NullMarked
class GenerationExecutionContext {

    @Nullable
    protected final Authentication authentication;
    protected final Map<String, Object> sessionAttributes;

    protected GenerationExecutionContext(@Nullable Authentication authentication,
                                         Map<String, Object> sessionAttributes) {
        this.authentication = authentication;
        this.sessionAttributes = sessionAttributes;
    }

    /**
     * Captures the context of the current thread. Outside an HTTP request the session attributes are empty.
     *
     * @return the captured context
     */
    static GenerationExecutionContext capture() {
        return new GenerationExecutionContext(SecurityContextHelper.getAuthentication(),
                ThreadLocalSessionData.extractHttpSessionAttributes());
    }

    /**
     * Runs the task on the current thread within the captured context and leaves no trace of it on the thread
     * afterwards, so a pooled thread starts the next task clean.
     *
     * @param task the task to run
     */
    void run(Runnable task) {
        // The worker gets its own context instance: the captured one may be shared with the HTTP session.
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        LogMdc.setup(authentication);
        ThreadLocalSessionData.setAttributes(sessionAttributes);
        try {
            task.run();
        } finally {
            SecurityContextHolder.clearContext();
            LogMdc.setup(null);
            ThreadLocalSessionData.clear();
        }
    }
}
