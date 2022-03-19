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

package io.jmix.ui.presentation.facet;

import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.HasInnerComponents;
import io.jmix.ui.component.impl.AbstractFacet;
import io.jmix.ui.presentation.PresentationsManager;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.settings.facet.ScreenSettingsFacetResolver;
import io.jmix.ui.settings.facet.ScreenSettingsFacetResolver.AfterShowEventHandler;
import io.jmix.ui.settings.facet.ScreenSettingsFacetResolver.PostAfterShowEventHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class PresentationsFacetImpl extends AbstractFacet
        implements PresentationsFacet, AfterShowEventHandler, PostAfterShowEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PresentationsFacetImpl.class);

    @Autowired
    protected ScreenSettingsFacetResolver settingsFacetResolver;
    @Autowired(required = false)
    protected PresentationsManager presentationsManager;

    protected boolean auto = false;
    protected Set<String> componentIds;

    private Subscription afterShowSubscription;
    private Subscription afterDetachedSubscription;

    protected boolean isAfterShowHandled = false;
    protected boolean isPostAfterShowHandled = false;

    @Override
    public boolean isAuto() {
        return auto;
    }

    @Override
    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    @Override
    public void addComponentIds(String... ids) {
        if (componentIds == null) {
            componentIds = new HashSet<>();
        }

        componentIds.addAll(Arrays.asList(ids));
    }

    @Override
    public Set<String> getComponentIds() {
        if (componentIds == null) {
            return Collections.emptySet();
        }

        return componentIds;
    }

    @Override
    public Collection<Component> getComponents() {
        checkAttachedFrame();
        assert getOwner() != null;

        if (auto) {
            return fillComponents(getOwner().getComponents());
        }

        if (CollectionUtils.isNotEmpty(componentIds)) {
            return getOwner().getComponents().stream()
                    .filter(component -> componentIds.contains(component.getId()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);

        unsubscribe();

        if (getScreenOwner() != null) {

            subscribe();

            if (!isPresentationsEnabled()) {
                log.warn("PresentationsFacet does not work for '{}' due to add-on "
                        + "that provides the ability to work with presentations is not added", getScreenOwner().getId());
            }
        }
    }

    protected void setupDefaultSettings(Collection<Component> components) {
        checkAttachedFrame();

        if (isPresentationsEnabled()) {
            presentationsManager.setupDefaultSettings(components);
        }
    }

    protected void applyDefaultPresentation(Collection<Component> components) {
        checkAttachedFrame();

        if (isPresentationsEnabled()) {
            presentationsManager.applyDefaultPresentation(components);
        }
    }

    protected void commitPresentations(Collection<Component> components) {
        checkAttachedFrame();

        if (isPresentationsEnabled()) {
            presentationsManager.commitPresentations(components);
        }
    }

    protected void subscribe() {
        checkAttachedFrame();

        //noinspection ConstantConditions
        EventHub screenEvents = UiControllerUtils.getEventHub(getScreenOwner());

        afterShowSubscription = screenEvents.subscribe(Screen.AfterShowEvent.class, settingsFacetResolver::resolveAfterShowEvent);
        afterDetachedSubscription = screenEvents.subscribe(Screen.AfterDetachEvent.class, this::onAfterDetachEvent);
    }

    protected void unsubscribe() {
        if (afterShowSubscription != null) {
            afterShowSubscription.remove();
            afterShowSubscription = null;
        }
        if (afterDetachedSubscription != null) {
            afterDetachedSubscription.remove();
            afterDetachedSubscription = null;
        }
    }

    @Override
    public void onAfterShowEvent(Screen.AfterShowEvent event) {
        if (!isAfterShowHandled) {
            setupDefaultSettings(getComponents());
            isAfterShowHandled = true;
        }
    }

    @Override
    public void onPostAfterShow() {
        if (!isPostAfterShowHandled) {
            applyDefaultPresentation(getComponents());
            isPostAfterShowHandled = true;
        }
    }

    private void onAfterDetachEvent(Screen.AfterDetachEvent event) {
        commitPresentations(getComponents());
    }

    protected void checkAttachedFrame() {
        Frame frame = getOwner();
        if (frame == null) {
            throw new IllegalStateException("PresentationsFacet is not attached to the screen");
        }
    }

    @Nullable
    protected Screen getScreenOwner() {
        Frame frame = getOwner();
        if (frame == null) {
            return null;
        }
        if (frame.getFrameOwner() instanceof ScreenFragment) {
            throw new IllegalStateException("PresentationsFacet does not work in fragments");
        }

        return (Screen) frame.getFrameOwner();
    }

    protected boolean isPresentationsEnabled() {
        return presentationsManager != null;
    }

    protected Collection<Component> fillComponents(Collection<Component> components) {
        Collection<Component> result = new ArrayList<>(components);
        for (Component component : components) {
            if (component instanceof HasInnerComponents) {
                fillWithInnerComponents(result, (HasInnerComponents) component);
            }
        }
        return result;
    }

    protected Collection<Component> fillWithInnerComponents(Collection<Component> components, HasInnerComponents hasInnerComponents) {
        Collection<Component> innerComponents = hasInnerComponents.getInnerComponents();
        components.addAll(innerComponents);

        for (Component component : innerComponents) {
            if (component instanceof HasInnerComponents) {
                fillWithInnerComponents(components, (HasInnerComponents) component);
            }
        }

        return components;
    }
}
