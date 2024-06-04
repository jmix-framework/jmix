/*
 * Copyright 2024 Haulmont.
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

package io.jmix.superset.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.lang.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class GuestTokenResponse implements Serializable {

    private String token;

    private String message;

    @JsonProperty("msg")
    private String systemMessage; // e.g. token is expired

    private List<Error> errors;

    @Nullable
    public String getToken() {
        return token;
    }

    public void setToken(@Nullable String token) {
        this.token = token;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    public void setMessage(@Nullable String message) {
        this.message = message;
    }

    @Nullable
    public String getSystemMessage() {
        return systemMessage;
    }

    public void setSystemMessage(@Nullable String systemMessage) {
        this.systemMessage = systemMessage;
    }

    public List<Error> getErrors() {
        return errors == null ? Collections.emptyList() : errors;
    }

    public void setErrors(@Nullable List<Error> errors) {
        this.errors = errors;
    }

    public static class Error {
        private String message;

        @Nullable
        public String getMessage() {
            return message;
        }

        public void setMessage(@Nullable String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return "{\"message\":\"" + message + "\"}";
        }
    }
}
