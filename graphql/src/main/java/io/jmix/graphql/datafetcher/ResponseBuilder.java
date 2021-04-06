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

package io.jmix.graphql.datafetcher;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.graphql.schema.NamingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

;

/**
 * Converts entities to Map&lt;String, Object&gt; response format.
 */
@Component
public class ResponseBuilder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    MetadataTools metadataTools;
    @Autowired
    Metadata metadata;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected EnvironmentUtils environmentUtils;


    /**
     * Convert loaded entity to data fetcher return format (Map&lt;String, Object&gt;)
     *
     * @param entity loaded entity
     * @param fetchPlan loaded entity properties
     * @param metaClass entity meta class
     * @param props we need pass full set of properties to have information about system props such '_instanceName'
     * @return entity converted to response as Map&lt;String, Object&gt;
     */
    public Map<String, Object> buildResponse(Entity entity, FetchPlan fetchPlan, MetaClass metaClass, Set<String> props) {
        Map<String, Object> entityAsMap = new HashMap<>();

        // check and evaluate _instanceName, if required
        if (environmentUtils.hasInstanceNameProperty(props)) {
            entityAsMap.put(NamingUtils.SYS_ATTR_INSTANCE_NAME, metadataTools.getInstanceName(entity));
        }

        // compose result object by iterating over fetch plan props
        fetchPlan.getProperties().forEach(prop -> {

            String propName = prop.getName();
            MetaProperty metaProperty = metaClass.getProperty(propName);
            Object fieldValue = EntityValues.getValue(entity, propName);
            Range propertyRange = metaProperty.getRange();

            if (fieldValue == null) {
                entityAsMap.put(propName, null);
                return;
            }

            if (propertyRange.isDatatype() || propertyRange.isEnum()) {
                entityAsMap.put(propName, fieldValue);
                return;
            }

            if (propertyRange.isClass()) {
                Set<String> nestedProps = environmentUtils.getNestedProps(props, propName);

                if (fieldValue instanceof Entity) {
                    entityAsMap.put(propName, buildResponse((Entity) fieldValue, prop.getFetchPlan(), propertyRange.asClass(), nestedProps));
                    return;
                }

                if (fieldValue instanceof Collection) {
                    Collection<Object> values = ((Collection<Entity>)fieldValue).stream()
                            .map(e -> buildResponse(e, prop.getFetchPlan(), propertyRange.asClass(), nestedProps))
                            .collect(Collectors.toList());
                    entityAsMap.put(propName, values);
                    return;
                }
            }

            log.warn("buildResponse: failed for {}.{} unsupported range type ", metaClass.getName(), prop.getName());
            throw new IllegalStateException("Unsupported range type " + propertyRange);
        });
        return entityAsMap;
    }

    /**
     * Method constructs {@link FetchPlanBuilder} for a regular {@link FetchPlan} from the {@link EntityImportPlan}. The
     * regular fetchPlan will include all properties defined in the import plan.
     *
     * @param importPlan regular import plan
     * @return builder for constructed fetch plan
     */
    protected FetchPlanBuilder constructFetchPlanFromImportPlan(EntityImportPlan importPlan) {
        FetchPlanBuilder fetchPlanBuilder = fetchPlans.builder(importPlan.getEntityClass());
        MetaClass metaClass = metadata.getClass(importPlan.getEntityClass());
        for (EntityImportPlanProperty importPlanProperty : importPlan.getProperties()) {
            EntityImportPlan importPlanPropertyPlan = importPlanProperty.getPlan();
            if (importPlanPropertyPlan == null) {
                MetaProperty metaProperty = metaClass.getProperty(importPlanProperty.getName());
                if (metaProperty.isReadOnly()) continue;
                fetchPlanBuilder.add(importPlanProperty.getName());
            } else {
                fetchPlanBuilder.add(importPlanProperty.getName(), constructFetchPlanFromImportPlan(importPlanPropertyPlan));
            }
        }
        return fetchPlanBuilder;
    }

}
