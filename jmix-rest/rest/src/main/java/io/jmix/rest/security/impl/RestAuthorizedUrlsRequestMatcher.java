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

package io.jmix.rest.security.impl;

import io.jmix.rest.RestProperties;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.List;

@Component("rest_RestAuthorizedUrlsRequestMatcher")
public class RestAuthorizedUrlsRequestMatcher {

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    private final List<String> restAuthorizedUrls;

    public RestAuthorizedUrlsRequestMatcher(RestProperties restProperties) {
        String basePath = restProperties.getBasePath();
        restAuthorizedUrls = Arrays.asList(
                basePath + restProperties.getEntitiesPath() + "/**",
                basePath + restProperties.getServicesPath() + "/**",
                basePath + restProperties.getQueriesPath() + "/**",
                basePath + restProperties.getMessagesPath() + "/**",
                basePath + restProperties.getMetadataPath() + "/**",
                basePath + restProperties.getFilesPath() + "/**",
                basePath + restProperties.getUserInfoPath(),
                basePath + restProperties.getPermissionsPath(),
                basePath + restProperties.getUserSessionPath() + "/locale"
        );
    }

    public boolean isAuthorizedUrl(ServletRequest request) {
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        String contextPath = ((HttpServletRequest) request).getContextPath();

        for (String urlPattern : restAuthorizedUrls) {
            if (ANT_PATH_MATCHER.match(contextPath + urlPattern, requestURI)) {
                return true;
            }
        }
        return false;
    }

}
