/*
 * Copyright 2026 Haulmont.
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

package io.jmix.core.impl.logging;

import io.jmix.core.security.CurrentAuthentication;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter that sets up MDC (Mapped Diagnostic Context) for each http request.
 */
public class LogMdcFilter extends OncePerRequestFilter {

    protected CurrentAuthentication currentAuthentication;

    public LogMdcFilter(CurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (currentAuthentication.isSet()) {
            LogMdc.setup(currentAuthentication.getAuthentication());
            try {
                filterChain.doFilter(request, response);
            } finally {
                LogMdc.setup(null);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
