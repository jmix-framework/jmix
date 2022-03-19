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

package io.jmix.rest.impl.controller;

import io.jmix.rest.impl.config.RestServicesConfiguration;
import io.jmix.rest.impl.service.ServicesControllerManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Collection;
import java.util.Map;

/**
 * Controller that is used for service method invocations with the REST API
 */
@RestController("rest_ServicesController")
@RequestMapping(value = "/rest/services")
public class ServicesController {

    @Autowired
    protected ServicesControllerManager servicesControllerManager;

    @PostMapping("/{serviceName}/{methodName}")
    public ResponseEntity<String> invokeServiceMethodPost(@PathVariable String serviceName,
                                                          @PathVariable String methodName,
                                                          @RequestParam(required = false) String modelVersion,
                                                          @RequestBody(required = false) String paramsJson) throws Throwable {
        ServicesControllerManager.ServiceCallResult result = servicesControllerManager.invokeServiceMethodPost(serviceName,
                methodName, paramsJson, modelVersion);
        HttpStatus status;
        if (result == null) {
            status = HttpStatus.NO_CONTENT;
            result = new ServicesControllerManager.ServiceCallResult("", false);
        } else {
            status = HttpStatus.OK;
        }
        String contentType = result.isValidJson() ? "application/json;charset=UTF-8" : "text/plain;charset=UTF-8";
        return ResponseEntity.status(status).header("Content-Type", contentType).body(result.getStringValue());
    }

    @GetMapping("/{serviceName}/{methodName}")
    public ResponseEntity<String> invokeServiceMethodGet(@PathVariable String serviceName,
                                                         @PathVariable String methodName,
                                                         @RequestParam(required = false) String modelVersion,
                                                         @RequestParam Map<String, String> paramsMap) throws Throwable {
        ServicesControllerManager.ServiceCallResult result = servicesControllerManager.invokeServiceMethodGet(serviceName,
                methodName, paramsMap, modelVersion);
        HttpStatus status;
        if (result == null) {
            status = HttpStatus.NO_CONTENT;
            result = new ServicesControllerManager.ServiceCallResult("", false);
        } else {
            status = HttpStatus.OK;
        }
        String contentType = result.isValidJson() ? "application/json;charset=UTF-8" : "text/plain;charset=UTF-8";
        return ResponseEntity.status(status).header("Content-Type", contentType).body(result.getStringValue());
    }

    @GetMapping
    public Collection<RestServicesConfiguration.RestServiceInfo> getServiceInfos() {
        return servicesControllerManager.getServiceInfos();
    }

    @GetMapping("/{serviceName}")
    public RestServicesConfiguration.RestServiceInfo getServiceInfo(@PathVariable String serviceName) {
        return servicesControllerManager.getServiceInfo(serviceName);
    }
}
