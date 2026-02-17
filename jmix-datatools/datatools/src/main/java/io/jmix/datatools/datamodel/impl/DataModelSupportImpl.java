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

package io.jmix.datatools.datamodel.impl;

import io.jmix.core.JmixModuleDescriptor;
import io.jmix.core.JmixModules;
import io.jmix.core.Metadata;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.persistence.DbmsType;
import io.jmix.datatools.datamodel.*;
import io.jmix.datatools.datamodel.engine.DiagramConstructor;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("datatl_DataModelSupport")
public class DataModelSupportImpl implements DataModelSupport, InitializingBean {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DataSource dataSource;
    @Autowired
    protected DbmsType dbmsType;
    @Autowired
    protected JmixModules jmixModules;
    @Autowired
    protected DiagramConstructor diagramConstructor;

    protected DataModelProvider dataModelProvider;
    protected JmixModuleDescriptor mainModuleInfo;

    protected List<EntityModel> filteredModels;
    protected Set<String> dataStoreNames;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.dataModelProvider = createDataModelProvider();
        this.mainModuleInfo = jmixModules.getLast();
        this.dataStoreNames = getDataStoreNames();

        constructDataModel();
    }

    protected Set<String> getDataStoreNames() {
        return dataModelProvider.getDataModels().keySet();
    }

    protected DataModelProvider createDataModelProvider() {
        return new DataModelProvider();
    }

    protected DataModel createEntityDescription(MetaClass entity,
                                                boolean isEmbeddable) {
        List<AttributeModel> attributeModelsList = new ArrayList<>();
        List<MetaProperty> fields = entity.getProperties().stream().toList();
        Map<RelationType, List<Relation>> relationsMap = new HashMap<>();
        String dataStoreName = entity.getStore().getName();
        String relationDescription;

        for (MetaProperty field : fields) {
            AttributeModel attributeModel = null;
            String fieldName = field.getName();

            if (field.getType().equals(MetaProperty.Type.DATATYPE)) {
                if (field.getAnnotatedElement().isAnnotationPresent(Column.class)) {
                    String fieldType = field.getJavaType().getSimpleName();

                    if (isEmbeddable) {
                        attributeModel = constructAttribute(fieldName, fieldType, field.isMandatory());
                    } else {
                        attributeModel = constructAttribute(field.getAnnotatedElement().getAnnotation(Column.class).name(),
                                fieldName, fieldType, entity, field.isMandatory());
                    }
                }
            }

            if (field.getType().equals(MetaProperty.Type.EMBEDDED)) {
                MetaClass embeddableClass = metadata.findClass(field.getJavaType());

                if (embeddableClass == null) {
                    throw new IllegalStateException("Embeddable class not found");
                }

                DataModel embeddableDataModel = createEntityDescription(embeddableClass, true);

                String fieldType = field.getJavaType().getSimpleName();

                String embeddableFieldName, embeddableJavaType;

                if (field.getAnnotatedElement().isAnnotationPresent(AttributeOverrides.class)) {
                    AttributeOverride[] attributeOverrides = field.getAnnotatedElement().getAnnotation(AttributeOverrides.class).value();

                    for (int i = 0; i < attributeOverrides.length; i++) {
                        AttributeModel temp = constructAttribute(field.getAnnotatedElement()
                                        .getAnnotation(AttributeOverrides.class).value()[i].column().name(),
                                fieldName, fieldType, entity, field.isMandatory());
                        AttributeModel dataModelEntityAttributes = embeddableDataModel.attributeModels().get(i);
                        embeddableFieldName = fieldName + "." + dataModelEntityAttributes.getAttributeName();
                        embeddableJavaType = dataModelEntityAttributes.getJavaType();

                        AttributeModel embeddableAttribute = constructAttribute(temp.getColumnName(),
                                embeddableFieldName, embeddableJavaType, temp.getDbType(),
                                field.getAnnotatedElement().isAnnotationPresent(NotNull.class));

                        attributeModelsList.add(embeddableAttribute);
                    }
                }
            }

            if (field.getAnnotatedElement().isAnnotationPresent(ManyToOne.class)) {
                String fieldType = field.getJavaType().getSimpleName();

                if (isEmbeddable) {
                    attributeModel = constructAttribute(fieldName, fieldType, field.getAnnotatedElement().isAnnotationPresent(NotNull.class));
                } else {
                    attributeModel = constructAttribute(field.getAnnotatedElement().getAnnotation(JoinColumn.class).name(),
                            fieldName, fieldType, entity, field.isMandatory());
                }

                relationDescription = diagramConstructor.constructRelationDescription(entity.getName(), fieldType, RelationType.MANY_TO_ONE, dataStoreName);
                Relation relation = new Relation(dataStoreName, fieldType, relationDescription);
                putRelation(relationsMap, RelationType.MANY_TO_ONE, relation);
            }

            if (field.getAnnotatedElement().isAnnotationPresent(OneToMany.class)) {
                String fieldType = field.getRange().asClass().getName();

                attributeModel = constructAttribute(fieldName, fieldType, field.getAnnotatedElement().isAnnotationPresent(NotNull.class));

                relationDescription = diagramConstructor.constructRelationDescription(entity.getName(), fieldType, RelationType.ONE_TO_MANY, dataStoreName);
                Relation relation = new Relation(dataStoreName, fieldType, relationDescription);

                putRelation(relationsMap, RelationType.ONE_TO_MANY, relation);
            }

            if (field.getAnnotatedElement().isAnnotationPresent(OneToOne.class)) {
                String fieldType = field.getJavaType().getSimpleName();

                if (field.getAnnotatedElement().isAnnotationPresent(JoinColumn.class)) {
                    if (isEmbeddable) {
                        attributeModel = constructAttribute(fieldName, fieldType, field.getAnnotatedElement().isAnnotationPresent(NotNull.class));
                    } else {
                        attributeModel = constructAttribute(field.getAnnotatedElement().getAnnotation(JoinColumn.class).name(),
                                fieldName, fieldType, entity, field.isMandatory());
                    }
                } else {
                    boolean isMandatory = field.getAnnotatedElement().getAnnotation(OneToOne.class).optional();
                    attributeModel = constructAttribute(fieldName, fieldType, isMandatory);
                }

                relationDescription = diagramConstructor.constructRelationDescription(entity.getName(), fieldType, RelationType.ONE_TO_ONE, dataStoreName);
                Relation relation = new Relation(dataStoreName, fieldType, relationDescription);

                putRelation(relationsMap, RelationType.ONE_TO_ONE, relation);
            }

            if (field.getAnnotatedElement().isAnnotationPresent(ManyToMany.class)) {
                String fieldType = field.getRange().asClass().getName();

                attributeModel = constructAttribute(fieldName, fieldType, field.getAnnotatedElement().isAnnotationPresent(NotNull.class));

                JoinTable annotation = field.getAnnotatedElement().getAnnotation(JoinTable.class);

                String columnName = annotation.name()
                        + "."
                        + annotation.joinColumns()[0].name();

                attributeModel.setColumnName(columnName);

                relationDescription = diagramConstructor.constructRelationDescription(entity.getName(), fieldType, RelationType.MANY_TO_MANY, dataStoreName);
                Relation relation = new Relation(dataStoreName, fieldType, relationDescription);

                putRelation(relationsMap, RelationType.MANY_TO_MANY, relation);
            }

            if (attributeModel != null) {
                attributeModelsList.add(attributeModel);
            }
        }

        boolean isSystem = entity.getJavaClass().isAnnotationPresent(SystemLevel.class);

        String currentEntityType = entity.getName();

        String entityDescription = diagramConstructor
                .constructEntityDescription(currentEntityType, dataStoreName, attributeModelsList);

        EntityModel entityModel = constructEntityModel(entity, isSystem);

        DataModel dataModel = new DataModel(
                currentEntityType,
                entity.getStore().getName(),
                entityModel,
                relationsMap,
                entityDescription,
                attributeModelsList
        );

        dataModelProvider.putDataModel(dataModel);

        return dataModel;
    }

    protected EntityModel constructEntityModel(MetaClass entity, boolean isSystem) {
        EntityModel entityModel = metadata.create(EntityModel.class);

        entityModel.setName(entity.getName());
        entityModel.setDataStore(entity.getStore().getName());
        entityModel.setIsSystem(isSystem);
        entityModel.setTableName(entity.getJavaClass().isAnnotationPresent(Table.class)
                ? entity.getJavaClass().getAnnotation(Table.class).name()
                : "");

        return entityModel;
    }

    @Override
    public void setFilteredModels(List<EntityModel> filteredModels) {
        this.filteredModels = filteredModels;
    }

    @Override
    public int filteredModelsCount() {
        return filteredModels.size();
    }

    @Override
    public DataModelProvider getDataModelProvider() {
        return dataModelProvider;
    }

    protected void putRelation(Map<RelationType, List<Relation>> relations,
                               RelationType relationType, Relation relation) {
        if (relations.containsKey(relationType)) {
            relations.get(relationType).add(relation);
        } else {
            relations.put(relationType, new ArrayList<>(List.of(relation)));
        }
    }

    protected AttributeModel constructAttribute(String fieldName, String fieldType) {
        AttributeModel attributeModel = metadata.create(AttributeModel.class);

        attributeModel.setAttributeName(fieldName);
        attributeModel.setJavaType(fieldType);

        return attributeModel;
    }

    protected AttributeModel constructAttribute(String fieldName, String fieldType,
                                                boolean isMandatory) {
        AttributeModel attributeModel = constructAttribute(fieldName, fieldType);

        attributeModel.setIsMandatory(isMandatory);

        return attributeModel;
    }

    protected AttributeModel constructAttribute(String columnName, String fieldName,
                                                String fieldType, MetaClass entity,
                                                boolean isMandatory) {
        AttributeModel attributeModel = constructAttribute(fieldName, fieldType);

        attributeModel.setColumnName(columnName);
        attributeModel.setDbType(getDatabaseColumnType(entity, attributeModel, columnName));
        attributeModel.setIsMandatory(isMandatory);

        return attributeModel;
    }

    protected AttributeModel constructAttribute(String columnName, String fieldName,
                                                String fieldType, String dbType,
                                                boolean isMandatory) {
        AttributeModel attributeModel = constructAttribute(fieldName, fieldType);

        attributeModel.setColumnName(columnName);
        attributeModel.setDbType(dbType);
        attributeModel.setIsMandatory(isMandatory);

        return attributeModel;
    }

    protected String getDatabaseColumnType(@Nullable String schemaName,
                                           @Nullable String catalogName,
                                           String tableName,
                                           String columnName,
                                           @Nullable AttributeModel attributeModel) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData dbMetaData = conn.getMetaData();

            try (ResultSet columns = dbMetaData.getColumns(
                    catalogName,
                    schemaName,
                    tableName,
                    columnName)) {

                if (columns.next()) {
                    String typeName = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    int decimalDigits = columns.getInt("DECIMAL_DIGITS");
                    if (attributeModel != null) {
                        attributeModel.setIsNullable(columns.getString("IS_NULLABLE").equals("YES"));
                    }

                    StringBuilder type = new StringBuilder(typeName);

                    if (columnSize > 0) {
                        type.append("(").append(columnSize);
                        if (decimalDigits > 0) {
                            type.append(",").append(decimalDigits);
                        }
                        type.append(")");
                    }

                    return type.toString();
                } else {
                    throw new IllegalStateException("Column: " + columnName + " is not found in table: " + tableName);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error while receiving from database", e);
        }
    }

    protected String getDatabaseColumnType(MetaClass entity, AttributeModel attributeModel, String columnName) {
        Table annotation = entity.getJavaClass().getAnnotation(Table.class);
        String tableName;
        String catalogName = annotation.catalog().isEmpty()
                ? null
                : annotation.catalog().toUpperCase();
        String schemaName = annotation.schema().isEmpty()
                ? null
                : annotation.schema().toUpperCase();

        if (dbmsType.getType(entity.getStore().getName()).equals("POSTGRESQL")) {
            tableName = annotation.name().toLowerCase();
            columnName = columnName.toLowerCase();
        } else {
            tableName = annotation.name().toUpperCase();
            columnName = columnName.toUpperCase();
        }

        return getDatabaseColumnType(schemaName, catalogName, tableName, columnName, attributeModel);
    }

    protected void constructDataModel() {
        Collection<MetaClass> metaClasses = metadata.getClasses();

        for (MetaClass metaClass : metaClasses) {
            if (metaClass.getJavaClass().isAnnotationPresent(Entity.class)
                    && metaClass.getJavaClass().isAnnotationPresent(Table.class)) {
                createEntityDescription(metaClass, false);
            }
        }
    }

    protected List<Relation> crossRelationCheck(String currentEntity, String referencedEntity,
                                                String dataStore, RelationType relationType) {
        if (RelationType.getReverseRelation(relationType).equals(RelationType.ONE_TO_MANY)) {
            // inverse relation emulation for MANY_TO_ONE relation
            return dataModelProvider.getDataModel(dataStore, currentEntity).relations().get(relationType).stream()
                    .filter(el ->
                            el.referencedClass().equals(referencedEntity))
                    .map(e ->
                            new Relation(dataStore, currentEntity, e.relationDescription()))
                    .toList();
        }
        return new ArrayList<>();
    }

    protected void constructRelations(String currentEntity, String referencedEntity,
                                      String dataStore, StringBuilder relationsDescription) {
        if (!dataModelProvider.isModelExists(dataStore, currentEntity)
                || !dataModelProvider.isModelExists(dataStore, referencedEntity)) {
            return;
        }

        Map<RelationType, List<Relation>> directRelations =
                dataModelProvider.getRelationsByEntity(dataStore, currentEntity);
        Map<RelationType, List<Relation>> referencedRelations =
                dataModelProvider.getRelationsByEntity(dataStore, referencedEntity);
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

    @Override
    public byte[] generateFilteredDiagram() {
        StringBuilder tempEntitiesDescription = new StringBuilder();
        StringBuilder tempRelationsDescription = new StringBuilder();
        Set<String> completedModels = new HashSet<>();
        List<String> entityModelsNames = filteredModels.stream().map(EntityModel::getName).toList();

        for (EntityModel model : filteredModels) {
            for (String dataStore : dataStoreNames) {
                tempEntitiesDescription
                        .append(dataModelProvider.getDataModel(dataStore, model.getName()).entityDescription());
                if (!dataModelProvider.hasRelations(dataStore, model.getName())) {
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

        return diagramConstructor.getDiagram(tempEntitiesDescription.toString(), tempRelationsDescription.toString());
    }

    public byte[] generateDiagram() {
        filteredModels = new ArrayList<>();

        for (String dataStore : dataStoreNames) {
            filteredModels.addAll(dataModelProvider.getDataModels(dataStore).values().stream()
                    .map(DataModel::entityModel).toList());
        }

        return generateFilteredDiagram();
    }
}