/*
 * Copyright (c) 2008-2016 Haulmont.
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

package io.jmix.core.impl.serialization;

/**
 *
 */
public class FetchPlanSerializationException extends RuntimeException {
    public FetchPlanSerializationException() {
    }

    public FetchPlanSerializationException(String message) {
        super(message);
    }

    public FetchPlanSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FetchPlanSerializationException(Throwable cause) {
        super(cause);
    }

    public FetchPlanSerializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
