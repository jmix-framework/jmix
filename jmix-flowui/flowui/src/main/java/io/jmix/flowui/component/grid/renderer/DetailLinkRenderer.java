/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.grid.renderer;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.router.RouteData;
import com.vaadin.flow.router.RouteParameters;
import io.jmix.core.DevelopmentException;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import io.jmix.flowui.view.navigation.RouteSupport;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public class DetailLinkRenderer<E> extends AbstractDetailRenderer<Anchor, E, DetailLinkRenderer<E>> {

    protected UiComponents uiComponents;
    protected RouteSupport routeSupport;
    protected ViewRegistry viewRegistry;

    protected SerializableFunction<E, String> hrefProvider;

    protected AnchorTarget target;
    protected MetaClass metaClass;

    public DetailLinkRenderer(UiComponents uiComponents, RouteSupport routeSupport, ViewRegistry viewRegistry,
                              MetaClass metaClass, ValueProvider<E, String> textValueProvider) {
        super(textValueProvider);

        this.uiComponents = uiComponents;
        this.routeSupport = routeSupport;
        this.viewRegistry = viewRegistry;
        this.metaClass = metaClass;

        initRenderer();
    }

    protected void initRenderer() {
        hrefProvider = this::createHref;
    }

    @Override
    protected Anchor createComponentInternal() {
        Anchor anchor = uiComponents.create(Anchor.class);
        if (target != null) {
            anchor.setTarget(target);
        }
        return anchor;
    }

    @Override
    protected void configureComponent(Anchor anchor, E item) {
        anchor.setHref(hrefProvider.apply(item));
    }

    protected String createHref(Object item) {
        Object id = Objects.requireNonNull(EntityValues.getId(item),
                "Detail link renderer item id must not be null");

        Class<? extends View<?>> detailViewClass = resolveDetailViewClass();
        RouteConfiguration routeConfiguration = RouteConfiguration.forSessionScope();
        RouteData routeData = findRouteData(routeConfiguration, detailViewClass);

        String routeParameter = getSingleRouteParameterName(routeData, detailViewClass);
        RouteParameters parameters = routeSupport.createRouteParameters(routeParameter, id);
        return routeConfiguration.getUrl(detailViewClass, parameters);
    }

    protected RouteData findRouteData(RouteConfiguration routeConfiguration, Class<? extends View<?>> detailViewClass) {
        return routeConfiguration.getAvailableRoutes()
                .stream()
                .filter(data -> Objects.equals(data.getNavigationTarget(), detailViewClass))
                .findAny()
                .orElseThrow(() -> new DevelopmentException(
                        "Detail view route is not registered",
                        Map.of("View class", detailViewClass.getName())));
    }

    protected String getSingleRouteParameterName(RouteData routeData, Class<? extends View<?>> detailViewClass) {
        Map<String, ?> routeParameters = routeData.getRouteParameters();
        if (routeParameters.size() != 1) {
            throw new DevelopmentException(
                    "Detail view route must contain exactly one route parameter. " +
                            "Use custom href provider to support routes with multiple parameters.",
                    Map.of(
                            "View class", detailViewClass.getName(),
                            "Route template", routeData.getTemplate()));
        }

        return routeParameters.keySet().iterator().next();
    }

    protected Class<? extends View<?>> resolveDetailViewClass() {
        if (viewClass != null) {
            return viewClass;
        }

        if (viewId != null) {
            return viewRegistry.getViewInfo(viewId).getControllerClass();
        }

        ViewInfo viewInfo = viewRegistry.getDetailViewInfo(metaClass);
        return viewInfo.getControllerClass();
    }

    @Nullable
    public AnchorTarget getTarget() {
        return target;
    }

    public DetailLinkRenderer<E> withTarget(@Nullable AnchorTarget target) {
        this.target = target;
        return this;
    }

    public SerializableFunction<E, String> getHrefProvider() {
        return hrefProvider;
    }

    public DetailLinkRenderer<E> withHrefProvider(SerializableFunction<E, String> hrefProvider) {
        this.hrefProvider = hrefProvider;
        return this;
    }
}
