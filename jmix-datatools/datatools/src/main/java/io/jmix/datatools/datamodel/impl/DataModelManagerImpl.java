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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.data.persistence.DbmsType;
import io.jmix.datatools.datamodel.DataModel;
import io.jmix.datatools.datamodel.DataModelHolder;
import io.jmix.datatools.datamodel.DataModelManager;
import io.jmix.datatools.datamodel.app.Relation;
import io.jmix.datatools.datamodel.app.RelationType;
import io.jmix.datatools.datamodel.engine.DiagramConstructor;
import io.jmix.datatools.datamodel.entity.AttributeModel;
import io.jmix.datatools.datamodel.entity.EntityModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("datatl_DataModelManager")
public class DataModelManagerImpl implements DataModelManager {
    protected final Metadata metadata;
    protected final DataModelHolder dataModelHolder;
    protected final DiagramConstructor diagramConstructor;

    protected final DataSource dataSource;
    protected final DbmsType dbmsType;
    protected final JmixModules jmixModules;

    protected final JmixModuleDescriptor mainModuleInfo;

    protected List<EntityModel> filteredModels;

    public DataModelManagerImpl(Metadata metadata, DataSource dataSource, DiagramConstructor diagramConstructor, DbmsType dbmsType,
                                JmixModules jmixModules) {
        this.metadata = metadata;
        this.dataModelHolder = new DataModelHolder();
        this.dataSource = dataSource;
        this.diagramConstructor = diagramConstructor;
        this.dbmsType = dbmsType;
        this.jmixModules = jmixModules;

        this.mainModuleInfo = jmixModules.getLast();

        constructDataModel();
    }

    protected DataModel createEntityDescription(MetaClass entity,
                                                boolean isEmbeddable) {
        List<AttributeModel> attributeModelsList = new ArrayList<>();
        List<Field> fields = Arrays.stream(entity.getJavaClass().getDeclaredFields()).toList();
        Map<RelationType, List<Relation>> relationsMap = new HashMap<>();
        String relationDescription;

        //entity.getProperties()

        for (Field field : fields) {
            AttributeModel attributeModel = null;
            String fieldName = null, fieldType = null;

            if (field.isAnnotationPresent(Column.class)) {
                fieldName = field.getName();
                fieldType = field.getType().getSimpleName();

                if (isEmbeddable) {
                    attributeModel = constructAttribute(fieldName, fieldType, field.isAnnotationPresent(NotNull.class));
                } else {
                    attributeModel = constructAttribute(field.getAnnotation(Column.class).name(), fieldName, fieldType, entity);
                }
            }
            if (field.isAnnotationPresent(Embedded.class)) {
                MetaClass embeddableClass = metadata.findClass(field.getType());

                if (embeddableClass == null) {
                    throw new IllegalStateException("Embeddable class not found");
                }

                DataModel embeddableDataModel = createEntityDescription(embeddableClass, true);

                fieldName = field.getName();
                fieldType = field.getType().getSimpleName();

                String embeddableFieldName, embeddableJavaType;

                if (field.isAnnotationPresent(AttributeOverrides.class)) {
                    AttributeOverride[] attributeOverrides = field.getAnnotation(AttributeOverrides.class).value();

                    for (int i = 0; i < attributeOverrides.length; i++ ) {
                        AttributeModel temp = constructAttribute(field.getAnnotation(AttributeOverrides.class).value()[i].column().name(),
                                fieldName, fieldType, entity);
                        AttributeModel dataModelEntityAttributes = embeddableDataModel.getAttributeModels().get(i);
                        embeddableFieldName = fieldName + "." + dataModelEntityAttributes.getAttributeName();
                        embeddableJavaType = dataModelEntityAttributes.getJavaType();

                        AttributeModel embeddableAttribute = constructAttribute(temp.getColumnName(),
                                embeddableFieldName, embeddableJavaType, temp.getDbType(),
                                field.isAnnotationPresent(NotNull.class));

                        attributeModelsList.add(embeddableAttribute);
                    }
                }
                //attributeModel = null;
            }

            if (field.isAnnotationPresent(ManyToOne.class)) {
                fieldName = field.getName();
                fieldType = field.getType().getSimpleName();

                if (isEmbeddable) {
                    attributeModel = constructAttribute(fieldName, fieldType, field.isAnnotationPresent(NotNull.class));
                } else {
                    attributeModel = constructAttribute(field.getAnnotation(JoinColumn.class).name(), fieldName, fieldType, entity);
                }

                relationDescription = diagramConstructor.constructRelationDescription(entity.getName(), fieldType, RelationType.MANY_TO_ONE);
                Relation relation = new Relation(fieldType, relationDescription);
                putRelation(relationsMap, RelationType.MANY_TO_ONE, relation);
            }

            if (field.isAnnotationPresent(OneToMany.class)) {
                fieldName = field.getName();
                Type genericType = field.getGenericType();

                if (field.getGenericType() instanceof ParameterizedType) {
                    String fullFieldType = Stream.of(((ParameterizedType) genericType).getActualTypeArguments())
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unknown generic type")).getTypeName();

                    String[] temp = fullFieldType.split("\\.");
                    fieldType = temp[temp.length-1];
                }

                attributeModel = constructAttribute(fieldName, fieldType, field.isAnnotationPresent(NotNull.class));

                relationDescription = diagramConstructor.constructRelationDescription(entity.getName(), fieldType, RelationType.ONE_TO_MANY);
                Relation relation = new Relation(fieldType, relationDescription);

                putRelation(relationsMap, RelationType.ONE_TO_MANY, relation);
            }

            if (field.isAnnotationPresent(OneToOne.class)) {
                fieldName = field.getName();
                fieldType = field.getType().getSimpleName();

                if (field.isAnnotationPresent(JoinColumn.class)) {
                    if (isEmbeddable) {
                        attributeModel = constructAttribute(fieldName, fieldType, field.isAnnotationPresent(NotNull.class));
                    } else {
                        attributeModel = constructAttribute(field.getAnnotation(JoinColumn.class).name(), fieldName, fieldType, entity);
                    }
                } else {
                    boolean isMandatory = field.getAnnotation(OneToOne.class).optional();
                    attributeModel = constructAttribute(fieldName, fieldType, isMandatory);
                }

                relationDescription = diagramConstructor.constructRelationDescription(entity.getName(), fieldType, RelationType.ONE_TO_ONE);
                Relation relation = new Relation(fieldType, relationDescription);

                putRelation(relationsMap, RelationType.ONE_TO_ONE, relation);
            }

            if (field.isAnnotationPresent(ManyToMany.class)) {
                fieldName = field.getName();
                Type genericType = field.getGenericType();

                if (field.getGenericType() instanceof ParameterizedType) {
                    String fullFieldType = Stream.of(((ParameterizedType) genericType).getActualTypeArguments())
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unknown generic type")).getTypeName();

                    String[] temp = fullFieldType.split("\\.");
                    fieldType = temp[temp.length-1];
                }

                attributeModel = constructAttribute(fieldName, fieldType, field.isAnnotationPresent(NotNull.class));

                JoinTable annotation = field.getAnnotation(JoinTable.class);

                String columnName = annotation.name()
                        + "."
                        + annotation.joinColumns()[0].name();

                attributeModel.setColumnName(columnName);

                relationDescription = diagramConstructor.constructRelationDescription(entity.getName(), fieldType, RelationType.MANY_TO_MANY);
                Relation relation = new Relation(fieldType, relationDescription);

                putRelation(relationsMap, RelationType.MANY_TO_MANY, relation);
            }

            if (fieldType == null || fieldName == null) {
                continue;
            }

            if (attributeModel != null) {
                attributeModelsList.add(attributeModel);
            }
        }

        String entityBasePackage = getEntityBasePackage(entity);
        boolean isSystem = !entityBasePackage.equals(mainModuleInfo.getBasePackage());

        String currentEntityType = entity.getName();

        String entityDescription = diagramConstructor.constructEntityDescription(currentEntityType, attributeModelsList);

        EntityModel entityModel = constructEntityModel(entity, isSystem);

        DataModel dataModel = new DataModel(
                currentEntityType,
                entityModel,
                relationsMap,
                entityDescription,
                attributeModelsList
        );

        dataModelHolder.putDataModel(dataModel);

        return dataModel;
    }

    protected String getEntityBasePackage(MetaClass entity) {
        String[] splittedPackageName = entity.getJavaClass().getPackage().getName().split("\\.");
        StringBuilder entityBasePackage = new StringBuilder(3);

        int maxId = 3;
        int cutId = maxId-1;

        for (int i = 0; i < maxId; i++) {
            entityBasePackage.append(splittedPackageName[i]);

            if (i != cutId) {
                entityBasePackage.append(".");
            }
        }

        return entityBasePackage.toString();
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
    public DataModelHolder getDataModelHolder() {
        return dataModelHolder;
    }

    protected void putRelation(Map<RelationType, List<Relation>> relations, RelationType relationType, Relation relation) {
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

    protected AttributeModel constructAttribute(String fieldName, String fieldType, boolean isMandatory) {
        AttributeModel attributeModel = constructAttribute(fieldName, fieldType);

        attributeModel.setIsMandatory(isMandatory);

        return attributeModel;
    }

    /**
     * Construct attribute with database access
     * @param columnName
     * @param fieldName
     * @param fieldType
     * @param entity
     * @return
     */
    protected AttributeModel constructAttribute(String columnName, String fieldName, String fieldType, MetaClass entity) {
        AttributeModel attributeModel = constructAttribute(fieldName, fieldType);

        attributeModel.setColumnName(columnName);
        attributeModel.setDbType(getDatabaseColumnType(entity, attributeModel, columnName));
        attributeModel.setIsMandatory(!attributeModel.getIsNullable());

        return attributeModel;
    }

    protected AttributeModel constructAttribute(String columnName, String fieldName, String fieldType, String dbType, boolean isMandatory) {
        AttributeModel attributeModel = constructAttribute(fieldName, fieldType);

        attributeModel.setColumnName(columnName);
        attributeModel.setDbType(dbType);
        attributeModel.setIsMandatory(isMandatory);

        return attributeModel;
    }

    protected String getDatabaseColumnType(@Nullable String schemaName, @Nullable String catalogName, String tableName,
                                           String columnName, @Nullable AttributeModel attributeModel) {
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
        Collection<MetaClass> metaClasses  = metadata.getClasses();
        List<JmixModuleDescriptor> modulesList = jmixModules.getAll();
        JmixModuleDescriptor appPackageModule = jmixModules.getLast();

        for (MetaClass metaClass : metaClasses) {
            if (metaClass.getJavaClass().isAnnotationPresent(Entity.class)
            && metaClass.getJavaClass().isAnnotationPresent(Table.class)) {
                createEntityDescription(metaClass, false);
            }
        }
    }

    protected List<Relation> crossRelationCheck(String currentEntity, String referencedEntity, RelationType relationType) {
        if (RelationType.getReverseRelation(relationType).equals(RelationType.ONE_TO_MANY)) {
            // emulate reverse relation for MANY_TO_ONE relation
            return dataModelHolder.getDataModel(currentEntity).getRelations().get(relationType).stream()
                    .filter(el -> el.referencedClass().equals(referencedEntity))
                    .map(e -> new Relation(currentEntity, e.relationDescription()))
                    .toList();
        }
        return new ArrayList<>();
    }

    protected void constructRelations(String currentEntity, String referencedEntity, StringBuilder relationsDescription) {
        if (!dataModelHolder.isModelExists(currentEntity)
            || !dataModelHolder.isModelExists(referencedEntity)) {
            return;
        }

        Map<RelationType, List<Relation>> directRelations = dataModelHolder.getRelationsByEntity(currentEntity);
        Map<RelationType, List<Relation>> referencedRelations = dataModelHolder.getRelationsByEntity(referencedEntity);
        Set<RelationType> directRelationTypes = directRelations.keySet();

        if (directRelationTypes.isEmpty()) {
            return;
        }


        for (RelationType relationType : directRelationTypes) {
            referencedRelations.getOrDefault(RelationType.getReverseRelation(relationType),
                            crossRelationCheck(currentEntity, referencedEntity, relationType))
                    .stream().filter(el -> el.referencedClass().equals(currentEntity))
                    .forEach(e -> relationsDescription.append(e.relationDescription()));
        };
    }

    @Override
    public byte[] generateFilteredDiagram() {
        StringBuilder tempEntitiesDescription = new StringBuilder();
        StringBuilder tempRelationsDescription = new StringBuilder();
        Set<String> completedModels = new HashSet<>();
        List<String> entityModelsNames = filteredModels.stream().map(EntityModel::getName).toList();

        for (EntityModel model : filteredModels) {
            tempEntitiesDescription.append(dataModelHolder.getDataModel(model.getName()).getEntityDescription());
            if (!dataModelHolder.hasRelations(model.getName())) {
                continue;
            }
            for (String referencedEntity : entityModelsNames) {
                if (model.getName().equals(referencedEntity) || completedModels.contains(referencedEntity)) continue;
                constructRelations(model.getName(), referencedEntity, tempRelationsDescription);
            }
            completedModels.add(model.getName());
        }

        return diagramConstructor.getDiagram(tempEntitiesDescription.toString(), tempRelationsDescription.toString());
    }

    public byte[] generateDiagram() {
        filteredModels = dataModelHolder.getDataModels().values().stream().map(DataModel::getEntityModel).toList();

        return generateFilteredDiagram();
    }
}