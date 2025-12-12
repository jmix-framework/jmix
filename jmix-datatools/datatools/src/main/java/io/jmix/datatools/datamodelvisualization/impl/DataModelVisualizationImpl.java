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

package io.jmix.datatools.datamodelvisualization.impl;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.datatools.datamodelvisualization.DataModelVisualization;
import io.jmix.datatools.datamodelvisualization.util.Relation;
import io.jmix.datatools.datamodelvisualization.util.RelationType;
import jakarta.persistence.*;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.core.DiagramDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

@Component("datatl_DataModelVisualization")
public class DataModelVisualizationImpl implements DataModelVisualization {

    @Autowired
    private Metadata metadata;

    protected StringBuilder constructEntityDescription(MetaClass entity,
                                                       Map<String, Relation> relations,
                                                       Map<String, StringBuilder> embeddableEntities) {
        StringBuilder entityDescription = new StringBuilder(String.format("entity %s {\n", entity.getName()));
        List<Field> fields = Arrays.stream(entity.getJavaClass().getDeclaredFields()).toList();

        for (Field field : fields) {
            String fieldName = "";
            String fieldType = "";

            if (field.isAnnotationPresent(Column.class)) {
                fieldName = field.getName();
                fieldType = field.getType().getSimpleName();
            }

            if (field.isAnnotationPresent(Embedded.class)) {
                MetaClass embeddableClass = metadata.findClass(field.getType());

                if (embeddableClass == null) {
                    throw new IllegalArgumentException("Embeddable class not found");
                }

                embeddableEntities.put(embeddableClass.getName(), constructEntityDescription(embeddableClass, relations, embeddableEntities));

                fieldName = field.getName();
                fieldType = field.getType().getSimpleName();
            }

            if (field.isAnnotationPresent(ManyToOne.class)) {
                fieldName = field.getName();
                fieldType = field.getType().getSimpleName();
                String currentEntityType = entity.getName();

                if (relations.containsKey(fieldType)) {
                    if (relations.get(fieldType).referencedClass().equals(entity.getName())
                            && relations.get(fieldType).relationType().equals(RelationType.ONE_TO_MANY)) {
                        continue;
                    }
                    relations.put(currentEntityType, new Relation(fieldType, RelationType.MANY_TO_ONE));
                }

                relations.put(currentEntityType, new Relation(fieldType, RelationType.MANY_TO_ONE));
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

                String currentEntityType = entity.getName();

                if (relations.containsKey(fieldType)) {
                    if (relations.get(fieldType).referencedClass().equals(currentEntityType)
                            && relations.get(fieldType).relationType().equals(RelationType.MANY_TO_ONE)) {
                        continue;
                    }
                    relations.put(currentEntityType, new Relation(fieldType, RelationType.ONE_TO_MANY));
                }

                relations.put(currentEntityType, new Relation(fieldType, RelationType.ONE_TO_MANY));
            }

            if (field.isAnnotationPresent(OneToOne.class)) {
                fieldName = field.getName();
                fieldType = field.getType().getSimpleName();
                String currentEntityType = entity.getName();

                if (relations.containsKey(fieldType)) {
                    if (relations.get(fieldType).referencedClass().equals(currentEntityType)
                            && relations.get(fieldType).relationType().equals(RelationType.ONE_TO_ONE)) {
                        continue;
                    }
                    relations.put(currentEntityType, new Relation(fieldType, RelationType.ONE_TO_ONE));
                }

                relations.put(currentEntityType, new Relation(fieldType, RelationType.ONE_TO_ONE));
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

                String currentEntityType = entity.getName();

                if (relations.containsKey(fieldType)) {
                    if (relations.get(fieldType).referencedClass().equals(currentEntityType)
                            && relations.get(fieldType).relationType().equals(RelationType.MANY_TO_MANY)) {
                        continue;
                    }
                    relations.put(currentEntityType, new Relation(fieldType, RelationType.MANY_TO_MANY));
                }

                relations.put(currentEntityType, new Relation(fieldType, RelationType.MANY_TO_MANY));
            }

            if (fieldType.isEmpty() || fieldName.isEmpty()) {
                continue;
            }

            entityDescription.append((String.format("    %s %s\n", fieldType, fieldName)));
        }

        entityDescription.append("}\n");
        return entityDescription;
    }

    protected StringBuilder constructDescription() {
        Collection<MetaClass> metaClasses = metadata.getClasses();
        Map<String, Relation> relationsMap = new HashMap<>();
        Map<String, StringBuilder> embeddableEntities = new HashMap<>();

        StringBuilder descriptionResult = new StringBuilder("@startuml\n");

        for (MetaClass metaClass : metaClasses) {
            if (metaClass.getJavaClass().isAnnotationPresent(Entity.class)) {
                if (!metaClass.getJavaClass().getPackage().getName().startsWith("io.jmix")) {
                    descriptionResult.append(constructEntityDescription(metaClass, relationsMap, embeddableEntities));
                }
            }
        }

        if (!embeddableEntities.isEmpty()) {
            Set<String> embeddableEntitiesKeys = embeddableEntities.keySet();
            for (String embeddableEntity : embeddableEntitiesKeys) {
                descriptionResult.append(embeddableEntities.get(embeddableEntity));
            }
        }

        descriptionResult.append(constructRelations(relationsMap));
        descriptionResult.append("@enduml");

        return descriptionResult;
    }

    protected StringBuilder constructRelations(Map<String, Relation> relations) {
        StringBuilder relationDescription = new StringBuilder();
        Set<String> relationKeys = relations.keySet();

        for (String relationKey : relationKeys) {
            String relationSign = switch (relations.get(relationKey).relationType()) {
                case MANY_TO_ONE -> "}--";
                case ONE_TO_MANY -> "--{";
                case MANY_TO_MANY -> "}--{";
                case ONE_TO_ONE -> "--";
            };

            relationDescription.append(String.format("%s %s %s\n", relationKey, relationSign, relations.get(relationKey).referencedClass()));
        }

        return relationDescription;
    }
    
    @Override
    public DiagramDescription createStringReader(ByteArrayOutputStream outputStream) throws IOException {
        SourceStringReader reader = new SourceStringReader(constructDescription().toString());

        return reader.outputImage(outputStream);
    }
}
