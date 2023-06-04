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

package io.jmix.core.security;

import org.springframework.lang.Nullable;
import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;

public class ClientDetails implements Serializable {

    private static final long serialVersionUID = 8397588752541616904L;

    public static final ClientDetails UNKNOWN = builder().build();

    private Locale locale;
    private TimeZone timeZone;
    private String address;
    private String info;
    private String clientType;
    private String scope;
    private String sessionId;

    private ClientDetails() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public String getAddress() {
        return address;
    }

    public String getInfo() {
        return info;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getClientType() {
        return clientType;
    }

    public String getScope() {
        return scope;
    }

    public String getSessionId() {
        return sessionId;
    }

    public static class Builder {
        private ClientDetails obj;

        public Builder() {
            obj = new ClientDetails();
            obj.timeZone = TimeZone.getDefault();
        }

        public Builder of(ClientDetails clientDetails) {
            return locale(clientDetails.getLocale())
                    .timeZone(clientDetails.getTimeZone())
                    .address(clientDetails.getAddress())
                    .info(clientDetails.getInfo())
                    .clientType(clientDetails.getClientType())
                    .scope(clientDetails.getScope())
                    .sessionId(clientDetails.getSessionId());
        }

        public Builder locale(Locale locale) {
            obj.locale = locale;
            return this;
        }

        public Builder timeZone(@Nullable TimeZone timeZone) {
            obj.timeZone = timeZone;
            return this;
        }

        public Builder address(String address) {
            obj.address = address;
            return this;
        }

        public Builder info(String info) {
            obj.info = info;
            return this;
        }

        public Builder clientType(String clientType) {
            obj.clientType = clientType;
            return this;
        }

        public Builder scope(String scope) {
            obj.scope = scope;
            return this;
        }

        public Builder sessionId(String sessionId) {
            obj.sessionId = sessionId;
            return this;
        }

        public ClientDetails build() {
            return obj;
        }
    }
}
