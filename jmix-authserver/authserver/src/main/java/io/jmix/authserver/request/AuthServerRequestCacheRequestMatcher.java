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


package io.jmix.authserver.request;

import io.jmix.authserver.AuthServerProperties;
import io.jmix.security.configurer.JmixRequestCacheRequestMatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("authsr_AuthServerRequestCacheRequestMatcher")
public class AuthServerRequestCacheRequestMatcher extends JmixRequestCacheRequestMatcher {

    protected final AuthServerProperties authServerProperties;

    @Autowired
    public AuthServerRequestCacheRequestMatcher(AuthServerProperties authServerProperties) {
        this.authServerProperties = authServerProperties;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = ensureRelativeNonNull(request.getServletPath());
        String authorizeEndpoint = authServerProperties.getAuthorizeEndpoint();
        return path.startsWith(authorizeEndpoint);
    }

    protected static String ensureRelativeNonNull(String location) {
        if (location == null) {
            return "";
        } else {
            location = location.trim();
            if (!location.startsWith("/")) {
                location = "/" + location;
            }

            return location;
        }
    }
}

