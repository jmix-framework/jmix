/*
 * Copyright 2019 Haulmont.
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

package io.jmix.rest.security;

import io.jmix.core.security.ClientDetails;

import java.io.Serializable;
import java.util.TimeZone;

public class RestAuthDetails implements Serializable {

    public static final RestAuthDetails UNKNOWN = builder().build();

    private String sessionId;

    private RestAuthDetails() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getSessionId() {
        return sessionId;
    }

    public static class Builder {
        private RestAuthDetails obj;

        public Builder() {
            obj = new RestAuthDetails();
        }

        public Builder sessionId(String sessionId) {
            obj.sessionId = sessionId;
            return this;
        }

        public RestAuthDetails build() {
            return obj;
        }
    }
}
