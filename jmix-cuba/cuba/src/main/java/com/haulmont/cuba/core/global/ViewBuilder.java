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

import io.jmix.core.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * @deprecated use only in legacy CUBA code. In new code, use {@link FetchPlanBuilder}.
 */
@Deprecated
public class ViewBuilder extends FetchPlanBuilder {

    public static ViewBuilder of(Class<? extends Entity> entityClass) {
        return AppBeans.getPrototype(ViewBuilderFactory.class).builder(entityClass);
    }

    protected ViewBuilder(ViewBuilderFactory factory, Class<? extends Entity> entityClass) {
        super(factory, entityClass);
    }

    @Override
    protected FetchPlan createFetchPlan(Class<?> entityClass, String name, List<FetchPlanProperty> properties, boolean loadPartialEntities) {
        return new View((Class<Entity>) entityClass, name, properties, loadPartialEntities);
    }

    public ViewBuilder add(String property) {
        super.add(property);
        return this;
    }

    public ViewBuilder add(String property, Consumer<FetchPlanBuilder> consumer) {
        super.add(property, consumer);
        return this;
    }

    public ViewBuilder add(String property, String fetchPlanName) {
        super.add(property, fetchPlanName);
        return this;
    }

    public ViewBuilder add(String property, String fetchPlanName, FetchMode fetchMode) {
        super.add(property, fetchPlanName, fetchMode);
        return this;
    }

    public ViewBuilder addAll(String... properties) {
        super.addAll(properties);
        return this;
    }

    public ViewBuilder addSystem() {
        super.addSystem();
        return this;
    }

    public ViewBuilder addView(FetchPlan view) {
        super.addFetchPlan(view);
        return this;
    }

    public ViewBuilder addView(String viewName) {
        super.addFetchPlan(viewName);
        return this;
    }

    @Override
    public ViewBuilder partial() {
        super.partial();
        return this;
    }

    @Override
    public ViewBuilder partial(boolean partial) {
        super.partial(partial);
        return this;
    }

    @Override
    public ViewBuilder name(String name) {
        super.name(name);
        return this;
    }

}
