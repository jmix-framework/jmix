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
import io.jmix.core.accesscontext.ExportImportEntityContext;
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
@Component("core_EntityImportPlanJsonBuilder")
public class EntityImportPlanJsonBuilderImpl implements EntityImportPlanJsonBuilder {

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected EntityImportPlans entityImportPlans;

    @Override
    public EntityImportPlan buildFromJson(String json, MetaClass metaClass) {
        JsonElement rootElement = JsonParser.parseString(json);
        if (!rootElement.isJsonObject()) {
            throw new RuntimeException("Passed json is not a JSON object");
        }
        return buildFromJsonObject(rootElement.getAsJsonObject(), metaClass);
    }

    protected EntityImportPlan buildFromJsonObject(JsonObject jsonObject, MetaClass metaClass) {
        EntityImportPlanBuilder importPlanBuilder = entityImportPlans.builder(metaClass.getJavaClass());

        ExportImportEntityContext importContext = new ExportImportEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(importContext);

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String propertyName = entry.getKey();
            MetaProperty metaProperty = metaClass.findProperty(propertyName);
            if (metaProperty == null) {
                continue;
            }
            Range propertyRange = metaProperty.getRange();
            Class<?> propertyType = metaProperty.getJavaType();
            if (propertyRange.isDatatype() || propertyRange.isEnum()) {
                if (importContext.canImported(propertyName)) {
                    importPlanBuilder.addLocalProperty(propertyName);
                }
            } else if (propertyRange.isClass()) {
                if (Entity.class.isAssignableFrom(propertyType)) {
                    if (metaProperty.getType() == MetaProperty.Type.EMBEDDED) {
                        MetaClass propertyMetaClass = metadata.getClass(propertyType);
                        JsonElement propertyJsonObject = entry.getValue();
                        if (!propertyJsonObject.isJsonObject()) {
                            throw new RuntimeException("JsonObject was expected for property " + propertyName);
                        }
                        if (importContext.canImported(propertyName)) {
                            EntityImportPlan propertyImportPlan = buildFromJsonObject(propertyJsonObject.getAsJsonObject(), propertyMetaClass);
                            importPlanBuilder.addEmbeddedProperty(propertyName, propertyImportPlan);
                        }
                    } else {
                        MetaClass propertyMetaClass = metadata.getClass(propertyType);
                        if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
                            JsonElement propertyJsonObject = entry.getValue();
                            if (importContext.canImported(propertyName)) {
                                if (propertyJsonObject.isJsonNull()) {
                                    //in case of null we must add such import behavior to update the reference with null value later
                                    if (metaProperty.getRange().getCardinality() == Range.Cardinality.MANY_TO_ONE) {
                                        importPlanBuilder.addManyToOneProperty(propertyName, ReferenceImportBehaviour.IGNORE_MISSING);
                                    } else {
                                        importPlanBuilder.addOneToOneProperty(propertyName, ReferenceImportBehaviour.IGNORE_MISSING);
                                    }
                                } else {
                                    if (!propertyJsonObject.isJsonObject()) {
                                        throw new RuntimeException("JsonObject was expected for property " + propertyName);
                                    }
                                    EntityImportPlan propertyImportPlan = buildFromJsonObject(propertyJsonObject.getAsJsonObject(), propertyMetaClass);
                                    if (metaProperty.getRange().getCardinality() == Range.Cardinality.MANY_TO_ONE) {
                                        importPlanBuilder.addManyToOneProperty(propertyName, propertyImportPlan);
                                    } else {
                                        importPlanBuilder.addOneToOneProperty(propertyName, propertyImportPlan);
                                    }
                                }
                            }
                        } else {
                            if (importContext.canImported(propertyName))
                                if (metaProperty.getRange().getCardinality() == Range.Cardinality.MANY_TO_ONE) {
                                    importPlanBuilder.addManyToOneProperty(propertyName, ReferenceImportBehaviour.ERROR_ON_MISSING);
                                } else {
                                    importPlanBuilder.addOneToOneProperty(propertyName, ReferenceImportBehaviour.ERROR_ON_MISSING);
                                }
                        }
                    }
                } else if (Collection.class.isAssignableFrom(propertyType)) {
                    MetaClass propertyMetaClass = metaProperty.getRange().asClass();
                    switch (metaProperty.getRange().getCardinality()) {
                        case MANY_TO_MANY: {
                            if (importContext.canImported(propertyName))
                                importPlanBuilder.addManyToManyProperty(propertyName, ReferenceImportBehaviour.ERROR_ON_MISSING,
                                        CollectionImportPolicy.REMOVE_ABSENT_ITEMS);
                            break;
                        }
                        case ONE_TO_MANY: {
                            if (metaProperty.getType() == MetaProperty.Type.COMPOSITION) {
                                JsonElement compositionJsonArray = entry.getValue();
                                if (!compositionJsonArray.isJsonArray()) {
                                    throw new RuntimeException("JsonArray was expected for property " + propertyName);
                                }
                                EntityImportPlan propertyImportPlan = buildFromJsonArray(compositionJsonArray.getAsJsonArray(), propertyMetaClass);
                                if (importContext.canImported(propertyName))
                                    importPlanBuilder.addOneToManyProperty(propertyName, propertyImportPlan, CollectionImportPolicy.REMOVE_ABSENT_ITEMS);
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

        return importPlanBuilder.build();
    }

    @Override
    public EntityImportPlan buildFromJsonArray(JsonArray jsonArray, MetaClass metaClass) {
        List<EntityImportPlan> plansArrayElements = new ArrayList<>();
        for (JsonElement arrayElement : jsonArray.getAsJsonArray()) {
            EntityImportPlan planForArrayElement = buildFromJsonObject(arrayElement.getAsJsonObject(), metaClass);
            plansArrayElements.add(planForArrayElement);
        }
        EntityImportPlan resultPlan = plansArrayElements.isEmpty() ?
                new EntityImportPlan(metaClass.getJavaClass()) :
                plansArrayElements.get(0);
        if (plansArrayElements.size() > 1) {
            for (int i = 1; i < plansArrayElements.size(); i++) {
                resultPlan = mergeImportPlans(resultPlan, plansArrayElements.get(i));
            }
        }
        return resultPlan;
    }

    /**
     * Recursively merges two import plans. The result import plan will contain all fields that are defined either in
     * plan1 or in plan2.
     */
    protected EntityImportPlan mergeImportPlans(@Nullable EntityImportPlan plan1, @Nullable EntityImportPlan plan2) {
        if (plan1 == null) return plan2;
        if (plan2 == null) return plan1;
        EntityImportPlanBuilder importPlanBuilder = entityImportPlans.builder(plan1.getEntityClass());

        for (EntityImportPlanProperty p1 : plan1.getProperties()) {
            EntityImportPlanProperty newProperty = new EntityImportPlanProperty(p1.getName());
            newProperty.setReferenceImportBehaviour(p1.getReferenceImportBehaviour());
            newProperty.setCollectionImportPolicy(p1.getCollectionImportPolicy());
            EntityImportPlanProperty p2 = plan2.getProperty(p1.getName());
            if (p2 == null) {
                newProperty.setPlan(p1.getPlan());
            } else {
                newProperty.setPlan(mergeImportPlans(p1.getPlan(), p2.getPlan()));
            }
            importPlanBuilder.addProperty(newProperty);
        }

        //add properties that exist in p2 but not in p1
        for (EntityImportPlanProperty p2 : plan2.getProperties()) {
            if (plan1.getProperty(p2.getName()) == null) {
                EntityImportPlanProperty newProperty = new EntityImportPlanProperty(p2.getName());
                newProperty.setPlan(p2.getPlan());
                newProperty.setReferenceImportBehaviour(p2.getReferenceImportBehaviour());
                newProperty.setCollectionImportPolicy(p2.getCollectionImportPolicy());
                importPlanBuilder.addProperty(newProperty);
            }
        }

        return importPlanBuilder.build();
    }
}
