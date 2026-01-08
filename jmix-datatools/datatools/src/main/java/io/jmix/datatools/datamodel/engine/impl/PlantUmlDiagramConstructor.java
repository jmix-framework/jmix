package io.jmix.datatools.datamodel.engine.impl;

import io.jmix.datatools.DatatoolsProperties;
import io.jmix.datatools.datamodel.app.RelationType;
import io.jmix.datatools.datamodel.engine.DiagramConstructor;
import io.jmix.datatools.datamodel.engine.PlantUMLEncoder;
import io.jmix.datatools.datamodel.entity.AttributeModel;
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
import java.util.zip.Deflater;

public class PlantUmlDiagramConstructor implements DiagramConstructor {
    protected DatatoolsProperties datatoolsProperties;
    protected String template;
    protected String baseUrl;
    protected String entityTemplate;
    protected String attributeTemplate;
    protected String relationTemplate;
    protected String urlTemplate;
    protected RestClient restClient;
    protected PlantUMLEncoder plantUMLEncoder;

    public PlantUmlDiagramConstructor(DatatoolsProperties datatoolsProperties) {
        this.datatoolsProperties = datatoolsProperties;
        this.plantUMLEncoder = new PlantUMLEncoderImpl();
        configureEngine();
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

        endpoint = String.format(urlTemplate, plantUMLEncoder.encode(outputStream.toByteArray()));

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

    protected void configureEngine() {
        template =
                """
                @startuml
                {0}
                
                {1}
                @enduml
                """;
        baseUrl = datatoolsProperties.getDiagramConstructor().getHost() == null
                ? "https://www.plantuml.com"
                : datatoolsProperties.getDiagramConstructor().getHost();
        urlTemplate = "/plantuml/png/%s";
        entityTemplate = "entity %s {\n";
        attributeTemplate = "    %s : %s\n";
        relationTemplate = "%s %s %s\n";
        restClient = configureClient(baseUrl);
    }

    @Override
    public String constructEntityDescription(String entityName, List<AttributeModel> attributeModelList) {
        StringBuilder entityDescription = new StringBuilder(String.format(entityTemplate, entityName));

        for (AttributeModel attribute : attributeModelList) {
            entityDescription.append(String.format(attributeTemplate, attribute.getAttributeName(), attribute.getJavaType()));
        }

        entityDescription.append("}\n");

        return entityDescription.toString();
    }

    @Override
    public String constructRelationDescription(String currentEntityType, String refEntityType, RelationType relationType) {
        String relationSign = switch(relationType) {
            case MANY_TO_ONE -> "}--";
            case ONE_TO_MANY -> "--{";
            case MANY_TO_MANY -> "}--{";
            case ONE_TO_ONE -> "--";
        };

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
    public byte[] getDiagram(String entitiesDescription, String relationsDescriptions) {
        MessageFormat descriptionFormatter = new MessageFormat(template);

        String resultDescription = descriptionFormatter.format(new String[]{entitiesDescription, relationsDescriptions});
        return receiveDiagramFile(resultDescription);
    }
}
