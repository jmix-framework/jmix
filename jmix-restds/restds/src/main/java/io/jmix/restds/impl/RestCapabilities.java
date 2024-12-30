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

package io.jmix.restds.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.restds.exception.RestDataStoreAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestCapabilities {

    private static final Logger log = LoggerFactory.getLogger(RestCapabilities.class);

    private volatile boolean initialized;

    private volatile boolean inlineFetchPlans;

    private final RestInvoker restInvoker;

    private final ObjectMapper objectMapper;

    public RestCapabilities(RestInvoker restInvoker) {
        this.restInvoker = restInvoker;
        objectMapper = new ObjectMapper();
    }

    public boolean isInlineFetchPlanEnabled() {
        checkInitialized();
        return inlineFetchPlans;
    }

    private void checkInitialized() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    initialize();
                    initialized = true;
                }
            }
        }
    }

    private void initialize() {
        try {
            String json = restInvoker.capabilities();
            JsonNode rootNode = objectMapper.readTree(json);
            inlineFetchPlans = rootNode.get("inlineFetchPlans").asBoolean();
        } catch (RestDataStoreAccessException e) {
            log.info("Cannot determine REST capabilities for {}: {}", e.getDataStoreName(), e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing REST capabilities JSON", e);
        }
    }
}
