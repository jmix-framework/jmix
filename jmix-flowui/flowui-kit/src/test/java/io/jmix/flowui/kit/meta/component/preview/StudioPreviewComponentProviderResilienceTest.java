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

package io.jmix.flowui.kit.meta.component.preview;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudioPreviewComponentProviderResilienceTest {

    // A step that makes the fake iterator's next() throw, mirroring the timesheets failure:
    // an outdated add-on loader links against a class absent from the spring-free preview
    // classloader -> ServiceLoader wraps the NoClassDefFoundError in ServiceConfigurationError.
    private static final Object FAIL = new Object();

    @Test
    void skipsServicesThatFailToLoadAndKeepsTheRest() {
        Set<String> target = new LinkedHashSet<>();

        StudioPreviewComponentProvider.addServicesResiliently(steps("a", FAIL, "b"), target);

        assertEquals(List.of("a", "b"), new ArrayList<>(target));
    }

    @Test
    void addsAllServicesWhenNoneFail() {
        Set<String> target = new LinkedHashSet<>();

        StudioPreviewComponentProvider.addServicesResiliently(steps("a", "b"), target);

        assertEquals(List.of("a", "b"), new ArrayList<>(target));
    }

    @Test
    void yieldsEmptyWhenEveryServiceFails() {
        Set<String> target = new LinkedHashSet<>();

        StudioPreviewComponentProvider.addServicesResiliently(steps(FAIL, FAIL), target);

        assertEquals(List.of(), new ArrayList<>(target));
    }

    /**
     * Fake {@link Iterator} yielding each step; a {@link #FAIL} step makes {@code next()} throw
     * {@link ServiceConfigurationError} (and is consumed, so iteration continues) - the best-effort
     * recovery contract {@code addServicesResiliently} relies on.
     */
    private static Iterator<String> steps(Object... steps) {
        return new Iterator<>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < steps.length;
            }

            @Override
            public String next() {
                if (i >= steps.length) {
                    throw new NoSuchElementException();
                }
                Object step = steps[i++];
                if (step == FAIL) {
                    throw new ServiceConfigurationError("boom", new NoClassDefFoundError("Missing"));
                }
                return (String) step;
            }
        };
    }
}
