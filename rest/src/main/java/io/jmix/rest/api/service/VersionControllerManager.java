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

package io.jmix.rest.api.service;

import io.jmix.rest.api.controller.VersionController;
import io.jmix.rest.api.exception.RestAPIException;
//import io.jmix.core.entity.BuildInfo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Class that is used by {@link VersionController} for getting REST API version
 */
@Component("rest_VersionControllerManager")
public class VersionControllerManager {

    private String apiVersion;

//    @Autowired
//    protected BuildInfo buildInfo;

    public String getApiVersion() {
        if (apiVersion != null && apiVersion.length() > 0) {
            return apiVersion;
        } else {
            throw new RestAPIException("Could not determine REST API version", null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    @PostConstruct
//    private void determineApiVersion() {
//        BuildInfo.Content content = buildInfo.getContent();
//
//        if (content.getAppName().equals("restapi") && content.getArtifactGroup().equals("io.jmix.rest")) {
//            // Standalone REST API
//            apiVersion = content.getVersion();
//        } else {
//            // REST API as part of a Cuba application
//            for (String component : content.getAppComponents()) {
//                if (component.trim().startsWith("io.jmix.rest:")) {
//                    apiVersion = component.split(":")[1];
//                    break;
//                }
//            }
//        }
//    }
}
