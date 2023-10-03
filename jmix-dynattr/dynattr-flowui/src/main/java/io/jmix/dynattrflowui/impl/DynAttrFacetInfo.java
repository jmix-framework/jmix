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

package io.jmix.dynattrflowui.impl;

import com.vaadin.flow.spring.annotation.UIScope;
import io.jmix.core.JmixOrder;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.Resources;
import io.jmix.core.common.xmlparsing.Dom4jTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattrflowui.facet.DynAttrFacet;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.view.*;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@UIScope
@Order(JmixOrder.LOWEST_PRECEDENCE)
@Component("dynattr_DynAttrFacetInfo")
public class DynAttrFacetInfo {

    private static final Logger log = LoggerFactory.getLogger(DynAttrFacetInfo.class);

    protected final ViewRegistry viewRegistry;

    protected final Resources resources;

    protected final Dom4jTools dom4jTools;

    protected final Metadata metadata;

    protected final MetadataTools metadataTools;

    private final Map<String, DynAttrFacetViewInfo> dynAttrViewsComponentMapping = new ConcurrentHashMap<>();
    private volatile boolean isInitialized = false;

    public DynAttrFacetInfo(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") ViewRegistry viewRegistry,
                            Resources resources,
                            Dom4jTools dom4jTools,
                            Metadata metadata,
                            MetadataTools metadataTools) {
        this.viewRegistry = viewRegistry;
        this.resources = resources;
        this.dom4jTools = dom4jTools;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
    }


    protected boolean isFacetAvailable(Element window, List<String> facetNames) {
        if (facetNames.isEmpty()) {
            return true;
        }

        Element facets = window.element("facets");
        if (facets != null) {
            for (String facetName : facetNames) {
                if (facets.element(facetName) == null) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }


    private void getDynAttrViews(MetaClass metaClass) {
        if (isInitialized) {
            return;
        }
        Set<String> visitedWindowIds = new HashSet<>();

        for (ViewInfo windowInfo : viewRegistry.getViewInfos()) {
            String windowId = windowInfo.getId();

            // just skip for now, we assume all versions of screen can operate with the same entity
            if (visitedWindowIds.contains(windowId)) {
                continue;
            }

            Optional<String> src = windowInfo.getTemplatePath();
            if (src.isPresent()) {
                try {
                    Element windowElement = getWindowElement(src.get());
                    if (windowElement != null) {

                        if (isEntityAvailable(windowElement, metaClass.getJavaClass()) &&
                                isFacetAvailable(windowElement, Collections.singletonList(DynAttrFacet.FACET_NAME))) {
                            DynAttrFacetViewInfo facetViewInfo = new DynAttrFacetViewInfo();
                            facetViewInfo.setViewId(windowId);
                            List<String> targetElementNames = new ArrayList<>();
                            findTargetElementNames(targetElementNames, windowElement.element("layout"));
                            facetViewInfo.setPossibleTargetUiComponentIds(targetElementNames);
                            dynAttrViewsComponentMapping.put(windowId, facetViewInfo);
                        }
                    }
                } catch (FileNotFoundException e) {
                    log.error("Unable to find file of screen: {}", e.getMessage());
                }
            }

            visitedWindowIds.add(windowId);
        }
        isInitialized = true;
    }

    protected boolean isEntityAvailable(Element window, Class<?> entityClass) {

        Element data = window.element("data");
        if (data == null) {
            return false;
        }


        List<Element> dataElements = data.elements();
        List<String> dataElementIds = dataElements.stream()
                .filter(de -> isEntityAvailableInDataElement(entityClass, de))
                .map(de -> de.attributeValue("id"))
                .toList();

        return !dataElementIds.isEmpty();
    }

    protected boolean isEntityAvailableForClass(Class<?> entityClass, String className) {
        Class<?> entity = entityClass;
        boolean isAvailable;
        boolean process;
        do {
            isAvailable = className.equals(entity.getName());
            entity = entity.getSuperclass();
            process = metadata.findClass(entity) != null && metadataTools.isJpaEntity(entity);
        } while (process && !isAvailable);
        return isAvailable;
    }

    protected boolean isEntityAvailableInDataElement(Class<?> entityClass, @Nullable Element dataContainer) {
        if (dataContainer == null) {
            return false;
        }

        String dsClassValue = dataContainer.attributeValue("class");
        if (StringUtils.isEmpty(dsClassValue)) {
            return false;
        }

        return isEntityAvailableForClass(entityClass, dsClassValue);
    }

    protected boolean isEntityAvailableInDataElement(Class<?> entityClass, Element dataElement, String datasourceId) {
        Element datasource = elementById(dataElement, datasourceId);
        return isEntityAvailableInDataElement(entityClass, datasource);
    }

    @Nullable
    protected Element elementById(Element root, String elementId) {
        for (Element element : root.elements()) {
            String id = element.attributeValue("id");
            if (StringUtils.isNotEmpty(id) && elementId.equals(id)) {
                return element;
            } else {
                element = elementById(element, elementId);
                if (element != null) {
                    return element;
                }
            }
        }
        return null;
    }

    public Collection<String> getDynAttrViewIds(MetaClass metaClass) {
        getDynAttrViews(metaClass);
        return dynAttrViewsComponentMapping.keySet().stream().toList();
    }

    public Collection<String> getDynAttrViewTargetComponentIds(String viewId) {
        if (!dynAttrViewsComponentMapping.containsKey(viewId)) {
            return new ArrayList<>();
        }
        return dynAttrViewsComponentMapping.get(viewId).getPossibleTargetUiComponentIds();
    }

    private void findTargetElementNames(List<String> targetElementList, Element searchElement) {
        for (var child : searchElement.elements()) {
            if (child.getQName().getName().equals(JmixFormLayout.QUALIFIED_XML_NAME) ||
                    child.getQName().getName().equals("dataGrid")) {
                targetElementList.add(child.attributeValue("id"));
            }
            if (!child.elements().isEmpty()) {
                findTargetElementNames(targetElementList, child);
            }
        }
    }

    @Nullable
    protected Element getWindowElement(String src) throws FileNotFoundException {
        String text = resources.getResourceAsString(src);
        if (StringUtils.isNotEmpty(text)) {
            try {
                Document document = dom4jTools.readDocument(text);
                Element root = document.getRootElement();

                if (root.getName().equals("view")) {
                    return root;
                }
            } catch (RuntimeException e) {
                log.error(String.format("Can't parse screen file: %s", src));
            }
        } else {
            throw new FileNotFoundException("File doesn't exist or empty: " + src);
        }
        return null;
    }

    public static class DynAttrFacetViewInfo {
        private String viewId;
        private Collection<String> possibleTargetUiComponentIds = new ArrayList<>();

        public String getViewId() {
            return viewId;
        }

        public void setViewId(String viewId) {
            this.viewId = viewId;
        }

        public Collection<String> getPossibleTargetUiComponentIds() {
            return possibleTargetUiComponentIds;
        }

        public void setPossibleTargetUiComponentIds(Collection<String> possibleTargetUiComponentIds) {
            this.possibleTargetUiComponentIds = possibleTargetUiComponentIds;
        }
    }
}
