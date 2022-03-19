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

package io.jmix.rest.impl.service;

import com.google.common.base.Joiner;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.rest.impl.RestControllerUtils;
import io.jmix.rest.exception.RestAPIException;
import io.jmix.rest.impl.service.filter.data.MetaClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class is used by the {@link io.jmix.rest.impl.controller.EntitiesMetadataController}. Class is sed for getting
 * entities metadata. User permissions for entities access aren't taken into account at the moment.
 */
@Component("rest_EntitiesMetadataControllerManager")
public class EntitiesMetadataControllerManager {

    private static final Logger log = LoggerFactory.getLogger(EntitiesMetadataControllerManager.class);

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected RestControllerUtils restControllersUtils;

    @Autowired
    protected FetchPlanSerialization viewSerializationAPI;

    @Autowired
    protected FetchPlanRepository viewRepository;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected ExtendedEntities extendedEntities;

    public MetaClassInfo getMetaClassInfo(String entityName) {
        MetaClass metaClass = restControllersUtils.getMetaClass(entityName);
        return new MetaClassInfo(metaClass, messageTools, datatypeRegistry, metadataTools);
    }

    public Collection<MetaClassInfo> getAllMetaClassesInfo() {
        Set<MetaClass> metaClasses = new HashSet<>(metadataTools.getAllJpaEntityMetaClasses());
        metaClasses.addAll(metadataTools.getAllJpaEmbeddableMetaClasses());

        return metaClasses.stream()
                .filter(metaClass -> extendedEntities.getExtendedClass(metaClass) == null)
                .map(metaClass -> new MetaClassInfo(metaClass, messageTools, datatypeRegistry, metadataTools))
                .collect(Collectors.toList());
    }

    public String getFetchPlan(String entityName, String viewName) {
        MetaClass metaClass = restControllersUtils.getMetaClass(entityName);
        FetchPlan view = viewRepository.findFetchPlan(metaClass, viewName);
        if (view == null) {
            throw new RestAPIException("Fetch plan not found",
                    String.format("View %s for metaClass %s not found", viewName, entityName),
                    HttpStatus.NOT_FOUND);
        }
        return viewSerializationAPI.toJson(view);
    }

    public String getAllFetchPlansForMetaClass(String entityName) {
        MetaClass metaClass = restControllersUtils.getMetaClass(entityName);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        List<String> jsonViews = new ArrayList<>();
        for (String viewName : viewRepository.getFetchPlanNames(metaClass)) {
            FetchPlan view = viewRepository.getFetchPlan(metaClass, viewName);
            jsonViews.add(viewSerializationAPI.toJson(view));
        }
        sb.append(Joiner.on(",").join(jsonViews));
        sb.append("]");
        return sb.toString();
    }
}
