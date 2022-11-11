/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.impl;

import com.google.common.base.Splitter;
import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;


@Component("core_FetchPlanLoader")
public class FetchPlanLoader {

    private final Logger log = LoggerFactory.getLogger(FetchPlanLoader.class);

    protected Metadata metadata;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Element> getFetchPlanElements(Element element) {
        return element.elements().stream()
                .filter(el -> el.getName().equals("fetchPlan") || el.getName().equals("view"))
                .collect(Collectors.toList());
    }

    public FetchPlanInfo getFetchPlanInfo(Element fetchPlanElement) {
        return getFetchPlanInfo(fetchPlanElement, null);
    }

    public FetchPlanInfo getFetchPlanInfo(Element fetchPlanElement, @Nullable MetaClass providedMetaClass) {
        String fetchPlanName = fetchPlanElement.attributeValue("name");

        MetaClass metaClass = providedMetaClass != null ? providedMetaClass : getMetaClass(fetchPlanElement);

        FetchPlanInfo fetchPlanInfo = new FetchPlanInfo(metaClass, fetchPlanName);

        boolean overwrite = Boolean.parseBoolean(fetchPlanElement.attributeValue("overwrite"));

        String extended = fetchPlanElement.attributeValue("extends");
        List<String> ancestors = null;

        if (isNotBlank(extended)) {
            ancestors = splitExtends(extended);
        }

        if (!overwrite && ancestors != null) {
            overwrite = ancestors.contains(fetchPlanName);
        }

        fetchPlanInfo.setAncestors(ancestors);
        fetchPlanInfo.setOverwrite(overwrite);
        fetchPlanInfo.setSystemProperties(Boolean.parseBoolean(fetchPlanElement.attributeValue("systemProperties")));
        return fetchPlanInfo;
    }


    public FetchPlanBuilder getFetchPlanBuilder(FetchPlanInfo fetchPlanInfo, Function<String, FetchPlan> ancestorFetchPlanResolver) {
        MetaClass metaClass = fetchPlanInfo.getMetaClass();
        String fetchPlanName = fetchPlanInfo.name;

        FetchPlanBuilder fetchPlanParams = fetchPlans.builder(metaClass.getJavaClass()).name(fetchPlanName);
        if (isNotEmpty(fetchPlanInfo.ancestors)) {
            fetchPlanInfo.ancestors.stream()
                    .map(ancestorFetchPlanResolver)
                    .forEach(fetchPlanParams::merge);
        }
        if (fetchPlanInfo.systemProperties) fetchPlanParams.addSystem();
        return fetchPlanParams;
    }


    public void loadFetchPlanProperties(Element fetchPlanElem,
                                        FetchPlanBuilder fetchPlanBuilder,
                                        boolean systemProperties,
                                        BiFunction<MetaClass, String, FetchPlan> refFetchPlanResolver) {
        final MetaClass metaClass = metadata.getClass(fetchPlanBuilder.getEntityClass());
        final String fetchPlanName = fetchPlanBuilder.getName();

        Set<String> propertyNames = new HashSet<>();

        for (Element propElem : fetchPlanElem.elements("property")) {
            String propertyName = propElem.attributeValue("name");

            if (propertyNames.contains(propertyName)) {
                throw new DevelopmentException(String.format("Fetch plan %s/%s definition error: fetch plan declared property %s twice",
                        metaClass.getName(), fetchPlanName, propertyName));
            }
            propertyNames.add(propertyName);

            MetaProperty metaProperty = metaClass.getProperty(propertyName);
            if (metaProperty == null) {
                throw new DevelopmentException(String.format("Fetch plan %s/%s definition error: property %s doesn't exist",
                        metaClass.getName(), fetchPlanName, propertyName));
            }

            FetchPlanBuilder refFetchPlanBuilder = null;
            String refFetchPlanName = propElem.attributeValue("fetchPlan");
            if (refFetchPlanName == null) {
                refFetchPlanName = propElem.attributeValue("view");
            }

            MetaClass refMetaClass;
            Range range = metaProperty.getRange();
            if (range == null) {
                throw new RuntimeException("cannot find range for meta property: " + metaProperty);
            }

            final List<Element> propertyElements = propElem.elements("property");
            boolean inlineFetchPlan = !propertyElements.isEmpty();

            // use "_base" if fetch plan is not specified and there are no nested properties
            if (refFetchPlanName == null && range.isClass() && !inlineFetchPlan) {
                refFetchPlanName = FetchPlan.BASE;
            }

            if (!range.isClass() && (refFetchPlanName != null || inlineFetchPlan)) {
                throw new DevelopmentException(String.format("Fetch plan %s/%s definition error: property %s is not an entity",
                        metaClass.getName(), fetchPlanName, propertyName));
            }

            if (refFetchPlanName != null) {
                refMetaClass = getMetaClass(propElem, range);
                refFetchPlanBuilder = fetchPlans.builder(refMetaClass.getJavaClass()).name(refFetchPlanName)
                        .addFetchPlan(refFetchPlanResolver.apply(refMetaClass, refFetchPlanName));
            }

            if (inlineFetchPlan) {
                // try to import anonymous fetch plan
                Class<?> rangeClass = range.asClass().getJavaClass();

                if (refFetchPlanBuilder == null) {
                    refFetchPlanBuilder = fetchPlans.builder(rangeClass);
                    if (systemProperties)
                        refFetchPlanBuilder.addSystem();
                }
                loadFetchPlanProperties(propElem, refFetchPlanBuilder, systemProperties, refFetchPlanResolver);
            }
            FetchMode fetchMode = FetchMode.AUTO;
            String fetch = propElem.attributeValue("fetch");
            if (fetch != null)
                fetchMode = FetchMode.valueOf(fetch);
            fetchPlanBuilder.mergeProperty(propertyName,
                    refFetchPlanBuilder != null ? refFetchPlanBuilder.build() : null,
                    fetchMode);
        }
    }

    protected String getFetchPlanName(Element fetchPlanElem) {
        String fetchPlanName = fetchPlanElem.attributeValue("name");
        if (StringUtils.isBlank(fetchPlanName))
            throw new DevelopmentException("Invalid fetch plan definition: no 'name' attribute present");
        return fetchPlanName;
    }

    protected MetaClass getMetaClass(Element fetchPlanElem) {
        MetaClass metaClass;
        String entity = fetchPlanElem.attributeValue("entity");
        if (StringUtils.isBlank(entity)) {
            String className = fetchPlanElem.attributeValue("class");
            if (StringUtils.isBlank(className))
                throw new DevelopmentException("Invalid fetch plan definition: no 'entity' or 'class' attribute present");
            Class entityClass = ReflectionHelper.getClass(className);
            metaClass = metadata.getClass(entityClass);
        } else {
            metaClass = metadata.getClass(entity);
        }
        return metaClass;
    }

    protected MetaClass getMetaClass(String entityName, String entityClass) {
        if (entityName != null) {
            return metadata.getClass(entityName);
        } else {
            return metadata.getClass(ReflectionHelper.getClass(entityClass));
        }
    }

    protected MetaClass getMetaClass(Element propElem, Range range) {
        MetaClass refMetaClass;
        String refEntityName = propElem.attributeValue("entity"); // this attribute is deprecated
        if (refEntityName == null) {
            refMetaClass = range.asClass();
        } else {
            refMetaClass = metadata.getClass(refEntityName);
        }
        return refMetaClass;
    }

    protected void checkDuplicates(Element rootElem) {
        Set<String> checked = new HashSet<>();
        for (Element fetchPlanElem : getFetchPlanElements(rootElem)) {
            String fetchPlanName = getFetchPlanName(fetchPlanElem);
            String key = getMetaClass(fetchPlanElem) + "/" + fetchPlanName;
            if (!Boolean.parseBoolean(fetchPlanElem.attributeValue("overwrite"))) {
                String extend = fetchPlanElem.attributeValue("extends");
                if (extend != null) {
                    List<String> ancestors = splitExtends(extend);

                    if (!ancestors.contains(fetchPlanName) && checked.contains(key)) {
                        log.warn("Duplicate fetch plan definition without 'overwrite' attribute and not extending parent fetch plan: " + key);
                    }
                }
            }
            checked.add(key);
        }
    }

    protected List<String> splitExtends(String extend) {
        return Splitter.on(',').omitEmptyStrings().trimResults().splitToList(extend);
    }

    public static class FetchPlanInfo {
        protected MetaClass metaClass;
        protected String name;
        protected List<String> ancestors;
        protected boolean overwrite = false;
        protected boolean systemProperties = false;

        public FetchPlanInfo(MetaClass metaClass, String name) {
            this.metaClass = metaClass;
            this.name = name;
        }

        public MetaClass getMetaClass() {
            return metaClass;
        }

        public Class getJavaClass() {
            return metaClass.getJavaClass();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getAncestors() {
            return ancestors;
        }

        public void setAncestors(@Nullable List<String> ancestors) {
            this.ancestors = ancestors;
        }

        public boolean isOverwrite() {
            return overwrite;
        }

        public void setOverwrite(boolean overwrite) {
            this.overwrite = overwrite;
        }

        public boolean isSystemProperties() {
            return systemProperties;
        }

        public void setSystemProperties(boolean systemProperties) {
            this.systemProperties = systemProperties;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FetchPlanInfo)) {
                return false;
            }

            FetchPlanInfo that = (FetchPlanInfo) obj;
            return this.getJavaClass() == that.getJavaClass() && Objects.equals(this.name, that.name);
        }

        @Override
        public int hashCode() {
            int result = getJavaClass().hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }

}
