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

package io.jmix.flowui.data.binding;

/**
 * Represents a binding mechanism that can be suspended and resumed.
 */
public interface SuspendableBinding {

    /**
     * Suspends the current binding, temporarily pausing its operation.
     */
    void suspend();

    /**
     * Resumes the operation of the binding mechanism, restoring it to its active state
     * after being previously suspended.
     */
    void resume();

    /**
     * Indicates whether the current binding is in a suspended state.
     *
     * @return {@code true} if the binding is currently suspended, {@code false} otherwise
     */
    boolean suspended();
}
