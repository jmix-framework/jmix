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
import io.jmix.core.entity.Entity;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringTokenizer;
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
 * Base implementation of the {@link ViewRepository}. Contains methods to store {@link View} objects and deploy
 * them from XML. <br>
 * <br> Don't replace this class completely, because the framework uses it directly.
 */
@Component(ViewRepository.NAME)
public class ViewRepositoryImpl implements ViewRepository {

    private final Logger log = LoggerFactory.getLogger(ViewRepositoryImpl.class);

    protected List<String> readFileNames = new LinkedList<>();

    protected Map<MetaClass, Map<String, View>> storage = new ConcurrentHashMap<>();

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

    protected volatile boolean initialized;

    protected ReadWriteLock lock = new ReentrantReadWriteLock();

    protected void checkInitialized() {
        if (!initialized) {
            lock.readLock().unlock();
            lock.writeLock().lock();
            try {
                if (!initialized) {
                    log.info("Initializing views");
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

        String configName = environment.getProperty("jmix.viewsConfig");
        if (!StringUtils.isBlank(configName)) {
            Element rootElem = DocumentHelper.createDocument().addElement("views");

            StringTokenizer tokenizer = new StringTokenizer(configName);
            for (String fileName : tokenizer.getTokenArray()) {
                addFile(rootElem, fileName);
            }

            checkDuplicates(rootElem);

            for (Element viewElem : rootElem.elements("view")) {
                deployView(rootElem, viewElem, new HashSet<>());
            }
        }

//        initTiming.stop();
    }

    protected void checkDuplicates(Element rootElem) {
        Set<String> checked = new HashSet<>();
        for (Element viewElem : rootElem.elements("view")) {
            String viewName = getViewName(viewElem);
            String key = getMetaClass(viewElem) + "/" + viewName;
            if (!Boolean.parseBoolean(viewElem.attributeValue("overwrite"))) {
                String extend = viewElem.attributeValue("extends");
                if (extend != null) {
                    List<String> ancestors = splitExtends(extend);

                    if (!ancestors.contains(viewName) && checked.contains(key)) {
                        log.warn("Duplicate view definition without 'overwrite' attribute and not extending parent view: " + key);
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

        log.debug("Deploying views config: " + fileName);
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
                throw new RuntimeException("Unable to parse view file " + fileName, e);
            }
            Element rootElem = doc.getRootElement();

            for (Element includeElem : rootElem.elements("include")) {
                String incFile = includeElem.attributeValue("file");
                if (!StringUtils.isBlank(incFile))
                    addFile(commonRootElem, incFile);
            }

            for (Element viewElem : rootElem.elements("view")) {
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
     * Get View for an entity.
     *
     * @param entityClass entity class
     * @param name        view name
     * @return view instance. Throws {@link io.jmix.core.ViewNotFoundException} if not found.
     */
    @Override
    public View getView(Class<? extends Entity> entityClass, String name) {
        return getView(metadata.getClass(entityClass), name);
    }

    /**
     * Get View for an entity.
     *
     * @param metaClass entity class
     * @param name      view name
     * @return view instance. Throws {@link io.jmix.core.ViewNotFoundException} if not found.
     */
    @Override
    public View getView(MetaClass metaClass, String name) {
        Preconditions.checkNotNullArgument(metaClass, "MetaClass is null");

        View view = findView(metaClass, name);

        if (view == null) {
            throw new ViewNotFoundException(String.format("View %s/%s not found", metaClass.getName(), name));
        }
        return view;
    }

    /**
     * Searches for a View for an entity
     *
     * @param metaClass entity class
     * @param name      view name
     * @return view instance or null if no view found
     */
    @Override
    @Nullable
    public View findView(MetaClass metaClass, @Nullable String name) {
        if (metaClass == null) {
            throw new IllegalArgumentException("Passed metaClass should not be null");
        }

        if (name == null) {
            return null;
        }

        lock.readLock().lock();
        try {
            checkInitialized();

            View view = retrieveView(metaClass, name, new HashSet<>());
            return copyView(view);
        } finally {
            lock.readLock().unlock();
        }
    }

    protected View copyView(@Nullable View view) {
        if (view == null) {
            return null;
        }

        View.ViewParams viewParams = new View.ViewParams()
                .entityClass(view.getEntityClass())
                .name(view.getName());
        View copy = new View(viewParams);
        for (ViewProperty property : view.getProperties()) {
            copy.addProperty(property.getName(), copyView(property.getView()), property.getFetchMode());
        }

        return copy;
    }

    @Override
    public Collection<String> getViewNames(MetaClass metaClass) {
        Preconditions.checkNotNullArgument(metaClass, "MetaClass is null");
        lock.readLock().lock();
        try {
            checkInitialized();
            Map<String, View> viewMap = storage.get(metaClass);
            if (viewMap != null && !viewMap.isEmpty()) {
                Set<String> keySet = new HashSet<>(viewMap.keySet());
                keySet.remove(View.LOCAL);
                keySet.remove(View.MINIMAL);
                keySet.remove(View.BASE);
                return keySet;
            } else {
                return Collections.emptyList();
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Collection<String> getViewNames(Class<? extends Entity> entityClass) {
        Preconditions.checkNotNullArgument(entityClass, "entityClass is null");
        MetaClass metaClass = metadata.getClass(entityClass);
        return getViewNames(metaClass);
    }

    protected View deployDefaultView(MetaClass metaClass, String name, Set<ViewInfo> visited) {
        Class<? extends Entity> javaClass = metaClass.getJavaClass();

        ViewInfo info = new ViewInfo(javaClass, name);
        if (visited.contains(info)) {
            throw new DevelopmentException(String.format("Views cannot have cyclic references. View %s for class %s",
                    name, metaClass.getName()));
        }

        View view;
        if (View.LOCAL.equals(name)) {
            view = new View(javaClass, name, false);
            addAttributesToLocalView(metaClass, view);
        } else if (View.MINIMAL.equals(name)) {
            view = new View(javaClass, name, false);
            addAttributesToMinimalView(metaClass, view, info, visited);
        } else if (View.BASE.equals(name)) {
            view = new View(javaClass, name, false);
            addAttributesToMinimalView(metaClass, view, info, visited);
            addAttributesToLocalView(metaClass, view);
        } else {
            throw new UnsupportedOperationException("Unsupported default view: " + name);
        }

        storeView(metaClass, view);

        return view;
    }

    protected void addAttributesToLocalView(MetaClass metaClass, View view) {
        for (MetaProperty property : metaClass.getProperties()) {
            if (!property.getRange().isClass()
                    && !metadataTools.isSystem(property)
                    && metadataTools.isPersistent(property)) {
                view.addProperty(property.getName());
            }
        }
    }

    protected void addAttributesToMinimalView(MetaClass metaClass, View view, ViewInfo info, Set<ViewInfo> visited) {
        Collection<MetaProperty> metaProperties = metadataTools.getNamePatternProperties(metaClass, true);
        for (MetaProperty metaProperty : metaProperties) {
            if (metadataTools.isPersistent(metaProperty)) {
                addPersistentAttributeToMinimalView(metaClass, visited, info, view, metaProperty);
            } else {
                List<String> relatedProperties = metadataTools.getRelatedProperties(metaProperty);
                for (String relatedPropertyName : relatedProperties) {
                    MetaProperty relatedProperty = metaClass.getProperty(relatedPropertyName);
                    if (metadataTools.isPersistent(relatedProperty)) {
                        addPersistentAttributeToMinimalView(metaClass, visited, info, view, relatedProperty);
                    } else {
                        log.warn(
                                "Transient attribute '{}' is listed in 'related' properties of another transient attribute '{}'",
                                relatedPropertyName, metaProperty.getName());
                    }
                }
            }
        }
    }

    protected void addPersistentAttributeToMinimalView(MetaClass metaClass, Set<ViewInfo> visited, ViewInfo info, View view, MetaProperty metaProperty) {
        if (metaProperty.getRange().isClass()
                && !metaProperty.getRange().getCardinality().isMany()) {

            Map<String, View> views = storage.get(metaProperty.getRange().asClass());
            View refMinimalView = (views == null ? null : views.get(View.MINIMAL));

            if (refMinimalView != null) {
                view.addProperty(metaProperty.getName(), refMinimalView);
            } else {
                visited.add(info);
                View referenceMinimalView = deployDefaultView(metaProperty.getRange().asClass(), View.MINIMAL, visited);
                visited.remove(info);

                view.addProperty(metaProperty.getName(), referenceMinimalView);
            }
        } else {
            view.addProperty(metaProperty.getName());
        }
    }

    public void deployViews(String resourceUrl) {
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

            for (Element viewElem : rootElem.elements("view")) {
                deployView(rootElem, viewElem, new HashSet<>());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void deployViews(InputStream xml) {
        deployViews(new InputStreamReader(xml, StandardCharsets.UTF_8));
    }

    public void deployViews(Reader xml) {
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
                deployViews(file);
        }

        for (Element viewElem : rootElem.elements("view")) {
            deployView(rootElem, viewElem);
        }
    }

    protected View retrieveView(MetaClass metaClass, String name, Set<ViewInfo> visited) {
        Map<String, View> views = storage.get(metaClass);
        View view = (views == null ? null : views.get(name));
        if (view == null && (name.equals(View.LOCAL) || name.equals(View.MINIMAL) || name.equals(View.BASE))) {
            view = deployDefaultView(metaClass, name, visited);
        }
        return view;
    }

    public View deployView(Element rootElem, Element viewElem) {
        lock.writeLock().lock();
        try {
            return deployView(rootElem, viewElem, new HashSet<>());
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected View deployView(Element rootElem, Element viewElem, Set<ViewInfo> visited) {
        String viewName = getViewName(viewElem);
        MetaClass metaClass = getMetaClass(viewElem);

        ViewInfo info = new ViewInfo(metaClass.getJavaClass(), viewName);
        if (visited.contains(info)) {
            throw new DevelopmentException(String.format("Views cannot have cyclic references. View %s for class %s",
                    viewName, metaClass.getName()));
        }

        View v = retrieveView(metaClass, viewName, visited);
        boolean overwrite = Boolean.parseBoolean(viewElem.attributeValue("overwrite"));

        String extended = viewElem.attributeValue("extends");
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

        boolean systemProperties = Boolean.valueOf(viewElem.attributeValue("systemProperties"));

        View.ViewParams viewParam = new View.ViewParams().entityClass(metaClass.getJavaClass()).name(viewName);
        if (isNotEmpty(ancestors)) {
            List<View> ancestorsViews = ancestors.stream()
                    .map(a -> getAncestorView(metaClass, a, visited))
                    .collect(Collectors.toList());

            viewParam.src(ancestorsViews);
        }
        viewParam.includeSystemProperties(systemProperties);
        View view = new View(viewParam);

        visited.add(info);
        loadView(rootElem, viewElem, view, systemProperties, visited);
        visited.remove(info);

        storeView(metaClass, view);

        if (overwrite) {
            replaceOverridden(view);
        }

        return view;
    }

    protected void replaceOverridden(View replacementView) {
        // todo perf4j
//        StopWatch replaceTiming = new Slf4JStopWatch("ViewRepository.replaceOverridden");

        HashSet<View> checked = new HashSet<>();

        for (View view : getAllInitialized()) {
            if (!checked.contains(view)) {
                replaceOverridden(view, replacementView, checked);
            }
        }

//        replaceTiming.stop();
    }

    protected void replaceOverridden(View root, View replacementView, HashSet<View> checked) {
        checked.add(root);

        List<ViewProperty> replacements = null;

        for (ViewProperty property : root.getProperties()) {
            View propertyView = property.getView();

            if (propertyView != null) {
                if (Objects.equals(propertyView.getName(), replacementView.getName())
                        && replacementView.getEntityClass() == propertyView.getEntityClass()) {
                    if (replacements == null) {
                        replacements = new LinkedList<>();
                    }
                    replacements.add(new ViewProperty(property.getName(), replacementView, property.getFetchMode()));
                } else if (propertyView.getEntityClass() != null && !checked.contains(propertyView)) {
                    replaceOverridden(propertyView, replacementView, checked);
                }
            }
        }

        if (replacements != null) {
            for (ViewProperty replacement : replacements) {
                root.addProperty(replacement.getName(), replacement.getView(), replacement.getFetchMode());
            }
        }
    }

    protected View getAncestorView(MetaClass metaClass, String ancestor, Set<ViewInfo> visited) {
        View ancestorView = retrieveView(metaClass, ancestor, visited);
        if (ancestorView == null) {
            MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
            if (originalMetaClass != null) {
                ancestorView = retrieveView(originalMetaClass, ancestor, visited);
            }
            if (ancestorView == null) {
                // Last resort - search for all ancestors
                for (MetaClass ancestorMetaClass : metaClass.getAncestors()) {
                    if (ancestorMetaClass.equals(metaClass)) {
                        ancestorView = retrieveView(ancestorMetaClass, ancestor, visited);
                        if (ancestorView != null)
                            break;
                    }
                }
            }
            if (ancestorView == null) {
                throw new DevelopmentException("No ancestor view found: " + ancestor + " for " + metaClass.getName());
            }
        }
        return ancestorView;
    }

    protected void loadView(Element rootElem, Element viewElem, View view, boolean systemProperties, Set<ViewInfo> visited) {
        final MetaClass metaClass = metadata.getClass(view.getEntityClass());
        final String viewName = view.getName();

        Set<String> propertyNames = new HashSet<>();

        for (Element propElem : viewElem.elements("property")) {
            String propertyName = propElem.attributeValue("name");

            if (propertyNames.contains(propertyName)) {
                throw new DevelopmentException(String.format("View %s/%s definition error: view declared property %s twice",
                        metaClass.getName(), viewName, propertyName));
            }
            propertyNames.add(propertyName);

            MetaProperty metaProperty = metaClass.findProperty(propertyName);
            if (metaProperty == null) {
                throw new DevelopmentException(String.format("View %s/%s definition error: property %s doesn't exist",
                        metaClass.getName(), viewName, propertyName));
            }

            View refView = null;
            String refViewName = propElem.attributeValue("view");

            MetaClass refMetaClass;
            Range range = metaProperty.getRange();
            if (range == null) {
                throw new RuntimeException("cannot find range for meta property: " + metaProperty);
            }

            final List<Element> propertyElements = propElem.elements("property");
            boolean inlineView = !propertyElements.isEmpty();

            if (!range.isClass() && (refViewName != null || inlineView)) {
                throw new DevelopmentException(String.format("View %s/%s definition error: property %s is not an entity",
                        metaClass.getName(), viewName, propertyName));
            }

            if (refViewName != null) {
                refMetaClass = getMetaClass(propElem, range);

                refView = retrieveView(refMetaClass, refViewName, visited);
                if (refView == null) {
                    for (Element e : rootElem.elements("view")) {
                        if (refMetaClass.equals(getMetaClass(e.attributeValue("entity"), e.attributeValue("class")))
                                && refViewName.equals(e.attributeValue("name"))) {
                            refView = deployView(rootElem, e, visited);
                            break;
                        }
                    }

                    if (refView == null) {
                        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(refMetaClass);
                        if (originalMetaClass != null) {
                            refView = retrieveView(originalMetaClass, refViewName, visited);
                        }
                    }

                    if (refView == null) {
                        throw new DevelopmentException(
                                String.format("View %s/%s definition error: unable to find/deploy referenced view %s/%s",
                                        metaClass.getName(), viewName, range.asClass().getName(), refViewName));
                    }
                }
            }

            if (inlineView) {
                // try to import anonymous views
                Class<? extends Entity> rangeClass = range.asClass().getJavaClass();

                if (refView != null) {
                    refView = new View(refView, rangeClass, "", false); // system properties are already in the source view
                } else {
                    ViewProperty existingProperty = view.getProperty(propertyName);
                    if (existingProperty != null && existingProperty.getView() != null) {
                        refView = new View(existingProperty.getView(), rangeClass, "", systemProperties);
                    } else {
                        refView = new View(rangeClass, systemProperties);
                    }
                }
                loadView(rootElem, propElem, refView, systemProperties, visited);
            }

            FetchMode fetchMode = FetchMode.AUTO;
            String fetch = propElem.attributeValue("fetch");
            if (fetch != null)
                fetchMode = FetchMode.valueOf(fetch);

            view.addProperty(propertyName, refView, fetchMode);
        }
    }

    protected String getViewName(Element viewElem) {
        String viewName = viewElem.attributeValue("name");
        if (StringUtils.isBlank(viewName))
            throw new DevelopmentException("Invalid view definition: no 'name' attribute present");
        return viewName;
    }

    protected MetaClass getMetaClass(Element viewElem) {
        MetaClass metaClass;
        String entity = viewElem.attributeValue("entity");
        if (StringUtils.isBlank(entity)) {
            String className = viewElem.attributeValue("class");
            if (StringUtils.isBlank(className))
                throw new DevelopmentException("Invalid view definition: no 'entity' or 'class' attribute present");
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

    protected void storeView(MetaClass metaClass, View view) {
        Map<String, View> views = storage.get(metaClass);
        if (views == null) {
            views = new ConcurrentHashMap<>();
        }

        views.put(view.getName(), view);
        storage.put(metaClass, views);
    }

    protected List<View> getAllInitialized() {
        List<View> list = new ArrayList<>();
        for (Map<String, View> viewMap : storage.values()) {
            list.addAll(viewMap.values());
        }
        return list;
    }

    public List<View> getAll() {
        lock.readLock().lock();
        try {
            checkInitialized();
            List<View> list = new ArrayList<>();
            for (Map<String, View> viewMap : storage.values()) {
                list.addAll(viewMap.values());
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }

    protected static class ViewInfo {
        protected Class javaClass;
        protected String name;

        public ViewInfo(Class javaClass, String name) {
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
            if (!(obj instanceof ViewInfo)) {
                return false;
            }

            ViewInfo that = (ViewInfo) obj;
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
