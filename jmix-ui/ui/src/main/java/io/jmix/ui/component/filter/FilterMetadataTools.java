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

package io.jmix.ui.component.filter;

import com.google.common.base.Strings;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.datastruct.Node;
import io.jmix.core.impl.keyvalue.KeyValueMetaClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.UiComponentProperties;
import io.jmix.ui.accesscontext.UiEntityAttributeContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

@Internal
@Component("ui_FilterMetadataTools")
public class FilterMetadataTools {

    protected MetadataTools metadataTools;
    protected Metadata metadata;
    protected UiComponentProperties uiComponentProperties;
    protected AccessManager accessManager;

    @Autowired
    public FilterMetadataTools(MetadataTools metadataTools,
                               UiComponentProperties uiComponentProperties,
                               AccessManager accessManager,
                               Metadata metadata) {
        this.metadataTools = metadataTools;
        this.uiComponentProperties = uiComponentProperties;
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
        if (currentDepth < uiComponentProperties.getFilterPropertiesHierarchyDepth()) {
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

    public List<MetaPropertyPath> getPropertyPathsFromIncludedProperties(MetaClass filterMetaClass,
                                                                         String query,
                                                                         @Nullable Predicate<MetaPropertyPath> propertiesFilterPredicate,
                                                                         List<String> includedProperties) {
        Node<String> includedPropertiesTree = new Node<>("");

        List<MetaPropertyPath> paths = new ArrayList<>();

        for (String property : includedProperties) {
            List<String> pathStrings = Arrays.asList(property.split("\\."));
            recursivelyAddPropertyToIncludedPropertiesTree(filterMetaClass, includedPropertiesTree, pathStrings, filterMetaClass, query);
        }

        List<String> propertyPaths = getAllPathsFromTree(includedPropertiesTree);
        for (String propertyPath : new ArrayList<>(new HashSet<>(propertyPaths))) {
            if (propertyPath.isBlank()) {
                continue;
            }
            MetaPropertyPath metaPropertyPath = metadataTools.resolveMetaPropertyPathOrNull(filterMetaClass, propertyPath);
            if (metaPropertyPath == null || !isMetaPropertyPathAllowed(metaPropertyPath, query)
                    || (propertiesFilterPredicate != null && !propertiesFilterPredicate.test(metaPropertyPath))) {
                continue;
            }
            paths.add(metaPropertyPath);
        }
        return paths;
    }

    private List<String> getAllPathsFromTree(Node<String> node) {
        return getAllPathsFromTreeRecur(node);
    }

    private List<String> getAllPathsFromTreeRecur(Node<String> node) {
        if (node.children == null || node.children.size() == 0) {
            return Collections.singletonList(node.data);
        }

        List<String> output = new ArrayList<>();

        for (Node<String> child : node.children) {
            for (String path : getAllPathsFromTreeRecur(child)) {
                String newPath = path;
                if (node.data != null && !node.data.isBlank()) {
                    newPath = node.data + "." + newPath;
                }
                output.add(node.data);
                output.add(newPath);
            }
        }

        return output;
    }

    /**
     * @param parentNode current node in the traversal
     * @param nodes      list of remaining nosed to search for
     * @param metaClass  the metaClass of the parentNode
     */
    private void recursivelyAddPropertyToIncludedPropertiesTree(MetaClass filterMetaClass, Node<String> parentNode, List<String> nodes, MetaClass metaClass, String query) {
        if (nodes.size() == 0) {
            return;
        }

        // get the first path from the list
        String first = nodes.get(0);
        List<String> rest = nodes.subList(1, nodes.size());

        // parse the first string to determine if child attributes should be included.
        boolean addNonSystemChildren = false;
        boolean addSystemChildren = false;
        boolean isClass = false;
        if (first.equals("*")) {
            addNonSystemChildren = true;
            addSystemChildren = true;
        } else if (first.equals("+")) {
            addNonSystemChildren = true;
        }

        // If we are adding we are adding children, we need to replace the + or * with each metaproperty
        if(addSystemChildren||addNonSystemChildren){
            for (MetaProperty e : metaClass.getProperties()) {
                boolean shouldAdd = !uiComponentProperties.getFilterIncludedSystemProperties().contains(e.getName()) || addSystemChildren;

                if(shouldAdd){
                    List<String> nodesCopy = new ArrayList<>(nodes);
                    nodesCopy.set(0, e.getName());
                    recursivelyAddPropertyToIncludedPropertiesTree(filterMetaClass, parentNode, nodesCopy, metaClass, query);
                }
            }
            return;
        }

        Node<String> nextNode;
        MetaClass childClass = metaClass;

        // get the next node to traverse by either finding an existing one, or creating a new node
        if (first.isEmpty()) {
            nextNode = parentNode;
            isClass = true;
        } else {
            int indexOfFirst = nodeChildrenContainsElement(parentNode, first);

            if (indexOfFirst > -1) {
                nextNode = parentNode.getChildren().get(indexOfFirst);
            } else {
                nextNode = new Node<>(first);
                parentNode.addChild(nextNode);
            }

            MetaPropertyPath mpp = metaClass.getPropertyPath(first);
            if (mpp == null) {
                throw new RuntimeException("Filter includeProperties: Unable to find property " + first + " of " + metaClass.getName());
            }

            MetaProperty metaProperty = mpp.getMetaProperty();

            if (metaProperty.getRange().isClass()) {
                isClass = true;
                if (metadataTools.getCrossDataStoreReferenceIdProperty(metaProperty.getStore().getName(), metaProperty) == null)
                {
                    childClass = metaProperty.getRange().asClass();
                }
            }
        }

        if(isClass) {
            recursivelyAddPropertyToIncludedPropertiesTree(filterMetaClass, nextNode, rest, childClass, query);
        }
    }

    private <T extends Object> int nodeChildrenContainsElement(Node<T> parentNode, T object) {
        for (int i = 0; i < parentNode.getChildren().size(); i++) {
            if (parentNode.getChildren().get(i).getData().equals(object)) {
                return i;
            }
        }

        return -1;
    }

    protected boolean isMetaPropertyPathAllowed(MetaPropertyPath propertyPath, String query) {
        UiEntityAttributeContext context = new UiEntityAttributeContext(propertyPath);
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
