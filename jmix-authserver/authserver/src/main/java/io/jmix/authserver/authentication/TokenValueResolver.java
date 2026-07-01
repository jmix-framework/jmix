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

package io.jmix.authserver.authentication;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides an ability to extract token value from {@link HttpServletRequest}.
 * Based on {@link DefaultBearerTokenResolver} with additional customizations.
 */
public class TokenValueResolver {

    private static final Logger log = LoggerFactory.getLogger(TokenValueResolver.class);

    private static final Pattern authorizationPattern = Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
            Pattern.CASE_INSENSITIVE);

    protected final String headerName;
    protected final String urlParameterName;
    protected final String bodyFormParameterName;

    protected final boolean urlParameterSupportEnabled;
    protected final boolean bodyFormParameterSupportEnabled;

    private TokenValueResolver(Builder builder) {
        this.headerName = builder.headerName;

        this.urlParameterSupportEnabled = builder.urlParameterSupportEnabled;
        this.urlParameterName = builder.urlParameterName;

        this.bodyFormParameterSupportEnabled = builder.bodyFormParameterSupportEnabled;
        this.bodyFormParameterName = builder.bodyFormParameterName;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Extracts token value from {@link HttpServletRequest}. It will check the following places:
     * <ul>
     *     <li>Header. 'Authorization' by default</li>
     *     <li>URL parameter. 'token' by default</li>
     *     <li>Body form parameter. 'token' by default</li>
     * </ul>
     * <p>
     * Only header check is enabled by default. The rest should be enabled explicitly via builder.
     *
     * @param request HttpServletRequest
     * @return token value or null if no token was resolved
     */
    @Nullable
    public String resolve(HttpServletRequest request) {
        return resolveTokenInternal(
                resolveTokenFromHeader(request),
                resolveTokenFromUrlParameter(request),
                resolveTokenFromBodyParameter(request)
        );
    }

    @Nullable
    protected String resolveTokenFromHeader(HttpServletRequest request) {
        log.debug("Check '{}' header for token value", headerName);

        String authorization = request.getHeader(this.headerName);
        if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
            return null;
        }

        Matcher matcher = authorizationPattern.matcher(authorization);
        if (!matcher.matches()) {
            BearerTokenError error = BearerTokenErrors.invalidToken("Bearer token is malformed");
            throw new OAuth2AuthenticationException(error);
        }

        return matcher.group("token");
    }

    @Nullable
    protected String resolveTokenFromUrlParameter(HttpServletRequest request) {
        if (!this.urlParameterSupportEnabled || !HttpMethod.GET.name().equals(request.getMethod())) {
            return null;
        }
        log.debug("Check URL parameter '{}' for token value", urlParameterName);

        return resolveTokenInternal(request.getParameterValues(urlParameterName));
    }

    @Nullable
    protected String resolveTokenFromBodyParameter(HttpServletRequest request) {
        if (!this.bodyFormParameterSupportEnabled
                || !MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(request.getContentType())
                || HttpMethod.GET.name().equals(request.getMethod())) {
            return null;
        }
        log.debug("Check body parameter '{}' for token value", bodyFormParameterName);

        String queryString = request.getQueryString();
        if (queryString != null && queryString.contains(bodyFormParameterName)) {
            return null;
        }

        return resolveTokenInternal(request.getParameterValues(bodyFormParameterName));
    }

    @Nullable
    protected String resolveTokenInternal(String... accessTokens) {
        if (accessTokens == null || accessTokens.length == 0) {
            return null;
        }

        String accessToken = null;
        for (String token : accessTokens) {
            if (accessToken == null) {
                accessToken = token;
            } else if (StringUtils.isNotBlank(token)) {
                BearerTokenError error = BearerTokenErrors
                        .invalidRequest("Found multiple bearer tokens in the request");
                throw new OAuth2AuthenticationException(error);
            }
        }

        if (StringUtils.isNotBlank(accessToken)) {
            return accessToken;
        } else {
            return null;
        }
    }

    public static class Builder {

        private static final String DEFAULT_HEADER_NAME = "Authorization";
        private static final String DEFAULT_URL_PARAMETER_NAME = "token";
        private static final String DEFAULT_BODY_FORM_PARAMETER_NAME = "token";

        protected String headerName;
        protected String urlParameterName;
        protected String bodyFormParameterName;

        protected boolean urlParameterSupportEnabled = false;
        protected boolean bodyFormParameterSupportEnabled = false;

        private Builder() {
            this.headerName = DEFAULT_HEADER_NAME;
            this.urlParameterName = DEFAULT_URL_PARAMETER_NAME;
            this.bodyFormParameterName = DEFAULT_BODY_FORM_PARAMETER_NAME;
        }

        /**
         * Name of the header to check for token. `Authorization` by default.
         */
        public Builder withHeaderName(String headerName) {
            this.headerName = headerName;
            return this;
        }

        /**
         * Name of the body form parameter to check for token. `token` by default.
         */
        public Builder withBodyFormParameterName(String bodyParameterName) {
            this.bodyFormParameterName = bodyParameterName;
            return this;
        }

        /**
         * Name of the URL parameter to check for token. `token` by default.
         */
        public Builder withUrlParameterName(String urlParameterName) {
            this.urlParameterName = urlParameterName;
            return this;
        }

        /**
         * Enables/Disables check of body form parameter for token. 'False' by default.
         */
        public Builder withBodyFormParameterSupportEnabled(boolean enabled) {
            this.bodyFormParameterSupportEnabled = enabled;
            return this;
        }

        /**
         * Enables/Disables check of URL parameter for token. 'False' by default.
         */
        public Builder withUrlParameterSupportEnabled(boolean enabled) {
            this.urlParameterSupportEnabled = enabled;
            return this;
        }

        public TokenValueResolver build() {
            Assert.hasText(headerName, "Header name must be specified");
            Assert.isTrue(!urlParameterSupportEnabled || StringUtils.isNotBlank(urlParameterName),
                    "URL parameter name must be specified");
            Assert.isTrue(!bodyFormParameterSupportEnabled || StringUtils.isNotBlank(bodyFormParameterName),
                    "Body form parameter name must be specified");
            return new TokenValueResolver(this);
        }
    }
}
