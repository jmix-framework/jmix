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

package io.jmix.rest.impl.controller;

import io.jmix.rest.RestProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that is used for getting an information about REST capabilities.
 */
@RestController("rest_CapabilitiesController")
@RequestMapping(path = "/rest/capabilities", produces = MediaType.APPLICATION_JSON_VALUE)
public class CapabilitiesController {

    @Autowired
    private RestProperties restProperties;

    @GetMapping
    public Capabilities getCapabilities() {
        return new Capabilities(restProperties.isInlineFetchPlanEnabled());
    }

    public record Capabilities (boolean inlineFetchPlans) {
    }
}
