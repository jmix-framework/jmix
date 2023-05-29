/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.component.genericfilter;

import com.google.common.base.Strings;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.accesscontext.FlowuiEntityAttributeContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Internal
@Component("flowui_FilterMetadataTools")
public class FilterMetadataTools {

    protected final MetadataTools metadataTools;
    protected final Metadata metadata;
    protected final FlowuiComponentProperties componentProperties;
    protected final AccessManager accessManager;

    public FilterMetadataTools(MetadataTools metadataTools,
                               FlowuiComponentProperties uiComponentProperties,
                               AccessManager accessManager,
                               Metadata metadata) {
        this.metadataTools = metadataTools;
        this.componentProperties = uiComponentProperties;
        this.accessManager = accessManager;
        this.metadata = metadata;
    }

    public List<MetaPropertyPath> getPropertyPaths(MetaClass filterMetaClass,
                                                   String query,
                                                   @Nullable Predicate<MetaPropertyPath> propertiesFilterPredicate) {
        return getPropertyPaths(filterMetaClass, query, filterMetaClass, 0, "",
                propertiesFilterPredicate);
    }

    protected List<MetaPropertyPath> getPropertyPaths(MetaClass filterMetaClass,
                                                      String query,
                                                      MetaClass currentMetaClass,
                                                      int currentDepth,
                                                      String currentPropertyPath,
                                                      @Nullable Predicate<MetaPropertyPath> propertiesFilterPredicate) {
        List<MetaProperty> properties = new ArrayList<>(currentMetaClass.getProperties());
        properties.addAll(metadataTools.getAdditionalProperties(currentMetaClass));

        List<MetaPropertyPath> paths = new ArrayList<>();
        if (currentDepth < componentProperties.getFilterPropertiesHierarchyDepth()) {
            for (MetaProperty property : properties) {
                String propertyPath = Strings.isNullOrEmpty(currentPropertyPath)
                        ? property.getName()
                        : currentPropertyPath + "." + property.getName();
                MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(filterMetaClass, propertyPath);

                if (metaPropertyPath == null || !isMetaPropertyPathAllowed(metaPropertyPath, query)
                        || (propertiesFilterPredicate != null && !propertiesFilterPredicate.test(metaPropertyPath))) {
                    continue;
                }

                paths.add(metaPropertyPath);

                if (property.getRange().isClass() && !metadataTools.isAdditionalProperty(currentMetaClass, property.getName())) {
                    MetaClass childMetaClass = property.getRange().asClass();
                    List<MetaPropertyPath> childPaths = getPropertyPaths(filterMetaClass, query, childMetaClass,
                            currentDepth + 1, propertyPath, propertiesFilterPredicate);
                    paths.addAll(childPaths);
                }
            }
        }

        return paths;
    }

    protected boolean isMetaPropertyPathAllowed(MetaPropertyPath propertyPath, String query) {
        FlowuiEntityAttributeContext context = new FlowuiEntityAttributeContext(propertyPath);
        accessManager.applyRegisteredConstraints(context);

        return context.canView()
                && !metadataTools.isSystemLevel(propertyPath.getMetaProperty())
                && ((metadataTools.isJpa(propertyPath)
                || (propertyPath.getMetaClass() instanceof KeyValueMetaClass
                && !isAggregateFunction(propertyPath, query)
                && isKeyValueCrossDataStoreReferenceAllowed(propertyPath, query)))
                || (isCrossDataStoreReference(propertyPath.getMetaProperty())
                && !(propertyPath.getMetaClass() instanceof KeyValueMetaClass)))
                && !propertyPath.getMetaProperty().getRange().getCardinality().isMany()
                && !(byte[].class.equals(propertyPath.getMetaProperty().getJavaType()));
    }

    @SuppressWarnings("unused")
    protected boolean isAggregateFunction(MetaPropertyPath propertyPath, String query) {
        return false;
    }

    protected boolean isCrossDataStoreReference(MetaProperty metaProperty) {
        return metadataTools.getCrossDataStoreReferenceIdProperty(
                metaProperty.getDomain().getStore().getName(),
                metaProperty) != null;
    }

    protected boolean isKeyValueCrossDataStoreReferenceAllowed(MetaPropertyPath propertyPath, String query) {
        MetaClass filterMetaClass = propertyPath.getMetaClass();
        if (!(filterMetaClass instanceof KeyValueMetaClass) || Strings.isNullOrEmpty(query)) {
            return true;
        }

        MetaClass domainMetaClass = propertyPath.getMetaProperty().getDomain();
        MetaClass propertyMetaClass = propertyPath.getMetaProperty().getRange().isClass()
                ? propertyPath.getMetaProperty().getRange().asClass()
                : null;

        if (!domainMetaClass.equals(filterMetaClass)) {
            return propertyMetaClass == null
                    || domainMetaClass.getStore().getName().equals(propertyMetaClass.getStore().getName());
        } else if (propertyMetaClass != null) {
            String entityName = query.substring(query.indexOf("from") + 4)
                    .trim()
                    .split(" ")[0];

            MetaClass mainFromMetaClass = metadata.getClass(entityName);
            return mainFromMetaClass.getStore().getName().equals(propertyMetaClass.getStore().getName())
                    || propertyMetaClass instanceof KeyValueMetaClass;
        } else {
            return true;
        }
    }
}
