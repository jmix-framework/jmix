/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsrest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.jmix.core.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController("report_ReportRestDocsController")
@RequestMapping("/rest/reports/docs")
public class ReportRestDocsController {

    @Autowired
    protected Resources resources;

    @RequestMapping(value = "/openapi.yaml", method = RequestMethod.GET, produces = "application/yaml")
    public String getSwaggerYaml() {
        return resources.getResourceAsString("classpath:io/jmix/reportsrest/reports-rest-openapi.yaml");
    }

    @RequestMapping(value = "/openapi.json", method = RequestMethod.GET, produces = "application/json")
    public String getSwaggerJson() {
        String yaml = getSwaggerYaml();
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj;
        try {
            obj = yamlReader.readValue(yaml, Object.class);
            ObjectMapper jsonWriter = new ObjectMapper();
            return jsonWriter.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RestAPIException("Internal server error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}