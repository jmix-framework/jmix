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
 *
 */

package io.jmix.rest.impl.service;

import io.jmix.rest.exception.RestAPIException;
import io.jmix.rest.impl.controller.VersionController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Class that is used by {@link VersionController} for getting REST API version
 */
@Component("rest_VersionControllerManager")
public class VersionControllerManager {

    private String apiVersion;
    private final BuildProperties buildProperties;

    public VersionControllerManager(@Autowired(required = false) BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
        determineApiVersion();
    }

    public String getApiVersion() {
        if (apiVersion != null && apiVersion.length() > 0) {
            return apiVersion;
        } else {
            throw new RestAPIException("Could not determine REST API version", null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void determineApiVersion() {
        if (buildProperties != null) {
            if (buildProperties.getName().equals("restapi") && buildProperties.getArtifact().equals("io.jmix.rest")) {
                // Standalone REST API
                apiVersion = buildProperties.getVersion();
            } else {
                // REST API as part of a Jmix application
                //TODO app components
//            for (String component : buildProperties.getAppComponents()) {
//                if (component.trim().startsWith("io.jmix.rest:")) {
//                    apiVersion = component.split(":")[1];
//                    break;
//                }
//            }
            }
        }
    }
}
