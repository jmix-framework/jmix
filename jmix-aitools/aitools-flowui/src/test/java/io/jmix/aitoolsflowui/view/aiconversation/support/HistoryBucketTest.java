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

package io.jmix.aitoolsflowui.view.aiconversation.support;

import io.jmix.aitoolsflowui.view.chathub.component.HistoryBucket;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryBucketTest {

    private final ZoneId zone = ZoneOffset.UTC;
    private final LocalDate today = LocalDate.of(2026, 6, 5);

    private OffsetDateTime at(LocalDate date) {
        return date.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    @Test
    void nullDate_isEarlier() {
        assertEquals(HistoryBucket.EARLIER, HistoryBucket.of(null, today, zone));
    }

    @Test
    void today_isToday() {
        assertEquals(HistoryBucket.TODAY, HistoryBucket.of(at(today), today, zone));
    }

    @Test
    void future_isToday() {
        assertEquals(HistoryBucket.TODAY, HistoryBucket.of(at(today.plusDays(1)), today, zone));
    }

    @Test
    void yesterday_isYesterday() {
        assertEquals(HistoryBucket.YESTERDAY, HistoryBucket.of(at(today.minusDays(1)), today, zone));
    }

    @Test
    void threeDaysAgo_isLastWeek() {
        assertEquals(HistoryBucket.LAST_WEEK, HistoryBucket.of(at(today.minusDays(3)), today, zone));
    }

    @Test
    void sixDaysAgo_isLastWeek() {
        assertEquals(HistoryBucket.LAST_WEEK, HistoryBucket.of(at(today.minusDays(6)), today, zone));
    }

    @Test
    void sevenDaysAgo_isEarlier() {
        assertEquals(HistoryBucket.EARLIER, HistoryBucket.of(at(today.minusDays(7)), today, zone));
    }
}
