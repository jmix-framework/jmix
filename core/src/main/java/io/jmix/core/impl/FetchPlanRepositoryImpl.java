/*
 * Copyright 2019 Haulmont.
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
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.commons.util.ReflectionHelper;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Implementation of the {@link FetchPlanRepository}. Contains methods to store {@link FetchPlan} objects and deploy
 * them from XML.
 */
@Component(FetchPlanRepository.NAME)
public class FetchPlanRepositoryImpl implements FetchPlanRepository {

    private final Logger log = LoggerFactory.getLogger(FetchPlanRepositoryImpl.class);

    protected List<String> readFileNames = new LinkedList<>();

    protected Map<MetaClass, Map<String, FetchPlan>> storage = new ConcurrentHashMap<>();

    @Inject
    protected Environment environment;

    @Inject
    protected Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected ExtendedEntities extendedEntities;
    
    @Inject
    protected Resources resources;

    @Inject
    protected JmixModules modules;

    protected volatile boolean initialized;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    log.info("Initializing fetch plans");
                    init();
                    initialized = true;
                }
            } finally {
                lock.readLock().lock();
                lock.writeLock().unlock();
            }
        }
    }

    protected void init() {
        // todo perf4j
//        StopWatch initTiming = new Slf4JStopWatch("ViewRepository.init." + getClass().getSimpleName());

        storage.clear();
        readFileNames.clear();

        Element rootElem = DocumentHelper.createDocument().addElement("fetchPlans");

        for (String location : modules.getPropertyValues("jmix.core.fetchPlansConfig")) {
            addFile(rootElem, location);
        }

        checkDuplicates(rootElem);

        for (Element viewElem : getFetchPlanElements(rootElem)) {
            deployFetchPlan(rootElem, viewElem, new HashSet<>());
        }

//        initTiming.stop();
    }

    protected List<Element> getFetchPlanElements(Element element) {
        return element.elements().stream()
                .filter(el -> el.getName().equals("fetchPlan") || el.getName().equals("view"))
                .collect(Collectors.toList());
    }


    protected void checkDuplicates(Element rootElem) {
        Set<String> checked = new HashSet<>();
        for (Element viewElem : getFetchPlanElements(rootElem)) {
            String viewName = getFetchPlanName(viewElem);
            String key = getMetaClass(viewElem) + "/" + viewName;
            if (!Boolean.parseBoolean(viewElem.attributeValue("overwrite"))) {
                String extend = viewElem.attributeValue("extends");
                if (extend != null) {
                    List<String> ancestors = splitExtends(extend);

                    if (!ancestors.contains(viewName) && checked.contains(key)) {
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

    protected void addFile(Element commonRootElem, String fileName) {
        if (readFileNames.contains(fileName))
            return;

        log.debug("Deploying fetch plans config: " + fileName);
        readFileNames.add(fileName);

        InputStream stream = null;
        try {
            stream = resources.getResourceAsStream(fileName);
            if (stream == null) {
                throw new IllegalStateException("Resource is not found: " + fileName);
            }

            SAXReader reader = new SAXReader();
            Document doc;
            try {
                doc = reader.read(new InputStreamReader(stream, StandardCharsets.UTF_8));
            } catch (DocumentException e) {
                throw new RuntimeException("Unable to parse fetch plans file " + fileName, e);
            }
            Element rootElem = doc.getRootElement();

            for (Element includeElem : rootElem.elements("include")) {
                String incFile = includeElem.attributeValue("file");
                if (!StringUtils.isBlank(incFile))
                    addFile(commonRootElem, incFile);
            }

            for (Element viewElem : getFetchPlanElements(rootElem)) {
                commonRootElem.add(viewElem.createCopy());
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @EventListener(ContextRefreshedEvent.class)
    @Order(Events.HIGHEST_CORE_PRECEDENCE + 40)
    public void reset() {
        initialized = false;
    }

    /**
     * Get FetchPlan for an entity.
     *
     * @param entityClass entity class
     * @param name        fetch plan name
     * @return fetch plan instance. Throws {@link FetchPlanNotFoundException} if not found.
     */
    @Override
    public FetchPlan getFetchPlan(Class<? extends Entity> entityClass, String name) {
        return getFetchPlan(metadata.getClass(entityClass), name);
    }

    /**
     * Get FetchPlan for an entity.
     *
     * @param metaClass entity class
     * @param name      fetch plan name
     * @return fetch plan instance. Throws {@link FetchPlanNotFoundException} if not found.
     */
    @Override
    public FetchPlan getFetchPlan(MetaClass metaClass, String name) {
        Preconditions.checkNotNullArgument(metaClass, "MetaClass is null");

        FetchPlan fetchPlan = findFetchPlan(metaClass, name);

        if (fetchPlan == null) {
            throw new FetchPlanNotFoundException(String.format("FetchPlan %s/%s not found", metaClass.getName(), name));
        }
        return fetchPlan;
    }

    /**
     * Searches for a FetchPlan for an entity
     *
     * @param metaClass entity class
     * @param name      fetch plan name
     * @return fetch plan instance or null if no fetch plan found
     */
    @Override
    @Nullable
    public FetchPlan findFetchPlan(MetaClass metaClass, @Nullable String name) {
        Preconditions.checkNotNullArgument(metaClass, "metaClass is null");

        if (name == null) {
            return null;
        }

        lock.readLock().lock();
        try {
            checkInitialized();

            FetchPlan view = retrieveFetchPlan(metaClass, name, new HashSet<>());
            return FetchPlan.copyNullable(view);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<String> getFetchPlanNames(MetaClass metaClass) {
        Preconditions.checkNotNullArgument(metaClass, "MetaClass is null");
        lock.readLock().lock();
        try {
            checkInitialized();
            Map<String, FetchPlan> viewMap = storage.get(metaClass);
            if (viewMap != null && !viewMap.isEmpty()) {
                Set<String> keySet = new HashSet<>(viewMap.keySet());
                keySet.remove(FetchPlan.LOCAL);
                keySet.remove(FetchPlan.MINIMAL);
                keySet.remove(FetchPlan.BASE);
                return keySet;
            } else {
                return Collections.emptyList();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<String> getFetchPlanNames(Class<? extends Entity> entityClass) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        MetaClass metaClass = metadata.getClass(entityClass);
        return getFetchPlanNames(metaClass);
    }

    protected FetchPlan deployDefaultFetchPlan(MetaClass metaClass, String name, Set<FetchPlanInfo> visited) {
        Class<? extends Entity> javaClass = metaClass.getJavaClass();

        FetchPlanInfo info = new FetchPlanInfo(javaClass, name);
        if (visited.contains(info)) {
            throw new DevelopmentException(String.format("Views cannot have cyclic references. FetchPlan %s for class %s",
                    name, metaClass.getName()));
        }

        FetchPlan view;
        if (FetchPlan.LOCAL.equals(name)) {
            view = new FetchPlan(javaClass, name, false);
            addAttributesToLocalFetchPlan(metaClass, view);
        } else if (FetchPlan.MINIMAL.equals(name)) {
            view = new FetchPlan(javaClass, name, false);
            addAttributesToMinimalFetchPlan(metaClass, view, info, visited);
        } else if (FetchPlan.BASE.equals(name)) {
            view = new FetchPlan(javaClass, name, false);
            addAttributesToMinimalFetchPlan(metaClass, view, info, visited);
            addAttributesToLocalFetchPlan(metaClass, view);
        } else {
            throw new UnsupportedOperationException("Unsupported default fetch plan: " + name);
        }

        storeFetchPlan(metaClass, view);

        return view;
    }

    protected void addAttributesToLocalFetchPlan(MetaClass metaClass, FetchPlan fetchPlan) {
        for (MetaProperty property : metaClass.getProperties()) {
            if (!property.getRange().isClass()
                    && !metadataTools.isSystem(property)
                    && metadataTools.isPersistent(property)) {
                fetchPlan.addProperty(property.getName());
            }
        }
    }

    protected void addAttributesToMinimalFetchPlan(MetaClass metaClass, FetchPlan fetchPlan, FetchPlanInfo info, Set<FetchPlanInfo> visited) {
        Collection<MetaProperty> metaProperties = metadataTools.getNamePatternProperties(metaClass, true);
        for (MetaProperty metaProperty : metaProperties) {
            if (metadataTools.isPersistent(metaProperty)) {
                addPersistentAttributeToMinimalFetchPlan(metaClass, visited, info, fetchPlan, metaProperty);
            } else {
                List<String> relatedProperties = metadataTools.getRelatedProperties(metaProperty);
                for (String relatedPropertyName : relatedProperties) {
                    MetaProperty relatedProperty = metaClass.getProperty(relatedPropertyName);
                    if (metadataTools.isPersistent(relatedProperty)) {
                        addPersistentAttributeToMinimalFetchPlan(metaClass, visited, info, fetchPlan, relatedProperty);
                    } else {
                        log.warn(
                                "Transient attribute '{}' is listed in 'related' properties of another transient attribute '{}'",
                                relatedPropertyName, metaProperty.getName());
                    }
                }
            }
        }
    }

    protected void addPersistentAttributeToMinimalFetchPlan(MetaClass metaClass, Set<FetchPlanInfo> visited, FetchPlanInfo info, FetchPlan fetchPlan, MetaProperty metaProperty) {
        if (metaProperty.getRange().isClass()
                && !metaProperty.getRange().getCardinality().isMany()) {

            Map<String, FetchPlan> views = storage.get(metaProperty.getRange().asClass());
            FetchPlan refMinimalView = (views == null ? null : views.get(FetchPlan.MINIMAL));

            if (refMinimalView != null) {
                fetchPlan.addProperty(metaProperty.getName(), refMinimalView);
            } else {
                visited.add(info);
                FetchPlan referenceMinimalView = deployDefaultFetchPlan(metaProperty.getRange().asClass(), FetchPlan.MINIMAL, visited);
                visited.remove(info);

                fetchPlan.addProperty(metaProperty.getName(), referenceMinimalView);
            }
        } else {
            fetchPlan.addProperty(metaProperty.getName());
        }
    }

    public void deployFetchPlans(String resourceUrl) {
        lock.readLock().lock();
        try {
            checkInitialized();
        } finally {
            lock.readLock().unlock();
        }

        Element rootElem = DocumentHelper.createDocument().addElement("views");

        lock.writeLock().lock();
        try {
            addFile(rootElem, resourceUrl);

            for (Element viewElem : getFetchPlanElements(rootElem)) {
                deployFetchPlan(rootElem, viewElem, new HashSet<>());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void deployFetchPlans(InputStream xml) {
        deployFetchPlans(new InputStreamReader(xml, StandardCharsets.UTF_8));
    }

    public void deployFetchPlans(Reader xml) {
        lock.readLock().lock();
        try {
            checkInitialized();
        } finally {
            lock.readLock().unlock();
        }

        SAXReader reader = new SAXReader();
        Document doc;
        try {
            doc = reader.read(xml);
        } catch (DocumentException e) {
            throw new RuntimeException("Unable to read views xml", e);
        }
        Element rootElem = doc.getRootElement();

        for (Element includeElem : rootElem.elements("include")) {
            String file = includeElem.attributeValue("file");
            if (!StringUtils.isBlank(file))
                deployFetchPlans(file);
        }

        for (Element viewElem : getFetchPlanElements(rootElem)) {
            deployFetchPlan(rootElem, viewElem);
        }
    }

    @Nullable
    protected FetchPlan retrieveFetchPlan(MetaClass metaClass, String name, Set<FetchPlanInfo> visited) {
        Map<String, FetchPlan> views = storage.get(metaClass);
        FetchPlan view = (views == null ? null : views.get(name));
        if (view == null && (name.equals(FetchPlan.LOCAL) || name.equals(FetchPlan.MINIMAL) || name.equals(FetchPlan.BASE))) {
            view = deployDefaultFetchPlan(metaClass, name, visited);
        }
        return view;
    }

    public FetchPlan deployFetchPlan(Element rootElem, Element fetchPlanElem) {
        lock.writeLock().lock();
        try {
            return deployFetchPlan(rootElem, fetchPlanElem, new HashSet<>());
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected FetchPlan deployFetchPlan(Element rootElem, Element fetchPlanElem, Set<FetchPlanInfo> visited) {
        String viewName = getFetchPlanName(fetchPlanElem);
        MetaClass metaClass = getMetaClass(fetchPlanElem);

        FetchPlanInfo info = new FetchPlanInfo(metaClass.getJavaClass(), viewName);
        if (visited.contains(info)) {
            throw new DevelopmentException(String.format("FetchPlans cannot have cyclic references. FetchPlan %s for class %s",
                    viewName, metaClass.getName()));
        }

        FetchPlan v = retrieveFetchPlan(metaClass, viewName, visited);
        boolean overwrite = Boolean.parseBoolean(fetchPlanElem.attributeValue("overwrite"));

        String extended = fetchPlanElem.attributeValue("extends");
        List<String> ancestors = null;

        if (isNotBlank(extended)) {
            ancestors = splitExtends(extended);
        }

        if (!overwrite && ancestors != null) {
            overwrite = ancestors.contains(viewName);
        }

        if (v != null && !overwrite) {
            return v;
        }

        boolean systemProperties = Boolean.valueOf(fetchPlanElem.attributeValue("systemProperties"));

        FetchPlan.FetchPlanParams viewParam = new FetchPlan.FetchPlanParams().entityClass(metaClass.getJavaClass()).name(viewName);
        if (isNotEmpty(ancestors)) {
            List<FetchPlan> ancestorsViews = ancestors.stream()
                    .map(a -> getAncestorFetchPlan(metaClass, a, visited))
                    .collect(Collectors.toList());

            viewParam.src(ancestorsViews);
        }
        viewParam.includeSystemProperties(systemProperties);
        FetchPlan view = new FetchPlan(viewParam);

        visited.add(info);
        loadFetchPlan(rootElem, fetchPlanElem, view, systemProperties, visited);
        visited.remove(info);

        storeFetchPlan(metaClass, view);

        if (overwrite) {
            replaceOverridden(view);
        }

        return view;
    }

    protected void replaceOverridden(FetchPlan replacementView) {
        // todo perf4j
//        StopWatch replaceTiming = new Slf4JStopWatch("ViewRepository.replaceOverridden");

        HashSet<FetchPlan> checked = new HashSet<>();

        for (FetchPlan view : getAllInitialized()) {
            if (!checked.contains(view)) {
                replaceOverridden(view, replacementView, checked);
            }
        }

//        replaceTiming.stop();
    }

    protected void replaceOverridden(FetchPlan root, FetchPlan replacementView, HashSet<FetchPlan> checked) {
        checked.add(root);

        List<FetchPlanProperty> replacements = null;

        for (FetchPlanProperty property : root.getProperties()) {
            FetchPlan propertyView = property.getFetchPlan();

            if (propertyView != null) {
                if (Objects.equals(propertyView.getName(), replacementView.getName())
                        && replacementView.getEntityClass() == propertyView.getEntityClass()) {
                    if (replacements == null) {
                        replacements = new LinkedList<>();
                    }
                    replacements.add(new FetchPlanProperty(property.getName(), replacementView, property.getFetchMode()));
                } else if (propertyView.getEntityClass() != null && !checked.contains(propertyView)) {
                    replaceOverridden(propertyView, replacementView, checked);
                }
            }
        }

        if (replacements != null) {
            for (FetchPlanProperty replacement : replacements) {
                root.addProperty(replacement.getName(), replacement.getFetchPlan(), replacement.getFetchMode());
            }
        }
    }

    protected FetchPlan getAncestorFetchPlan(MetaClass metaClass, String ancestor, Set<FetchPlanInfo> visited) {
        FetchPlan ancestorView = retrieveFetchPlan(metaClass, ancestor, visited);
        if (ancestorView == null) {
            MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
            if (originalMetaClass != null) {
                ancestorView = retrieveFetchPlan(originalMetaClass, ancestor, visited);
            }
            if (ancestorView == null) {
                // Last resort - search for all ancestors
                for (MetaClass ancestorMetaClass : metaClass.getAncestors()) {
                    if (ancestorMetaClass.equals(metaClass)) {
                        ancestorView = retrieveFetchPlan(ancestorMetaClass, ancestor, visited);
                        if (ancestorView != null)
                            break;
                    }
                }
            }
            if (ancestorView == null) {
                throw new DevelopmentException("No ancestor fetch plan found: " + ancestor + " for " + metaClass.getName());
            }
        }
        return ancestorView;
    }

    protected void loadFetchPlan(Element rootElem, Element fetchPlanElem, FetchPlan fetchPlan, boolean systemProperties, Set<FetchPlanInfo> visited) {
        final MetaClass metaClass = metadata.getClass(fetchPlan.getEntityClass());
        final String viewName = fetchPlan.getName();

        Set<String> propertyNames = new HashSet<>();

        for (Element propElem : fetchPlanElem.elements("property")) {
            String propertyName = propElem.attributeValue("name");

            if (propertyNames.contains(propertyName)) {
                throw new DevelopmentException(String.format("FetchPlan %s/%s definition error: fetch plan declared property %s twice",
                        metaClass.getName(), viewName, propertyName));
            }
            propertyNames.add(propertyName);

            MetaProperty metaProperty = metaClass.findProperty(propertyName);
            if (metaProperty == null) {
                throw new DevelopmentException(String.format("FetchPlan %s/%s definition error: property %s doesn't exist",
                        metaClass.getName(), viewName, propertyName));
            }

            FetchPlan refView = null;
            String refViewName = propElem.attributeValue("fetchPlan");
            if (refViewName == null) {
                refViewName = propElem.attributeValue("view");
            }

            MetaClass refMetaClass;
            Range range = metaProperty.getRange();
            if (range == null) {
                throw new RuntimeException("cannot find range for meta property: " + metaProperty);
            }

            final List<Element> propertyElements = propElem.elements("property");
            boolean inlineView = !propertyElements.isEmpty();

            if (!range.isClass() && (refViewName != null || inlineView)) {
                throw new DevelopmentException(String.format("FetchPlan %s/%s definition error: property %s is not an entity",
                        metaClass.getName(), viewName, propertyName));
            }

            if (refViewName != null) {
                refMetaClass = getMetaClass(propElem, range);

                refView = retrieveFetchPlan(refMetaClass, refViewName, visited);
                if (refView == null) {
                    for (Element e : getFetchPlanElements(rootElem)) {
                        if (refMetaClass.equals(getMetaClass(e.attributeValue("entity"), e.attributeValue("class")))
                                && refViewName.equals(e.attributeValue("name"))) {
                            refView = deployFetchPlan(rootElem, e, visited);
                            break;
                        }
                    }

                    if (refView == null) {
                        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(refMetaClass);
                        if (originalMetaClass != null) {
                            refView = retrieveFetchPlan(originalMetaClass, refViewName, visited);
                        }
                    }

                    if (refView == null) {
                        throw new DevelopmentException(
                                String.format("FetchPlan %s/%s definition error: unable to find/deploy referenced fetch plan %s/%s",
                                        metaClass.getName(), viewName, range.asClass().getName(), refViewName));
                    }
                }
            }

            if (inlineView) {
                // try to import anonymous views
                Class<? extends Entity> rangeClass = range.asClass().getJavaClass();

                if (refView != null) {
                    refView = new FetchPlan(refView, rangeClass, "", false); // system properties are already in the source fetch plan
                } else {
                    FetchPlanProperty existingProperty = fetchPlan.getProperty(propertyName);
                    if (existingProperty != null && existingProperty.getFetchPlan() != null) {
                        refView = new FetchPlan(existingProperty.getFetchPlan(), rangeClass, "", systemProperties);
                    } else {
                        refView = new FetchPlan(rangeClass, systemProperties);
                    }
                }
                loadFetchPlan(rootElem, propElem, refView, systemProperties, visited);
            }

            FetchMode fetchMode = FetchMode.AUTO;
            String fetch = propElem.attributeValue("fetch");
            if (fetch != null)
                fetchMode = FetchMode.valueOf(fetch);

            fetchPlan.addProperty(propertyName, refView, fetchMode);
        }
    }

    protected String getFetchPlanName(Element fetchPlanElem) {
        String viewName = fetchPlanElem.attributeValue("name");
        if (StringUtils.isBlank(viewName))
            throw new DevelopmentException("Invalid fetch plan definition: no 'name' attribute present");
        return viewName;
    }

    protected MetaClass getMetaClass(Element viewElem) {
        MetaClass metaClass;
        String entity = viewElem.attributeValue("entity");
        if (StringUtils.isBlank(entity)) {
            String className = viewElem.attributeValue("class");
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

    protected void storeFetchPlan(MetaClass metaClass, FetchPlan fetchPlan) {
        Map<String, FetchPlan> views = storage.get(metaClass);
        if (views == null) {
            views = new ConcurrentHashMap<>();
        }

        views.put(fetchPlan.getName(), fetchPlan);
        storage.put(metaClass, views);
    }

    protected List<FetchPlan> getAllInitialized() {
        List<FetchPlan> list = new ArrayList<>();
        for (Map<String, FetchPlan> viewMap : storage.values()) {
            list.addAll(viewMap.values());
        }
        return list;
    }

    public List<FetchPlan> getAll() {
        lock.readLock().lock();
        try {
            checkInitialized();
            List<FetchPlan> list = new ArrayList<>();
            for (Map<String, FetchPlan> viewMap : storage.values()) {
                list.addAll(viewMap.values());
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }

    protected static class FetchPlanInfo {
        protected Class javaClass;
        protected String name;

        public FetchPlanInfo(Class javaClass, String name) {
            this.javaClass = javaClass;
            this.name = name;
        }

        public Class getJavaClass() {
            return javaClass;
        }

        public void setJavaClass(Class javaClass) {
            this.javaClass = javaClass;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FetchPlanInfo)) {
                return false;
            }

            FetchPlanInfo that = (FetchPlanInfo) obj;
            return this.javaClass == that.javaClass && Objects.equals(this.name, that.name);
        }

        @Override
        public int hashCode() {
            int result = javaClass.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }
}
