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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.rest.api.exception.ErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter that is used for logging exceptions thrown in other filters
 */
public class JmixRestExceptionLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JmixRestExceptionLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("REST API error", e);
            response.setHeader("Content-Type", "application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            ErrorInfo errorInfo = new ErrorInfo("REST API error", e.getMessage());
            byte[] responseBytes = new ObjectMapper().writeValueAsBytes(errorInfo);
            response.getOutputStream().write(responseBytes);
        }
    }
}
