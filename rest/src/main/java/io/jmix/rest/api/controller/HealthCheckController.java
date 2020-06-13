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

package io.jmix.rest.api.controller;

import io.jmix.core.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Provides health check URL for the blocks running REST API.
 */
@Controller("rest_HealthCheckController")
public class HealthCheckController {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckController.class);

//    @Autowired
//    protected GlobalConfig config;

    @Autowired
    protected Events events;
    //todo health check
//    @RequestMapping(value = "/health", method = RequestMethod.GET)
//    public void healthCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        log.trace("Health check request {} from {}", request.getRequestURI(), request.getRemoteAddr());
//        response.setContentType("text/plain");
//        response.setCharacterEncoding("UTF-8");
//
//        if (AppContext.isReady()) {
//            HealthCheckEvent event = new HealthCheckEvent(this);
//            events.publish(event);
//
//            response.getOutputStream().print(Strings.isNullOrEmpty(event.getResponse()) ?
//                    config.getHealthCheckResponse() : event.getResponse());
//        } else {
//            response.getOutputStream().print("not ready");
//        }
//    }
}
