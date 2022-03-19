/*
 * Copyright (c) 2008-2016 Haulmont.
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

package io.jmix.core.impl.serialization;

import com.google.gson.*;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.jmix.core.FetchMode.AUTO;
import static io.jmix.core.FetchPlanSerializationOption.COMPACT_FORMAT;
import static io.jmix.core.FetchPlanSerializationOption.INCLUDE_FETCH_MODE;

/**
 *
 */
@Component("core_FetchPlanSerialization")
public class FetchPlanSerializationImpl implements FetchPlanSerialization {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected CoreProperties coreProperties;

    private static final Logger log = LoggerFactory.getLogger(FetchPlanSerializationImpl.class);

    @Override
    public FetchPlan fromJson(String json) {
        return createGson().fromJson(json, FetchPlan.class);
    }

    @Override
    public String toJson(FetchPlan fetchPlan, FetchPlanSerializationOption... options) {
        return createGson(options).toJson(fetchPlan);
    }

    protected Gson createGson(FetchPlanSerializationOption... options) {
        return new GsonBuilder()
                .registerTypeHierarchyAdapter(FetchPlan.class, new FetchPlanSerializer(options))
                .registerTypeHierarchyAdapter(FetchPlan.class, new FetchPlanDeserializer())
                .create();
    }

    protected class FetchPlanSerializer implements JsonSerializer<FetchPlan> {

        protected boolean compactFormat = false;

        protected boolean includeFetchMode = false;

        protected String fetchPlanAttributeName = "fetchPlan";

        protected List<FetchPlan> processedFetchPlans = new ArrayList<>();

        public FetchPlanSerializer(FetchPlanSerializationOption[] options) {
            for (FetchPlanSerializationOption option : options) {
                if (option == COMPACT_FORMAT) {
                    compactFormat = true;
                }
                if (option == INCLUDE_FETCH_MODE) {
                    includeFetchMode = true;
                }
            }
            if (coreProperties.isLegacyFetchPlanSerializationAttributeName()) {
                fetchPlanAttributeName = "view";
            }
        }

        @Override
        public JsonElement serialize(FetchPlan src, Type typeOfSrc, JsonSerializationContext context) {
            return serializeFetchPlan(src);
        }

        protected JsonObject serializeFetchPlan(FetchPlan fetchPlan) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", fetchPlan.getName());
            MetaClass metaClass = metadata.getClass(fetchPlan.getEntityClass());
            jsonObject.addProperty("entity", metaClass.getName());
            jsonObject.add("properties", createJsonArrayOfFetchPlanProperties(fetchPlan));
            return jsonObject;
        }

        protected JsonArray createJsonArrayOfFetchPlanProperties(FetchPlan fetchPlan) {
            JsonArray propertiesArray = new JsonArray();
            for (FetchPlanProperty fetchPlanProperty : fetchPlan.getProperties()) {
                FetchPlan nestedFetchPlan = fetchPlanProperty.getFetchPlan();
                if (nestedFetchPlan == null) {
                    //add simple property as string primitive
                    propertiesArray.add(fetchPlanProperty.getName());
                } else {
                    JsonObject propertyObject = new JsonObject();
                    propertyObject.addProperty("name", fetchPlanProperty.getName());
                    String nestedFetchPlanName = nestedFetchPlan.getName();
                    if (compactFormat) {
                        if (StringUtils.isNotEmpty(nestedFetchPlanName)) {
                            FetchPlan processedFetchPlan = findProcessedFetchPlan(processedFetchPlans, nestedFetchPlan.getEntityClass(), nestedFetchPlanName);
                            if (processedFetchPlan == null) {
                                processedFetchPlans.add(nestedFetchPlan);
                                propertyObject.add(fetchPlanAttributeName, createJsonObjectForNestedFetchPlan(nestedFetchPlan));
                            } else {
                                //if we already processed this fetchPlan, just add its name as a string
                                propertyObject.addProperty(fetchPlanAttributeName, nestedFetchPlanName);
                            }
                        } else {
                            propertyObject.add(fetchPlanAttributeName, createJsonObjectForNestedFetchPlan(nestedFetchPlan));
                        }
                    } else {
                        propertyObject.add(fetchPlanAttributeName, createJsonObjectForNestedFetchPlan(nestedFetchPlan));
                    }

                    if (includeFetchMode && fetchPlanProperty.getFetchMode() != null && fetchPlanProperty.getFetchMode() != FetchMode.AUTO) {
                        propertyObject.addProperty("fetch", fetchPlanProperty.getFetchMode().name());
                    }

                    propertiesArray.add(propertyObject);
                }
            }
            return propertiesArray;
        }

        protected JsonObject createJsonObjectForNestedFetchPlan(FetchPlan nestedFetchPlan) {
            JsonObject fetchPlanObject = new JsonObject();
            String nestedFetchPlanName = nestedFetchPlan.getName();
            if (StringUtils.isNotEmpty(nestedFetchPlanName)) {
                fetchPlanObject.addProperty("name", nestedFetchPlanName);
            }
            JsonArray nestedFetchPlanProperties = createJsonArrayOfFetchPlanProperties(nestedFetchPlan);
            fetchPlanObject.add("properties", nestedFetchPlanProperties);
            return fetchPlanObject;
        }
    }


    protected class FetchPlanDeserializer implements JsonDeserializer<FetchPlan> {

        protected List<FetchPlanBuilder> fetchPlanBuilders = new ArrayList<>();

        protected String fetchPlanAttributeName = "fetchPlan";

        public FetchPlanDeserializer() {
            if (coreProperties.isLegacyFetchPlanSerializationAttributeName()) {
                fetchPlanAttributeName = "view";
            }
        }

        @Override
        public FetchPlan deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return deserializeFetchPlan(json.getAsJsonObject());
        }

        protected FetchPlan deserializeFetchPlan(JsonObject jsonObject) {
            String fetchPlanName = jsonObject.getAsJsonPrimitive("name").getAsString();
            String entityName = jsonObject.getAsJsonPrimitive("entity").getAsString();
            JsonArray properties = jsonObject.getAsJsonArray("properties");
            MetaClass metaClass = metadata.getClass(entityName);
            if (metaClass == null) {
                throw new FetchPlanSerializationException(String.format("Entity with name %s not found", entityName));
            }
            FetchPlanBuilder builder = fetchPlans.builder(metaClass.getJavaClass()).name(fetchPlanName);
            fillFetchPlanProperties(builder, properties, metaClass);
            return builder.build();
        }

        protected void fillFetchPlanProperties(FetchPlanBuilder builder, JsonArray propertiesArray, MetaClass fetchPlanMetaClass) {
            for (JsonElement propertyElement : propertiesArray) {
                //there may be a primitive or json object inside the properties array
                if (propertyElement.isJsonPrimitive()) {
                    String propertyName = propertyElement.getAsJsonPrimitive().getAsString();
                    builder.add(propertyName);
                } else {
                    JsonObject fetchPlanPropertyObj = propertyElement.getAsJsonObject();

                    FetchMode fetchMode = AUTO;
                    JsonPrimitive fetchPrimitive = fetchPlanPropertyObj.getAsJsonPrimitive("fetch");
                    if (fetchPrimitive != null) {
                        String fetch = fetchPrimitive.getAsString();
                        try {
                            fetchMode = FetchMode.valueOf(fetch);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid fetch mode {}", fetch);
                        }
                    }

                    String propertyName = fetchPlanPropertyObj.getAsJsonPrimitive("name").getAsString();
                    JsonElement nestedPlanElement = fetchPlanPropertyObj.get(fetchPlanAttributeName);
                    if (nestedPlanElement == null) {
                        builder.add(propertyName, b -> {
                        }, fetchMode);
                    } else {
                        MetaProperty metaProperty = fetchPlanMetaClass.getProperty(propertyName);
                        if (metaProperty == null) {
                            log.warn("Cannot deserialize fetchPlan property. Property {} of entity {} doesn't exist",
                                    propertyName, fetchPlanMetaClass.getName());
                            continue;
                        }

                        MetaClass nestedFetchPlanMetaClass = metaProperty.getRange().asClass();
                        Class<?> nestedPlanEntityClass = nestedFetchPlanMetaClass.getJavaClass();

                        if (nestedPlanElement.isJsonObject()) {
                            builder.add(propertyName, nestedBuilder -> {
                                JsonObject nestedFetchPlanObject = nestedPlanElement.getAsJsonObject();
                                JsonPrimitive fetchPlanNamePrimitive = nestedFetchPlanObject.getAsJsonPrimitive("name");
                                if (fetchPlanNamePrimitive != null) {
                                    nestedBuilder.name(fetchPlanNamePrimitive.getAsString());
                                    fetchPlanBuilders.add(nestedBuilder);
                                }
                                JsonArray nestedProperties = nestedFetchPlanObject.getAsJsonArray("properties");
                                fillFetchPlanProperties(nestedBuilder, nestedProperties, nestedFetchPlanMetaClass);
                            }, fetchMode);

                        } else if (nestedPlanElement.isJsonPrimitive()) {
                            //if fetchPlan was serialized with the FetchPlanSerializationOption.COMPACT_FORMAT
                            String nestedPlanName = nestedPlanElement.getAsString();
                            FetchPlanBuilder loadedBuilder = findFetchPlanBuilder(fetchPlanBuilders, nestedPlanEntityClass, nestedPlanName);
                            if (loadedBuilder != null) {
                                builder.add(propertyName, loadedBuilder, fetchMode);
                            } else {
                                throw new FetchPlanSerializationException(String.format("FetchPlan %s was not defined in the JSON", nestedPlanName));
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    protected FetchPlanBuilder findFetchPlanBuilder(Collection<FetchPlanBuilder> loadedBuilders, Class<?> aClass, String fetchPlanName) {
        for (FetchPlanBuilder builder : loadedBuilders) {
            if (aClass.equals(builder.getEntityClass()) && fetchPlanName.equals(builder.getName())) {
                return builder;
            }
        }
        return null;
    }

    @Nullable
    protected FetchPlan findProcessedFetchPlan(Collection<FetchPlan> processedFetchPlans, Class<?> aClass, String fetchPlanName) {
        for (FetchPlan fetchPlan : processedFetchPlans) {
            if (aClass.equals(fetchPlan.getEntityClass()) && fetchPlanName.equals(fetchPlan.getName())) {
                return fetchPlan;
            }
        }
        return null;
    }
}
