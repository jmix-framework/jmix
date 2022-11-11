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

package io.jmix.flowui.facet.impl;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import io.jmix.core.DevelopmentException;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.JpqlCondition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.DataLoadCoordinator;
import io.jmix.flowui.facet.dataloadcoordinator.OnComponentValueChangedLoadTrigger;
import io.jmix.flowui.facet.dataloadcoordinator.OnContainerItemChangedLoadTrigger;
import io.jmix.flowui.facet.dataloadcoordinator.OnViewEventLoadTrigger;
import io.jmix.flowui.model.DataLoader;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.model.ViewData;
import io.jmix.flowui.model.impl.DataLoadersHelper;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.View.BeforeShowEvent;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.sys.ViewControllerReflectionInspector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DataLoadCoordinatorImpl extends AbstractFacet implements DataLoadCoordinator {

    protected static final Pattern LIKE_PATTERN = Pattern.compile("\\s+like\\s+:([\\w$]+)");

    protected String containerPrefix = DEFAULT_CONTAINER_PREFIX;
    protected String componentPrefix = DEFAULT_COMPONENT_PREFIX;

    protected List<Trigger> triggers = new ArrayList<>();

    protected ViewControllerReflectionInspector reflectionInspector;

    public DataLoadCoordinatorImpl(ViewControllerReflectionInspector reflectionInspector) {
        this.reflectionInspector = reflectionInspector;
    }

    @Override
    public void setContainerPrefix(String value) {
        containerPrefix = value;
    }

    @Override
    public void setComponentPrefix(String value) {
        componentPrefix = value;
    }

    @Override
    public List<Trigger> getTriggers() {
        return Collections.unmodifiableList(triggers);
    }

    @Override
    public void addOnViewEventLoadTrigger(DataLoader loader, Class<?> eventClass) {
        triggers.add(new OnViewEventLoadTrigger(getOwnerNN(), reflectionInspector, loader, eventClass));
    }

    @Override
    public void addOnContainerItemChangedLoadTrigger(DataLoader loader, InstanceContainer<?> container,
                                                     @Nullable String param) {
        String nonNullParam = param != null
                ? param
                : findSingleParam(loader);

        OnContainerItemChangedLoadTrigger loadTrigger = new OnContainerItemChangedLoadTrigger(loader,
                container, nonNullParam);

        triggers.add(loadTrigger);
    }

    @Override
    public void addOnComponentValueChangedLoadTrigger(DataLoader loader, Component component, @Nullable String param,
                                                      LikeClause likeClause) {
        String nonNullParam = param != null
                ? param
                : findSingleParam(loader);

        OnComponentValueChangedLoadTrigger loadTrigger = new OnComponentValueChangedLoadTrigger(
                loader, component, nonNullParam, likeClause);

        triggers.add(loadTrigger);
    }

    @Override
    public void configureAutomatically() {
        View<?> owner = getOwnerNN();
        ViewData viewData = ViewControllerUtils.getViewData(owner);

        getUnconfiguredLoaders(viewData).forEach(loader -> configureAutomatically(loader, owner));
    }

    protected Stream<DataLoader> getUnconfiguredLoaders(ViewData viewData) {
        return viewData.getLoaderIds().stream()
                .map(viewData::<DataLoader>getLoader)
                .distinct()
                .filter(this::loaderIsNotConfiguredYet);
    }

    protected boolean loaderIsNotConfiguredYet(DataLoader loader) {
        return triggers.stream()
                .map(Trigger::getLoader)
                .noneMatch(configuredLoader -> configuredLoader == loader);
    }

    protected void configureAutomatically(DataLoader loader, View<?> view) {
        List<String> queryParameters = DataLoadersHelper.getQueryParameters(loader);
        List<String> allParameters = new ArrayList<>(queryParameters);
        allParameters.addAll(getConditionParameters(loader));

        // add triggers on container/component events
        for (String parameter : allParameters) {
            if (parameter.startsWith(containerPrefix)) {
                InstanceContainer<?> container = ViewControllerUtils.getViewData(view)
                        .getContainer(parameter.substring(containerPrefix.length()));

                addOnContainerItemChangedLoadTrigger(loader, container, parameter);

            } else if (parameter.startsWith(componentPrefix)) {
                String componentId = parameter.substring(componentPrefix.length());
                Component component = UiComponentUtils.getComponent(view, componentId);
                LikeClause likeClause = findLikeClause(loader, parameter);

                addOnComponentValueChangedLoadTrigger(loader, component, parameter, likeClause);
            }
        }

        // if the loader has no parameters in query, add trigger on BeforeShowEvent
        if (queryParameters.isEmpty()) {
            addOnViewEventLoadTrigger(loader, BeforeShowEvent.class);
        }
    }

    protected boolean containsLikeClause(Condition condition, String parameter) {
        if (condition instanceof JpqlCondition) {
            String where = ((JpqlCondition) condition).getWhere();
            Matcher matcher = LIKE_PATTERN.matcher(where);
            while (matcher.find()) {
                if (matcher.group(1).equals(parameter)) {
                    return true;
                }
            }
        } else if (condition instanceof LogicalCondition) {
            for (Condition nestedCondition : ((LogicalCondition) condition).getConditions()) {
                if (containsLikeClause(nestedCondition, parameter))
                    return true;
            }
        }
        return false;
    }

    protected List<String> getConditionParameters(DataLoader loader) {
        List<String> parameters = new ArrayList<>();

        Condition condition = loader.getCondition();
        if (condition != null) {
            parameters.addAll(condition.getParameters());
        }

        return parameters;
    }

    protected String findSingleParam(DataLoader loader) {
        List<String> parameters = DataLoadersHelper.getQueryParameters(loader);
        parameters.addAll(getConditionParameters(loader));

        if (parameters.isEmpty()) {
            throw new DevelopmentException("Cannot find a query parameter for " +
                    "onContainerItemChanged load trigger.\nQuery: " + loader.getQuery());
        }

        if (parameters.size() > 1) {
            throw new DevelopmentException("There is more than one query parameter for " +
                    "onContainerItemChanged load trigger. Specify the parameter name in " +
                    "the 'param' attribute.\nQuery: " + loader.getQuery());
        }

        return parameters.get(0);
    }

    protected LikeClause findLikeClause(DataLoader loader, String parameter) {
        if (!Strings.isNullOrEmpty(loader.getQuery())) {
            if (containsLikeClause(loader.getQuery(), parameter)) {
                return LikeClause.CASE_INSENSITIVE;
            }
        }
        if (loader.getCondition() != null) {
            if (containsLikeClause(loader.getCondition(), parameter)) {
                return LikeClause.CASE_INSENSITIVE;
            }
        }
        return LikeClause.NONE;
    }

    protected boolean containsLikeClause(String query, String parameter) {
        Matcher matcher = LIKE_PATTERN.matcher(query);
        while (matcher.find()) {
            if (matcher.group(1).equals(parameter)) {
                return true;
            }
        }
        return false;
    }

    protected View<?> getOwnerNN() {
        View<?> view = getOwner();
        if (view == null) {
            throw new IllegalStateException("Owner view is null");
        }

        return view;
    }
}
