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
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanNotFoundException;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.rest.impl.config.RestJsonTransformations;
import io.jmix.rest.exception.RestAPIException;
import io.jmix.rest.transform.JsonTransformationDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;


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
    protected FetchPlanRepository viewRepository;

    /**
     * Finds metaClass by entityName. Throws a RestAPIException if metaClass not found
     */
    public MetaClass getMetaClass(String entityName) {
        MetaClass metaClass = metadata.findClass(entityName);
        if (metaClass == null) {
            throw new RestAPIException("MetaClass not found",
                    String.format("MetaClass %s not found", entityName),
                    HttpStatus.NOT_FOUND);
        }

        return metaClass;
    }

    /**
     * Finds a view for a given metaClass. Throws a RestAPIException if Fetch plan not found
     */
    public FetchPlan getView(MetaClass metaClass, String viewName) {
        try {
            return viewRepository.getFetchPlan(metaClass, viewName);
        } catch (FetchPlanNotFoundException e) {
            throw new RestAPIException("Fetch plan not found",
                    String.format("Fetch plan %s for entity %s not found", viewName, metaClass.getName()),
                    HttpStatus.BAD_REQUEST);
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
}

