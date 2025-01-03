/*
 * Copyright 2021 Haulmont.
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

package io.jmix.rest.impl;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.rest.RestProperties;
import io.jmix.rest.exception.RestAPIException;
import io.jmix.rest.impl.config.RestJsonTransformations;
import io.jmix.rest.transform.JsonTransformationDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;


/**
 *
 */
@Component("rest_RestControllerUtils")
public class RestControllerUtils {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected RestJsonTransformations restJsonTransformations;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected FetchPlanSerialization fetchPlanSerialization;

    @Autowired
    protected RestProperties restProperties;

    /**
     * Finds metaClass by entityName. Throws a RestAPIException if metaClass not found
     */
    public MetaClass getMetaClass(String entityName) {
        MetaClass metaClass = metadata.findClass(entityName);
        if (metaClass == null) {
            throw new RestAPIException("Entity not found",
                    String.format("Entity %s not found", entityName),
                    HttpStatus.NOT_FOUND);
        }

        return metaClass;
    }

    /**
     * Returns a fetch plan by name.
     *
     * @param metaClass entity MetaClass
     * @param name fetch plan name (nullable)
     * @return a fetch plan by name, or null if name is null
     *
     * @throws RestAPIException if the fetch plan is not found
     */
    @Nullable
    public FetchPlan getFetchPlan(MetaClass metaClass, @Nullable String name) {
        if (name == null)
            return null;
        try {
            return fetchPlanRepository.getFetchPlan(metaClass, name);
        } catch (FetchPlanNotFoundException e) {
            throw new RestAPIException("Fetch plan not found",
                    String.format("Fetch plan %s for entity %s not found", name, metaClass.getName()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Returns a fetch plan by name or deserializes it from JSON.
     *
     * @param metaClass entity MetaClass
     * @param fetchPlanNameOrJson fetch plan name or JSON representation (nullable)
     * @return a fetch plan by name, or null if name is null
     *
     * @throws RestAPIException if the provided fetch plan is a name, and it's not found in repository. Also, if
     * inline fetch plans are disabled by {@link RestProperties#isInlineFetchPlanEnabled()}.
     */
    @Nullable
    public FetchPlan getFetchPlanByNameOrJson(MetaClass metaClass, @Nullable String fetchPlanNameOrJson) {
        if (fetchPlanNameOrJson == null)
            return null;
        if (isJsonObject(fetchPlanNameOrJson)) {
            if (!restProperties.isInlineFetchPlanEnabled()) {
                throw new RestAPIException("Inline fetch plans are disabled",
                        "Inline fetch plans are disabled. Use only named fetch plans.",
                        HttpStatus.BAD_REQUEST);
            }
            return fetchPlanSerialization.fromJson(fetchPlanNameOrJson);
        } else {
            return getFetchPlan(metaClass, fetchPlanNameOrJson);
        }
    }

    public String transformEntityNameIfRequired(String entityName, @Nullable String modelVersion, JsonTransformationDirection direction) {
        return Strings.isNullOrEmpty(modelVersion) ? entityName :
                restJsonTransformations.getTransformer(entityName, modelVersion, direction).getTransformedEntityName();
    }

    public String transformJsonIfRequired(String entityName, @Nullable String modelVersion, JsonTransformationDirection direction, String json) {
        return Strings.isNullOrEmpty(modelVersion) ? json :
                restJsonTransformations.getTransformer(entityName, modelVersion, direction).transformJson(json);
    }

    private boolean isJsonObject(String s) {
        String trimmed = s.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }
}

