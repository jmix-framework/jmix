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

import java.util.ServiceLoader;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StudioPreviewComponentProviderResilienceTest {

    @Test
    void returnsValueWhenProviderSucceeds() {
        assertEquals("ok", StudioPreviewComponentProvider.instantiateSafely(provider(() -> "ok")));
    }

    @Test
    void skipsProviderThrowingLinkageError() {
        // Mirrors the timesheets failure: an outdated add-on loader links against a class absent
        // from the spring-free preview classloader -> NoClassDefFoundError on instantiation.
        assertNull(StudioPreviewComponentProvider.instantiateSafely(
                provider(() -> { throw new NoClassDefFoundError("io/jmix/flowui/component/ComponentContainer"); })));
    }

    @Test
    void skipsProviderThrowingRuntimeException() {
        assertNull(StudioPreviewComponentProvider.instantiateSafely(
                provider(() -> { throw new RuntimeException("boom"); })));
    }

    private static ServiceLoader.Provider<String> provider(Supplier<String> get) {
        return new ServiceLoader.Provider<>() {
            @Override
            public Class<? extends String> type() {
                return String.class;
            }

            @Override
            public String get() {
                return get.get();
            }
        };
    }
}
