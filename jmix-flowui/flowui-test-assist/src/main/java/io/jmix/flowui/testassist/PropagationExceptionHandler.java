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

package io.jmix.flowui.testassist;

import io.jmix.flowui.exception.UiExceptionHandler;

public class PropagationExceptionHandler implements UiExceptionHandler {

    @Override
    public boolean handle(Throwable exception) {
        // Exception can be thrown while navigation is performing. However,
        // this exception is not propagated and the test is considered as passed.
        // So we should propagate the exception to fail the test.
        if (exception instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }

        throw new RuntimeException(exception);
    }
}
