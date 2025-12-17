/*
 * Copyright 2025 Haulmont.
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

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DetachEvent;
import io.jmix.flowui.component.HasFacetsComponents;
import io.jmix.flowui.facet.FacetOwner;
import io.jmix.flowui.facet.SettingsFacet;
import io.jmix.flowui.facet.ViewSettingsFacet;
import io.jmix.flowui.facet.settings.ComponentSettingsManager;
import io.jmix.flowui.facet.settings.SettingsFacetUrlQueryParametersHelper;
import io.jmix.flowui.facet.settings.ViewSettings;
import io.jmix.flowui.facet.settings.ViewSettingsJson;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.settings.UserSettingsService;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import org.springframework.lang.Nullable;

/**
 * An implementation of the {@link SettingsFacet} interfacet that provides functionality for managing
 * and applying settings related to {@link View} and their components.
 */
public class ViewSettingsFacetImpl extends AbstractSettingsFacet<ViewSettings>
        implements ViewSettingsFacet {

    protected OwnerEventListener beforeShowListener;
    protected OwnerEventListener readyListener;
    protected OwnerEventListener detachListener;
    protected OwnerEventListener queryParametersChangeListener;

    public ViewSettingsFacetImpl(SettingsFacetUrlQueryParametersHelper settingsHelper,
                                 ReflectionCacheManager reflectionCacheManager,
                                 UserSettingsCache userSettingsCache,
                                 ComponentSettingsManager settingsManager,
                                 @Nullable UserSettingsService userSettingsService) {
        super(settingsHelper, reflectionCacheManager, userSettingsCache, settingsManager, userSettingsService);
    }

    @Override
    protected ViewSettings createSettings(FacetOwner owner) {
        return new ViewSettingsJson(owner.getId()
                .orElseThrow(() ->
                        new IllegalStateException("Cannot create " + ViewSettings.class.getSimpleName() +
                                " because " + owner.getClass().getSimpleName() + " does not contain an id")));
    }

    @Override
    protected void unsubscribeOwnerLifecycle() {
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

    @Override
    protected void subscribeOwnerLifecycle() {
        View<?> view = ((View<?>) getOwnerComponent());

        beforeShowListener = new OwnerEventListener(view, View.BeforeShowEvent.class, this::onViewBeforeShow);
        readyListener = new OwnerEventListener(view, View.ReadyEvent.class, this::onViewReady);
        detachListener = new OwnerEventListener(view, DetachEvent.class, this::onViewDetach);
        queryParametersChangeListener = new OwnerEventListener(view, View.QueryParametersChangeEvent.class, this::onQueryParametersChange);
    }

    protected void onViewBeforeShow(ComponentEvent<?> event) {
        checkAttachedToOwner();

        if (applyDataLoadingSettingsDelegate != null) {
            applyDataLoadingSettingsDelegate.accept(createSettingsContext());
        } else {
            applyDataLoadingSettings();
        }
    }

    protected void onViewReady(ComponentEvent<?> event) {
        checkAttachedToOwner();

        if (applySettingsDelegate != null) {
            applySettingsDelegate.accept(createSettingsContext());
        } else {
            applySettings();
        }
    }

    protected void onViewDetach(ComponentEvent<?> event) {
        checkAttachedToOwner();

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

    @Override
    protected HasFacetsComponents getFacets() {
        View<?> view = (View<?>) getOwnerComponent();
        return ViewControllerUtils.getViewFacets(view);
    }
}
