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

package io.jmix.email;

import io.jmix.core.common.util.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Formats exception cause chains for logging failures that are expected outcomes (connection
 * probes, send retries) and therefore should not flood the log with a full stack trace. The
 * diagnostic value of such failures sits in the cause messages (failing layer, endpoint, HTTP
 * status, provider error code); the full trace should be logged separately at DEBUG level.
 */
@NullMarked
public final class ExceptionMessagesSupport {

    private ExceptionMessagesSupport() {
    }

    /**
     * Flattens the cause-message chain of the given throwable into a single line: one entry per
     * cause (outermost first) separated by {@code " | "}, multi-line messages (e.g. JSON response
     * bodies) collapsed to one line, messages already contained in an outer entry dropped
     * (wrappers often repeat the message of their cause verbatim). A cause without a message is
     * represented by its class name.
     */
    public static String flatten(Throwable throwable) {
        Preconditions.checkNotNullArgument(throwable, "throwable is null");

        List<String> messages = new ArrayList<>();
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable current = throwable; current != null && visited.add(current); current = current.getCause()) {
            String message = current.getMessage();
            String normalized = StringUtils.isBlank(message)
                    ? current.getClass().getSimpleName()
                    : StringUtils.normalizeSpace(message);
            if (messages.stream().noneMatch(kept -> kept.contains(normalized))) {
                messages.add(normalized);
            }
        }
        return String.join(" | ", messages);
    }
}
