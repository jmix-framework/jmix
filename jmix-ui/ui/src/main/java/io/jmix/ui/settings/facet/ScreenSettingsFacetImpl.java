/*
 * Copyright 2020 Haulmont.
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
import io.jmix.core.common.event.EventHub;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Frame;
import io.jmix.ui.component.HasInnerComponents;
import io.jmix.ui.component.impl.AbstractFacet;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Screen.AfterDetachEvent;
import io.jmix.ui.screen.Screen.AfterShowEvent;
import io.jmix.ui.screen.Screen.BeforeShowEvent;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.settings.ScreenSettingsManager;
import io.jmix.ui.settings.ScreenSettings;
import io.jmix.ui.settings.facet.ScreenSettingsFacetResolver.AfterShowEventHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Internal
public class ScreenSettingsFacetImpl extends AbstractFacet implements ScreenSettingsFacet, AfterShowEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ScreenSettingsFacetImpl.class);

    protected Set<String> componentIds;
    protected Set<String> excludedComponentIds;

    protected boolean auto = false;
    protected boolean isAfterShowHandled = false;

    protected ScreenSettings screenSettings;

    protected Consumer<SettingsContext> applySettingsDelegate;
    protected Consumer<SettingsContext> applyDataLoadingSettingsDelegate;
    protected Consumer<SettingsContext> saveSettingsDelegate;

    protected Subscription beforeShowSubscription;
    protected Subscription afterShowSubscription;
    protected Subscription afterDetachedSubscription;

    @Autowired(required = false)
    protected ScreenSettingsManager settingsManager;
    @Autowired
    protected ScreenSettingsFacetResolver settingsFacetResolver;

    @Autowired
    protected BeanFactory beanFactory;

    @Override
    public ScreenSettings getSettings() {
        return screenSettings;
    }

    @Override
    public void applySettings() {
        Collection<Component> components = getComponents();

        applyScreenSettings(components);
    }

    @Override
    public void applySettings(Collection<Component> components) {
        Collection<Component> componentsToApply = filterByManagedComponents(components);

        applyScreenSettings(componentsToApply);
    }

    @Override
    public void applyDataLoadingSettings() {
        Collection<Component> components = getComponents();

        applyDataLoadingScreenSettings(components);
    }

    @Override
    public void applyDataLoadingSettings(Collection<Component> components) {
        Collection<Component> componentsToApply = filterByManagedComponents(components);

        applyDataLoadingScreenSettings(componentsToApply);
    }

    @Override
    public void saveSettings() {
        Collection<Component> components = getComponents();

        saveScreenSettings(components);
    }

    @Override
    public void saveSettings(Collection<Component> components) {
        Collection<Component> componentsToSave = filterByManagedComponents(components);

        saveScreenSettings(componentsToSave);
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
    public void excludeComponentIds(String... ids) {
        if (excludedComponentIds == null) {
            excludedComponentIds = new HashSet<>();
        }

        excludedComponentIds.addAll(Arrays.asList(ids));
    }

    @Override
    public Set<String> getExcludedComponentIds() {
        if (excludedComponentIds == null) {
            return Collections.emptySet();
        }
        return excludedComponentIds;
    }

    @Override
    public Collection<Component> getComponents() {
        checkAttachedFrame();
        assert getOwner() != null;

        Collection<Component> components = Collections.emptyList();

        if (auto) {
            components = fillComponents(getOwner().getComponents());
        } else if (CollectionUtils.isNotEmpty(componentIds)) {
            components = getOwner().getComponents().stream()
                    .filter(component -> componentIds.contains(component.getId()))
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(excludedComponentIds)) {
            components = components.stream()
                    .filter(component -> !excludedComponentIds.contains(component.getId()))
                    .collect(Collectors.toList());
        }
        return components;
    }

    @Override
    public Consumer<SettingsContext> getApplySettingsDelegate() {
        return applySettingsDelegate;
    }

    @Override
    public void setApplySettingsDelegate(Consumer<SettingsContext> delegate) {
        this.applySettingsDelegate = delegate;
    }

    @Override
    public Consumer<SettingsContext> getApplyDataLoadingSettingsDelegate() {
        return applyDataLoadingSettingsDelegate;
    }

    @Override
    public void setApplyDataLoadingSettingsDelegate(Consumer<SettingsContext> delegate) {
        this.applyDataLoadingSettingsDelegate = delegate;
    }

    @Override
    public Consumer<SettingsContext> getSaveSettingsDelegate() {
        return saveSettingsDelegate;
    }

    @Override
    public void setSaveSettingsDelegate(Consumer<SettingsContext> delegate) {
        this.saveSettingsDelegate = delegate;
    }

    @Override
    public void setOwner(@Nullable Frame owner) {
        super.setOwner(owner);

        unsubscribe();
        screenSettings = null;

        if (getScreenOwner() != null) {
            screenSettings = beanFactory.getBean(ScreenSettings.class, getScreenOwner().getId());

            subscribe();

            if (!isSettingsEnabled()) {
                log.warn("ScreenSettingsFacet does not work for '{}' due to starter "
                        + "that provides the ability to work with settings is not added", getScreenOwner().getId());
            }
        }
    }

    protected void subscribe() {
        checkAttachedFrame();

        //noinspection ConstantConditions
        EventHub screenEvents = UiControllerUtils.getEventHub(getScreenOwner());

        beforeShowSubscription = screenEvents.subscribe(BeforeShowEvent.class, this::onBeforeShowEvent);
        afterShowSubscription = screenEvents.subscribe(AfterShowEvent.class, settingsFacetResolver::resolveAfterShowEvent);
        afterDetachedSubscription = screenEvents.subscribe(AfterDetachEvent.class, this::onAfterDetachEvent);
    }

    protected void unsubscribe() {
        if (beforeShowSubscription != null) {
            beforeShowSubscription.remove();
            beforeShowSubscription = null;
        }
        if (afterShowSubscription != null) {
            afterShowSubscription.remove();
            afterShowSubscription = null;
        }
        if (afterDetachedSubscription != null) {
            afterDetachedSubscription.remove();
            afterDetachedSubscription = null;
        }
    }

    @Nullable
    protected Screen getScreenOwner() {
        Frame frame = getOwner();
        if (frame == null) {
            return null;
        }
        if (frame.getFrameOwner() instanceof ScreenFragment) {
            throw new IllegalStateException("ScreenSettingsFacet does not work in fragments");
        }

        return (Screen) frame.getFrameOwner();
    }

    protected void onBeforeShowEvent(BeforeShowEvent event) {
        checkAttachedFrame();

        if (applyDataLoadingSettingsDelegate != null) {
            //noinspection ConstantConditions
            applyDataLoadingSettingsDelegate.accept(new SettingsContext(
                    getScreenOwner().getWindow(),
                    getComponents(),
                    screenSettings));
        } else {
            applyDataLoadingSettings();
        }
    }

    @Override
    public void onAfterShowEvent(AfterShowEvent event) {
        checkAttachedFrame();

        if (isAfterShowHandled) {
            return;
        }

        if (applySettingsDelegate != null) {
            //noinspection ConstantConditions
            applySettingsDelegate.accept(new SettingsContext(
                    getScreenOwner().getWindow(),
                    getComponents(),
                    screenSettings));
        } else {
            applySettings();
        }

        isAfterShowHandled = true;
    }

    protected void onAfterDetachEvent(AfterDetachEvent event) {
        checkAttachedFrame();

        if (saveSettingsDelegate != null) {
            //noinspection ConstantConditions
            saveSettingsDelegate.accept(new SettingsContext(
                    getScreenOwner().getWindow(),
                    getComponents(),
                    screenSettings));
        } else {
            saveSettings();
        }
    }

    protected void applyScreenSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.applySettings(components, screenSettings);
        }
    }

    protected void applyDataLoadingScreenSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.applyDataLoadingSettings(components, screenSettings);
        }
    }

    protected void saveScreenSettings(Collection<Component> components) {
        if (isSettingsEnabled()) {
            settingsManager.saveSettings(components, screenSettings);
        }
    }

    protected boolean isSettingsEnabled() {
        return settingsManager != null;
    }

    protected Collection<Component> filterByManagedComponents(Collection<Component> components) {
        Collection<Component> attachedComponents = getComponents();
        return components.stream()
                .filter(attachedComponents::contains)
                .collect(Collectors.toList());
    }

    protected void checkAttachedFrame() {
        Frame frame = getOwner();
        if (frame == null) {
            throw new IllegalStateException("ScreenSettingsFacet is not attached to the screen");
        }
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
