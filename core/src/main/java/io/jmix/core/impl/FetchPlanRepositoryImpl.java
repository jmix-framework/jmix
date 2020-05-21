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

import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
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
import org.springframework.beans.factory.annotation.Autowired;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of the {@link FetchPlanRepository}. Contains methods to store {@link FetchPlan} objects and deploy
 * them from XML.
 */
@Component(FetchPlanRepository.NAME)
public class FetchPlanRepositoryImpl implements FetchPlanRepository {

    private final Logger log = LoggerFactory.getLogger(FetchPlanRepositoryImpl.class);

    protected List<String> readFileNames = new LinkedList<>();

    protected Map<MetaClass, Map<String, FetchPlan>> storage = new ConcurrentHashMap<>();

    @Autowired
    protected Environment environment;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected ExtendedEntities extendedEntities;
    
    @Autowired
    protected Resources resources;

    @Autowired
    protected JmixModules modules;

    @Autowired
    protected FetchPlanLoader fetchPlanLoader;

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
        storage.clear();
        readFileNames.clear();

        Element rootElem = DocumentHelper.createDocument().addElement("fetchPlans");

        for (String location : modules.getPropertyValues("jmix.core.fetchPlansConfig")) {
            addFile(rootElem, location);
        }

        fetchPlanLoader.checkDuplicates(rootElem);

        for (Element fetchPlanElement : fetchPlanLoader.getFetchPlanElements(rootElem)) {
            deployFetchPlan(rootElem, fetchPlanElement, new HashSet<>());
        }
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

            for (Element fetchPlanElement : fetchPlanLoader.getFetchPlanElements(rootElem)) {
                commonRootElem.add(fetchPlanElement.createCopy());
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

            FetchPlan fetchPlan = retrieveFetchPlan(metaClass, name, new HashSet<>());
            return FetchPlan.copyNullable(fetchPlan);
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
            Map<String, FetchPlan> fetchPlanMap = storage.get(metaClass);
            if (fetchPlanMap != null && !fetchPlanMap.isEmpty()) {
                Set<String> keySet = new HashSet<>(fetchPlanMap.keySet());
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

    protected FetchPlan deployDefaultFetchPlan(MetaClass metaClass, String name, Set<FetchPlanLoader.FetchPlanInfo> visited) {
        Class<? extends Entity> javaClass = metaClass.getJavaClass();

        FetchPlanLoader.FetchPlanInfo info = new FetchPlanLoader.FetchPlanInfo(metaClass, name);
        if (visited.contains(info)) {
            throw new DevelopmentException(String.format("Fetch plans cannot have cyclic references. FetchPlan %s for class %s",
                    name, metaClass.getName()));
        }

        FetchPlan fetchPlan;
        if (FetchPlan.LOCAL.equals(name)) {
            fetchPlan = new FetchPlan(javaClass, name, false);
            addAttributesToLocalFetchPlan(metaClass, fetchPlan);
        } else if (FetchPlan.MINIMAL.equals(name)) {
            fetchPlan = new FetchPlan(javaClass, name, false);
            addAttributesToMinimalFetchPlan(metaClass, fetchPlan, info, visited);
        } else if (FetchPlan.BASE.equals(name)) {
            fetchPlan = new FetchPlan(javaClass, name, false);
            addAttributesToMinimalFetchPlan(metaClass, fetchPlan, info, visited);
            addAttributesToLocalFetchPlan(metaClass, fetchPlan);
        } else {
            throw new UnsupportedOperationException("Unsupported default fetch plan: " + name);
        }

        storeFetchPlan(metaClass, fetchPlan);

        return fetchPlan;
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

    protected void addAttributesToMinimalFetchPlan(MetaClass metaClass, FetchPlan fetchPlan, FetchPlanLoader.FetchPlanInfo info, Set<FetchPlanLoader.FetchPlanInfo> visited) {
        Collection<MetaProperty> metaProperties = metadataTools.getInstanceNameRelatedProperties(metaClass, true);
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

    protected void addPersistentAttributeToMinimalFetchPlan(MetaClass metaClass, Set<FetchPlanLoader.FetchPlanInfo> visited, FetchPlanLoader.FetchPlanInfo info, FetchPlan fetchPlan, MetaProperty metaProperty) {
        if (metaProperty.getRange().isClass()
                && !metaProperty.getRange().getCardinality().isMany()) {

            Map<String, FetchPlan> fetchPlans = storage.get(metaProperty.getRange().asClass());
            FetchPlan refMinimalFetchPlan = (fetchPlans == null ? null : fetchPlans.get(FetchPlan.MINIMAL));

            if (refMinimalFetchPlan != null) {
                fetchPlan.addProperty(metaProperty.getName(), refMinimalFetchPlan);
            } else {
                visited.add(info);
                FetchPlan referenceMinimalFetchPlan = deployDefaultFetchPlan(metaProperty.getRange().asClass(), FetchPlan.MINIMAL, visited);
                visited.remove(info);

                fetchPlan.addProperty(metaProperty.getName(), referenceMinimalFetchPlan);
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

        Element rootElem = DocumentHelper.createDocument().addElement("fetchPlans");

        lock.writeLock().lock();
        try {
            addFile(rootElem, resourceUrl);

            for (Element fetchPlanElem : fetchPlanLoader.getFetchPlanElements(rootElem)) {
                deployFetchPlan(rootElem, fetchPlanElem, new HashSet<>());
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
            throw new RuntimeException("Unable to read fetch plans xml", e);
        }
        Element rootElem = doc.getRootElement();

        for (Element includeElem : rootElem.elements("include")) {
            String file = includeElem.attributeValue("file");
            if (!StringUtils.isBlank(file))
                deployFetchPlans(file);
        }

        for (Element fetchPlanElem : fetchPlanLoader.getFetchPlanElements(rootElem)) {
            deployFetchPlan(rootElem, fetchPlanElem);
        }
    }

    @Nullable
    protected FetchPlan retrieveFetchPlan(MetaClass metaClass, String name, Set<FetchPlanLoader.FetchPlanInfo> visited) {
        Map<String, FetchPlan> fetchPlans = storage.get(metaClass);
        FetchPlan fetchPlan = (fetchPlans == null ? null : fetchPlans.get(name));
        if (fetchPlan == null && (name.equals(FetchPlan.LOCAL) || name.equals(FetchPlan.MINIMAL) || name.equals(FetchPlan.BASE))) {
            fetchPlan = deployDefaultFetchPlan(metaClass, name, visited);
        }
        return fetchPlan;
    }

    public FetchPlan deployFetchPlan(Element rootElem, Element fetchPlanElem) {
        lock.writeLock().lock();
        try {
            return deployFetchPlan(rootElem, fetchPlanElem, new HashSet<>());
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected FetchPlan deployFetchPlan(Element rootElem, Element fetchPlanElem, Set<FetchPlanLoader.FetchPlanInfo> visited) {
        FetchPlanLoader.FetchPlanInfo fetchPlanInfo = fetchPlanLoader.getFetchPlanInfo(fetchPlanElem);
        MetaClass metaClass = fetchPlanInfo.getMetaClass();
        String fetchPlanName = fetchPlanInfo.getName();

        if (StringUtils.isBlank(fetchPlanName)) {
            throw new DevelopmentException("Invalid fetch plan definition: no 'name' attribute present");
        }

        if (visited.contains(fetchPlanInfo)) {
            throw new DevelopmentException(String.format("Fetch plans cannot have cyclic references. Fetch plan %s for class %s",
                    fetchPlanName, metaClass.getName()));
        }

        FetchPlan defaultFetchPlan = retrieveFetchPlan(metaClass, fetchPlanName, visited);

        if (defaultFetchPlan != null && !fetchPlanInfo.isOverwrite()) {
            return defaultFetchPlan;
        }

        FetchPlan.FetchPlanParams fetchPlanParams = fetchPlanLoader.getFetchPlanParams(
                fetchPlanInfo,
                ancestorFetchPlanName -> getAncestorFetchPlan(metaClass, ancestorFetchPlanName, visited)
        );

        FetchPlan fetchPlan = new FetchPlan(fetchPlanParams);

        visited.add(fetchPlanInfo);
        fetchPlanLoader.loadFetchPlanProperties(fetchPlanElem, fetchPlan, fetchPlanInfo.isSystemProperties(), (MetaClass refMetaClass, String refFetchPlanName) -> {
            if (refFetchPlanName == null) {
                return null;
            }
            FetchPlan refFetchPlan = retrieveFetchPlan(refMetaClass, refFetchPlanName, visited);
            if (refFetchPlan == null) {
                for (Element e : fetchPlanLoader.getFetchPlanElements(rootElem)) {
                    if (refMetaClass.equals(fetchPlanLoader.getMetaClass(e.attributeValue("entity"), e.attributeValue("class")))
                            && refFetchPlanName.equals(e.attributeValue("name"))) {
                        refFetchPlan = deployFetchPlan(rootElem, e, visited);
                        break;
                    }
                }

                if (refFetchPlan == null) {
                    MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(refMetaClass);
                    if (originalMetaClass != null) {
                        refFetchPlan = retrieveFetchPlan(originalMetaClass, refFetchPlanName, visited);
                    }
                }

                if (refFetchPlan == null) {
                    throw new DevelopmentException(
                            String.format("Fetch plan %s/%s definition error: unable to find/deploy referenced fetch plan %s/%s",
                                    metaClass.getName(), fetchPlanName, refMetaClass, refFetchPlanName));
                }
            }
            return refFetchPlan;
        });
        visited.remove(fetchPlanInfo);

        storeFetchPlan(metaClass, fetchPlan);

        if (fetchPlanInfo.isOverwrite()) {
            replaceOverridden(fetchPlan);
        }

        return fetchPlan;
    }

    protected void replaceOverridden(FetchPlan replacementFetchPlan) {
        HashSet<FetchPlan> checked = new HashSet<>();

        for (FetchPlan fetchPlan : getAllInitialized()) {
            if (!checked.contains(fetchPlan)) {
                replaceOverridden(fetchPlan, replacementFetchPlan, checked);
            }
        }
    }

    protected void replaceOverridden(FetchPlan root, FetchPlan replacementFetchPlan, HashSet<FetchPlan> checked) {
        checked.add(root);

        List<FetchPlanProperty> replacements = null;

        for (FetchPlanProperty property : root.getProperties()) {
            FetchPlan propertyFetchPlan = property.getFetchPlan();

            if (propertyFetchPlan != null) {
                if (Objects.equals(propertyFetchPlan.getName(), replacementFetchPlan.getName())
                        && replacementFetchPlan.getEntityClass() == propertyFetchPlan.getEntityClass()) {
                    if (replacements == null) {
                        replacements = new LinkedList<>();
                    }
                    replacements.add(new FetchPlanProperty(property.getName(), replacementFetchPlan, property.getFetchMode()));
                } else if (propertyFetchPlan.getEntityClass() != null && !checked.contains(propertyFetchPlan)) {
                    replaceOverridden(propertyFetchPlan, replacementFetchPlan, checked);
                }
            }
        }

        if (replacements != null) {
            for (FetchPlanProperty replacement : replacements) {
                root.addProperty(replacement.getName(), replacement.getFetchPlan(), replacement.getFetchMode());
            }
        }
    }

    protected FetchPlan getAncestorFetchPlan(MetaClass metaClass, String ancestor, Set<FetchPlanLoader.FetchPlanInfo> visited) {
        FetchPlan ancestorFetchPlan = retrieveFetchPlan(metaClass, ancestor, visited);
        if (ancestorFetchPlan == null) {
            MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
            if (originalMetaClass != null) {
                ancestorFetchPlan = retrieveFetchPlan(originalMetaClass, ancestor, visited);
            }
            if (ancestorFetchPlan == null) {
                // Last resort - search for all ancestors
                for (MetaClass ancestorMetaClass : metaClass.getAncestors()) {
                    if (ancestorMetaClass.equals(metaClass)) {
                        ancestorFetchPlan = retrieveFetchPlan(ancestorMetaClass, ancestor, visited);
                        if (ancestorFetchPlan != null)
                            break;
                    }
                }
            }
            if (ancestorFetchPlan == null) {
                throw new DevelopmentException("No ancestor fetch plan found: " + ancestor + " for " + metaClass.getName());
            }
        }
        return ancestorFetchPlan;
    }

    protected void storeFetchPlan(MetaClass metaClass, FetchPlan fetchPlan) {
        Map<String, FetchPlan> fetchPlans = storage.get(metaClass);
        if (fetchPlans == null) {
            fetchPlans = new ConcurrentHashMap<>();
        }

        fetchPlans.put(fetchPlan.getName(), fetchPlan);
        storage.put(metaClass, fetchPlans);
    }

    protected List<FetchPlan> getAllInitialized() {
        List<FetchPlan> list = new ArrayList<>();
        for (Map<String, FetchPlan> fetchPlanMap : storage.values()) {
            list.addAll(fetchPlanMap.values());
        }
        return list;
    }

    public List<FetchPlan> getAll() {
        lock.readLock().lock();
        try {
            checkInitialized();
            List<FetchPlan> list = new ArrayList<>();
            for (Map<String, FetchPlan> fetchPlanMap : storage.values()) {
                list.addAll(fetchPlanMap.values());
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }
}
