/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.api.sys;

import io.jmix.rest.property.RestProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class RestRequestMatcher
        implements RequestMatcher, ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(RestRequestMatcher.class);

    protected static final String REST_BASE_PATTERN = "/rest/v2";
    protected static final String MATCH_ALL_PATTERN = "/**";

    @Autowired
    protected RestProperties restProperties;

    protected RequestMatcher matcher;

    protected List<String> bypassPatterns;

    @Override
    public boolean matches(HttpServletRequest request) {
        if (bypass(request)) {
            return false;
        }

        return matcher.matches(request);
    }

    protected boolean bypass(HttpServletRequest request) {
        String requestPath = getRequestPath(request);

        for (String pattern : bypassPatterns) {
            if (requestPath.startsWith(pattern)) {
                log.debug("Request '{}' is skipped as matched to '{}' bypass pattern",
                        requestPath, pattern);

                return true;
            }
        }

        return false;
    }

    protected String getRequestPath(HttpServletRequest request) {
        String url = request.getServletPath();

        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            url = StringUtils.hasLength(url)
                    ? url + pathInfo
                    : pathInfo;
        }

        return url;
    }

    @Override
    public void onApplicationEvent(@Nonnull ContextRefreshedEvent event) {
        matcher = new AntPathRequestMatcher(REST_BASE_PATTERN + MATCH_ALL_PATTERN);

        bypassPatterns = restProperties.getExternalRestBypassPatterns()
                .stream()
                .map(pattern -> {
                    String formatted = pattern.startsWith("/")
                            ? pattern
                            : "/" + pattern;

                    return REST_BASE_PATTERN + formatted;
                })
                .collect(Collectors.toList());
    }
}
