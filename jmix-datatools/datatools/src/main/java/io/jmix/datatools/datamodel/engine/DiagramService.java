package io.jmix.datatools.datamodel.engine;

import io.jmix.datatools.datamodel.RelationType;
import io.jmix.datatools.datamodel.entity.AttributeModel;

import java.util.List;

/**
 * Interface for constructing entity and relationship descriptions, as well as generating diagrams,
 * designed to work with a specific diagramming library.
 */
public interface DiagramService {

    /**
     * Constructs a description of an entity in the format required by the selected diagramming library.
     *
     * @param entityName         the name of the entity to be described
     * @param dataStoreName      the name of the data store where the entity resides
     * @param attributeModelList a list of attributes that define the structure of the entity
     * @return a string representing the description of the entity in the required format
     */
    String constructEntityDescription(String entityName, String dataStoreName, List<AttributeModel> attributeModelList);

    /**
     * Constructs a string representation of a relationship between two entities in a specific format
     * for use with a diagramming library.
     *
     * @param currentEntityType the type of the current entity involved in the relationship
     * @param refEntityType     the type of the referenced entity involved in the relationship
     * @param relationType      the type of the relationship (e.g., MANY_TO_ONE, ONE_TO_MANY)
     * @param dataStoreName     the name of the data store where the entities reside
     * @return a string describing the relationship between the specified entities in the required format
     */
    String constructRelationDescription(String currentEntityType, String refEntityType, RelationType relationType, String dataStoreName);

    /**
     * Generates a diagram in a binary format based on the provided descriptions of entities and their relationships.
     *
     * @param entitiesDescription   a string containing the descriptions of entities to be included in the diagram
     * @param relationsDescriptions a string containing the descriptions of relationships between entities
     * @return a byte array representing the generated diagram
     */
    byte[] generateDiagram(String entitiesDescription, String relationsDescriptions);

    /**
     * Checks the availability of the remote diagramming service.
     *
     * @return {@code true} if the service is accessible, otherwise {@code false}.
     */
    boolean pingService();
}
