/*
 * Copyright 2021 Haulmont.
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

package io.jmix.data.exception;

public class UniqueConstraintViolationException extends RuntimeException {
    private static final long serialVersionUID = -2835377177926310461L;

    protected String constraintName = "UniqueConstraint";

    public UniqueConstraintViolationException() {
        super();
    }

    public UniqueConstraintViolationException(Throwable cause) {
        super(cause);
    }

    public UniqueConstraintViolationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniqueConstraintViolationException(String message, String constraintName, Throwable cause) {
        super(message, cause);
        this.constraintName = constraintName;
    }

    public String getConstraintName() {
        return constraintName;
    }
}
