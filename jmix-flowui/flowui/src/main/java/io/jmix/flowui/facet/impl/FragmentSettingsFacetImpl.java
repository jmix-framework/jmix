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
import io.jmix.flowui.facet.FragmentSettingsFacet;
import io.jmix.flowui.facet.settings.ComponentSettingsManager;
import io.jmix.flowui.facet.settings.FragmentSettings;
import io.jmix.flowui.facet.settings.FragmentSettingsJson;
import io.jmix.flowui.facet.settings.SettingsFacetUrlQueryParametersHelper;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentOwner;
import io.jmix.flowui.fragment.FragmentUtils;
import io.jmix.flowui.settings.UserSettingsCache;
import io.jmix.flowui.settings.UserSettingsService;
import io.jmix.flowui.sys.autowire.ReflectionCacheManager;
import io.jmix.flowui.view.View;
import org.springframework.lang.Nullable;

public class FragmentSettingsFacetImpl extends AbstractSettingsFacet<FragmentSettings>
        implements FragmentSettingsFacet {

    protected OwnerEventListener ownerViewBeforeShowListener;
    protected OwnerEventListener ownerViewReadyListener;
    protected OwnerEventListener ownerViewDetachListener;
    protected OwnerEventListener ownerViewQueryParametersChangeListener;

    public FragmentSettingsFacetImpl(SettingsFacetUrlQueryParametersHelper settingsHelper,
                                     ReflectionCacheManager reflectionCacheManager,
                                     UserSettingsCache userSettingsCache,
                                     ComponentSettingsManager settingsManager,
                                     @Nullable UserSettingsService userSettingsService) {
        super(settingsHelper, reflectionCacheManager, userSettingsCache, settingsManager, userSettingsService);
    }

    @Override
    protected FragmentSettings createSettings(FacetOwner owner) {
        String hostId = FragmentUtils.getHostView((FragmentOwner) owner).getId()
                .orElseThrow(() ->
                        new IllegalStateException("Cannot create " + FragmentSettings.class.getSimpleName()
                                + " because the host of " + owner.getClass().getSimpleName()
                                + " does not contain an id"));

        return new FragmentSettingsJson(hostId + "." + owner.getId()
                .orElseThrow(() ->
                        new IllegalStateException("Cannot create " + FragmentSettings.class.getSimpleName() +
                                " because " + owner.getClass().getSimpleName() + " does not contain an id"))
        );
    }

    @Override
    protected void unsubscribeOwnerLifecycle() {
        if (ownerViewBeforeShowListener != null) {
            ownerViewBeforeShowListener.unsubscribe();
            ownerViewBeforeShowListener = null;
        }

        if (ownerViewReadyListener != null) {
            ownerViewReadyListener.unsubscribe();
            ownerViewReadyListener = null;
        }

        if (ownerViewDetachListener != null) {
            ownerViewDetachListener.unsubscribe();
            ownerViewDetachListener = null;
        }

        if (ownerViewQueryParametersChangeListener != null) {
            ownerViewQueryParametersChangeListener.unsubscribe();
            ownerViewQueryParametersChangeListener = null;
        }
    }

    @Override
    protected void subscribeOwnerLifecycle() {
        Fragment<?> fragment = ((Fragment<?>) getOwnerComponent());
        View<?> ownerView = FragmentUtils.getHostView(fragment);


        ownerViewBeforeShowListener =
                new OwnerEventListener(ownerView, View.BeforeShowEvent.class, this::onOwnerViewBeforeShow);
        ownerViewReadyListener = new OwnerEventListener(ownerView, View.ReadyEvent.class, this::onOwnerViewReady);
        ownerViewDetachListener = new OwnerEventListener(ownerView, DetachEvent.class, this::onOwnerViewDetach);
        ownerViewQueryParametersChangeListener = new OwnerEventListener(ownerView,
                View.QueryParametersChangeEvent.class, this::onQueryParametersChange);
    }

    protected void onOwnerViewBeforeShow(ComponentEvent<?> event) {
        checkAttachedToOwner();

        if (applyDataLoadingSettingsDelegate != null) {
            applyDataLoadingSettingsDelegate.accept(createSettingsContext());
        } else {
            applyDataLoadingSettings();
        }
    }

    protected void onOwnerViewReady(ComponentEvent<?> componentEvent) {
        checkAttachedToOwner();

        if (applySettingsDelegate != null) {
            applySettingsDelegate.accept(createSettingsContext());
        } else {
            applySettings();
        }
    }

    private void onOwnerViewDetach(ComponentEvent<?> componentEvent) {
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
        Fragment<?> fragment = (Fragment<?>) getOwnerComponent();
        return FragmentUtils.getFragmentFacets(fragment);
    }
}
