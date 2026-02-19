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

package io.jmix.datatools.datamodel.engine.plantuml.impl;

import io.jmix.core.Metadata;
import io.jmix.datatools.DatatoolsProperties;
import io.jmix.datatools.datamodel.RelationType;
import io.jmix.datatools.datamodel.engine.DiagramEngine;
import io.jmix.datatools.datamodel.engine.plantuml.PlantUmlEncoder;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.Deflater;

public class PlantUmlDiagramEngine implements DiagramEngine {

    protected final DatatoolsProperties datatoolsProperties;
    protected final PlantUmlEncoder plantUmlEncoder;
    protected final String template;
    protected final String entityTemplate;
    protected final String attributeTemplate;
    protected final String relationTemplate;
    protected final String urlTemplate;
    protected final RestClient restClient;
    protected final int dataStoresCount;

    public PlantUmlDiagramEngine(DatatoolsProperties datatoolsProperties,
                                 Metadata metadata) {
        this.datatoolsProperties = datatoolsProperties;
        this.dataStoresCount = getDataStoresCount(metadata);
        this.plantUmlEncoder = createEncoder();
        this.template = createTemplate();
        this.urlTemplate = createURLTemplate();
        this.entityTemplate = createEntityTemplate();
        this.attributeTemplate = createAttributeTemplate();
        this.relationTemplate = createRelationTemplate();

        String baseUrl = createBaseURL();
        this.restClient = configureClient(baseUrl);
    }

    protected int getDataStoresCount(Metadata metadata) {
        return metadata.getClasses().stream()
                .filter(e -> e.getJavaClass().isAnnotationPresent(Entity.class)
                        && e.getJavaClass().isAnnotationPresent(Table.class))
                .map(mc -> mc.getStore().getName())
                .collect(Collectors.toSet()).size();
    }

    protected String createBaseURL() {
        return datatoolsProperties.getDiagramConstructor().getHost() == null
                ? "https://www.plantuml.com"
                : datatoolsProperties.getDiagramConstructor().getHost();
    }

    protected PlantUmlEncoder createEncoder() {
        return new PlantUmlEncoderImpl();
    }

    protected String createTemplate() {
        return """
                @startuml
                {0}
                
                {1}
                @enduml
                """;
    }

    protected String createURLTemplate() {
        return "/plantuml/png/%s";
    }

    protected String createEntityTemplate() {
        if (dataStoresCount > 1) {
            return "entity %s:%s {\n";
        }
        return "entity %s {\n";
    }

    protected String createAttributeTemplate() {
        return "    %s : %s\n";
    }

    protected String createRelationTemplate() {
        if (dataStoresCount > 1) {
            return "\"%s:%s\" %s \"%s:%s\"\n";
        }
        return "%s %s %s\n";
    }

    protected RestClient configureClient(String baseUrl) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setAccept(List.of(MediaType.IMAGE_PNG));
                }).build();
    }

    protected byte[] receiveDiagramFile(String diagramDescription) {
        String endpoint;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
        deflater.setInput(diagramDescription.getBytes(StandardCharsets.UTF_8));
        deflater.finish();

        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }

        deflater.end();

        endpoint = String.format(urlTemplate, plantUmlEncoder.encode(outputStream.toByteArray()));

        byte[] resultPngBuf = restClient
                .get()
                .uri(endpoint)
                .retrieve()
                .body(byte[].class);

        if (resultPngBuf == null || resultPngBuf.length == 0) {
            throw new IllegalStateException("The diagram was not received");
        }
        return resultPngBuf;
    }

    @Override
    public String constructEntityDescription(String entityName, String dataStoreName, List<AttributeModel> attributeModelList) {
        StringBuilder entityDescription;

        if (dataStoresCount > 1) {
            entityDescription = new StringBuilder(String.format(entityTemplate, entityName, dataStoreName));
        } else {
            entityDescription = new StringBuilder(String.format(entityTemplate, entityName));
        }

        for (AttributeModel attribute : attributeModelList) {
            entityDescription.append(String.format(attributeTemplate, attribute.getAttributeName(), attribute.getJavaType()));
        }

        entityDescription.append("}\n");

        return entityDescription.toString();
    }

    @Override
    public String constructRelationDescription(String currentEntityType, String refEntityType, RelationType relationType, String dataStoreName) {
        String relationSign = switch (relationType) {
            case MANY_TO_ONE -> "}--";
            case ONE_TO_MANY -> "--{";
            case MANY_TO_MANY -> "}--{";
            case ONE_TO_ONE -> "--";
        };

        if (dataStoresCount > 1) {
            return String.format(relationTemplate, currentEntityType, dataStoreName, relationSign, refEntityType, dataStoreName);
        }
        return String.format(relationTemplate, currentEntityType, relationSign, refEntityType);
    }

    @Override
    public boolean pingService() {
        HttpStatusCode responseStatus;
        try {
            responseStatus = restClient
                    .head()
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode();
        } catch (ResourceAccessException exception) {
            return false;
        }

        return responseStatus.is2xxSuccessful() || responseStatus.is3xxRedirection();
    }

    @Override
    public byte[] generateDiagram(String entitiesDescription, String relationsDescriptions) {
        MessageFormat descriptionFormatter = new MessageFormat(template);

        String resultDescription = descriptionFormatter.format(new String[]{entitiesDescription, relationsDescriptions});
        return receiveDiagramFile(resultDescription);
    }
}
