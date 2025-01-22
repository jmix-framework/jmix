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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.jmix.core.Resources;
import io.jmix.rest.RestProperties;
import io.jmix.rest.exception.RestAPIException;
import io.jmix.rest.impl.openapi.OpenAPIGenerator;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController("rest_DocumentationController")
@RequestMapping("${jmix.rest.base-path}${jmix.rest.docs-path}")
public class DocumentationController {

    @Autowired
    protected Resources resources;

    @Autowired
    protected OpenAPIGenerator openAPIGenerator;

    @Autowired
    protected RestProperties restProperties;

    @Autowired(required = false)
    protected ServletContext servletContext;

    protected ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    protected ObjectMapper jsonMapper = new ObjectMapper();

    @RequestMapping(value = "/openapi.yaml", method = RequestMethod.GET, produces = "application/yaml")
    public String getOpenApiYaml() {
        try {
            JsonNode rootNode = getOpenApiRootNode();
            return yamlMapper.writeValueAsString(rootNode);
        } catch (JsonProcessingException e) {
            throw new RestAPIException("Internal server error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @RequestMapping(value = "/openapi.json", method = RequestMethod.GET, produces = "application/json")
    public String getOpenApiJson() {
        try {
            JsonNode rootNode = getOpenApiRootNode();
            return jsonMapper.writeValueAsString(rootNode);
        } catch (IOException e) {
            throw new RestAPIException("Internal server error", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    protected JsonNode getOpenApiRootNode() throws JsonProcessingException {
        String yaml = resources.getResourceAsString("classpath:io/jmix/rest/rest-openapi.yaml");
        JsonNode rootNode = yamlMapper.readTree(yaml);
        ArrayNode serversNode = (ArrayNode) rootNode.get("servers");
        serversNode.removeAll();
        ObjectNode serverNode = yamlMapper.createObjectNode();
        serverNode.put("url", getServerUrl());
        serversNode.add(serverNode);
        return rootNode;
    }

    protected String getServerUrl() {
        if (servletContext != null)
            return servletContext.getContextPath() + restProperties.getBasePath();
        else
            return restProperties.getBasePath();
    }

    @RequestMapping(value = "/openapiDetailed.yaml", method = RequestMethod.GET, produces = "application/yaml")
    public String getProjectOpenApiYaml() {
        try {
            OpenAPI openAPI = openAPIGenerator.generateOpenAPI();

            return Yaml.pretty().writeValueAsString(openAPI);
        } catch (IOException e) {
            throw new RestAPIException("An error occurred while generating Swagger documentation", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @RequestMapping(value = "/openapiDetailed.json", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    public String getProjectOpenApiJson() {
        try {
            OpenAPI openAPI = openAPIGenerator.generateOpenAPI();

            return Json.pretty().writeValueAsString(openAPI);
        } catch (JsonProcessingException e) {
            throw new RestAPIException("An error occurred while generating Swagger documentation", e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
