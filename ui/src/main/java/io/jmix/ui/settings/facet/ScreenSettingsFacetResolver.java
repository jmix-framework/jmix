/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui.settings.facet;

import io.jmix.core.annotation.Internal;
import io.jmix.ui.component.*;
import io.jmix.ui.presentation.PresentationsManager;
import io.jmix.ui.presentation.facet.PresentationsFacet;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.settings.ScreenSettings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Class that manages the right order of working {@link ScreenSettingsFacet} and {@link PresentationsFacet}.
 */
@Internal
@org.springframework.stereotype.Component("ui_ScreenSettingsFacetResolver")
public class ScreenSettingsFacetResolver {

    protected PresentationsManager presentationsManager;

    public ScreenSettingsFacetResolver(@Autowired(required = false) PresentationsManager presentationsManager) {
        this.presentationsManager = presentationsManager;
    }

    /**
     * Resolves applying settings and presentations on {@link Screen.AfterShowEvent}.
     *
     * @param event after show event
     */
    public void resolveAfterShowEvent(Screen.AfterShowEvent event) {
        Screen screen = event.getSource();

        List<Facet> presentationsFacet = screen.getWindow().getFacets()
                .filter(facet -> facet instanceof PresentationsFacet)
                .collect(Collectors.toList());

        for (Facet facet : presentationsFacet) {
            ((AfterShowEventHandler) facet).onAfterShowEvent(event);
        }

        screen.getWindow().getFacets()
                .filter(facet -> facet instanceof ScreenSettingsFacet)
                .forEach(facet -> ((AfterShowEventHandler) facet).onAfterShowEvent(event));

        for (Facet facet : presentationsFacet) {
            ((PostAfterShowEventHandler) facet).onPostAfterShow();
        }
    }

    /**
     * Resolves applying settings and presentations for components in a lazy tab e.g. {@link TabSheet} or
     * {@link Accordion}.
     *
     * @param window     screen window
     * @param source     component source
     * @param components tab content
     */
    public void resolveLazyTabSelectEvent(Window window, Component source, Collection<Component> components) {
        List<PresentationsFacet> presentationsFacet = window.getFacets()
                .filter(facet -> facet instanceof PresentationsFacet)
                .map(facet -> (PresentationsFacet) facet)
                .collect(Collectors.toList());

        boolean isPresentationsAdded = presentationsFacet.size() > 0;
        Collection<Component> managedComponents = getManagedComponents(presentationsFacet, components);

        if (isPresentationsAdded) {
            for (Component component : managedComponents) {
                if (component instanceof HasTablePresentations) {
                    ((HasTablePresentations) component).loadPresentations();
                }
            }

            if (presentationsManager != null) {
                presentationsManager.setupDefaultSettings(managedComponents);
            }
        }

        window.getFacets().forEach(facet -> {
            if (facet instanceof ScreenSettingsFacet) {
                ScreenSettingsFacet settingsFacet = (ScreenSettingsFacet) facet;
                Consumer<ScreenSettingsFacet.SettingsContext> applyHandler = settingsFacet.getApplySettingsDelegate();

                ScreenSettings settings = settingsFacet.getSettings();
                if (settings == null) {
                    throw new IllegalStateException("ScreenSettingsFacet is not attached to the frame");
                }

                if (applyHandler != null) {
                    applyHandler.accept(new ScreenSettingsFacet.SettingsContext(source, components, settings));
                } else {
                    settingsFacet.applySettings(components);
                }
            }
        });

        if (isPresentationsAdded && presentationsManager != null) {
            presentationsManager.applyDefaultPresentation(managedComponents);
        }
    }

    protected Collection<Component> getManagedComponents(Collection<PresentationsFacet> facets,
                                                         Collection<Component> lazyTabComponents) {
        Set<Component> facetComponents = new HashSet<>();
        for (PresentationsFacet facet : facets) {
            facetComponents.addAll(facet.getComponents());
        }

        return lazyTabComponents.stream()
                .filter(facetComponents::contains)
                .collect(Collectors.toList());
    }

    /**
     * Interface for {@link Screen.AfterShowEvent} handler.
     *
     * @see #resolveAfterShowEvent(Screen.AfterShowEvent)
     */
    @FunctionalInterface
    public interface AfterShowEventHandler {

        void onAfterShowEvent(Screen.AfterShowEvent event);
    }

    /**
     * Interface for post {@link Screen.AfterShowEvent} handler.
     *
     * @see #resolveAfterShowEvent(Screen.AfterShowEvent)
     */
    @FunctionalInterface
    public interface PostAfterShowEventHandler {

        void onPostAfterShow();
    }
}
