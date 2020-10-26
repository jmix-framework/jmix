/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.api.common;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.rest.api.config.RestJsonTransformations;
import io.jmix.rest.api.exception.RestAPIException;
import io.jmix.rest.api.transform.JsonTransformationDirection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component("rest_RestControllerUtils")
public class RestControllerUtils {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected AccessManager accessManager;

    @Autowired
    protected MetadataTools metadataTools;

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
     * Finds a view for a given metaClass. Throws a RestAPIException if view not found
     */
    public FetchPlan getView(MetaClass metaClass, String viewName) {
        try {
            return viewRepository.getFetchPlan(metaClass, viewName);
        } catch (FetchPlanNotFoundException e) {
            throw new RestAPIException("View not found",
                    String.format("View %s for entity %s not found", viewName, metaClass.getName()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * By default entity is loaded from the middleware with the attributes that are not allowed for reading according
     * to roles permissions. This methods removes attributes not allowed for the user.
     *
     * @param entity the entity. After the method is executed forbidden attributes will be cleaned.
     */
    public void applyAttributesSecurity(Object entity) {
        metadataTools.traverseAttributes(entity, new FillingInaccessibleAttributesVisitor());
    }

    public String transformEntityNameIfRequired(String entityName, String modelVersion, JsonTransformationDirection direction) {
        return Strings.isNullOrEmpty(modelVersion) ? entityName :
                restJsonTransformations.getTransformer(entityName, modelVersion, direction).getTransformedEntityName();
    }

    public String transformJsonIfRequired(String entityName, String modelVersion, JsonTransformationDirection direction, String json) {
        return Strings.isNullOrEmpty(modelVersion) ? json :
                restJsonTransformations.getTransformer(entityName, modelVersion, direction).transformJson(json);
    }

    private class FillingInaccessibleAttributesVisitor implements EntityAttributeVisitor {

        @Override
        public boolean skip(MetaProperty property) {
            return !metadataTools.isPersistent(property);
        }

        @Override
        public void visit(Object entity, MetaProperty property) {
            //todo:rest
//            MetaClass metaClass = metadata.getClass(entity.getClass());
//            ReadEntityQueryContext = accessManager.applyRegisteredConstraints(new CRUDEntityContext());
//            if (!security.isEntityAttrReadPermitted(metaClass, property.getName())) {
//                if (!metadataTools.isSystem(property) && !property.isReadOnly()) {
//                    // Using reflective access to field because the attribute can be unfetched if loading not partial entities,
//                    // which is the case when in-memory constraints exist
//                    EntityValues.setValue(entity, property.getName(), null);
//                }
//            }
        }
    }
}
