/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.util;

import java.util.function.Supplier;

/**
 * Represents an {@link OperationResult} that indicates a failed operation.
 */
public final class FailedOperationResult implements OperationResult {

    public static final OperationResult INSTANCE = new FailedOperationResult();

    private FailedOperationResult() {
    }

    @Override
    public Status getStatus() {
        return Status.FAIL;
    }

    @Override
    public OperationResult compose(Supplier<OperationResult> nextStep) {
        return this;
    }

    @Override
    public OperationResult then(Runnable runnable) {
        // do nothing
        return this;
    }

    @Override
    public OperationResult otherwise(Runnable runnable) {
        runnable.run();
        return this;
    }

    @Override
    public String toString() {
        return "{OPERATION FAILED}";
    }
}