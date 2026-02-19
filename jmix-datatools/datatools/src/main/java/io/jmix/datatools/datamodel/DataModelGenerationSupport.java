/*
 * Copyright 2025 Haulmont.
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

package io.jmix.datatools.datamodel;

import io.jmix.datatools.datamodel.engine.DiagramEngine;
import io.jmix.datatools.datamodel.entity.EntityModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provides functionality to generate visual representations of data models, including
 * entities and their relationships, in diagram format.
 */
@Component("datatl_DataModelSupport")
public class DataModelGenerationSupport {

    @Autowired
    protected DiagramEngine diagramEngine;
    @Autowired
    protected DataModelRegistry dataModelRegistry;

    /**
     * Generate a PNG representation of a diagram as a byte array.
     *
     * @return PNG representation of a diagram as a byte array
     */
    public byte[] generateDiagram(List<EntityModel> models) {
        StringBuilder tempEntitiesDescription = new StringBuilder();
        StringBuilder tempRelationsDescription = new StringBuilder();
        Set<String> completedModels = new HashSet<>();
        List<String> entityModelsNames = models.stream().map(EntityModel::getName).toList();
        Set<String> dataStoreNames = dataModelRegistry.getDataStoreNames();

        for (EntityModel model : models) {
            for (String dataStore : dataStoreNames) {
                DataModel dataModel = dataModelRegistry.getDataModel(dataStore, model.getName());
                if (dataModel == null) {
                    continue;
                }
                tempEntitiesDescription.append(dataModel.entityDescription());

                if (!dataModelRegistry.hasRelations(dataStore, model.getName())) {
                    continue;
                }

                for (String referencedEntity : entityModelsNames) {
                    if (!model.getName().equals(referencedEntity)
                            && !completedModels.contains(referencedEntity)) {
                        constructRelations(model.getName(), referencedEntity, dataStore, tempRelationsDescription);
                    }
                }

                completedModels.add(model.getName());

            }
        }

        return diagramEngine.generateDiagram(tempEntitiesDescription.toString(), tempRelationsDescription.toString());
    }

    protected void constructRelations(String currentEntity, String referencedEntity,
                                      String dataStore, StringBuilder relationsDescription) {
        if (!(containsModel(dataStore, currentEntity)
                && containsModel(dataStore, referencedEntity))) {
            return;
        }

        Map<RelationType, List<Relation>> directRelations =
                dataModelRegistry.getEntityRelations(dataStore, currentEntity);
        Map<RelationType, List<Relation>> referencedRelations =
                dataModelRegistry.getEntityRelations(dataStore, referencedEntity);
        Set<RelationType> directRelationTypes = directRelations.keySet();

        if (directRelationTypes.isEmpty()) {
            return;
        }

        for (RelationType relationType : directRelationTypes) {
            referencedRelations.getOrDefault(RelationType.getReverseRelation(relationType),
                            crossRelationCheck(currentEntity, referencedEntity, dataStore, relationType))
                    .stream()
                    .filter(el ->
                            el.referencedClass().equals(currentEntity))
                    .forEach(e ->
                            relationsDescription.append(e.relationDescription()));
        }
    }

    protected boolean containsModel(String dataStore, String entityName) {
        return dataModelRegistry.getDataModels(dataStore).containsKey(entityName);
    }

    protected List<Relation> crossRelationCheck(String currentEntity, String referencedEntity,
                                                String dataStore, RelationType relationType) {
        if (RelationType.getReverseRelation(relationType).equals(RelationType.ONE_TO_MANY)) {
            // inverse relation emulation for MANY_TO_ONE relation
            DataModel dataModel = dataModelRegistry.getDataModel(dataStore, currentEntity);
            return dataModel != null
                    ? dataModel.relations().get(relationType).stream()
                    .filter(el ->
                            el.referencedClass().equals(referencedEntity))
                    .map(e ->
                            new Relation(dataStore, currentEntity, e.relationDescription()))
                    .toList()
                    : Collections.emptyList();
        }

        return Collections.emptyList();
    }
}