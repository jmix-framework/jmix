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
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.component.HasFacets;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.facet.FacetOwner;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.UrlQueryParametersFacet;
import io.jmix.flowui.facet.settings.ComponentSettingsManager;
import io.jmix.flowui.facet.settings.SettingsFacetUrlQueryParametersHelper;
import io.jmix.flowui.facet.settings.UiComponentSettings;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.settings.UserSettingsService;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * An abstract implementation of the {@link SettingsFacet} interface that provides functionality
 * for managing and applying settings related to {@link FacetOwner} and their components.
 */
public abstract class AbstractSettingsFacet<S extends UiComponentSettings<S>> extends AbstractFacet
        implements SettingsFacet<S> {

    private static final Logger log = LoggerFactory.getLogger(AbstractSettingsFacet.class);

    protected SettingsFacetUrlQueryParametersHelper settingsHelper;
    protected ReflectionCacheManager reflectionCacheManager;
    protected ComponentSettingsManager settingsManager;
    protected UserSettingsCache userSettingsCache;
    @Nullable
    private final UserSettingsService userSettingsService;

    protected Set<String> componentIds;
    protected Set<String> excludedComponentIds;

    protected S componentSettings;

    protected boolean auto = false;

    protected Consumer<SettingsContext<S>> applySettingsDelegate;
    protected Consumer<SettingsContext<S>> applyDataLoadingSettingsDelegate;
    protected Consumer<SettingsContext<S>> saveSettingsDelegate;

    protected QueryParameters viewQueryParameters;

    protected AbstractSettingsFacet(SettingsFacetUrlQueryParametersHelper settingsHelper,
                                    ReflectionCacheManager reflectionCacheManager,
                                    UserSettingsCache userSettingsCache,
                                    ComponentSettingsManager settingsManager,
                                    @Nullable UserSettingsService userSettingsService) {
        this.settingsHelper = settingsHelper;
        this.reflectionCacheManager = reflectionCacheManager;
        this.settingsManager = settingsManager;
        this.userSettingsCache = userSettingsCache;
        this.userSettingsService = userSettingsService;
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
    public S getSettings() {
        return componentSettings;
    }

    @Override
    public void applySettings() {
        Collection<Component> components = getManagedComponents();

        components = excludeUrlQueryParametersFacetComponents(components);

        applyComponentSettings(components);
    }

    @Override
    public void applyDataLoadingSettings() {
        Collection<Component> components = getManagedComponents();

        components = excludeUrlQueryParametersFacetComponents(components);

        applyDataLoadingComponentSettings(components);
    }

    @Override
    public void saveSettings() {
        Collection<Component> components = getManagedComponents();

        saveComponentSettings(components);
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
        checkAttachedToOwner();

        Collection<Component> ownerComponents = UiComponentUtils.getComponents(
                Objects.requireNonNull(getOwnerComponent()).getContent());

        return getManagedComponentsFromCollection(ownerComponents);
    }

    @Nullable
    @Override
    public Consumer<SettingsContext<S>> getApplySettingsDelegate() {
        return applySettingsDelegate;
    }

    @Override
    public void setApplySettingsDelegate(@Nullable Consumer<SettingsContext<S>> delegate) {
        this.applySettingsDelegate = delegate;
    }

    @Nullable
    @Override
    public Consumer<SettingsContext<S>> getApplyDataLoadingSettingsDelegate() {
        return applyDataLoadingSettingsDelegate;
    }

    @Override
    public void setApplyDataLoadingSettingsDelegate(@Nullable Consumer<SettingsContext<S>> delegate) {
        this.applyDataLoadingSettingsDelegate = delegate;
    }

    @Nullable
    @Override
    public Consumer<SettingsContext<S>> getSaveSettingsDelegate() {
        return saveSettingsDelegate;
    }

    @Override
    public void setSaveSettingsDelegate(@Nullable Consumer<SettingsContext<S>> delegate) {
        this.saveSettingsDelegate = delegate;
    }

    @Override
    public <T extends Composite<?> & FacetOwner> void setOwner(@Nullable T owner) {
        super.setOwner(owner);

        unsubscribeOwnerLifecycle();

        if (owner != null) {
            componentSettings = createSettings(owner);
            initSettings(componentSettings);

            subscribeOwnerLifecycle();

            if (!isSettingsEnabled()) {
                log.warn("SettingsFacet does not work for '{}' because UserSettingsService implementation is not available",
                        owner.getId().orElse(null));
            }
        }
    }

    protected Collection<Component> getManagedComponentsFromCollection(Collection<Component> ownerComponents) {
        Collection<Component> components = Collections.emptyList();
        if (auto) {
            components = ownerComponents;
        } else if (CollectionUtils.isNotEmpty(componentIds)) {
            components = ownerComponents
                    .stream()
                    .filter(c -> componentIds.contains(UiComponentUtils.getComponentId(c).orElse(null)))
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(excludedComponentIds)) {
            components = components.stream()
                    .filter(component ->
                            !excludedComponentIds.contains(UiComponentUtils.getComponentId(component).orElse(null)))
                    .collect(Collectors.toList());
        }
        return components;
    }

    protected abstract S createSettings(FacetOwner owner);

    protected void initSettings(S settings) {
        if (isSettingsEnabled()) {
            String rawSettings = userSettingsCache.get(settings.getOwnerId());
            settings.initialize(rawSettings);
        }
    }

    protected abstract void unsubscribeOwnerLifecycle();

    protected abstract void subscribeOwnerLifecycle();

    protected void applyComponentSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.applySettings(components, componentSettings);
        }
    }

    protected void applyDataLoadingComponentSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.applyDataLoadingSettings(components, componentSettings);
        }
    }

    protected void saveComponentSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.saveSettings(components, componentSettings);
        }
    }

    protected void checkAttachedToOwner() {
        if (getOwner() == null) {
            throw new IllegalStateException(
                    SettingsFacet.class.getSimpleName() + " is not attached to " + FacetOwner.class.getSimpleName());
        }
    }

    protected boolean isSettingsEnabled() {
        return userSettingsService != null;
    }

    protected Collection<Component> excludeUrlQueryParametersFacetComponents(Collection<Component> components) {
        Collection<Component> resultComponents = new ArrayList<>(components);

        if (viewQueryParameters == null
                || viewQueryParameters.getParameters().isEmpty()) {
            return resultComponents;
        }

        HasFacets facets = getFacets();
        List<UrlQueryParametersFacet> urlQueryFacets = facets.getFacets()
                .filter(f -> f instanceof UrlQueryParametersFacet)
                .map(UrlQueryParametersFacet.class::cast)
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

    protected abstract HasFacets getFacets();

    protected Composite<?> getOwnerComponent() {
        checkAttachedToOwner();

        // Used only to hide inspection, cannot be null here
        return Objects.requireNonNull(getOwner());
    }

    protected SettingsContext<S> createSettingsContext() {
        Composite<?> owner = getOwnerComponent();

        return new SettingsContext<>(owner, getManagedComponents(), componentSettings);
    }

    protected class OwnerEventListener {

        protected FacetOwner owner;
        protected Class<?> eventClass;
        protected Consumer<ComponentEvent<?>> listener;

        protected Registration registration;

        public OwnerEventListener(FacetOwner owner, Class<?> eventClass, Consumer<ComponentEvent<?>> listener) {
            this.owner = owner;
            this.eventClass = eventClass;
            this.listener = listener;

            subscribe();
        }

        protected void subscribe() {
            MethodHandle addListenerMethod = reflectionCacheManager.getTargetAddListenerMethod(
                    owner.getClass(), eventClass, null
            );
            if (addListenerMethod == null) {
                throw new IllegalStateException("Cannot find addListener method for " + eventClass);
            }

            try {
                registration = (Registration) addListenerMethod.invoke(owner,
                        (ComponentEventListener<?>) event -> listener.accept(event));
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
