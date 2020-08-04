/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core.impl.importexport;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.jmix.core.*;
import io.jmix.core.context.ImportEntityContext;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Component(EntityImportViewJsonBuilder.NAME)
public class EntityImportViewJsonBuilderImpl implements EntityImportViewJsonBuilder {

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected EntityImportViews entityImportViews;

    @Override
    public EntityImportView buildFromJson(String json, MetaClass metaClass) {
        JsonElement rootElement = JsonParser.parseString(json);
        if (!rootElement.isJsonObject()) {
            throw new RuntimeException("Passed json is not a JSON object");
        }
        return buildFromJsonObject(rootElement.getAsJsonObject(), metaClass);
    }

    protected EntityImportView buildFromJsonObject(JsonObject jsonObject, MetaClass metaClass) {
        EntityImportViewBuilder viewBuilder = entityImportViews.builder(metaClass.getJavaClass());

        ImportEntityContext importContext = new ImportEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(importContext);

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String propertyName = entry.getKey();
            MetaProperty metaProperty = metaClass.getProperty(propertyName);

            Range propertyRange = metaProperty.getRange();
            Class<?> propertyType = metaProperty.getJavaType();
            if (propertyRange.isDatatype() || propertyRange.isEnum()) {
                if (importContext.isImportPermitted(propertyName)) {
                    viewBuilder.addLocalProperty(propertyName);
                }
            } else if (propertyRange.isClass()) {
                if (JmixEntity.class.isAssignableFrom(propertyType)) {
                    if (metadataTools.isEmbedded(metaProperty)) {
                        MetaClass propertyMetaClass = metadata.getClass(propertyType);
                        JsonElement propertyJsonObject = entry.getValue();
                        if (!propertyJsonObject.isJsonObject()) {
                            throw new RuntimeException("JsonObject was expected for property " + propertyName);
                        }
                        if (importContext.isImportPermitted(propertyName)) {
                            EntityImportView propertyImportView = buildFromJsonObject(propertyJsonObject.getAsJsonObject(), propertyMetaClass);
                            viewBuilder.addEmbeddedProperty(propertyName, propertyImportView);
                        }
                    } else {
                        MetaClass propertyMetaClass = metadata.getClass(propertyType);
                        if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
                            JsonElement propertyJsonObject = entry.getValue();
                            if (importContext.isImportPermitted(propertyName)) {
                                if (propertyJsonObject.isJsonNull()) {
                                    //in case of null we must add such import behavior to update the reference with null value later
                                    if (metaProperty.getRange().getCardinality() == Range.Cardinality.MANY_TO_ONE) {
                                        viewBuilder.addManyToOneProperty(propertyName, ReferenceImportBehaviour.IGNORE_MISSING);
                                    } else {
                                        viewBuilder.addOneToOneProperty(propertyName, ReferenceImportBehaviour.IGNORE_MISSING);
                                    }
                                } else {
                                    if (!propertyJsonObject.isJsonObject()) {
                                        throw new RuntimeException("JsonObject was expected for property " + propertyName);
                                    }
                                    EntityImportView propertyImportView = buildFromJsonObject(propertyJsonObject.getAsJsonObject(), propertyMetaClass);
                                    if (metaProperty.getRange().getCardinality() == Range.Cardinality.MANY_TO_ONE) {
                                        viewBuilder.addManyToOneProperty(propertyName, propertyImportView);
                                    } else {
                                        viewBuilder.addOneToOneProperty(propertyName, propertyImportView);
                                    }
                                }
                            }
                        } else {
                            if (importContext.isImportPermitted(propertyName))
                                if (metaProperty.getRange().getCardinality() == Range.Cardinality.MANY_TO_ONE) {
                                    viewBuilder.addManyToOneProperty(propertyName, ReferenceImportBehaviour.ERROR_ON_MISSING);
                                } else {
                                    viewBuilder.addOneToOneProperty(propertyName, ReferenceImportBehaviour.ERROR_ON_MISSING);
                                }
                        }
                    }
                } else if (Collection.class.isAssignableFrom(propertyType)) {
                    MetaClass propertyMetaClass = metaProperty.getRange().asClass();
                    switch (metaProperty.getRange().getCardinality()) {
                        case MANY_TO_MANY: {
                            if (importContext.isImportPermitted(propertyName))
                                viewBuilder.addManyToManyProperty(propertyName, ReferenceImportBehaviour.ERROR_ON_MISSING,
                                        CollectionImportPolicy.REMOVE_ABSENT_ITEMS);
                            break;
                        }
                        case ONE_TO_MANY: {
                            if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
                                JsonElement compositionJsonArray = entry.getValue();
                                if (!compositionJsonArray.isJsonArray()) {
                                    throw new RuntimeException("JsonArray was expected for property " + propertyName);
                                }
                                EntityImportView propertyImportView = buildFromJsonArray(compositionJsonArray.getAsJsonArray(), propertyMetaClass);
                                if (importContext.isImportPermitted(propertyName))
                                    viewBuilder.addOneToManyProperty(propertyName, propertyImportView, CollectionImportPolicy.REMOVE_ABSENT_ITEMS);
                            }
                            break;
                        }
                        default:
                            // ignore other options
                            break;
                    }
                }
            }
        }

        return viewBuilder.build();
    }

    /**
     * Builds a EntityImportView that contains properties from all collection members.
     * If the first member contains the property A, and the second one contains a property B then a result view will contain
     * both properties A and B. Views for nested collections (2nd level compositions) are also merged.
     *
     * @param jsonArray a JsonArray
     * @param metaClass a metaClass of entities that are in the jsonArray
     * @return an EntityImportView
     */
    protected EntityImportView buildFromJsonArray(JsonArray jsonArray, MetaClass metaClass) {
        List<EntityImportView> viewsForArrayElements = new ArrayList<>();
        for (JsonElement arrayElement : jsonArray.getAsJsonArray()) {
            EntityImportView viewForArrayElement = buildFromJsonObject(arrayElement.getAsJsonObject(), metaClass);
            viewsForArrayElements.add(viewForArrayElement);
        }
        EntityImportView resultView = viewsForArrayElements.isEmpty() ?
                new EntityImportView(metaClass.getJavaClass()) :
                viewsForArrayElements.get(0);
        if (viewsForArrayElements.size() > 1) {
            for (int i = 1; i < viewsForArrayElements.size(); i++) {
                resultView = mergeViews(resultView, viewsForArrayElements.get(i));
            }
        }
        return resultView;
    }

    /**
     * Recursively merges two views. The result view will contain all fields that are defined either in view1 or in
     * view2.
     */
    protected EntityImportView mergeViews(@Nullable EntityImportView view1, @Nullable EntityImportView view2) {
        if (view1 == null) return view2;
        if (view2 == null) return view1;
        EntityImportViewBuilder viewBuilder = entityImportViews.builder(view1.getEntityClass());

        for (EntityImportViewProperty p1 : view1.getProperties()) {
            EntityImportViewProperty newProperty = new EntityImportViewProperty(p1.getName());
            newProperty.setReferenceImportBehaviour(p1.getReferenceImportBehaviour());
            newProperty.setCollectionImportPolicy(p1.getCollectionImportPolicy());
            EntityImportViewProperty p2 = view2.getProperty(p1.getName());
            if (p2 == null) {
                newProperty.setView(p1.getView());
            } else {
                newProperty.setView(mergeViews(p1.getView(), p2.getView()));
            }
            viewBuilder.addProperty(newProperty);
        }

        //add properties that exist in p2 but not in p1
        for (EntityImportViewProperty p2 : view2.getProperties()) {
            if (view1.getProperty(p2.getName()) == null) {
                EntityImportViewProperty newProperty = new EntityImportViewProperty(p2.getName());
                newProperty.setView(p2.getView());
                newProperty.setReferenceImportBehaviour(p2.getReferenceImportBehaviour());
                newProperty.setCollectionImportPolicy(p2.getCollectionImportPolicy());
                viewBuilder.addProperty(newProperty);
            }
        }

        return viewBuilder.build();
    }
}
