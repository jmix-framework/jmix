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
import io.jmix.core.JmixEntity;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;

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
     *  A synonym for {@link #INSTANCE_NAME}. Left for backward compatibility
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

    public View(Class<? extends JmixEntity> entityClass) {
        super(entityClass);
    }

    public View(Class<? extends JmixEntity> entityClass, boolean includeSystemProperties) {
        super(entityClass, includeSystemProperties);
    }

    public View(Class<? extends JmixEntity> entityClass, String name) {
        super(entityClass, name);
    }

    public View(Class<? extends JmixEntity> entityClass, String name, boolean includeSystemProperties) {
        super(entityClass, name, includeSystemProperties);
    }

    public View(View src, String name, boolean includeSystemProperties) {
        super(src, name, includeSystemProperties);
    }

    public View(View src, @Nullable Class<? extends JmixEntity> entityClass, String name, boolean includeSystemProperties) {
        super(src, entityClass, name, includeSystemProperties);
    }

    public View(ViewParams viewParams) {
        super(viewParams);
    }
}
