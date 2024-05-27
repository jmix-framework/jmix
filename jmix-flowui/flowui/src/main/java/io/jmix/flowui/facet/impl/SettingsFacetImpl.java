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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.settings.SettingsFacetUrlQueryParametersHelper;
import io.jmix.flowui.facet.settings.ViewSettings;
import io.jmix.flowui.facet.settings.ViewSettingsJson;
import io.jmix.flowui.facet.settings.ViewSettingsComponentManager;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.sys.ViewControllerReflectionInspector;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.ViewFacets;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SettingsFacetImpl extends AbstractFacet implements SettingsFacet {

    private static final Logger log = LoggerFactory.getLogger(SettingsFacetImpl.class);

    protected SettingsFacetUrlQueryParametersHelper settingsHelper;
    protected ViewControllerReflectionInspector reflectionInspector;
    protected ViewSettingsComponentManager settingsManager;
    protected UserSettingsCache userSettingsCache;

    protected Set<String> componentIds;
    protected Set<String> excludedComponentIds;

    protected ViewSettings viewSettings;

    protected boolean auto = false;

    protected ViewEventListener beforeShowListener;
    protected ViewEventListener readyListener;
    protected ViewEventListener detachListener;

    protected Consumer<SettingsContext> applySettingsDelegate;
    protected Consumer<SettingsContext> applyDataLoadingSettingsDelegate;
    protected Consumer<SettingsContext> saveSettingsDelegate;

    protected QueryParameters viewQueryParameters;

    public SettingsFacetImpl(SettingsFacetUrlQueryParametersHelper settingsHelper,
                             ViewControllerReflectionInspector reflectionInspector,
                             @Autowired(required = false) UserSettingsCache userSettingsCache,
                             @Autowired(required = false) ViewSettingsComponentManager settingsManager) {
        this.settingsHelper = settingsHelper;
        this.reflectionInspector = reflectionInspector;
        this.settingsManager = settingsManager;
        this.userSettingsCache = userSettingsCache;
    }

    @Override
    public boolean isAuto() {
        return auto;
    }

    @Override
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    @Override
    public ViewSettings getSettings() {
        return viewSettings;
    }

    @Override
    public void applySettings() {
        Collection<Component> components = getManagedComponents();

        components = excludeUrlQueryParametersFacetComponents(components);

        applyViewSettings(components);
    }

    @Override
    public void applyDataLoadingSettings() {
        Collection<Component> components = getManagedComponents();

        components = excludeUrlQueryParametersFacetComponents(components);

        applyDataLoadingViewSettings(components);
    }

    @Override
    public void saveSettings() {
        Collection<Component> components = getManagedComponents();

        saveViewSettings(components);
    }

    @Override
    public void addComponentIds(String... ids) {
        if (componentIds == null) {
            componentIds = new HashSet<>();
        }
        componentIds.addAll(List.of(ids));
    }

    @Override
    public Set<String> getComponentIds() {
        if (componentIds == null) {
            return Collections.emptySet();
        }
        return componentIds;
    }

    @Override
    public void addExcludedComponentIds(String... ids) {
        if (excludedComponentIds == null) {
            excludedComponentIds = new HashSet<>();
        }
        excludedComponentIds.addAll(List.of(ids));
    }

    @Override
    public Set<String> getExcludedComponentIds() {
        return Objects.requireNonNullElse(excludedComponentIds, Collections.emptySet());
    }

    @Override
    public Collection<Component> getManagedComponents() {
        checkAttachedToView();

        Collection<Component> viewComponents = UiComponentUtils.getComponents(
                Objects.requireNonNull(getView()).getContent());

        return getManagedComponentsFromCollection(viewComponents);
    }

    @Nullable
    @Override
    public Consumer<SettingsContext> getApplySettingsDelegate() {
        return applySettingsDelegate;
    }

    @Override
    public void setApplySettingsDelegate(@Nullable Consumer<SettingsContext> delegate) {
        this.applySettingsDelegate = delegate;
    }

    @Nullable
    @Override
    public Consumer<SettingsContext> getApplyDataLoadingSettingsDelegate() {
        return applyDataLoadingSettingsDelegate;
    }

    @Override
    public void setApplyDataLoadingSettingsDelegate(@Nullable Consumer<SettingsContext> delegate) {
        this.applyDataLoadingSettingsDelegate = delegate;
    }

    @Nullable
    @Override
    public Consumer<SettingsContext> getSaveSettingsDelegate() {
        return saveSettingsDelegate;
    }

    @Override
    public void setSaveSettingsDelegate(@Nullable Consumer<SettingsContext> delegate) {
        this.saveSettingsDelegate = delegate;
    }

    @Override
    public void setOwner(@Nullable View<?> owner) {
        super.setOwner(owner);

        unsubscribeViewLifecycle();

        if (owner != null) {
            viewSettings = createViewSettings(owner);
            initViewSettings(viewSettings);

            subscribeViewLifecycle();

            if (!isSettingsEnabled()) {
                log.warn(SettingsFacet.class.getSimpleName() + " does not work for '{}' due to starter "
                        + "that provides the ability to work with settings is not added", owner.getId().orElse(null));
            }
        }
    }

    protected Collection<Component> getManagedComponentsFromCollection(Collection<Component> viewComponents) {
        Collection<Component> components = Collections.emptyList();
        if (auto) {
            components = viewComponents;
        } else if (CollectionUtils.isNotEmpty(componentIds)) {
            components = viewComponents
                    .stream()
                    .filter(c -> componentIds.contains(c.getId().orElse(null)))
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(excludedComponentIds)) {
            components = components.stream()
                    .filter(component -> !excludedComponentIds.contains(component.getId().orElse(null)))
                    .collect(Collectors.toList());
        }
        return components;
    }

    protected ViewSettings createViewSettings(View<?> view) {
        return new ViewSettingsJson(view.getId()
                .orElseThrow(() ->
                        new IllegalStateException("Cannot create " + ViewSettings.class.getSimpleName() +
                                " because " + view.getClass().getSimpleName() + " does not contain an id")));
    }

    protected void initViewSettings(ViewSettings viewSettings) {
        if (isSettingsEnabled()) {
            String rawSettings = userSettingsCache.get(viewSettings.getViewId());
            viewSettings.initialize(rawSettings);
        }
    }

    protected void unsubscribeViewLifecycle() {
        if (beforeShowListener != null) {
            beforeShowListener.unsubscribe();
            beforeShowListener = null;
        }
        if (readyListener != null) {
            readyListener.unsubscribe();
            readyListener = null;
        }
        if (detachListener != null) {
            detachListener.unsubscribe();
            detachListener = null;
        }
    }

    protected void subscribeViewLifecycle() {
        View<?> view = getView();

        beforeShowListener = new ViewEventListener(view, View.BeforeShowEvent.class, this::onViewBeforeShow);
        readyListener = new ViewEventListener(view, View.ReadyEvent.class, this::onViewReady);
        detachListener = new ViewEventListener(view, DetachEvent.class, this::onViewDetach);
        detachListener = new ViewEventListener(view, View.QueryParametersChangeEvent.class, this::onQueryParametersChange);
    }

    protected void onViewBeforeShow(ComponentEvent<?> event) {
        checkAttachedToView();

        if (applyDataLoadingSettingsDelegate != null) {
            applyDataLoadingSettingsDelegate.accept(createSettingsContext());
        } else {
            applyDataLoadingSettings();
        }
    }

    protected void onViewReady(ComponentEvent<?> event) {
        checkAttachedToView();

        if (applySettingsDelegate != null) {
            applySettingsDelegate.accept(createSettingsContext());
        } else {
            applySettings();
        }
    }

    protected void onViewDetach(ComponentEvent<?> event) {
        checkAttachedToView();

        if (saveSettingsDelegate != null) {
            saveSettingsDelegate.accept(createSettingsContext());
        } else {
            saveSettings();
        }
    }

    protected void onQueryParametersChange(ComponentEvent<?> event) {
        if (event instanceof View.QueryParametersChangeEvent) {
            viewQueryParameters = ((View.QueryParametersChangeEvent) event).getQueryParameters();
        }
    }

    protected void applyViewSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.applySettings(components, viewSettings);
        }
    }

    protected void applyDataLoadingViewSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.applyDataLoadingSettings(components, viewSettings);
        }
    }

    protected void saveViewSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.saveSettings(components, viewSettings);
        }
    }

    protected void checkAttachedToView() {
        if (getOwner() == null) {
            throw new IllegalStateException(
                    SettingsFacet.class.getSimpleName() + " is not attached to " + View.class.getSimpleName());
        }
    }

    protected boolean isSettingsEnabled() {
        return settingsManager != null;
    }

    protected Collection<Component> excludeUrlQueryParametersFacetComponents(Collection<Component> components) {
        Collection<Component> resultComponents = new ArrayList<>(components);
        View<?> view = getView();

        if (viewQueryParameters == null
                || viewQueryParameters.getParameters().isEmpty()) {
            return resultComponents;
        }

        ViewFacets viewFacets = ViewControllerUtils.getViewFacets(view);
        List<UrlQueryParametersFacet> urlQueryFacets = viewFacets.getFacets()
                .filter(f -> f instanceof UrlQueryParametersFacet)
                .map(f -> (UrlQueryParametersFacet) f)
                .toList();

        if (urlQueryFacets.isEmpty()) {
            return resultComponents;
        }

        List<UrlQueryParametersFacet.Binder> binders = new ArrayList<>();
        urlQueryFacets.forEach(f -> binders.addAll(f.getBinders()));

        for (UrlQueryParametersFacet.Binder binder : binders) {
            if (!settingsHelper.containsParametersForBinder(viewQueryParameters, binder)) {
                continue;
            }
            settingsHelper.getComponentFromBinder(binder)
                    .ifPresent(resultComponents::remove);
        }

        return resultComponents;
    }

    protected View<?> getView() {
        checkAttachedToView();

        // Used only to hide inspection, cannot be null here
        return Objects.requireNonNull(getOwner());
    }

    protected SettingsContext createSettingsContext() {
        View<?> owner = getView();

        return new SettingsContext(owner, getManagedComponents(), viewSettings);
    }

    protected class ViewEventListener {

        protected View<?> view;
        protected Class<?> eventClass;
        protected Consumer<ComponentEvent<?>> listener;

        protected Registration registration;

        public ViewEventListener(View<?> view, Class<?> eventClass, Consumer<ComponentEvent<?>> listener) {
            this.view = view;
            this.eventClass = eventClass;
            this.listener = listener;

            subscribe();
        }

        protected void subscribe() {
            MethodHandle addListenerMethod = reflectionInspector.getAddListenerMethod(view.getClass(), eventClass);
            if (addListenerMethod == null) {
                throw new IllegalStateException("Cannot find addListener method for " + eventClass);
            }

            try {
                registration = (Registration) addListenerMethod.invoke(view, (ComponentEventListener<?>) event -> listener.accept(event));
            } catch (Error e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException("Unable to add listener for " + eventClass, e);
            }
        }

        public void unsubscribe() {
            if (registration != null) {
                registration.remove();
                registration = null;
            }
        }
    }
}
