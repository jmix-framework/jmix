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

package generation;

import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.generation.AiToolStatusUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiToolStatusUtilsTest {

    @Test
    void fold_newMessage_appends() {
        List<AiToolStatusUpdate> statuses = new ArrayList<>();

        assertTrue(AiToolStatusUtils.fold(statuses, new AiToolStatusUpdate("Searching")));

        assertEquals(1, statuses.size());
    }

    @Test
    void fold_completionOfInFlightStep_replacesLast() {
        List<AiToolStatusUpdate> statuses = new ArrayList<>(List.of(new AiToolStatusUpdate("Searching")));

        assertTrue(AiToolStatusUtils.fold(statuses, new AiToolStatusUpdate("Searching", "3 found")));

        assertEquals(1, statuses.size());
        assertTrue(statuses.get(0).isCompleted());
    }

    @Test
    void fold_repeatedInFlightStep_ignored() {
        List<AiToolStatusUpdate> statuses = new ArrayList<>(List.of(new AiToolStatusUpdate("Searching")));

        assertFalse(AiToolStatusUtils.fold(statuses, new AiToolStatusUpdate("Searching")));

        assertEquals(1, statuses.size());
    }

    @Test
    void fold_sameMessageAfterCompletedStep_appendsNewCall() {
        List<AiToolStatusUpdate> statuses = new ArrayList<>(List.of(new AiToolStatusUpdate("Searching", "3 found")));

        assertTrue(AiToolStatusUtils.fold(statuses, new AiToolStatusUpdate("Searching")));

        assertEquals(2, statuses.size());
    }

    @Test
    void fold_overCap_dropsOldest() {
        List<AiToolStatusUpdate> statuses = new ArrayList<>();
        for (int i = 0; i < AiToolStatusUtils.MAX_STATUSES + 1; i++) {
            AiToolStatusUtils.fold(statuses, new AiToolStatusUpdate("Step " + i));
        }

        assertEquals(AiToolStatusUtils.MAX_STATUSES, statuses.size());
        assertEquals("Step 1", statuses.get(0).getMessage());
    }

    @Test
    void fold_nullOrBlank_ignored() {
        List<AiToolStatusUpdate> statuses = new ArrayList<>();

        assertFalse(AiToolStatusUtils.fold(statuses, null));
        assertFalse(AiToolStatusUtils.fold(statuses, new AiToolStatusUpdate(" ")));
    }
}
