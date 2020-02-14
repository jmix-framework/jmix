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

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.entity.Entity;

import javax.annotation.Nullable;

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
 */
public class View extends FetchPlan {

    /**
     * Parameters object to be used in constructors.
     */
    public static class ViewParams extends FetchPlanParams {
    }

    /**
     * Includes all local non-system properties.
     */
    public static final String LOCAL = FetchPlan.LOCAL;

    /**
     * Includes only properties contained in {@link io.jmix.core.metamodel.annotations.NamePattern}.
     */
    public static final String MINIMAL = FetchPlan.MINIMAL;

    /**
     * Includes all local non-system properties and properties defined by {@link io.jmix.core.metamodel.annotations.NamePattern}
     * (effectively {@link #MINIMAL} + {@link #LOCAL}).
     */
    public static final String BASE = FetchPlan.BASE;

    private static final long serialVersionUID = 4313784222934349594L;

    public View(Class<? extends Entity> entityClass) {
        super(entityClass);
    }

    public View(Class<? extends Entity> entityClass, boolean includeSystemProperties) {
        super(entityClass, includeSystemProperties);
    }

    public View(Class<? extends Entity> entityClass, String name) {
        super(entityClass, name);
    }

    public View(Class<? extends Entity> entityClass, String name, boolean includeSystemProperties) {
        super(entityClass, name, includeSystemProperties);
    }

    public View(View src, String name, boolean includeSystemProperties) {
        super(src, name, includeSystemProperties);
    }

    public View(View src, @Nullable Class<? extends Entity> entityClass, String name, boolean includeSystemProperties) {
        super(src, entityClass, name, includeSystemProperties);
    }

    public View(ViewParams viewParams) {
        super(viewParams);
    }
}
