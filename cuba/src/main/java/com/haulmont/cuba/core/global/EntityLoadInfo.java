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

import io.jmix.core.Entity;
import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;

/**
 * Class that encapsulates an information needed to load an entity instance.
 * <br>
 * This information has the following string representation:
 * {@code metaclassName-id[-fetchPlanName]}, e.g.:
 * <pre>
 * sec$User-60885987-1b61-4247-94c7-dff348347f93
 * sec$Role-0c018061-b26f-4de2-a5be-dff348347f93-role.browse
 * ref$Seller-101
 * ref$Currency-{usd}
 * </pre>
 * <br> fetch plan name part is optional.
 * <br> id part should be:
 * <ul>
 *     <li>For UUID keys: canonical UUID representation with 5 groups of hex digits delimited by dashes</li>
 *     <li>For numeric keys: decimal representation of the number</li>
 *     <li>For string keys: the key surrounded by curly brackets, e.g {mykey}</li>
 * </ul>
 * Use {@link EntityLoadInfoBuilder#parse(String)} and {@link #toString()} methods to convert from/to a string.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.IdSerialization}.
 */
@Deprecated
public class EntityLoadInfo {

    public static final String NEW_PREFIX = "NEW-";

    private MetaClass metaClass;
    private Object id;
    private String fetchPlanName;
    private boolean newEntity;
    private boolean stringKey;

    protected EntityLoadInfo(Object id, MetaClass metaClass, String fetchPlanName, boolean stringKey) {
        this(id, metaClass, fetchPlanName, stringKey, false);
    }

    protected EntityLoadInfo(Object id, MetaClass metaClass, String fetchPlanName, boolean stringKey, boolean newEntity) {
        this.id = id;
        this.metaClass = metaClass;
        this.fetchPlanName = fetchPlanName;
        this.newEntity = newEntity;
        this.stringKey = stringKey;
    }

    public Object getId() {
        return id;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    @Nullable
    @Deprecated
    public String getViewName() {
        return getFetchPlanName();
    }

    public String getFetchPlanName() {
        return fetchPlanName;
    }

    public boolean isNewEntity() {
        return newEntity;
    }

    /**
     * Create a new info instance.
     * <p>Consider using {@link EntityLoadInfoBuilder} for better performance.</p>
     * @param entity    entity instance
     * @param fetchPlanName  fetch plan name, can be null
     * @return          info instance
     */
    public static EntityLoadInfo create(Entity entity, @Nullable String fetchPlanName) {
        EntityLoadInfoBuilder builder = AppBeans.get(EntityLoadInfoBuilder.NAME);
        return builder.create(entity, fetchPlanName);
    }

    /**
     * Create a new info instance with empty fetch plan.
     * <p>Consider using {@link EntityLoadInfoBuilder} for better performance.</p>
     * @param entity    entity instance
     * @return          info instance
     */
    public static EntityLoadInfo create(Entity entity) {
        return create(entity, null);
    }

    /**
     * Parse an info from the string.
     * <p>Consider using {@link EntityLoadInfoBuilder} for better performance.</p>
     * @param str   string representation of the info
     * @return      info instance or null if the string can not be parsed. Any exception is silently swallowed.
     */
    public static @Nullable
    EntityLoadInfo parse(String str) {
        EntityLoadInfoBuilder builder = AppBeans.get(EntityLoadInfoBuilder.NAME);
        return builder.parse(str);
    }

    @Override
    public String toString() {
        String key = stringKey ? "{" + id + "}" : id.toString();
        return metaClass.getName() + "-" + key + (fetchPlanName == null ? "" : "-" + fetchPlanName);
    }
}
