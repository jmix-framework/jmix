/*
 * Copyright 2025 Haulmont.
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

package io.jmix.securityresourceserver.authentication;

import io.jmix.core.security.ClientDetails;
import io.jmix.security.model.SecurityScope;
import io.jmix.security.util.RequestLocaleProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationDetailsSource;

import java.util.Locale;
import java.util.TimeZone;

/**
 * Implementation of {@link ApiClientDetailsSource} that provides {@link ClientDetails} with API security scope.
 */
public class ApiClientDetailsSource implements AuthenticationDetailsSource<HttpServletRequest, ClientDetails> {

    protected final RequestLocaleProvider requestLocaleProvider;

    public ApiClientDetailsSource(RequestLocaleProvider requestLocaleProvider) {
        this.requestLocaleProvider = requestLocaleProvider;
    }

    @Override
    public ClientDetails buildDetails(HttpServletRequest request) {
        return ClientDetails.builder()
                .clientType("API")
                .scope(SecurityScope.API)
                .locale(getLocale(request))
                .sessionId(request.getSession().getId())
                .timeZone(getTimeZone())
                .build();
    }

    protected Locale getLocale(HttpServletRequest request) {
        return requestLocaleProvider.getLocale(request);
    }

    protected TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }
}