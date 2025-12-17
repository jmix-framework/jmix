package io.jmix.datatools.datamodel.engine.impl;

import io.jmix.datatools.DatatoolsProperties;
import io.jmix.datatools.datamodel.app.RelationType;
import io.jmix.datatools.datamodel.engine.DiagramConstructor;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.text.MessageFormat;
import java.util.HexFormat;
import java.util.List;

public class PlantUmlDiagramConstructor implements DiagramConstructor {
    protected DatatoolsProperties datatoolsProperties;
    protected String template;
    protected String entityTemplate;
    protected String attributeTemplate;
    protected String relationTemplate;
    protected String urlTemplate;
    protected RestClient restClient;

    public PlantUmlDiagramConstructor(DatatoolsProperties datatoolsProperties) {
        this.datatoolsProperties = datatoolsProperties;
        configureEngine();
    }

    protected RestClient configureClient(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.setAccept(List.of(MediaType.IMAGE_PNG));
                }).build();
    }

    protected byte[] receiveDiagramFile(String diagramDescription) {
        String ecnodedString = HexFormat.of().formatHex(diagramDescription.getBytes());
        String endpoint = String.format(urlTemplate, ecnodedString);

        byte[] resultPngBuf = restClient.get()
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
        String baseUrl = datatoolsProperties.getDiagramConstructor().getHost() == null
                ? "https://www.plantuml.com"
                : datatoolsProperties.getDiagramConstructor().getHost();
        urlTemplate = "/plantuml/png/~h%s";
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
    public byte[] getDiagram(String entitiesDescription, String relationsDescriptions) {
        MessageFormat descriptionFormatter = new MessageFormat(template);

        String resultDescription = descriptionFormatter.format(new String[]{entitiesDescription, relationsDescriptions});
        return receiveDiagramFile(resultDescription);
    }
}
