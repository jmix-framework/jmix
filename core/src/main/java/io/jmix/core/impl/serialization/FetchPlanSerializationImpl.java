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
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
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
@Component(FetchPlanSerialization.NAME)
public class FetchPlanSerializationImpl implements FetchPlanSerialization {

    @Inject
    protected Metadata metadata;

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
                .registerTypeHierarchyAdapter(FetchPlan.class, new ViewDeserializer())
                .create();
    }

    protected class FetchPlanSerializer implements JsonSerializer<FetchPlan> {

        protected boolean compactFormat = false;

        protected boolean includeFetchMode = false;

        protected List<FetchPlan> processedViews = new ArrayList<>();

        public FetchPlanSerializer(FetchPlanSerializationOption[] options) {
            for (FetchPlanSerializationOption option : options) {
                if (option == COMPACT_FORMAT) {
                    compactFormat = true;
                }
                if (option == INCLUDE_FETCH_MODE) {
                    includeFetchMode = true;
                }
            }
        }

        @Override
        public JsonElement serialize(FetchPlan src, Type typeOfSrc, JsonSerializationContext context) {
            return serializeView(src);
        }

        protected JsonObject serializeView(FetchPlan view) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", view.getName());
            MetaClass metaClass = metadata.getClass(view.getEntityClass());
            jsonObject.addProperty("entity", metaClass.getName());
            jsonObject.add("properties", createJsonArrayOfViewProperties(view));
            return jsonObject;
        }

        protected JsonArray createJsonArrayOfViewProperties(FetchPlan view) {
            JsonArray propertiesArray = new JsonArray();
            for (FetchPlanProperty viewProperty : view.getProperties()) {
                FetchPlan nestedView = viewProperty.getFetchPlan();
                if (nestedView == null) {
                    //add simple property as string primitive
                    propertiesArray.add(viewProperty.getName());
                } else {
                    JsonObject propertyObject = new JsonObject();
                    propertyObject.addProperty("name", viewProperty.getName());
                    String nestedViewName = nestedView.getName();
                    if (compactFormat) {
                        if (StringUtils.isNotEmpty(nestedViewName)) {
                            FetchPlan processedView = findProcessedView(processedViews, nestedView.getEntityClass(), nestedViewName);
                            if (processedView == null) {
                                processedViews.add(nestedView);
                                propertyObject.add("view", createJsonObjectForNestedView(nestedView));
                            } else {
                                //if we already processed this view, just add its name as a string
                                propertyObject.addProperty("view", nestedViewName);
                            }
                        } else {
                            propertyObject.add("view", createJsonObjectForNestedView(nestedView));
                        }
                    } else {
                        propertyObject.add("view", createJsonObjectForNestedView(nestedView));
                    }

                    if (includeFetchMode && viewProperty.getFetchMode() != null && viewProperty.getFetchMode() != FetchMode.AUTO) {
                        propertyObject.addProperty("fetch", viewProperty.getFetchMode().name());
                    }

                    propertiesArray.add(propertyObject);
                }
            }
            return propertiesArray;
        }

        protected JsonObject createJsonObjectForNestedView(FetchPlan nestedView) {
            JsonObject viewObject = new JsonObject();
            String nestedViewName = nestedView.getName();
            if (StringUtils.isNotEmpty(nestedViewName)) {
                viewObject.addProperty("name", nestedViewName);
            }
            JsonArray nestedViewProperties = createJsonArrayOfViewProperties(nestedView);
            viewObject.add("properties", nestedViewProperties);
            return viewObject;
        }
    }


    protected class ViewDeserializer implements JsonDeserializer<FetchPlan> {

        protected List<FetchPlan> processedViews = new ArrayList<>();

        @Override
        public FetchPlan deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return deserializeView(json.getAsJsonObject());
        }

        protected FetchPlan deserializeView(JsonObject jsonObject) {
            String viewName = jsonObject.getAsJsonPrimitive("name").getAsString();
            String entityName = jsonObject.getAsJsonPrimitive("entity").getAsString();
            JsonArray properties = jsonObject.getAsJsonArray("properties");
            MetaClass metaClass = metadata.getClass(entityName);
            if (metaClass == null) {
                throw new FetchPlanSerializationException(String.format("Entity with name %s not found", entityName));
            }
            FetchPlan view = new FetchPlan(metaClass.getJavaClass(), viewName, false);
            fillViewProperties(view, properties, metaClass);
            return view;
        }

        protected void fillViewProperties(FetchPlan view, JsonArray propertiesArray, MetaClass viewMetaClass) {
            for (JsonElement propertyElement : propertiesArray) {
                //there may be a primitive or json object inside the properties array
                if (propertyElement.isJsonPrimitive()) {
                    String propertyName = propertyElement.getAsJsonPrimitive().getAsString();
                    view.addProperty(propertyName);
                } else {
                    JsonObject viewPropertyObj = propertyElement.getAsJsonObject();

                    FetchMode fetchMode = AUTO;
                    JsonPrimitive fetchPrimitive = viewPropertyObj.getAsJsonPrimitive("fetch");
                    if (fetchPrimitive != null) {
                        String fetch = fetchPrimitive.getAsString();
                        try {
                            fetchMode = FetchMode.valueOf(fetch);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid fetch mode {}", fetch);
                        }
                    }

                    String propertyName = viewPropertyObj.getAsJsonPrimitive("name").getAsString();
                    JsonElement nestedViewElement = viewPropertyObj.get("view");
                    if (nestedViewElement == null) {
                        view.addProperty(propertyName, null, fetchMode);
                    } else {
                        MetaProperty metaProperty = viewMetaClass.getProperty(propertyName);
                        if (metaProperty == null) {
                            log.warn("Cannot deserialize view property. Property {} of entity {} doesn't exist",
                                    propertyName, viewMetaClass.getName());
                            continue;
                        }
                        MetaClass nestedViewMetaClass = metaProperty.getRange().asClass();
                        Class<? extends Entity> nestedViewEntityClass = nestedViewMetaClass.getJavaClass();
                        if (nestedViewElement.isJsonObject()) {
                            JsonObject nestedViewObject = nestedViewElement.getAsJsonObject();
                            FetchPlan nestedView;
                            JsonPrimitive viewNamePrimitive = nestedViewObject.getAsJsonPrimitive("name");
                            if (viewNamePrimitive != null) {
                                nestedView = new FetchPlan(nestedViewEntityClass, viewNamePrimitive.getAsString(), false);
                                processedViews.add(nestedView);
                            } else {
                                nestedView = new FetchPlan(nestedViewEntityClass, false);
                            }
                            JsonArray nestedProperties = nestedViewObject.getAsJsonArray("properties");
                            fillViewProperties(nestedView, nestedProperties, nestedViewMetaClass);
                            view.addProperty(propertyName, nestedView, fetchMode);
                        } else if (nestedViewElement.isJsonPrimitive()) {
                            //if view was serialized with the ViewSerializationOption.COMPACT_FORMAT
                            String nestedViewName = nestedViewElement.getAsString();
                            FetchPlan processedView = findProcessedView(processedViews, nestedViewEntityClass, nestedViewName);
                            if (processedView != null) {
                                view.addProperty(propertyName, processedView, fetchMode);
                            } else {
                                throw new FetchPlanSerializationException(String.format("View %s was not defined in the JSON", nestedViewName));
                            }
                        }
                    }
                }
            }
        }
    }

    @Nullable
    protected FetchPlan findProcessedView(Collection<FetchPlan> processedViews, Class<? extends Entity> aClass, String viewName) {
        for (FetchPlan view : processedViews) {
            if (aClass.equals(view.getEntityClass()) && viewName.equals(view.getName())) {
                return view;
            }
        }
        return null;
    }
}
