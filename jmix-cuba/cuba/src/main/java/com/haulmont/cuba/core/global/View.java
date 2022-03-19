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
package com.haulmont.cuba.core.global;

import com.haulmont.chile.core.annotations.NamePattern;
import io.jmix.core.Metadata;
import io.jmix.core.*;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;
import java.util.*;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

/**
 * Class to declare a graph of objects that must be retrieved from the database.
 * <p>
 * A view can be constructed in Java code or defined in XML and deployed
 * to the {@link FetchPlanRepository} for recurring usage.
 * </p>
 * There are the following predefined view types:
 * <ul>
 * <li>{@link #LOCAL}</li>
 * <li>{@link #MINIMAL}</li>
 * <li>{@link #BASE}</li>
 * </ul>
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link FetchPlans} to build {@link FetchPlan}.
 */
@Deprecated
public class View extends FetchPlan {

    /**
     * Parameters object to be used in constructors.
     */
    public static class ViewParams {
        protected List<View> src = Collections.emptyList();
        protected Class<? extends Entity> entityClass;
        protected String name;
        protected boolean includeSystemProperties;

        public ViewParams src(View src) {
            this.src = Collections.singletonList(src);
            return this;
        }

        public void src(List<View> sources) {
            this.src = sources;
        }

        public ViewParams entityClass(Class<? extends Entity> entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public ViewParams name(String name) {
            this.name = name;
            return this;
        }

        public ViewParams includeSystemProperties(boolean includeSystemProperties) {
            this.includeSystemProperties = includeSystemProperties;
            return this;
        }
    }

    /**
     * Includes all local non-system properties.
     */
    public static final String LOCAL = FetchPlan.LOCAL;

    /**
     * A synonym for {@link #INSTANCE_NAME}. Left for backward compatibility
     */
    public static final String MINIMAL = "_minimal";

    /**
     * Includes only properties contained in {@link NamePattern}.
     */
    public static final String INSTANCE_NAME = FetchPlan.INSTANCE_NAME;

    /**
     * Includes all local non-system properties and properties defined by {@link NamePattern}
     * (effectively {@link #MINIMAL} + {@link #LOCAL}).
     */
    public static final String BASE = FetchPlan.BASE;

    private static final long serialVersionUID = 4313784222934349594L;

    public View(Class<? extends Entity> entityClass) {
        this(new ViewParams().entityClass(entityClass));
    }

    public View(Class<? extends Entity> entityClass, boolean includeSystemProperties) {
        this(new ViewParams().entityClass(entityClass).includeSystemProperties(includeSystemProperties));
    }

    public View(Class<? extends Entity> entityClass, String name) {
        this(new ViewParams().entityClass(entityClass).name(name));
    }

    public View(Class<? extends Entity> entityClass, String name, boolean includeSystemProperties) {
        this(new ViewParams().entityClass(entityClass).name(name).includeSystemProperties(includeSystemProperties));
    }

    public View(View src, String name, boolean includeSystemProperties) {
        this(new ViewParams().src(src).name(name).includeSystemProperties(includeSystemProperties));
    }

    public View(View src, @Nullable Class<? extends Entity> entityClass, String name, boolean includeSystemProperties) {
        this(new ViewParams().src(src).entityClass(entityClass).name(name).includeSystemProperties(includeSystemProperties));
    }

    public View(Class<? extends Entity> entityClass, String name, List<FetchPlanProperty> properties, boolean loadPartialEntities) {
        super(entityClass, name);
        this.loadPartialEntities = loadPartialEntities;

        for (FetchPlanProperty property : properties) {
            this.properties.put(property.getName(), property);
        }
    }


    public View(ViewParams viewParams) {
        super(viewParams.entityClass, viewParams.name);

        if (viewParams.includeSystemProperties)
            addSystemProperties();

        List<View> sources = viewParams.src;

        if (isNotEmpty(sources)) {
            if (this.entityClass == null) {
                this.entityClass = sources.get(0).getEntityClass();
            }

            for (FetchPlan view : sources) {
                putProperties(this.properties, view.getProperties());
            }
        }
    }

    //move to static helper in io.jmix.core and provide package local method to get map in case of problems with cast to View
    protected static void putProperties(Map<String, FetchPlanProperty> thisProperties, Collection<FetchPlanProperty> sourceProperties) {
        for (FetchPlanProperty sourceProperty : sourceProperties) {
            String sourcePropertyName = sourceProperty.getName();

            if (thisProperties.containsKey(sourcePropertyName)) {
                View thisPropertyView = (View) thisProperties.get(sourcePropertyName).getFetchPlan();
                if (thisPropertyView != null) {
                    FetchPlan sourcePropertyView = sourceProperty.getFetchPlan();
                    if (sourcePropertyView != null && isNotEmpty(sourcePropertyView.getProperties())) {
                        Map<String, FetchPlanProperty> thisViewProperties = thisPropertyView.properties;
                        putProperties(thisViewProperties, sourcePropertyView.getProperties());
                    }
                } else {
                    thisProperties.put(sourceProperty.getName(), sourceProperty);
                }
            } else {
                thisProperties.put(sourceProperty.getName(), sourceProperty);
            }
        }
    }


    /**
     * Add a property to this view.
     *
     * @param name      property name
     * @param view      a view for a reference attribute, or null
     * @param fetchMode fetch mode for a reference attribute
     * @return this view instance for chaining
     */
    public View addProperty(String name, @Nullable FetchPlan view, FetchMode fetchMode) {
        properties.put(name, new FetchPlanProperty(name, view, fetchMode));
        return this;
    }

    @Deprecated
    public View addProperty(String name, @Nullable FetchPlan view, boolean lazy) {
        properties.put(name, new FetchPlanProperty(name, view));
        return this;
    }

    /**
     * Add a property to this view.
     *
     * @param name property name
     * @param view a view for a reference attribute, or null
     * @return this view instance for chaining
     */
    public View addProperty(String name, FetchPlan view) {
        properties.put(name, new FetchPlanProperty(name, view));
        return this;
    }

    /**
     * Add a property to this view.
     *
     * @param name property name
     * @return this view instance for chaining
     */
    public View addProperty(String name) {
        properties.put(name, new FetchPlanProperty(name, null));
        return this;
    }

    /**
     * Specifies whether the view affects loading of local attributes. By default only reference attributes are affected and
     * local are always loaded.
     *
     * @param loadPartialEntities true to affect loading of local attributes
     * @return this view instance for chaining
     */
    public View setLoadPartialEntities(boolean loadPartialEntities) {
        this.loadPartialEntities = loadPartialEntities;
        return this;
    }

    public View addSystemProperties() {
        io.jmix.core.Metadata metadata = AppBeans.get(Metadata.class);
        MetadataTools metadataTools = AppBeans.get(MetadataTools.class);
        MetaClass metaClass = metadata.getClass(getEntityClass());
        for (String propertyName : metadataTools.getSystemProperties(metaClass)) {
            addProperty(propertyName);
        }
        return this;
    }


    public static View copy(View fetchPlan) {
        Preconditions.checkNotNullArgument(fetchPlan, "fetchPlan is null");
        return new View((Class<Entity>) fetchPlan.getEntityClass(),
                fetchPlan.getName(),
                new LinkedList<>(fetchPlan.getProperties()),
                fetchPlan.loadPartialEntities());
    }

}
