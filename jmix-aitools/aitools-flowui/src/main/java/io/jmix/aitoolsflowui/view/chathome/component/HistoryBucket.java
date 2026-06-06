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

package io.jmix.aitoolsflowui.view.chathome.component;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Date buckets used to group conversation history, ordered most-recent-first.
 */
@NullMarked
public enum HistoryBucket {
    TODAY,
    YESTERDAY,
    LAST_WEEK,
    EARLIER;

    /**
     * Classifies the given creation date into a bucket relative to {@code today}.
     * Boundaries: today (or future) → {@link #TODAY}, the calendar day before
     * → {@link #YESTERDAY}, within the previous 7 days → {@link #LAST_WEEK},
     * anything older (or missing) → {@link #EARLIER}.
     */
    public static HistoryBucket of(@Nullable OffsetDateTime created, LocalDate today, ZoneId zone) {
        if (created == null) {
            return EARLIER;
        }
        LocalDate date = created.atZoneSameInstant(zone).toLocalDate();
        if (!date.isBefore(today)) {
            return TODAY;
        }
        if (date.equals(today.minusDays(1))) {
            return YESTERDAY;
        }
        if (date.isAfter(today.minusDays(7))) {
            return LAST_WEEK;
        }
        return EARLIER;
    }
}
