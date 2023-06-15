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

package io.jmix.rest.impl.controller;

import org.springframework.lang.Nullable;

public class ConstraintViolationInfo {
    private String message;
    private String messageTemplate;
    private String path;
    private Object invalidValue;

    @Nullable
    public String getMessage() {
        return message;
    }

    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    @Nullable
    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(@Nullable String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @Nullable
    public String getPath() {
        return path;
    }

    public void setPath(@Nullable String path) {
        this.path = path;
    }

    @Nullable
    public Object getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(@Nullable Object invalidValue) {
        this.invalidValue = invalidValue;
    }
}
