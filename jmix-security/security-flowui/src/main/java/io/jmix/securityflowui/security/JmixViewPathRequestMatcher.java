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

package io.jmix.securityflowui.security;

import com.vaadin.flow.router.LocationUtil;
import com.vaadin.flow.server.RouteRegistry;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.security.configurer.JmixRequestCacheRequestMatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Collections;
import java.util.List;

@Internal
public class JmixViewPathRequestMatcher implements RequestMatcher {

    private final ViewRegistry viewRegistry;
    private final List<JmixRequestCacheRequestMatcher> additionalRequestMatchers;

    public JmixViewPathRequestMatcher(ViewRegistry viewRegistry) {
        this(viewRegistry, Collections.emptyList());
    }

    public JmixViewPathRequestMatcher(ViewRegistry viewRegistry,
                                      @Nullable List<JmixRequestCacheRequestMatcher> additionalRequestMatchers) {
        Preconditions.checkNotNullArgument(viewRegistry);
        this.viewRegistry = viewRegistry;
        this.additionalRequestMatchers = additionalRequestMatchers == null ? Collections.emptyList() : additionalRequestMatchers;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        boolean matches = additionalRequestMatchers.stream()
                .anyMatch(matcher -> matcher.matches(request));
        if (matches) {
            return true;
        }

        String path = LocationUtil.ensureRelativeNonNull(request.getServletPath());
        RouteRegistry handledRegistry = viewRegistry.getRouteConfiguration().getHandledRegistry();

        return handledRegistry.getNavigationRouteTarget(path).hasTarget();
    }
}
