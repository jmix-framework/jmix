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

package io.jmix.core.impl;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The "Calling &lt;trigger&gt;" message is a success report consumed by development tools, so it must
 * not be logged for a call that has failed.
 */
class TriggerFilesProcessorTest {

    private final ByteArrayOutputStream console = new ByteArrayOutputStream();

    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        originalErr = System.err;

        // the logging provider of the module is not fixed, so both streams are captured
        PrintStream stream = new PrintStream(console, true, StandardCharsets.UTF_8);
        System.setOut(stream);
        System.setErr(stream);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testSuccessfulCallIsReported() throws Exception {
        Stub stub = new Stub();
        String trigger = Stub.class.getName() + "#ok";

        processorFor(stub).processFile(trigger);

        assertTrue(stub.called, "The trigger method has not been called");
        assertTrue(isCallReported(trigger), "A successful call must be reported");
    }

    @Test
    void testFailedCallIsNotReported() {
        Stub stub = new Stub();
        String trigger = Stub.class.getName() + "#fail";

        assertThrows(InvocationTargetException.class, () -> processorFor(stub).processFile(trigger));

        assertFalse(isCallReported(trigger), "A failed call must not be reported as a call");
    }

    private boolean isCallReported(String trigger) {
        return console.toString(StandardCharsets.UTF_8).contains("Calling " + trigger);
    }

    private TriggerFilesProcessor processorFor(Object bean) {
        return new TriggerFilesProcessor() {
            @Nullable
            @Override
            protected Object getBean(String beanClassName) {
                return bean;
            }
        };
    }

    public static class Stub {

        boolean called;

        public void ok() {
            called = true;
        }

        public void fail() {
            throw new IllegalStateException("Trigger method failure");
        }
    }
}
