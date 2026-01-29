package io.jmix.datatools.datamodel.engine;

import io.jmix.datatools.datamodel.app.RelationType;
import io.jmix.datatools.datamodel.entity.AttributeModel;

import java.util.List;

/**
 * Describes a diagram constructor that provides creating the necessary diagram descriptions (for PlantUML and Mermaid)
 * in a compatible string format
 */
public interface DiagramConstructor {

    /**
     * Creates an entity description suitable for use in the selected library
     * @param entityName name of the entity for which the entity schema will be constructed
     * @param dataStoreName name of data store
     * @param attributeModelList list of attributes of this entity
     * @return entity schema description that describes a single entity and its attributes in the form of
     * a string description of the library required format for further embedding into the general code of
     * the diagram to construct the final diagram
     */
    String constructEntityDescription(String entityName, String dataStoreName, List<AttributeModel> attributeModelList);

    /**
     * Creates a relationship description suitable for use in the selected library
     * @param currentEntityType type of the entity for which the entity schema will be constructed
     * @param refEntityType type of entity referenced by the currentEntityType argument
     * @param relationType type of relationship between related entities
     * @param dataStoreName name of data store
     * @return the relationship between two entities in the form of a string description of the library required format
     * for further embedding into the general code of the diagram to construct the final diagram
     */
    String constructRelationDescription(String currentEntityType, String refEntityType, RelationType relationType, String dataStoreName);

    /**
     * Creates a diagram file in PNG format
     * @param entitiesDescription the full entity diagram in the format required by the selected library
     * @param relationsDescriptions the full relationship diagram in the format required by the selected library
     * @return a byte array representing the byte representation of the PNG image
     */
    byte[] getDiagram(String entitiesDescription, String relationsDescriptions);

    /**
     * Checks the availability of the diagram generation service
     * @return operation result. Returns true if the operation was successful, false otherwise
     */
    boolean pingService();
}
